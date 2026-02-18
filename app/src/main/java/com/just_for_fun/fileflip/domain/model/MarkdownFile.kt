package com.just_for_fun.fileflip.domain.model

data class MarkdownFile(
    val name: String,
    val path: String,
    val content: String,
    val lastModified: Long
)
