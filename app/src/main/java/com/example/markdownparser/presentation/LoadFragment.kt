package com.example.markdownparser.presentation

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.markdownparser.databinding.FragmentLoadBinding

class LoadFragment : Fragment() {
    private lateinit var binding: FragmentLoadBinding
    private val viewModel: MarkdownViewModel by activityViewModels()

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadFileFromUri(requireContext().contentResolver, uri) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadFile.setOnClickListener { openFilePicker() }
        binding.fetchFile.setOnClickListener { downloadFromUrl() }
    }

    override fun onDestroyView() {
        binding.root.removeAllViews()
        super.onDestroyView()
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("text/markdown"))
    }

    private fun downloadFromUrl() {
        val url = binding.enterUrl.text

        if (url.isNotBlank()) {
            viewModel.loadFileFromUrl(url.toString())
        }
    }

    private fun testContentLoading() {
        val testContent = """
# Заголовок 1
## Заголовок 2
Обычный текст
**Жирный текст**
*Курсивный текст*
~~Зачеркнутый текст~~
~~**_Жирный текст_**~~

| Header 1 | Header 2 |
|----------|----------|
| Ячейка 1 | Ячейка 2 |
| Ячейка 3 | Ячейка 4 |

![Описание картинки](https://i.pinimg.com/originals/b2/dc/9c/b2dc9c2cee44e45672ad6e3994563ac2.jpg)

| Left | Center | Right |
|:-----|:------:|------:|
| A    | B      | C     |

| Col1 | Col2 | Col3 |
|------|------|------|
|      | Data |      |
| Data |      | Data |

""".trimIndent()

        viewModel.loadMarkdownFile(testContent)
    }

    companion object {
        fun newInstance() = LoadFragment()
    }
}
