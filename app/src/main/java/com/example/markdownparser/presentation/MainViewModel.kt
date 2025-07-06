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

    private val _fileState = MutableStateFlow<FileResult>(FileResult.Success)
    val fileState: StateFlow<FileResult> = _fileState

    private var markdown: Markdown = ""

    fun enterLoadMode() {
        _fileState.value = FileResult.Success
        _state.value = AppState.LoadMode
    }

    fun enterEditMode() {
        _state.value = AppState.EditMode(markdown)
    }

    fun enterReadMode() {
        _state.value = AppState.ReadMode(markdown)
    }

    private fun loadMarkdownFile(content: Markdown) {
        markdown = content
        enterReadMode()
    }

    fun saveContent(content: Markdown) {
        markdown = content
        enterReadMode()
    }

    fun loadFileFromUri(contentResolver: ContentResolver, uri: Uri) {
        if (_fileState.value is FileResult.Loading) return
        _fileState.value = FileResult.Loading

        viewModelScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    readFileContent(contentResolver, uri)
                }
                _fileState.value = FileResult.Success
                loadMarkdownFile(content)
            } catch (e: Exception) {
                _fileState.value = FileResult.Error(e.message.orEmpty())
                Log.e(TAG, e.message ?: "Unknown error")
            }
        }
    }

    fun loadFileFromUrl(url: String) {
        if (_fileState.value is FileResult.Loading) return
        _fileState.value = FileResult.Loading

        viewModelScope.launch {
            try {
                val content = NetworkUtils.downloadMarkdownFile(url)
                loadMarkdownFile(content)
            } catch (e: Exception) {
                val message = when (e) {
                    is IOException -> "Network error: ${e.message}"
                    else -> "Failed to load file: ${e.message}"
                }
                _fileState.value = FileResult.Error(message)
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

sealed interface FileResult {
    data object Loading : FileResult
    data object Success : FileResult
    data class Error(val errorMessage: String) : FileResult
}
