package com.just_for_fun.fileflip.data.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object GroqWhisperClient {
    private const val TAG = "GroqWhisperClient"
    private const val ENDPOINT = "https://api.groq.com/openai/v1/audio/transcriptions"

    suspend fun transcribe(file: File, apiKey: String): String = withContext(Dispatchers.IO) {
        val boundary = "Boundary-${UUID.randomUUID()}"
        val lineEnd = "\r\n"
        var connection: HttpURLConnection? = null

        try {
            val url = URL(ENDPOINT)
            connection = url.openConnection() as HttpURLConnection
            connection.apply {
                doOutput = true
                doInput = true
                useCaches = false
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }

            val outputStream = connection.outputStream
            val writer = PrintWriter(OutputStreamWriter(outputStream, "UTF-8"), true)

            // model parameter
            writer.append("--$boundary").append(lineEnd)
            writer.append("Content-Disposition: form-data; name=\"model\"").append(lineEnd)
            writer.append(lineEnd)
            writer.append("whisper-large-v3").append(lineEnd)
            writer.flush()

            // file parameter
            writer.append("--$boundary").append(lineEnd)
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"").append(lineEnd)
            writer.append("Content-Type: audio/m4a").append(lineEnd)
            writer.append(lineEnd)
            writer.flush()

            // Write raw audio file bytes
            file.inputStream().use { input ->
                val buffer = ByteArray(4096)
                var bytesRead = input.read(buffer)
                while (bytesRead != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
            }
            outputStream.flush()
            
            writer.append(lineEnd)
            writer.append("--$boundary--").append(lineEnd)
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                // Parse simple JSON e.g., {"text":"Hello world"}
                val regex = Regex("\"text\"\\s*:\\s*\"([^\"]+)\"")
                val match = regex.find(responseText)
                val text = match?.groupValues?.get(1) ?: ""
                Log.d(TAG, "Transcription succeeded: $text")
                return@withContext text
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.e(TAG, "Transcription HTTP Error: $responseCode - $errorText")
                throw Exception("Whisper transcription HTTP error $responseCode: $errorText")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Transcription failed: ${e.message}", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }
}
