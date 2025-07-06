package com.example.markdownparser.presentation

import com.example.markdownparser.parser.Markdown

sealed interface AppState {
    data object LoadMode : AppState
    data class EditMode(val content: Markdown) : AppState
    data class ReadMode(val content: Markdown) : AppState
}
