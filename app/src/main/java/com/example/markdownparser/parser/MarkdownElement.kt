package com.example.markdownparser.parser

sealed interface MarkdownElement {
    data class Heading(
        val level: Int,
        val text: String,
    ) : MarkdownElement

    data class Text(
        val text: String,
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val isStrikethrough: Boolean = false,
    ) : MarkdownElement

    data class Table(
        val headers: List<String>,
        val rows: List<List<String>>
    ) : MarkdownElement

    data class Image(
        val altText: String,
        val url: String
    ) : MarkdownElement
}