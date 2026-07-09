package com.just_for_fun.fileflip.domain.model

import android.net.Uri

data class ExplorerItem(
    val name: String,
    val uri: Uri,
    val isDirectory: Boolean,
    val children: List<ExplorerItem> = emptyList()
)