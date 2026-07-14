package com.just_for_fun.fileflip.data.local.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "ModelDownloader"
    private val CHANNEL_ID = "model_download_channel"
    private val NOTIFICATION_ID = 4040
    private val MODEL_URL = "https://huggingface.co/Xenova/all-MiniLM-L6-v2/resolve/main/onnx/model_quantized.onnx"
    private val VOCAB_URL = "https://huggingface.co/Xenova/all-MiniLM-L6-v2/resolve/main/vocab.txt"

    private val _downloadProgress = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadProgress: StateFlow<DownloadStatus> = _downloadProgress.asStateFlow()

    sealed class DownloadStatus {
        object Idle : DownloadStatus()
        data class Downloading(val progress: Int) : DownloadStatus()
        object Completed : DownloadStatus()
        data class Failed(val error: String) : DownloadStatus()
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AI Model Downloader"
            val descriptionText = "Notifications for local AI model downloading"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getModelFile(): File {
        val dir = File(context.filesDir, "models")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "all-MiniLM-L6-v2.onnx")
    }

    fun getVocabFile(): File {
        val dir = File(context.filesDir, "models")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "vocab.txt")
    }

    fun isModelDownloaded(): Boolean {
        val modelFile = getModelFile()
        val vocabFile = getVocabFile()
        return modelFile.exists() && modelFile.length() > 10 * 1024 * 1024 &&
               vocabFile.exists() && vocabFile.length() > 50 * 1024
    }

    fun deleteModel(): Boolean {
        val modelFile = getModelFile()
        val vocabFile = getVocabFile()
        var deleted = false
        if (modelFile.exists()) {
            deleted = modelFile.delete()
        }
        if (vocabFile.exists()) {
            deleted = vocabFile.delete() || deleted
        }
        if (deleted) {
            _downloadProgress.value = DownloadStatus.Idle
        }
        return deleted
    }

    suspend fun startDownload() {
        if (isModelDownloaded()) {
            _downloadProgress.value = DownloadStatus.Completed
            return
        }
        if (_downloadProgress.value is DownloadStatus.Downloading) {
            return
        }

        withContext(Dispatchers.IO) {
            Log.d(TAG, "startDownload() triggered. Preparing background download task.")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Downloading Local AI Model")
                .setContentText("Preparing download...")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
            _downloadProgress.value = DownloadStatus.Downloading(0)

            try {
                // 1. Download Vocab File with retry loop
                builder.setContentText("Downloading vocabulary configurations...")
                notificationManager.notify(NOTIFICATION_ID, builder.build())
                var vocabSuccess = false
                var vocabAttempts = 0
                while (!vocabSuccess && vocabAttempts < 3) {
                    try {
                        Log.d(TAG, "Starting vocab download attempt ${vocabAttempts + 1}/3 from: $VOCAB_URL")
                        downloadUrlToFile(VOCAB_URL, getVocabFile())
                        vocabSuccess = true
                        Log.i(TAG, "Vocab file download successful.")
                    } catch (e: Exception) {
                        vocabAttempts++
                        Log.w(TAG, "Vocab download attempt $vocabAttempts failed: ${e.message}", e)
                        if (vocabAttempts >= 3) throw e
                        kotlinx.coroutines.delay(2000L * vocabAttempts)
                    }
                }

                // 2. Download Model File with range-resume support
                val outputFile = getModelFile()
                var totalBytesRead = outputFile.length()
                var downloadSuccess = false
                var attempts = 0
                val maxAttempts = 5

                Log.d(TAG, "Model file download setup. Current local file size: ${outputFile.length()} bytes.")

                while (!downloadSuccess && attempts < maxAttempts) {
                    var connection: HttpURLConnection? = null
                    try {
                        var redirectUrl = MODEL_URL
                        var redirectCount = 0
                        val maxRedirects = 5
                        val startByte = outputFile.length()

                        Log.d(TAG, "Initiating redirect handler for model. Attempt ${attempts + 1}/$maxAttempts. Starting at byte: $startByte")

                        // Manual redirection handler to follow HuggingFace redirects to AWS CDN
                        while (redirectCount < maxRedirects) {
                            val url = URL(redirectUrl)
                            Log.d(TAG, "Connecting to: $redirectUrl (Redirect hop $redirectCount)")
                            connection = url.openConnection() as HttpURLConnection
                            connection.instanceFollowRedirects = true
                            connection.connectTimeout = 15000
                            connection.readTimeout = 15000

                            // If we already have some bytes, request from that range
                            if (startByte > 0) {
                                Log.i(TAG, "Setting Range request header: bytes=$startByte-")
                                connection.setRequestProperty("Range", "bytes=$startByte-")
                            }

                            val status = connection.responseCode
                            Log.d(TAG, "HTTP Response code for redirect hop $redirectCount: $status")
                            if (status == HttpURLConnection.HTTP_MOVED_TEMP || 
                                status == HttpURLConnection.HTTP_MOVED_PERM || 
                                status == HttpURLConnection.HTTP_SEE_OTHER) {
                                val newUrl = connection.getHeaderField("Location")
                                Log.i(TAG, "Redirected from $redirectUrl to: $newUrl")
                                connection.disconnect()
                                redirectUrl = newUrl
                                redirectCount++
                            } else {
                                break
                            }
                        }

                        val conn = connection ?: throw Exception("Failed to open connection")
                        val responseCode = conn.responseCode
                        val isPartial = responseCode == HttpURLConnection.HTTP_PARTIAL
                        Log.i(TAG, "Final CDN response code: $responseCode (isPartial: $isPartial)")

                        if (responseCode != HttpURLConnection.HTTP_OK && !isPartial) {
                            Log.w(TAG, "Range request or download failed with response: $responseCode.")
                            // If Range request failed (e.g. range not satisfiable), delete file and start over
                            if (startByte > 0) {
                                Log.w(TAG, "Resumed Range failed. Deleting partial file and restarting download.")
                                outputFile.delete()
                                totalBytesRead = 0
                                attempts++
                                continue
                            }
                            throw Exception("HTTP error code: $responseCode")
                        }

                        val contentLength = if (isPartial) {
                            val contentRange = conn.getHeaderField("Content-Range")
                            Log.d(TAG, "Content-Range header returned: $contentRange")
                            contentRange?.substringAfter("/")?.toLongOrNull() ?: (conn.contentLength + startByte)
                        } else {
                            conn.contentLength.toLong()
                        }
                        Log.i(TAG, "Determined total target content length: $contentLength bytes")

                        // Open FileOutputStream in append mode if resuming (isPartial)
                        val appendMode = isPartial && startByte > 0
                        Log.d(TAG, "Opening FileOutputStream with appendMode = $appendMode")
                        val outputStream = java.io.FileOutputStream(outputFile, appendMode)
                        val inputStream = conn.inputStream
                        
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        if (totalBytesRead == 0L || !appendMode) {
                            totalBytesRead = 0L
                        }
                        var lastProgressUpdate = 0

                        Log.d(TAG, "Start piping input stream bytes to file.")
                        inputStream.use { input ->
                            outputStream.use { output ->
                                bytesRead = input.read(buffer)
                                while (bytesRead != -1) {
                                    output.write(buffer, 0, bytesRead)
                                    totalBytesRead += bytesRead
                                    if (contentLength > 0) {
                                        val progress = ((totalBytesRead * 100) / contentLength).toInt()
                                        if (progress >= lastProgressUpdate + 2 || progress == 100) {
                                            lastProgressUpdate = progress
                                            Log.d(TAG, "Download progress: $progress% ($totalBytesRead / $contentLength bytes)")
                                            _downloadProgress.value = DownloadStatus.Downloading(progress)
                                            builder.setProgress(100, progress, false)
                                                .setContentText("Downloading model... $progress%")
                                            notificationManager.notify(NOTIFICATION_ID, builder.build())
                                        }
                                    }
                                    bytesRead = input.read(buffer)
                                }
                            }
                        }
                        downloadSuccess = true
                        Log.i(TAG, "Model byte piping finished successfully. Total bytes: $totalBytesRead")
                    } catch (e: Exception) {
                        attempts++
                        Log.w(TAG, "Model download attempt $attempts failed: ${e.message}. Retrying...", e)
                        if (attempts >= maxAttempts) {
                            throw e
                        }
                        kotlinx.coroutines.delay(2000L * attempts)
                    } finally {
                        connection?.disconnect()
                    }
                }

                // Verification check
                Log.d(TAG, "Performing file post-download integrity verification.")
                if (isModelDownloaded()) {
                    Log.i(TAG, "Model downloader verification check passed.")
                    _downloadProgress.value = DownloadStatus.Completed
                    builder.setContentTitle("Download Complete")
                        .setContentText("Local AI model successfully loaded.")
                        .setProgress(0, 0, false)
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setOngoing(false)
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                } else {
                    throw Exception("File verification failed (file too small or corrupt)")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Download pipeline encountered fatal error: ${e.message}", e)
                _downloadProgress.value = DownloadStatus.Failed(e.message ?: "Unknown error")
                builder.setContentTitle("Download Failed")
                    .setContentText("Failed to download local AI model: ${e.message}")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                notificationManager.notify(NOTIFICATION_ID, builder.build())
                
                // Clean up files if download failed
                deleteModel()
            }
        }
    }

    private fun downloadUrlToFile(urlString: String, outputFile: File) {
        var urlConnection: HttpURLConnection? = null
        var redirectUrl = urlString
        var redirectCount = 0
        val maxRedirects = 5

        while (redirectCount < maxRedirects) {
            val url = URL(redirectUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.instanceFollowRedirects = true
            urlConnection.connectTimeout = 15000
            urlConnection.readTimeout = 15000
            
            val status = urlConnection.responseCode
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || 
                status == HttpURLConnection.HTTP_MOVED_PERM || 
                status == HttpURLConnection.HTTP_SEE_OTHER) {
                val newUrl = urlConnection.getHeaderField("Location")
                urlConnection.disconnect()
                redirectUrl = newUrl
                redirectCount++
            } else {
                break
            }
        }

        val connection = urlConnection ?: throw Exception("Failed to open connection")
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("HTTP error code: ${connection.responseCode}")
        }

        connection.inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
