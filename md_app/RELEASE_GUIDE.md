# Play Store Release Guide & Checklist

## 1. Google Play Console Account (Cost & Organization)

**Q: Do I pay for every app I publish?**
**A: No.**

*   **One-Time Fee**: There is a **one-time $25 registration fee** to create your Google Play Developer Account.
*   **Unlimited Apps**: Once you have an account (Standard or Organization), you can publish as many apps as you want **for free**. You do not pay a hosting fee per app.
*   **Organization vs. Personal**:
    *   If you create an Organization account, you verified as a business.
    *   You can publish multiple apps under this single Organization account.
    *   **Cost**: Still just the one-time $25.
*   **Commissions**: Google only charges a fee (service fee, usually 15%) when you verify payments (e.g., if your app costs money to download or has in-app purchases). Free apps are 100% free to host.

---

## 2. Technical Steps to Release

The app is currently configured for `debug` mode. To upload to the Play Store, you must sign it with a secure **Release Key**.

### Step A: Generate a Upload Keystore
1.  Open Android Studio.
2.  Go to **Build > Generate Signed Bundle / APK**.
3.  Select **Android App Bundle** (best for Play Store) -> Next.
4.  Under "Key store path", click **Create new**.
5.  **Path**: Save it as `upload-keystore.jks` in your `app/` folder (DO NOT share this file or upload it to public GitHub).
6.  **Password**: Create a strong password. Mention "Key3" or similar alias.
7.  Fill in the Certificate details (Name, Organization, etc.) -> OK.

### Step B: Configure Gradle (Recommended Way)
Instead of hardcoding passwords, use a `keystore.properties` file.

1.  **Create `keystore.properties`** in your root directory (add to `.gitignore`):
    ```properties
    storePassword=your_store_password
    keyPassword=your_key_password
    keyAlias=your_key_alias
    storeFile=../app/upload-keystore.jks
    ```

2.  **Update `app/build.gradle.kts`**:
    Add the signing config inside the `android { ... }` block:

    ```kotlin
    android {
        // ... existing config ...

        signingConfigs {
            create("release") {
                val keystorePropertiesFile = rootProject.file("keystore.properties")
                val keystoreProperties = java.util.Properties()
                if (keystorePropertiesFile.exists()) {
                    keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
                }
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }

        buildTypes {
            release {
                isMinifyEnabled = true // Verify this is safe for your app, or keep false
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    ```

### Step C: Build the Release Bundle
Run the following command in the terminal:
```bash
./gradlew bundleRelease
```

### Step D: Locate the Artifact
Once the build finishes, your uploadable file will be at:
`app/build/outputs/bundle/release/app-release.aab`

Upload this file to the **internal testing** track on the Google Play Console first!

---

## 3. Play Store Listing Checklist
Before you can hit "Publish", you need:
1.  **Privacy Policy URL**: Required because you access file storage. You can host a simple MD file on GitHub Pages.
2.  **App Icon**: High-res (512x512) PNG.
3.  **Feature Graphic**: 1024x500 PNG/JPG.
4.  **Screenshots**: At least 2 for phone, 7-inch tablet, and 10-inch tablet.
5.  **Short Description**: 80 chars.
6.  **Full Description**: up to 4000 chars.
