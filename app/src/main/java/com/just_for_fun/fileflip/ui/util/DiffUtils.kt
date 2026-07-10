package com.just_for_fun.fileflip.ui.util

enum class LineDiffType {
    UNCHANGED, ADDED, DELETED
}

data class LineDiffItem(
    val type: LineDiffType,
    val text: String
)

/**
 * Computes a line-by-line diff between two strings using the Longest Common Subsequence (LCS) algorithm.
 * Returns a list of [LineDiffItem] representing deletions, additions, and unchanged lines.
 */
fun computeLineDiff(original: String, proposed: String): List<LineDiffItem> {
    val originalLines = original.lines()
    val proposedLines = proposed.lines()
    val n = originalLines.size
    val m = proposedLines.size

    val dp = Array(n + 1) { IntArray(m + 1) }
    for (i in 1..n) {
        for (j in 1..m) {
            if (originalLines[i - 1] == proposedLines[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1] + 1
            } else {
                dp[i][j] = maxOf(dp[i - 1][j], dp[i][j - 1])
            }
        }
    }

    val diff = mutableListOf<LineDiffItem>()
    var i = n
    var j = m
    while (i > 0 || j > 0) {
        if (i > 0 && j > 0 && originalLines[i - 1] == proposedLines[j - 1]) {
            diff.add(LineDiffItem(LineDiffType.UNCHANGED, originalLines[i - 1]))
            i--
            j--
        } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
            diff.add(LineDiffItem(LineDiffType.ADDED, proposedLines[j - 1]))
            j--
        } else if (i > 0 && (j == 0 || dp[i - 1][j] > dp[i][j - 1])) {
            diff.add(LineDiffItem(LineDiffType.DELETED, originalLines[i - 1]))
            i--
        }
    }
    return diff.reversed()
}
