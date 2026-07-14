package com.just_for_fun.fileflip.data.local.util

data class TextChunk(
    val index: Int,
    val text: String,
    val startByte: Int,
    val endByte: Int
)

object RangedTextChunker {
    private const val CHUNK_SIZE_CHARS = 1800 // Approx 450 tokens
    private const val OVERLAP_CHARS = 200     // Approx 50 tokens

    fun chunkText(text: String): List<TextChunk> {
        if (text.isBlank()) return emptyList()
        val chunks = mutableListOf<TextChunk>()
        var start = 0
        var chunkIndex = 0

        val textLength = text.length
        while (start < textLength) {
            var end = start + CHUNK_SIZE_CHARS
            if (end >= textLength) {
                end = textLength
            } else {
                // Find nearest whitespace to prevent breaking words in middle
                var tempEnd = end
                while (tempEnd > start && !text[tempEnd].isWhitespace()) {
                    tempEnd--
                }
                if (tempEnd > start) {
                    end = tempEnd
                }
            }

            val chunkText = text.substring(start, end).trim()
            if (chunkText.isNotEmpty()) {
                chunks.add(
                    TextChunk(
                        index = chunkIndex++,
                        text = chunkText,
                        startByte = start,
                        endByte = end
                    )
                )
            }

            // Slide window
            if (end == textLength) {
                break
            }
            start = end - OVERLAP_CHARS
            if (start < 0) start = 0
            if (start >= end) {
                start = end
            }
        }
        return chunks
    }
}
