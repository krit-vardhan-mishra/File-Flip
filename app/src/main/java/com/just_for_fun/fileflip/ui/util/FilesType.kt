package com.just_for_fun.fileflip.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Image
import com.just_for_fun.fileflip.domain.model.EditorTool

enum class FileType {
    MARKDOWN, JSON, YAML, XML, HTML, TEXT, LOG, CSV, UNKNOWN;

    companion object {
        fun fromExtension(extension: String): FileType = when (extension.lowercase()) {
            "md" -> MARKDOWN
            "json" -> JSON
            "yaml", "yml" -> YAML
            "xml" -> XML
            "html", "htm" -> HTML
            "txt" -> TEXT
            "log" -> LOG
            "csv" -> CSV
            else -> UNKNOWN
        }
    }

    fun getToolbarTools(
        onShowValidation: (title: String, message: String, isError: Boolean) -> Unit = { _, _, _ -> },
        onShowWordCount: () -> Unit = {},
        onShowFindReplace: () -> Unit = {}
    ): List<EditorTool> = when (this) {
        MARKDOWN -> listOf(
            EditorTool(Icons.Default.FormatBold, "Insert Bold") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content****")
            },
            EditorTool(Icons.Default.FormatItalic, "Insert Italic") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content**")
            },
            EditorTool(Icons.Default.Link, "Insert Link") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content[](url)")
            },
            EditorTool(Icons.Default.Code, "Insert Code Block") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content```\n\n```")
            },
            EditorTool(Icons.AutoMirrored.Filled.FormatListBulleted, "Insert List") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content\n- ")
            },
            EditorTool(Icons.Outlined.Image, "Insert Image") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content![](image-url)")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        JSON -> listOf(
            EditorTool(Icons.Default.Code, "Format JSON") { viewModel, content, selectedText, range ->
                try {
                    val json = org.json.JSONObject(content)
                    viewModel.updateContent(json.toString(2))
                } catch (e: Exception) {
                    // Invalid JSON, keep as is
                }
            },
            EditorTool(Icons.Default.Description, "Validate JSON") { _, content, _, _ ->
                try {
                    org.json.JSONObject(content)
                    onShowValidation("JSON Validation", "✓ Valid JSON - No errors found.", false)
                } catch (e: Exception) {
                    try {
                        org.json.JSONArray(content)
                        onShowValidation("JSON Validation", "✓ Valid JSON Array - No errors found.", false)
                    } catch (e2: Exception) {
                        onShowValidation("JSON Validation Error", "✗ Invalid JSON:\n${e.message}", true)
                    }
                }
            },
            EditorTool(Icons.Default.Code, "Add Object") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content{\n  \"key\": \"value\"\n}")
            },
            EditorTool(Icons.Default.Code, "Add Array") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content[\n  \"item1\",\n  \"item2\"\n]")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        YAML -> listOf(
            EditorTool(Icons.Default.Code, "Format YAML") { viewModel, content, selectedText, range ->
                viewModel.updateContent("# YAML Configuration\nkey: value\nnested:\n  subkey: subvalue\n")
            },
            EditorTool(Icons.Default.Description, "Validate YAML") { _, content, _, _ ->
                try {
                    val yaml = org.yaml.snakeyaml.Yaml()
                    yaml.load<Any>(content)
                    onShowValidation("YAML Validation", "✓ Valid YAML - No errors found.", false)
                } catch (e: Exception) {
                    onShowValidation("YAML Validation Error", "✗ Invalid YAML:\n${e.message}", true)
                }
            },
            EditorTool(Icons.Default.Code, "Add Section") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content\n# New Section\nsection:\n  key: value")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        XML -> listOf(
            EditorTool(Icons.Default.Code, "Format XML") { viewModel, content, selectedText, range ->
                viewModel.updateContent("<?xml version=\"1.0\"?>\n<root>\n  <element>${content}</element>\n</root>")
            },
            EditorTool(Icons.Default.Description, "Validate XML") { _, content, _, _ ->
                try {
                    val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                    val builder = factory.newDocumentBuilder()
                    builder.parse(org.xml.sax.InputSource(java.io.StringReader(content)))
                    onShowValidation("XML Validation", "✓ Valid XML - No errors found.", false)
                } catch (e: Exception) {
                    onShowValidation("XML Validation Error", "✗ Invalid XML:\n${e.message}", true)
                }
            },
            EditorTool(
                Icons.Default.Code,
                "Add Element"
            ) { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<element></element>")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        HTML -> listOf(
            EditorTool(Icons.Default.FormatBold, "Insert Bold") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<strong></strong>")
            },
            EditorTool(Icons.Default.FormatItalic, "Insert Italic") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<em></em>")
            },
            EditorTool(Icons.Default.Link, "Insert Link") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<a href=\"url\"></a>")
            },
            EditorTool(Icons.Default.Code, "Insert Code") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<code></code>")
            },
            EditorTool(Icons.Default.Code, "Insert Paragraph") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<p></p>")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        CSV -> listOf(
            EditorTool(Icons.Default.Description, "Format CSV") { viewModel, content, _, _ ->
                // Auto-align CSV columns by padding cells
                try {
                    val rows = content.split("\n").filter { it.isNotBlank() }
                    if (rows.isNotEmpty()) {
                        val parsedRows = rows.map { row ->
                            row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                                .map { it.trim().removeSurrounding("\"") }
                        }
                        val colCount = parsedRows.maxOf { it.size }
                        val colWidths = (0 until colCount).map { col ->
                            parsedRows.maxOf { row -> (row.getOrNull(col) ?: "").length }
                        }
                        val formatted = parsedRows.joinToString("\n") { row ->
                            (0 until colCount).joinToString(", ") { col ->
                                (row.getOrNull(col) ?: "").padEnd(colWidths[col])
                            }
                        }
                        viewModel.updateContent(formatted)
                    }
                } catch (e: Exception) {
                    // Keep content as-is on error
                }
            },
            EditorTool(Icons.Default.Code, "Add Row") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content,New Value\n")
            },
            EditorTool(Icons.Default.Code, "Add Column") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content,New Column")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        TEXT, LOG, UNKNOWN -> listOf(
            EditorTool(Icons.Default.Description, "Word Count") { _, _, _, _ ->
                onShowWordCount()
            },
            EditorTool(Icons.Default.Code, "Line Numbers") { viewModel, content, _, _ ->
                // Add/remove line numbers prefix to each line
                val lines = content.lines()
                val hasLineNumbers = lines.firstOrNull()?.matches(Regex("^\\d+[:|.]\\s.*")) == true
                val result = if (hasLineNumbers) {
                    lines.joinToString("\n") { it.replace(Regex("^\\d+[:|.]\\s"), "") }
                } else {
                    lines.mapIndexed { index, line -> "${index + 1}: $line" }.joinToString("\n")
                }
                viewModel.updateContent(result)
            },
            EditorTool(Icons.Default.Code, "Find & Replace") { _, _, _, _ ->
                onShowFindReplace()
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
    }

    // Formatting tools for selected text - context-aware based on file type
    fun getFormattingTools(): List<EditorTool> = when (this) {
        MARKDOWN -> listOf(
            EditorTool(Icons.Default.FormatBold, "Bold") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before**$selectedText**$after")
                }
            },
            EditorTool(Icons.Default.FormatItalic, "Italic") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before*$selectedText*$after")
                }
            },
            EditorTool(Icons.Default.FormatStrikethrough, "Strikethrough") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before~~$selectedText~~$after")
                }
            },
            EditorTool(Icons.Default.Link, "Link") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before[$selectedText](url)$after")
                }
            },
            EditorTool(Icons.Default.Code, "Inline Code") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before`$selectedText`$after")
                }
            },
            EditorTool(Icons.AutoMirrored.Filled.FormatListBulleted, "List") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    val listItems = selectedText.lines().joinToString("\n") { "- $it" }
                    viewModel.updateContent("$before$listItems$after")
                }
            },
            EditorTool(Icons.Default.FormatQuote, "Blockquote") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    val quoted = selectedText.lines().joinToString("\n") { "> $it" }
                    viewModel.updateContent("$before$quoted$after")
                }
            }
        )
        HTML -> listOf(
            EditorTool(Icons.Default.FormatBold, "Bold") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<strong>$selectedText</strong>$after")
                }
            },
            EditorTool(Icons.Default.FormatItalic, "Italic") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<em>$selectedText</em>$after")
                }
            },
            EditorTool(Icons.Default.Link, "Link") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<a href=\"url\">$selectedText</a>$after")
                }
            },
            EditorTool(Icons.Default.Code, "Code") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<code>$selectedText</code>$after")
                }
            }
        )
        JSON -> listOf(
            EditorTool(Icons.Default.Code, "Format") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    try {
                        val json = org.json.JSONObject(selectedText.trim())
                        val before = content.take(range.start)
                        val after = content.substring(range.end)
                        viewModel.updateContent("$before${json.toString(2)}$after")
                    } catch (e: Exception) {
                        // Not valid JSON, keep as is
                    }
                }
            },
            EditorTool(Icons.Default.Code, "Wrap Object") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before{\"value\": $selectedText}$after")
                }
            },
            EditorTool(Icons.Default.Code, "Wrap Array") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before[$selectedText]$after")
                }
            }
        )
        XML -> listOf(
            EditorTool(Icons.Default.Code, "Wrap Element") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<element>$selectedText</element>$after")
                }
            },
            EditorTool(Icons.Default.Code, "Add Attribute") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<element attribute=\"value\">$selectedText</element>$after")
                }
            }
        )
        YAML -> listOf(
            EditorTool(Icons.Default.Code, "Comment") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    val commented = selectedText.lines().joinToString("\n") { "# $it" }
                    viewModel.updateContent("$before$commented$after")
                }
            },
            EditorTool(Icons.Default.Code, "Make Key") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before$selectedText: value$after")
                }
            }
        )
        TEXT, LOG, CSV, UNKNOWN -> listOf(
            EditorTool(Icons.Default.Code, "Uppercase") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before${selectedText.uppercase()}$after")
                }
            },
            EditorTool(Icons.Default.Code, "Lowercase") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before${selectedText.lowercase()}$after")
                }
            }
        )
    }

}
