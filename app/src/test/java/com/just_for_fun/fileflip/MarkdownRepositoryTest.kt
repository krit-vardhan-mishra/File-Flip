package com.just_for_fun.fileflip

import android.content.Context
import com.just_for_fun.fileflip.data.repository.MarkdownRepositoryImpl
import com.just_for_fun.fileflip.domain.model.MarkdownFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.File

class MarkdownRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Mock
    private lateinit var mockContext: Context

    private lateinit var repository: MarkdownRepositoryImpl
    private lateinit var testFilesDir: File

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testFilesDir = tempFolder.newFolder("Files")
        `when`(mockContext.getExternalFilesDir(null)).thenReturn(tempFolder.root)
        
        repository = MarkdownRepositoryImpl(mockContext)
    }

    @Test
    fun `createNewFile creates a file with correct content`() = runBlocking {
        val fileName = "test.md"
        val content = "# Test Content"
        
        val createdFile = repository.createNewFile(fileName, content)
        
        assertNotNull(createdFile)
        assertEquals(fileName, createdFile.name)
        assertEquals(content, createdFile.content)
        
        val actualFile = File(testFilesDir, fileName)
        assertEquals(true, actualFile.exists())
        assertEquals(content, actualFile.readText())
    }

    @Test
    fun `getFiles returns all files in the directory`() = runBlocking {
        File(testFilesDir, "file1.md").writeText("content1")
        File(testFilesDir, "file2.json").writeText("content2")
        File(testFilesDir, "image.png").writeText("not supported")
        
        val files = repository.getFiles()
        
        assertEquals(2, files.size)
        assertEquals(true, files.any { it.name == "file1.md" })
        assertEquals(true, files.any { it.name == "file2.json" })
        assertEquals(false, files.any { it.name == "image.png" })
    }

    @Test
    fun `deleteFile removes the file from disk`() = runBlocking {
        val file = File(testFilesDir, "todelete.md")
        file.writeText("delete me")
        
        repository.deleteFile(file.absolutePath)
        
        assertEquals(false, file.exists())
    }
}
