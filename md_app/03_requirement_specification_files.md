# Android App: MD to PDF Converter - Requirement & Specification Files

## 1. Functional Requirements (FR)

### **FR-01: File Import**
*   **FR-01.1:** The app must allow users to pick `.md` files from local device storage using standard Android System Picker (`Intent.ACTION_OPEN_DOCUMENT`).
*   **FR-01.2:** Supports reading text from common encodings (UTF-8, ASCII).
*   **FR-01.3:** (Phase 2) Support picking Source Code files (`.java`, `.kt`, `.py`, `.js`, `.json`, `.xml`, `.csv`).

### **FR-02: Parsing & Preview**
*   **FR-02.1:** Convert Markdown syntax to HTML.
    *   *Supported Elements:* Headers (H1-H6), Bold/Italic, Lists (Ordered/Unordered), Blockquotes, Code Blocks (with syntax highlighting), Tables, Images (local and remote), Task Lists.
*   **FR-02.2:** (Phase 2) Source Code Conversion:
    *   Wrap text in `<pre><code class="language-xyz">` block.
    *   Apply `Highlight.js` or `Prism.js` for syntax highlighting.
    *   Add Line Numbers (Optional toggle).
*   **FR-02.3:** (Phase 2) CSV Conversion:
    *   Parse CSV lines -> Render HTML `<table>` with striped rows.
*   **FR-02.4:** Display a "Live Preview" of the rendered HTML in a WebView.

### **FR-03: PDF Export**
*   **FR-03.1:** Generate a PDF file from the rendered HTML content.
*   **FR-03.2:** Allow user to choose Page Size (A4, Letter) and Orientation (Portrait, Landscape).
*   **FR-03.3:** Save the generated PDF to the device's "Documents" or "Downloads" folder.

### **FR-04: Offline Capability**
*   **FR-04.1:** All parsing and generation must occur locally on the device without network calls (except for fetching remote images if present in MD).

---

## 2. Non-Functional Requirements (NFR)

*   **NFR-01 Performance:** Parsing a 1MB Markdown file should take < 1 second. PDF Generation should take < 3 seconds.
*   **NFR-02 Privacy:** No telemetry or file data should leave the device.
*   **NFR-03 Usability:** Adherence to Material Design 3 guidelines (Dynamic Colors, Edge-to-Edge).
*   **NFR-04 Compatibility:** Minimum Android SDK: 26 (Android 8.0). Target SDK: 35.

---

## 3. Technical Specifications (Implementation Logic)

### **3.1 Architecture**
*   **Pattern:** MVVM (Model-View-ViewModel).
*   **UI:** Jetpack Compose.
*   **DI:** Hilt (Dagger).

### **3.2 Key Libraries**
*   **Parser:** `com.vladsch.flexmark:flexmark-android:0.64.8`
    *   *Rationale:* Superior HTML export capability compared to native TextView rendering.
*   **WebView:** `androidx.webkit`
    *   Used for rendering the HTML string (output of Flexmark) and printing.
*   **Styling:**
    *   Inject `github-markdown.css` + `highlight.js` (for code blocks) into the WebView HTML.

### **3.3 Data Flow**
1.  **Input:** User selects `file.md` -> Application reads `InputStream`.
2.  **Processing (ViewModel):**
    *   `FlexmarkParser.parse(text)` -> `Node`.
    *   `HtmlRenderer.render(Node)` -> `<html>...</html>`.
    *   Append CSS/JS headers.
3.  **Visualization (Compose):**
    *   `AndroidView(factory = { WebView(it) })` loads the HTML data.
4.  **Output (PrintManager):**
    *   `PrintDocumentAdapter` takes WebView content -> writes to PDF descriptor.

### **3.4 Known Challenges & Solutions**
*   **Problem:** `PrintDocumentAdapter` creates a "Print Preview" dialog.
*   **Solution:** Use a custom `PdfDocument` wrapper if "One-Click Save" is required, OR accept the system Print Dialog as a feature (allows choosing Printer vs PDF). *Decision: Use System Print Dialog for MVP simplicity.*

---

## 4. UI/UX Specifications
*   **Screen 1: Dashboard:** Empty state with giant "Convert File" button. List of "Recent Conversions".
*   **Screen 2: Previewer:**
    *   Top Bar: Title of file, "Export PDF" button.
    *   Body: WebView showing the rendered doc.
*   **Theme:** Force Light Mode for WebView (pdfs are usually white paper), App UI follows System Theme.
