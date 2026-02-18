package com.just_for_fun.fileflip.ui.screens

import android.print.PrintManager
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.yaml.snakeyaml.Yaml
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.asImageBitmap
import java.io.StringReader

// --- Design Colors (Matched to EditorScreen) ---
private val PrimaryBlue = Color(0xFF0DA6F2)
private val BackgroundDark = Color(0xFF101C22)
private val SurfaceDark = Color(0xFF1A2830)
private val TextWhite = Color(0xFFF1F5F9)
private val TextGray = Color(0xFF94A3B8)
private val DividerColor = Color(0xFF0DA6F2).copy(alpha = 0.1f)
private val CodeGreen = Color(0xFF98C379) // For values in TreeView
private val CodeKey = Color(0xFF61AFEF)   // For keys in TreeView

// --- Tree Node Data Structure ---
data class TreeNode(
    val name: String,
    var value: String? = null,
    val children: MutableList<TreeNode> = mutableListOf(),
    val level: Int = 0,
    var isExpanded: Boolean = true
)

// --- Parsers ---
fun parseXmlToTree(xmlContent: String): List<TreeNode> {
    val nodes = mutableListOf<TreeNode>()
    try {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlContent))

        var eventType = parser.eventType
        val stack = mutableListOf<TreeNode>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val node = TreeNode(name = parser.name, level = stack.size, isExpanded = stack.size < 2)
                    if (stack.isNotEmpty()) stack.last().children.add(node) else nodes.add(node)
                    stack.add(node)
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isNotEmpty() && stack.isNotEmpty()) stack.last().value = text
                }
                XmlPullParser.END_TAG -> {
                    if (stack.isNotEmpty()) stack.removeAt(stack.size - 1)
                }
            }
            eventType = parser.next()
        }
    } catch (e: Exception) {
        nodes.add(TreeNode("Error", "Failed to parse XML: ${e.message}"))
    }
    return nodes
}

fun parseYamlToTree(yamlContent: String): List<TreeNode> {
    val nodes = mutableListOf<TreeNode>()
    try {
        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(yamlContent)
        data?.forEach { (key, value) -> nodes.add(parseYamlValueToNode(key, value)) }
    } catch (e: Exception) {
        nodes.add(TreeNode("Error", "Failed to parse YAML: ${e.message}"))
    }
    return nodes
}

private fun parseYamlValueToNode(key: String, value: Any?, level: Int = 0): TreeNode {
    val node = TreeNode(name = key, level = level, isExpanded = level < 2)
    when (value) {
        is Map<*, *> -> value.forEach { (k, v) -> node.children.add(parseYamlValueToNode(k.toString(), v, level + 1)) }
        is List<*> -> value.forEachIndexed { index, item -> node.children.add(parseYamlValueToNode("[$index]", item, level + 1)) }
        else -> node.value = value?.toString() ?: "null"
    }
    return node
}

fun parseJsonToTree(jsonContent: String): List<TreeNode> {
    val nodes = mutableListOf<TreeNode>()
    try {
        val json = JSONObject(jsonContent)
        json.keys().forEach { key -> nodes.add(parseJsonValueToNode(key, json.get(key))) }
    } catch (e: Exception) {
        nodes.add(TreeNode("Error", "Failed to parse JSON: ${e.message}"))
    }
    return nodes
}

private fun parseJsonValueToNode(key: String, value: Any?, level: Int = 0): TreeNode {
    val node = TreeNode(name = key, level = level, isExpanded = level < 2)
    when (value) {
        is JSONObject -> value.keys().forEach { k -> node.children.add(parseJsonValueToNode(k, value.get(k), level + 1)) }
        is org.json.JSONArray -> for (i in 0 until value.length()) {
            node.children.add(parseJsonValueToNode("[$i]", value.get(i), level + 1))
        }
        else -> node.value = value?.toString() ?: "null"
    }
    return node
}

// --- Composable: Tree View (Dark Mode) ---
@Composable
fun TreeView(nodes: List<TreeNode>, modifier: Modifier = Modifier, zoom: Float = 1f) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    // Initialize expansion
    LaunchedEffect(nodes) {
        fun initializeStates(nodeList: List<TreeNode>, path: String = "") {
            nodeList.forEachIndexed { index, node ->
                val nodePath = if (path.isEmpty()) index.toString() else "$path-$index"
                if (node.children.isNotEmpty()) {
                    expandedStates[nodePath] = node.isExpanded
                    initializeStates(node.children, nodePath)
                }
            }
        }
        initializeStates(nodes)
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .graphicsLayer(scaleX = zoom, scaleY = zoom, translationX = 0f, translationY = 0f)
    ) {
        nodes.forEachIndexed { index, node ->
            TreeNodeView(node, index.toString(), expandedStates)
        }
        Spacer(modifier = Modifier.height(100.dp)) // Scroll padding
    }
}

@Composable
fun TreeNodeView(node: TreeNode, path: String, expandedStates: MutableMap<String, Boolean>) {
    val isExpanded = expandedStates[path] ?: node.isExpanded

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            repeat(node.level) { Spacer(modifier = Modifier.width(20.dp)) }

            if (node.children.isNotEmpty()) {
                val arrowText = if (isExpanded) "▼" else "▶"
                Text(
                    text = arrowText,
                    color = TextGray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { expandedStates[path] = !isExpanded }
                        .padding(end = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = node.name,
                color = CodeKey,
                fontSize = 14.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )

            node.value?.let { value ->
                Text(" : ", color = TextGray, fontSize = 14.sp)
                Text(
                    text = value,
                    color = CodeGreen,
                    fontSize = 14.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        if (isExpanded) {
            node.children.forEachIndexed { index, child ->
                TreeNodeView(child, "$path-$index", expandedStates)
            }
        }
    }
}

// --- Main Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(navController: NavController, filePath: String) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // States
    var content by remember { mutableStateOf("Loading...") }
    var zoomLevel by remember { mutableFloatStateOf(1f) }
    var textSizeIndex by remember { mutableIntStateOf(1) } // 0: Small, 1: Medium, 2: Large

    val fileName = filePath.substringAfterLast("/").substringAfterLast("%2F")
    val fileExtension = fileName.substringAfterLast(".", "").lowercase()

    // Load File
    LaunchedEffect(filePath) {
        withContext(Dispatchers.IO) {
            val file = java.io.File(filePath)
            if (file.exists()) {
                content = file.readText()
            } else {
                val demoFile = com.just_for_fun.fileflip.data.DemoFilesData.demoFiles.find { it.path == filePath }
                content = demoFile?.content ?: "# Error\nFile not found"
            }
        }
    }

    // Markdown Parser
    val markwon = remember {
        io.noties.markwon.Markwon.builder(context)
            .usePlugin(io.noties.markwon.html.HtmlPlugin.create())
            .usePlugin(io.noties.markwon.ext.tables.TablePlugin.create(context))
            .build()
    }

    // Export / Print Logic
    fun printContent(webView: WebView?) {
        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as PrintManager
        val jobName = "FileFlip_$fileName"

        if (webView != null) {
            // If we have a WebView (HTML/Markdown), print it directly
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, null)
        } else {
            // For other types, create a temporary WebView to render the "pretty" version for printing
            val tempWebView = WebView(context)
            val htmlContent = when(fileExtension) {
                "json", "xml", "yaml", "yml" -> "<pre>$content</pre>"
                "csv" -> "<html><body><pre>$content</pre></body></html>" // Simplified for print
                else -> "<pre>$content</pre>"
            }
            tempWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            tempWebView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printAdapter = view?.createPrintDocumentAdapter(jobName)
                    printManager.print(jobName, printAdapter!!, null)
                }
            }
        }
    }

    // Ref for the main WebView (if active)
    var activeWebView by remember { mutableStateOf<WebView?>(null) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Preview", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(fileName, color = TextGray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share stub */ }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = TextGray)
                    }
                    IconButton(onClick = { printContent(activeWebView) }) {
                        Icon(Icons.Default.Print, contentDescription = "Print", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        bottomBar = {
            Surface(
                color = BackgroundDark,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zoom Out
                    ControlIconButton(Icons.Default.Remove) {
                        if (zoomLevel > 0.5f) zoomLevel -= 0.1f
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Reset
                    ControlIconButton(Icons.Default.Refresh) {
                        zoomLevel = 1f
                        textSizeIndex = 1
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Reset / Format Size (Text Size)
                    ControlIconButton(Icons.Default.FormatSize) {
                        textSizeIndex = (textSizeIndex + 1) % 3
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Zoom In
                    ControlIconButton(Icons.Default.Add) {
                        if (zoomLevel < 3.0f) zoomLevel += 0.1f
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            val baseFontSize = when(textSizeIndex) {
                0 -> 12f; 1 -> 14f; else -> 18f
            }

            // Container for the preview "Card"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(SurfaceDark, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
            ) {
                when (fileExtension) {
                    "html" -> {
                        // For HTML, we respect the content but try to provide a dark container
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.builtInZoomControls = true
                                    settings.displayZoomControls = false
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    webViewClient = WebViewClient()
                                    setBackgroundColor(android.graphics.Color.parseColor("#1A2830")) // SurfaceDark
                                }
                            },
                            update = { webView ->
                                activeWebView = webView
                                webView.settings.textZoom = (zoomLevel * 100).toInt()
                                webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "md", "markdown" -> {
                        // Render Markdown with Custom Dark CSS
                        val htmlContent = remember(content) {
                            val spanned = markwon.toMarkdown(content)
                            val body = android.text.Html.toHtml(spanned)
                            """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { background-color: #1A2830; color: #F1F5F9; font-family: sans-serif; padding: 16px; line-height: 1.6; }
                                a { color: #0DA6F2; }
                                code { background-color: #101C22; color: #98C379; padding: 2px 4px; border-radius: 4px; }
                                pre { background-color: #101C22; padding: 12px; border-radius: 8px; overflow-x: auto; }
                                blockquote { border-left: 4px solid #0DA6F2; padding-left: 12px; color: #94A3B8; }
                                table { border-collapse: collapse; width: 100%; }
                                th, td { border: 1px solid #333; padding: 8px; text-align: left; }
                                th { background-color: #101C22; }
                                img { max-width: 100%; height: auto; }
                            </style>
                        </head>
                        <body>$body</body>
                        </html>
                        """.trimIndent()
                        }

                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = false
                                    setBackgroundColor(android.graphics.Color.parseColor("#1A2830"))
                                }
                            },
                            update = { webView ->
                                activeWebView = webView
                                webView.settings.textZoom = (zoomLevel * 100).toInt()
                                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "json" -> {
                        val jsonNodes = remember(content) { parseJsonToTree(content) }
                        TreeView(
                            nodes = jsonNodes,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            zoom = zoomLevel
                        )
                    }
                    "xml" -> {
                        val xmlNodes = remember(content) { parseXmlToTree(content) }
                        TreeView(
                            nodes = xmlNodes,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            zoom = zoomLevel
                        )
                    }
                    "yaml", "yml" -> {
                        val yamlNodes = remember(content) { parseYamlToTree(content) }
                        TreeView(
                            nodes = yamlNodes,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            zoom = zoomLevel
                        )
                    }
                    "csv" -> {
                        // CSV Table View with 2D Scrolling
                        AndroidView(
                            factory = { ctx ->
                                // Vertical Scroll Wrapper
                                android.widget.ScrollView(ctx).apply {
                                    isFillViewport = true
                                    setBackgroundColor(android.graphics.Color.parseColor("#1A2830"))

                                    // Horizontal Scroll Wrapper
                                    val horizontalScrollView = android.widget.HorizontalScrollView(ctx).apply {
                                        setPadding(16, 16, 16, 16)
                                    }

                                    // The Table
                                    val tableLayout = android.widget.TableLayout(ctx).apply {
                                        setPadding(0, 0, 0, 0)
                                        isStretchAllColumns = false
                                    }

                                    horizontalScrollView.addView(tableLayout)
                                    addView(horizontalScrollView)
                                }
                            },
                            update = { scrollView ->
                                val horizontalScrollView = scrollView.getChildAt(0) as android.widget.HorizontalScrollView
                                val tableLayout = horizontalScrollView.getChildAt(0) as android.widget.TableLayout
                                tableLayout.removeAllViews()

                                val rows = content.split("\n").filter { it.isNotBlank() }
                                if (rows.isNotEmpty()) {
                                    rows.forEachIndexed { rowIndex, row ->
                                        val tableRow = android.widget.TableRow(scrollView.context)
                                        val cells = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())

                                        cells.forEach { cell ->
                                            val textView = android.widget.TextView(scrollView.context).apply {
                                                text = cell.trim().removeSurrounding("\"")
                                                setTextColor(android.graphics.Color.parseColor("#F1F5F9")) // TextWhite
                                                textSize = baseFontSize * zoomLevel
                                                setPadding(16, 16, 16, 16)
                                                if (rowIndex == 0) {
                                                    setTypeface(null, android.graphics.Typeface.BOLD)
                                                    setBackgroundColor(android.graphics.Color.parseColor("#101C22")) // BackgroundDark
                                                } else {
                                                    setBackgroundColor(android.graphics.Color.parseColor("#1A2830")) // SurfaceDark
                                                }
                                            }
                                            tableRow.addView(textView)
                                        }
                                        tableLayout.addView(tableRow)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "pdf" -> {
                         PdfViewer(
                            filePath = filePath,
                            zoom = zoomLevel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        // Plain Text / LOG
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = content,
                                color = TextWhite,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = (baseFontSize * zoomLevel).sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PdfViewer(filePath: String, zoom: Float, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val renderer = remember(filePath) {
        try {
             val file = java.io.File(filePath)
             if (file.exists()) {
                val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                PdfRenderer(fileDescriptor)
             } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    if (renderer == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Cannot render PDF", color = androidx.compose.ui.graphics.Color.Red)
        }
        return
    }

    val pageCount = renderer.pageCount

    LazyColumn(
        modifier = modifier
            .background(Color.Transparent),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        items(pageCount) { index ->
            PdfPage(renderer = renderer, index = index, zoom = zoom)
        }
    }
}

@Composable
fun PdfPage(renderer: PdfRenderer, index: Int, zoom: Float) {
    // We need to render the bitmap. Since PdfRenderer is not thread safe, we should probably do this carefully.
    // However, simplest way is to render on main thread or use a key to trigger update.
    // For a smooth list, we should check if we can optimize.
    
    val bitmapState = remember(renderer, index, zoom) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    
    LaunchedEffect(renderer, index, zoom) {
        // Render in background to avoid blocking UI
        withContext(Dispatchers.IO) {
            synchronized(renderer) {
                try {
                    val page = renderer.openPage(index)
                    val width = (page.width * zoom).toInt()
                    val height = (page.height * zoom).toInt()
                    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bitmapState.value = bitmap.asImageBitmap()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val imageBitmap = bitmapState.value
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Page ${index + 1}",
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White) // PDFs usually have white background
                .padding(4.dp) // Border effect
        )
    } else {
        // Loading placeholder
        Box(
             modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
             androidx.compose.material3.CircularProgressIndicator(color = PrimaryBlue)
        }
    }
}

@Composable
fun ControlIconButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue
        )
    }
}