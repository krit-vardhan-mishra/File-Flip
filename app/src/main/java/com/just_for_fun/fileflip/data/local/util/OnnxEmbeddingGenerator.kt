package com.just_for_fun.fileflip.data.local.util

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.LongBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class OnnxEmbeddingGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelDownloader: ModelDownloader
) {
    private val TAG = "OnnxEmbeddingGenerator"
    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var tokenizer: WordPieceTokenizer? = null

    init {
        try {
            initialize()
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed: ${e.message}", e)
        }
    }

    @Synchronized
    fun initialize(): Boolean {
        if (session != null && tokenizer != null) return true

        val modelFile = modelDownloader.getModelFile()
        val vocabFile = modelDownloader.getVocabFile()

        if (!modelFile.exists() || !vocabFile.exists()) {
            Log.d(TAG, "ONNX model or vocab files not downloaded yet.")
            return false
        }

        try {
            env = OrtEnvironment.getEnvironment()
            session = env?.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
            tokenizer = WordPieceTokenizer(vocabFile)
            Log.d(TAG, "ONNX session and WordPieceTokenizer initialized successfully.")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ONNX session", e)
            close()
            return false
        }
    }

    fun isModelAvailable(): Boolean {
        return modelDownloader.isModelDownloaded() && initialize()
    }

    fun getEmbedding(text: String): FloatArray? {
        if (!isModelAvailable()) {
            Log.e(TAG, "Cannot generate embedding: model not loaded.")
            return null
        }

        val tok = tokenizer ?: return null
        val envInstance = env ?: return null
        val sessionInstance = session ?: return null

        try {
            val tokenIds = tok.tokenize(text)
            val seqLength = tokenIds.size

            // Inputs
            val inputIds = LongArray(seqLength)
            val attentionMask = LongArray(seqLength)
            val tokenTypeIds = LongArray(seqLength)

            for (i in 0 until seqLength) {
                inputIds[i] = tokenIds[i].toLong()
                attentionMask[i] = 1L
                tokenTypeIds[i] = 0L
            }

            // Create tensors of shape [1, seqLength]
            val shape = longArrayOf(1, seqLength.toLong())
            val inputIdsTensor = OnnxTensor.createTensor(envInstance, LongBuffer.wrap(inputIds), shape)
            val attentionMaskTensor = OnnxTensor.createTensor(envInstance, LongBuffer.wrap(attentionMask), shape)
            val tokenTypeIdsTensor = OnnxTensor.createTensor(envInstance, LongBuffer.wrap(tokenTypeIds), shape)

            val inputs = mapOf(
                "input_ids" to inputIdsTensor,
                "attention_mask" to attentionMaskTensor,
                "token_type_ids" to tokenTypeIdsTensor
            )

            val results = sessionInstance.run(inputs)
            val outputTensor = results.get(0) as? OnnxTensor ?: return null

            // outputTensor shape: [1, seqLength, 384]
            val outputData = outputTensor.value as Array<Array<FloatArray>>
            val lastHiddenState = outputData[0] // shape: [seqLength][384]

            // Mean Pooling: average vectors across tokens
            val embeddingSize = 384
            val meanEmbedding = FloatArray(embeddingSize)

            for (j in 0 until embeddingSize) {
                var sum = 0f
                for (i in 0 until seqLength) {
                    sum += lastHiddenState[i][j]
                }
                meanEmbedding[j] = sum / seqLength
            }

            // Normalize embedding to unit length (L2 normalization)
            var squareSum = 0f
            for (valItem in meanEmbedding) {
                squareSum += valItem * valItem
            }
            val norm = sqrt(squareSum.toDouble()).toFloat()
            if (norm > 0) {
                for (j in 0 until embeddingSize) {
                    meanEmbedding[j] /= norm
                }
            }

            // Clean up resources
            inputIdsTensor.close()
            attentionMaskTensor.close()
            tokenTypeIdsTensor.close()
            results.close()

            return meanEmbedding

        } catch (e: Exception) {
            Log.e(TAG, "Embedding generation failed: ${e.message}", e)
            return null
        }
    }

    @Synchronized
    fun close() {
        try {
            session?.close()
            env?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing OrtSession/OrtEnvironment", e)
        } finally {
            session = null
            env = null
            tokenizer = null
        }
    }
}
