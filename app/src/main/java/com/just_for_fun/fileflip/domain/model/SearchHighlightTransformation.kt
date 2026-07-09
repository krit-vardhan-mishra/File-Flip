package com.just_for_fun.fileflip.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation that highlights search matches in the editor.
 * All matches get a background highlight color, the current match gets a stronger highlight.
 * An optional replace highlight adds a separate colored span (red for pending, green for done).
 */
class SearchHighlightTransformation(
    private val matchRanges: List<IntRange>,
    private val currentMatchIndex: Int,
    private val searchHighlightColor: Color,
    private val currentMatchColor: Color,
    private val replaceHighlight: Pair<IntRange, Color>?
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (matchRanges.isEmpty() && replaceHighlight == null) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val builder = buildAnnotatedString {
            append(text)
            // Highlight all search matches
            matchRanges.forEachIndexed { index, range ->
                val safeStart = range.first.coerceIn(0, text.length)
                val safeEnd = (range.last + 1).coerceIn(0, text.length)
                if (safeStart < safeEnd) {
                    val bgColor =
                        if (index == currentMatchIndex) currentMatchColor else searchHighlightColor
                    addStyle(SpanStyle(background = bgColor), safeStart, safeEnd)
                }
            }
            // Apply replace highlight (red pending / green done)
            replaceHighlight?.let { (range, color) ->
                val safeStart = range.first.coerceIn(0, text.length)
                val safeEnd = (range.last + 1).coerceIn(0, text.length)
                if (safeStart < safeEnd) {
                    addStyle(SpanStyle(background = color), safeStart, safeEnd)
                }
            }
        }

        return TransformedText(builder, OffsetMapping.Identity)
    }
}