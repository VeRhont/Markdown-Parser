package com.example.markdownparser.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.setPadding
import com.example.markdownparser.parser.MarkdownElement
import com.example.markdownparser.parser.MarkdownElement.Table.Alignment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MarkdownRenderer(private val context: Context) {
    private val imageCache = mutableMapOf<String, Bitmap>()

    fun render(elements: List<MarkdownElement>, container: LinearLayout) {
        container.removeAllViews()

        for (element in elements) {
            when (element) {
                is MarkdownElement.Heading -> renderHeading(element, container)
                is MarkdownElement.Text -> renderText(element, container)
                is MarkdownElement.Table -> renderTable(element, container)
                is MarkdownElement.Image -> renderImage(element, container)
            }
        }
    }

    private fun renderHeading(heading: MarkdownElement.Heading, container: LinearLayout) {
        val textView = TextView(context).apply {
            text = heading.text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, getHeadingSize(heading.level))
            setTypeface(null, Typeface.BOLD)
            setPadding(dpToPx(8))
        }
        container.addView(textView)
    }

    private fun renderText(paragraph: MarkdownElement.Text, container: LinearLayout) {
        val textView = TextView(context).apply {
            text = SpannableString(paragraph.text).apply {
                if (paragraph.isBold) {
                    setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (paragraph.isItalic) {
                    setSpan(StyleSpan(Typeface.ITALIC), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (paragraph.isStrikethrough) {
                    setSpan(StrikethroughSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(dpToPx(8))
        }
        container.addView(textView)
    }

    private fun renderTable(table: MarkdownElement.Table, container: LinearLayout) {
        val context = container.context
        val tableLayout = TableLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.LTGRAY)
            setPadding(dpToPx(1))
        }

        val headerRow = TableRow(context)
        for ((index, header) in table.headers.withIndex()) {
            val alignment = table.alignments.getOrElse(index) { Alignment.LEFT }
            headerRow.addView(createTableCell(header, true, alignment))
        }
        tableLayout.addView(headerRow)

        val dividerRow = TableRow(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        }
        for (alignment in table.alignments) {
            val divider = View(context).apply {
                setBackgroundColor(Color.LTGRAY)
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    dpToPx(1),
                )
            }
            dividerRow.addView(divider)
        }
        tableLayout.addView(dividerRow)

        for (row in table.rows) {
            val tableRow = TableRow(context)
            for ((index, cell) in row.withIndex()) {
                val alignment = table.alignments.getOrElse(index) { Alignment.LEFT }
                tableRow.addView(createTableCell(cell, false, alignment))
            }
            tableLayout.addView(tableRow)
        }
        container.addView(tableLayout)
    }

    private fun createTableCell(
        text: String,
        isHeader: Boolean,
        alignment: Alignment,
    ): TextView {
        return TextView(context).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isHeader) 14f else 12f)
            setTypeface(null, if (isHeader) Typeface.BOLD else Typeface.NORMAL)

            gravity = when (alignment) {
                Alignment.LEFT -> Gravity.START or Gravity.CENTER_VERTICAL
                Alignment.CENTER -> Gravity.CENTER
                Alignment.RIGHT -> Gravity.END or Gravity.CENTER_VERTICAL
            }

            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1))
            }
            setBackgroundColor(Color.WHITE)
        }
    }

    private fun renderImage(image: MarkdownElement.Image, container: LinearLayout) {
        val imageContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(8), 0, dpToPx(8))
            }
        }

        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
        }
        imageContainer.addView(progressBar)

        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(200)
            )
            scaleType = ImageView.ScaleType.CENTER
            visibility = View.GONE
        }
        imageContainer.addView(imageView)

        if (image.altText.isNotEmpty()) {
            val altTextView = TextView(context).apply {
                text = image.altText
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                gravity = Gravity.CENTER
                setPadding(dpToPx(4))
            }
            imageContainer.addView(altTextView)
        }
        container.addView(imageContainer)

        loadImage(image.url, imageView, progressBar)
    }

    private fun loadImage(url: String, imageView: ImageView, progressBar: ProgressBar) {
        imageCache[url]?.let {
            imageView.setImageBitmap(it)
            imageView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val connection = URL(url).openConnection()
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.getInputStream().use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }

                bitmap?.let {
                    imageCache[url] = it

                    imageView.setImageBitmap(it)
                    imageView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                } ?: run {
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun getHeadingSize(level: Int) = (26f - 2 * level).coerceAtLeast(14f)

    private fun dpToPx(dp: Int) = (dp * context.resources.displayMetrics.density).toInt()
}
