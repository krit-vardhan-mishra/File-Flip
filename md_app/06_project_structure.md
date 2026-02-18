# Android App: MD to PDF Converter - Project Structure

## 1. Documentation Structure (This Folder)
*   `01_Init/` - Business Case, Charter.
*   `02_Plan/` - WBS, Schedule, Risks.
*   `03_Specs/` - Requirements, Tech Stack, UI Design.
*   `04_Dev/` - CI/CD Configs, Lint Rules.
*   `05_Close/` - Manuals, Release Notes.

## 2. Source Code Structure (Android Studio)

We follow **Clean Architecture** combined with **MVVM**.

```text
app/src/main/java/com/example/markpdf/
├── data/                  # Data Layer (Repositories, Data Sources)
│   ├── local/             # File System implementations
│   │   └── FileRepositoryImpl.kt
│   └── repository/
│       └── MarkdownRepositoryImpl.kt
│
├── domain/                # Domain Layer (Business Logic - PURE KOTLIN)
│   ├── model/             # Data Classes
│   │   └── MarkdownFile.kt
│   ├── repository/        # Interfaces
│   │   └── MarkdownRepository.kt
│   └── usecase/           # Atomic actions
│       ├── ParseMarkdownUseCase.kt
│       └── GeneratePdfUseCase.kt
│
├── presentation/          # UI Layer (MVVM + Compose)
│   ├── theme/             # Typography, Colors (Material3)
│   ├── home/              # Core Feature: Dashboard
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   └── preview/           # Core Feature: Viewer
│       ├── PreviewScreen.kt
│       └── PreviewViewModel.kt
│
└── di/                    # Dependency Injection (Hilt Modules)
    ├── AppModule.kt       # Provides Repositories
    └── ParserModule.kt    # Provides Flexmark instances
```

## 3. Key Gradle Modules
*   `app` - Main Android Application module.
*   *(Optional)* `core-ui` - Shared composables if project grows.

## 4. Important Configuration Files
*   `build.gradle.kts (Project)` - Classpath dependencies.
*   `app/build.gradle.kts` - Library versions (Compose, WebKit).
*   `AndroidManifest.xml` - Permissions (`READ_EXTERNAL_STORAGE`).
*   `proguard-rules.pro` - Rules to keep Flexmark classes from being obfuscated.
