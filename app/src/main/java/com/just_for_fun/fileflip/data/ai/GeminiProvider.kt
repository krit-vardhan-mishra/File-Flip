package com.just_for_fun.fileflip.data.ai

import android.util.Log
import com.just_for_fun.fileflip.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

@Singleton
class GeminiProvider @Inject constructor() : AiProvider {

    override fun providerName(): String = "Google Gemini"

    override suspend fun chat(
        systemPrompt: String,
        history: List<ChatMessageEntity>,
        apiKey: String,
        modelName: String
    ): String {
        val urlString = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
        val connection = java.net.URL(urlString).openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        val jsonBody = buildRequestBody(systemPrompt, history)

        connection.outputStream.use { os ->
            os.write(jsonBody.toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode
        if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            return parseGeminiResponse(responseText)
        } else {
            val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
            Log.e("GeminiProvider", "API request failed: Code $responseCode, Error: $errorText")
            throw Exception(parseError(errorText))
        }
    }

    override fun chatStream(
        systemPrompt: String,
        history: List<ChatMessageEntity>,
        apiKey: String,
        modelName: String
    ): Flow<String> = flow {
        val urlString = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:streamGenerateContent?alt=sse&key=$apiKey"
        val connection = java.net.URL(urlString).openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 0
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "text/event-stream")

        val jsonBody = buildRequestBody(systemPrompt, history)

        connection.outputStream.use { os ->
            os.write(jsonBody.toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode
        if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
            var line = reader.readLine()
            while (coroutineContext.isActive && line != null) {
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data.isEmpty()) {
                        line = reader.readLine()
                        continue
                    }
                    try {
                        val json = JSONObject(data)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val content = candidates.getJSONObject(0)
                                .optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            val text = parts?.getJSONObject(0)?.optString("text", "") ?: ""
                            if (text.isNotEmpty()) {
                                emit(text)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("GeminiProvider", "Failed to parse SSE chunk: $line", e)
                    }
                }
                line = reader.readLine()
            }
            reader.close()
        } else {
            val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
            Log.e("GeminiProvider", "Stream request failed: Code $responseCode, Error: $errorText")
            throw Exception(parseError(errorText))
        }
    }.flowOn(Dispatchers.IO)

    private fun buildRequestBody(
        systemPrompt: String,
        history: List<ChatMessageEntity>
    ): String {
        val contentsArray = StringBuilder()
        contentsArray.append("{\"role\":\"user\",\"parts\":[{\"text\":\"${escapeJson(systemPrompt)}\"}]},")
        contentsArray.append("{\"role\":\"model\",\"parts\":[{\"text\":\"Understood. I am ready to help.\"}]},")

        history.forEachIndexed { index, msg ->
            val role = if (msg.role == "user") "user" else "model"
            contentsArray.append("{\"role\":\"$role\",\"parts\":[{\"text\":\"${escapeJson(msg.content)}\"}]}")
            if (index < history.size - 1) contentsArray.append(",")
        }

        return "{\"contents\":[$contentsArray]}"
    }

    private fun parseGeminiResponse(response: String): String {
        val json = JSONObject(response)
        val candidates = json.getJSONArray("candidates")
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        return parts.getJSONObject(0).getString("text")
    }

    private fun parseError(errorText: String): String {
        return try {
            val json = JSONObject(errorText)
            if (json.has("error")) {
                val errorObj = json.get("error")
                if (errorObj is JSONObject && errorObj.has("message")) {
                    errorObj.getString("message")
                } else if (errorObj is String) {
                    errorObj
                } else {
                    json.toString()
                }
            } else {
                errorText
            }
        } catch (e: Exception) {
            errorText
        }
    }

    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
