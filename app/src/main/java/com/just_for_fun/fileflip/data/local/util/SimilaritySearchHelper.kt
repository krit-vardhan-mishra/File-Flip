package com.just_for_fun.fileflip.data.local.util

import java.nio.ByteBuffer
import java.nio.ByteOrder

object SimilaritySearchHelper {

    fun toFloatArray(byteArray: ByteArray): FloatArray {
        val floatBuffer = ByteBuffer.wrap(byteArray)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asFloatBuffer()
        val floatArray = FloatArray(floatBuffer.limit())
        floatBuffer.get(floatArray)
        return floatArray
    }

    fun toByteArray(floatArray: FloatArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(floatArray.size * 4)
            .order(ByteOrder.LITTLE_ENDIAN)
        for (f in floatArray) {
            byteBuffer.putFloat(f)
        }
        return byteBuffer.array()
    }

    fun computeCosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
        if (vectorA.size != vectorB.size) return 0f
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in vectorA.indices) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i] * vectorA[i]
            normB += vectorB[i] * vectorB[i]
        }
        val denom = kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB)
        return if (denom > 0) (dotProduct / denom).toFloat() else 0f
    }
}
