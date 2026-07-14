package com.just_for_fun.fileflip.data.local.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object WebSearchHelper {
    private const val TAG = "WebSearchHelper"

    suspend fun search(query: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Executing web search for query: $query")
            val urlString = "https://html.duckduckgo.com/html/?q=" + URLEncoder.encode(query, "UTF-8")
            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Search request failed with HTTP code: $responseCode")
                return@withContext "Error: Search request failed with code $responseCode."
            }

            val html = connection.inputStream.bufferedReader().use { it.readText() }
            val results = parseDuckDuckGoHtml(html)
            
            if (results.isEmpty()) {
                Log.d(TAG, "No results parsed from HTML.")
                "No results found for '$query'."
            } else {
                Log.d(TAG, "Parsed ${results.size} search results.")
                results.joinToString("\n\n")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during web search", e)
            "Error: Web search failed due to ${e.message}"
        }
    }

    private fun parseDuckDuckGoHtml(html: String): List<String> {
        val results = mutableListOf<String>()
        var index = 0
        
        while (index < html.length) {
            // Find result__a link
            val titleStart = html.indexOf("<a class=\"result__a\"", index)
            if (titleStart == -1) break

            // Find > to extract title text
            val titleTextStart = html.indexOf(">", titleStart) + 1
            if (titleTextStart == 0) break
            val titleTextEnd = html.indexOf("</a>", titleTextStart)
            if (titleTextEnd == -1) break

            // Find snippet
            val snippetStart = html.indexOf("<a class=\"result__snippet\"", titleTextEnd)
            if (snippetStart == -1) {
                index = titleTextEnd
                continue
            }
            val snippetTextStart = html.indexOf(">", snippetStart) + 1
            if (snippetTextStart == 0) {
                index = titleTextEnd
                continue
            }
            val snippetTextEnd = html.indexOf("</a>", snippetTextStart)
            if (snippetTextEnd == -1) {
                index = titleTextEnd
                continue
            }

            val title = html.substring(titleTextStart, titleTextEnd).replace(Regex("<[^>]*>"), "").trim()
            val snippet = html.substring(snippetTextStart, snippetTextEnd).replace(Regex("<[^>]*>"), "").trim()

            if (title.isNotEmpty() && snippet.isNotEmpty()) {
                // Decode HTML entities
                val cleanTitle = decodeHtmlEntities(title)
                val cleanSnippet = decodeHtmlEntities(snippet)
                results.add("- **$cleanTitle**\n  $cleanSnippet")
                if (results.size >= 4) break
            }

            index = snippetTextEnd
        }

        return results
    }

    private fun decodeHtmlEntities(text: String): String {
        return text.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#x27;", "'")
            .replace("&#x2F;", "/")
            .replace("&#39;", "'")
            .replace("&#47;", "/")
    }
}
