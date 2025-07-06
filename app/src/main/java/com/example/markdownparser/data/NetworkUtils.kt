package com.example.markdownparser.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


object NetworkUtils {
    private const val TAG = "NetworkUtils"
    private const val TIMEOUT = 15_000

    suspend fun downloadMarkdownFile(urlString: String): String = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection

            connection.apply {
                connectTimeout = TIMEOUT
                readTimeout = TIMEOUT
                instanceFollowRedirects = true
                requestMethod = "GET"
            }

            var redirectCount = 0
            while (connection!!.responseCode in setOf(
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    HttpURLConnection.HTTP_SEE_OTHER
                ) && redirectCount < 5
            ) {
                val newUrl = connection.getHeaderField("Location")
                connection.disconnect()
                connection = URL(newUrl).openConnection() as HttpURLConnection
                redirectCount++
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP error: ${connection.responseCode}")
            }

            if (!isMarkdownFile(connection, urlString)) {
                throw IOException("Not a Markdown file. Detected type: ${connection.contentType}")
            }

            connection.inputStream.use { stream ->
                stream.bufferedReader().readText()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }

    private fun isMarkdownFile(connection: HttpURLConnection, url: String): Boolean {
        val contentType = connection.contentType?.lowercase() ?: ""
        val validContentTypes = listOf(
            "text/markdown",
            "text/x-markdown",
            "application/markdown",
            "text/plain"
        )

        if (validContentTypes.any { contentType.contains(it) }) {
            return true
        }

        val path = url.substringBefore('?') // Игнорируем query-параметры
        return path.substringAfterLast('.', "").lowercase() == "md"
    }
}
