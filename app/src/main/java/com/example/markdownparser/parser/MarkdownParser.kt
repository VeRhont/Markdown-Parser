package com.example.markdownparser.parser

typealias Markdown = String

class MarkdownParser {
    fun parse(markdown: Markdown): List<MarkdownElement> {
        val lines = markdown.lines()
        val elements = mutableListOf<MarkdownElement>()
        var currentTable: TableBuilder? = null
        var inTable = false

        for (line in lines) {
            if (inTable) {
                if (isTableRow(line)) {
                    currentTable?.addRow(line)
                } else {
                    inTable = false
                    currentTable?.build()?.let { elements.add(it) }
                    currentTable = null
                    parseLine(line)?.let { elements.add(it) }
                }
                continue
            }

            if (isTableHeader(line)) {
                currentTable = TableBuilder(line)
                inTable = true
            } else {
                parseLine(line)?.let { elements.add(it) }
            }
        }

        currentTable?.build()?.let { elements.add(it) }
        return elements
    }

    private fun parseLine(line: String): MarkdownElement? {
        if (line.isBlank()) return null

        return when {
            isHeading(line) -> parseHeading(line)
            isImage(line) -> parseImage(line)
            else -> parseText(line)
        }
    }

    private fun parseHeading(line: String): MarkdownElement.Heading {
        val trimmed = line.trimStart()
        val level = trimmed.takeWhile { it == '#' }.length
        val text = trimmed.substring(level).trim()
        return MarkdownElement.Heading(level.coerceAtMost(6), text)
    }

    private fun parseImage(line: String): MarkdownElement.Image {
        val regex = Regex("!\\[(.*?)]\\((.*?)\\)")
        val matchResult = regex.find(line)

        return if (matchResult != null) {
            val (altText, url) = matchResult.destructured
            MarkdownElement.Image(altText, url)
        } else {
            MarkdownElement.Image("", line)
        }
    }

    private fun parseText(line: String): MarkdownElement.Text {
        val text = line
            .replace(Regex("""\*\*|__"""), "")
            .replace(Regex("""\*|_"""), "")
            .replace(Regex("~~"), "")

        return MarkdownElement.Text(
            text = text,
            isBold = isBoldText(line),
            isItalic = isItalicText(line),
            isStrikethrough = isStrikethroughText(line),
        )
    }

    private fun isHeading(line: String): Boolean {
        return line.trimStart().startsWith("#")
    }

    private fun isBoldText(line: String): Boolean {
        val hasDoubleAsterisk = line.contains(Regex("""\*\*(.*?)\*\*"""))
        val hasDoubleUnderscore = line.contains(Regex("""__(.*?)__"""))
        return hasDoubleAsterisk || hasDoubleUnderscore
    }

    private fun isItalicText(line: String): Boolean {
        val hasSingleAsterisk = line.contains(Regex("""\*(.*?)\*""")) &&
                !line.contains(Regex("""\*\*(.*?)\*\*"""))
        val hasSingleUnderscore = line.contains(Regex("""_(.*?)_""")) &&
                !line.contains(Regex("""__(.*?)__"""))
        return hasSingleAsterisk || hasSingleUnderscore
    }

    private fun isStrikethroughText(line: String): Boolean {
        return line.contains("~~")
    }

    private fun isTableHeader(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.startsWith('|') ||
                (trimmed.contains('|') && !trimmed.startsWith('-'))
    }

    private fun isTableRow(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.startsWith('|') ||
                trimmed.contains('|') && !trimmed.startsWith('-')
    }

    private fun isImage(line: String): Boolean {
        return line.contains("![]") || Regex("!\\[.*]\\(.+\\)").containsMatchIn(line)
    }
}
