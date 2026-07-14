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
class OpenRouterProvider @Inject constructor() : AiProvider {

    override fun providerName(): String = "OpenRouter"

    override suspend fun chat(
        systemPrompt: String,
        history: List<ChatMessageEntity>,
        apiKey: String,
        modelName: String
    ): String {
        val urlString = "https://openrouter.ai/api/v1/chat/completions"
        val connection = java.net.URL(urlString).openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.setRequestProperty("HTTP-Referer", "https://fileflip.app")
        connection.setRequestProperty("X-Title", "FileFlip")

        val jsonBody = buildRequestBody(systemPrompt, history, modelName, stream = false)

        connection.outputStream.use { os ->
            os.write(jsonBody.toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode
        if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            return parseOpenAiResponse(responseText)
        } else {
            val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
            Log.e("OpenRouterProvider", "API request failed: Code $responseCode, Error: $errorText")
            throw Exception(parseError(errorText))
        }
    }

    override fun chatStream(
        systemPrompt: String,
        history: List<ChatMessageEntity>,
        apiKey: String,
        modelName: String
    ): Flow<String> = flow {
        val urlString = "https://openrouter.ai/api/v1/chat/completions"
        val connection = java.net.URL(urlString).openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 0
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.setRequestProperty("HTTP-Referer", "https://fileflip.app")
        connection.setRequestProperty("X-Title", "FileFlip")
        connection.setRequestProperty("Accept", "text/event-stream")

        val jsonBody = buildRequestBody(systemPrompt, history, modelName, stream = true)

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
                    if (data == "[DONE]") break
                    try {
                        val json = JSONObject(data)
                        val choices = json.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val delta = choices.getJSONObject(0).optJSONObject("delta")
                            val content = delta?.optString("content", "") ?: ""
                            if (content.isNotEmpty()) {
                                emit(content)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("OpenRouterProvider", "Failed to parse SSE chunk: $line", e)
                    }
                }
                line = reader.readLine()
            }
            reader.close()
        } else {
            val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
            Log.e("OpenRouterProvider", "Stream request failed: Code $responseCode, Error: $errorText")
            throw Exception(parseError(errorText))
        }
    }.flowOn(Dispatchers.IO)

    private fun buildRequestBody(
        systemPrompt: String,
        history: List<ChatMessageEntity>,
        modelName: String,
        stream: Boolean
    ): String {
        val messagesArray = StringBuilder()
        messagesArray.append("{\"role\":\"system\",\"content\":\"${escapeJson(systemPrompt)}\"},")
        history.forEachIndexed { index, msg ->
            messagesArray.append("{\"role\":\"${msg.role}\",\"content\":\"${escapeJson(msg.content)}\"}")
            if (index < history.size - 1) messagesArray.append(",")
        }
        return "{\"model\":\"$modelName\",\"stream\":$stream,\"messages\":[$messagesArray]}"
    }

    private fun parseOpenAiResponse(response: String): String {
        val json = JSONObject(response)
        val choices = json.getJSONArray("choices")
        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.getJSONObject("message")
        return message.getString("content")
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
