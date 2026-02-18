# Android App: MD to PDF Converter - Closing & Review Documents

## 1. Project Closeout Checklist

### **1.1 Deliverables Verification**
*   [ ] **Source Code:** Pushed to `main` branch with all features implemented.
*   [ ] **APK/AAB:** Signed Release Bundle generated (`release-keys.jks`).
*   [ ] **Documentation:** `README.md` updated with "How to Build" and "Architecture" sections.

### **1.2 User Acceptance Testing (UAT) Criteria**
*   **Functional:**
    *   Can pick file from GDrive/Internal Storage? (Pass/Fail)
    *   Does H1/H2/Bold/Italic render correctly? (Pass/Fail)
    *   Does PDF save to Downloads folder? (Pass/Fail)
*   **Non-Functional:**
    *   Does the app handle a 5MB Markdown file without ANR? (Pass/Fail)
    *   Does it work in Airplane Mode? (Pass/Fail)

---

## 2. Deployment Guide (Play Store)

### **2.1 Pre-Launch**
*   **Privacy Policy:** Hosted URL explaining "No Data Collection".
*   **Assets:**
    *   Icon (512x512).
    *   Feature Graphic (1024x500).
    *   Screenshots (Phone + 7" Tablet + 10" Tablet).
*   **Obfuscation:** Verify ProGuard/R8 rules are active in `release` build to shrink code.

### **2.2 Post-Launch Monitoring**
*   **Crashlytics:** Monitor for production crashes.
*   **Feedback:** Reply to user reviews within 48 hours.

---

## 3. Post-Implementation Review (Retrospective)

### **Template Questions:**
1.  **What went well?** (e.g., "Using Flexmark library saved 1 week of regex coding.")
2.  **What went wrong?** (e.g., "Scoped Storage permissions took 3 days instead of 1.")
3.  **Action Items for Next Version:**
    *   Add OCR for image-to-markdown.
    *   Add Support for Mathematical Formulas (LaTeX).
