package com.example.markdownparser.parser

import com.example.markdownparser.parser.MarkdownElement.Table.Alignment

class TableBuilder(headerLine: String) {
    private val headers = parseTableRow(headerLine)
    private val rows = mutableListOf<List<String>>()
    private var alignments: List<Alignment>? = null

    fun addRow(line: String) {
        if (alignments == null && isAlignmentRow(line)) {
            alignments = parseAlignments(line)
        } else {
            rows.add(parseTableRow(line))
        }
    }

    fun build(): MarkdownElement.Table {
        return MarkdownElement.Table(
            headers = headers,
            rows = rows,
            alignments = alignments ?: List(headers.size) { Alignment.LEFT }
        )
    }

    private fun parseTableRow(line: String): List<String> {
        return line.trim()
            .removeSurrounding("|")
            .split("|")
            .map { it.trim() }
    }

    private fun isAlignmentRow(line: String): Boolean {
        return line.trim().matches(Regex("^\\|?[-: \\|]+\\|?$"))
    }

    private fun parseAlignments(line: String): List<Alignment> {
        return line.trim()
            .removeSurrounding("|")
            .split("|")
            .map {
                when {
                    it.startsWith(':') && it.endsWith(':') -> Alignment.CENTER
                    it.endsWith(':') -> Alignment.RIGHT
                    else -> Alignment.LEFT
                }
            }
    }
}
