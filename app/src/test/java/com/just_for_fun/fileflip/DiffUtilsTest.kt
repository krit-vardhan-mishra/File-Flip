package com.just_for_fun.fileflip

import com.just_for_fun.fileflip.ui.util.LineDiffType
import com.just_for_fun.fileflip.ui.util.computeLineDiff
import org.junit.Assert.assertEquals
import org.junit.Test

class DiffUtilsTest {

    @Test
    fun testUnchangedLines() {
        val original = "Line 1\nLine 2"
        val proposed = "Line 1\nLine 2"
        val diff = computeLineDiff(original, proposed)
        
        assertEquals(2, diff.size)
        assertEquals(LineDiffType.UNCHANGED, diff[0].type)
        assertEquals("Line 1", diff[0].text)
        assertEquals(LineDiffType.UNCHANGED, diff[1].type)
        assertEquals("Line 2", diff[1].text)
    }

    @Test
    fun testLineAdded() {
        val original = "Line 1\nLine 2"
        val proposed = "Line 1\nLine 1.5\nLine 2"
        val diff = computeLineDiff(original, proposed)
        
        assertEquals(3, diff.size)
        assertEquals(LineDiffType.UNCHANGED, diff[0].type)
        assertEquals(LineDiffType.ADDED, diff[1].type)
        assertEquals("Line 1.5", diff[1].text)
        assertEquals(LineDiffType.UNCHANGED, diff[2].type)
    }

    @Test
    fun testLineDeleted() {
        val original = "Line 1\nLine 2\nLine 3"
        val proposed = "Line 1\nLine 3"
        val diff = computeLineDiff(original, proposed)
        
        assertEquals(3, diff.size)
        assertEquals(LineDiffType.UNCHANGED, diff[0].type)
        assertEquals(LineDiffType.DELETED, diff[1].type)
        assertEquals("Line 2", diff[1].text)
        assertEquals(LineDiffType.UNCHANGED, diff[2].type)
    }

    @Test
    fun testLineModified() {
        val original = "Line 1\nOld Line 2"
        val proposed = "Line 1\nNew Line 2"
        val diff = computeLineDiff(original, proposed)
        
        // A modification is represented as deletion of old + addition of new
        assertEquals(3, diff.size)
        assertEquals(LineDiffType.UNCHANGED, diff[0].type)
        assertEquals(LineDiffType.DELETED, diff[1].type)
        assertEquals("Old Line 2", diff[1].text)
        assertEquals(LineDiffType.ADDED, diff[2].type)
        assertEquals("New Line 2", diff[2].text)
    }
}
