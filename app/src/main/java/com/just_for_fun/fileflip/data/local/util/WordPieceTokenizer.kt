package com.just_for_fun.fileflip.data.local.util

import java.io.File

class WordPieceTokenizer(private val vocabFile: File) {
    private val vocab = mutableMapOf<String, Int>()
    private val inverseVocab = mutableMapOf<Int, String>()
    
    companion object {
        const val UNK_TOKEN = "[UNK]"
        const val CLS_TOKEN = "[CLS]"
        const val SEP_TOKEN = "[SEP]"
        const val PAD_TOKEN = "[PAD]"
    }

    init {
        if (vocabFile.exists()) {
            vocabFile.forEachLine { line ->
                val word = line.trim()
                if (word.isNotEmpty()) {
                    val id = vocab.size
                    vocab[word] = id
                    inverseVocab[id] = word
                }
            }
        }
    }

    fun getVocabSize(): Int = vocab.size
    
    fun hasVocab(): Boolean = vocab.isNotEmpty()

    fun tokenize(text: String): List<Int> {
        val tokens = mutableListOf<Int>()
        tokens.add(vocab[CLS_TOKEN] ?: 101) // Standard BERT [CLS] id is 101

        val cleanText = text.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s##]"), " ") // Simple normalization
        
        val words = cleanText.split(Regex("\\s+")).filter { it.isNotEmpty() }

        for (word in words) {
            var start = 0
            val len = word.length
            var isUnknown = false
            val subTokens = mutableListOf<Int>()

            while (start < len) {
                var end = len
                var curSubword: String? = null
                while (start < end) {
                    var subword = word.substring(start, end)
                    if (start > 0) {
                        subword = "##$subword"
                    }
                    if (vocab.containsKey(subword)) {
                        curSubword = subword
                        break
                    }
                    end--
                }
                if (curSubword == null) {
                    isUnknown = true
                    break
                }
                subTokens.add(vocab[curSubword]!!)
                start = end
            }

            if (isUnknown) {
                tokens.add(vocab[UNK_TOKEN] ?: 100) // Standard BERT [UNK] id is 100
            } else {
                tokens.addAll(subTokens)
            }
        }

        tokens.add(vocab[SEP_TOKEN] ?: 102) // Standard BERT [SEP] id is 102
        return tokens
    }
}
