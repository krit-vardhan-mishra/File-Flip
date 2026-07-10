package com.just_for_fun.fileflip.ui.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

/**
 * A custom implementation of [TextToolbar] that intercepts system requests
 * to show/hide the selection toolbar and forwards them to a custom callback.
 */
class CustomTextToolbar(
    private val onShowMenu: (Rect, (() -> Unit)?, (() -> Unit)?, (() -> Unit)?, (() -> Unit)?) -> Unit,
    private val onHideMenu: () -> Unit
) : TextToolbar {
    private var _status by mutableStateOf(TextToolbarStatus.Hidden)
    override val status: TextToolbarStatus
        get() = _status

    override fun hide() {
        android.util.Log.d("FileFlip", "CustomTextToolbar: hide called")
        _status = TextToolbarStatus.Hidden
        onHideMenu()
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        android.util.Log.d("FileFlip", "CustomTextToolbar: showMenu called - rect=$rect")
        _status = TextToolbarStatus.Shown
        onShowMenu(rect, onCopyRequested, onPasteRequested, onCutRequested, onSelectAllRequested)
    }
}

data class CustomToolbarState(
    val rect: Rect,
    val onCopy: (() -> Unit)?,
    val onPaste: (() -> Unit)?,
    val onCut: (() -> Unit)?,
    val onSelectAll: (() -> Unit)?
)
