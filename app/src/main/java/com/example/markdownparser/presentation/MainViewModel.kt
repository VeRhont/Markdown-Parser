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

    fun enterLoadMode() {
        _state.value = AppState.LoadMode
    }

    fun enterEditMode() {
        _state.value = AppState.EditMode(markdown)
    }

    fun enterReadMode() {
        _state.value = AppState.ReadMode(markdown)
    }

    fun loadMarkdownFile(content: Markdown) {
        markdown = content
        enterReadMode()
    }

    fun saveContent(content: Markdown) {
        markdown = content
        enterReadMode()
    }
}
