package com.example.markdownparser.presentation

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markdownparser.data.NetworkUtils
import com.example.markdownparser.parser.Markdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

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

    fun loadFileFromUri(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    readFileContent(contentResolver, uri)
                }
                loadMarkdownFile(content)
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "Unknown error")
            }
        }
    }

    fun loadFileFromUrl(url: String) {
        viewModelScope.launch {
            try {
                val content = NetworkUtils.downloadMarkdownFile(url)
                loadMarkdownFile(content)
            } catch (e: Exception) {
                val message = when (e) {
                    is IOException -> "Network error: ${e.message}"
                    else -> "Failed to load file: ${e.message}"
                }
                Log.e(TAG, message)
            }
        }
    }

    private fun readFileContent(contentResolver: ContentResolver, uri: Uri): String {
        return contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                buildString {
                    var line = reader.readLine()
                    while (line != null) {
                        append(line)
                        line = reader.readLine()
                        if (line != null) append("\n")
                    }
                }
            }
        } ?: throw Exception("Не удалось открыть файл")
    }

    private companion object {
        private const val TAG = "MainViewModel"
    }
}
