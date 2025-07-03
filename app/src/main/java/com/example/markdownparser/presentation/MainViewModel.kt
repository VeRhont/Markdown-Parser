package com.example.markdownparser.presentation

import androidx.lifecycle.ViewModel
import com.example.markdownparser.AppState
import com.example.markdownparser.parser.Markdown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MarkdownViewModel : ViewModel() {

    private val _state = MutableStateFlow<AppState>(AppState.LoadMode)
    val state: StateFlow<AppState> = _state

    private var markdown: Markdown = ""

    fun loadMarkdownFile(content: Markdown) {
        markdown = content
        _state.value = AppState.LoadMode
    }

    fun enterEditMode() {
        _state.value = AppState.EditMode(markdown)
    }

    fun saveContent(newContent: String) {
        markdown = newContent
        _state.value = AppState.ReadMode(markdown)
    }

    fun resetToLoadMode() {
        _state.value = AppState.LoadMode
    }
}
