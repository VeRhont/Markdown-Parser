package com.example.markdownparser.parser

class TableBuilder(headerLine: String) {
    private val headers = parseRow(headerLine)
    private val rows = mutableListOf<List<String>>()

    fun addRow(line: String) {
        rows.add(parseRow(line))
    }

    fun build(): MarkdownElement.Table {
        return MarkdownElement.Table(headers, rows)
    }

    private fun parseRow(line: String): List<String> {
        return line.split('|')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}