# Android App: MD to PDF Converter - Execution & Monitoring Documents

## 1. Development Workflows

### **1.1 Version Control Strategy (Git Flow)**
*   **Branches:**
    *   `main`: Stable, production-ready code.
    *   `develop`: Integration branch for ongoing development.
    *   `feature/feature-name`: For new requirements (e.g., `feature/pdf-export`).
    *   `bugfix/issue-id`: For resolving specific bugs.
*   **Commit Message Convention:** Conventional Commits (e.g., `feat: add markdown parser`, `fix: crash on rotation`).

### **1.2 Code Review Checklist (Pull Requests)**
*   [ ] Does the code follow Kotlin Style Guide?
*   [ ] Are there memory leaks (check Composable references)?
*   [ ] Is the Accessibility (TalkBack) handled for new UI elements?
*   [ ] Do Unit Tests pass locally?

---

## 2. Quality Assurance (QA) & Monitoring

### **2.1 Bug Tracking (Jira / GitHub Issues)**
*   **Severity Levels:**
    *   **P0 (Critical):** App Crash, Data Loss. *SLA: Fix within 24h.*
    *   **P1 (High):** Major feature broken (e.g., PDF export fails).
    *   **P2 (Medium):** UI glitch, dark mode issue.
    *   **P3 (Low):** Typo, minor animation jank.

### **2.2 Performance Monitoring (Android Vitals)**
*   **Boot Time:** Cold start should be < 500ms.
*   **Jank Stats:** Frames rendering > 16ms should be < 5%.
*   **Memory Usage:** Profile with Android Studio Profiler to ensure Heap < 128MB during PDF generation.

### **2.3 CI/CD Pipeline (GitHub Actions)**
1.  **Trigger:** On Push to `develop` or PR creation.
2.  **Job 1 (Build):** Run ` ./gradlew assembleDebug`.
3.  **Job 2 (Test):** Run `./gradlew testDebugUnitTest`.
4.  **Job 3 (Lint):** Run `./gradlew lintDebug`.
5.  **Artifact:** Upload APK to "Actions" tab for QA testing.

---

## 3. Daily/Weekly Routines
*   **Daily Standup (Solo/Team):** "What did I code yesterday? What is blocking me (e.g., Scoped Storage API complexity)?"
*   **Weekly Demo:** Generate a PDF from a complex Markdown file to verify progress.
