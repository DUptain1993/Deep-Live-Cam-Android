# GitHub Actions Build Guide

## 🎯 Overview

This project uses GitHub Actions to build your Android APK in the cloud, so you **don't need to build locally** on your laptop with limited resources.

## 🚀 How to Build Using GitHub Actions

### Option 1: Automatic Build (Push to GitHub)

Every time you push to the `main` or `master` branch, GitHub Actions automatically builds a debug APK.

```bash
# Make any changes to your code
git add .
git commit -m "Update code"
git push origin master
```

Then:
1. Go to your GitHub repository
2. Click on **"Actions"** tab
3. Click on the latest workflow run
4. Wait for the build to complete (~5-10 minutes)
5. Download the APK from **"Artifacts"** section

### Option 2: Manual Build (Workflow Dispatch)

You can trigger a build manually without pushing code:

1. Go to your GitHub repository
2. Click on **"Actions"** tab
3. Select **"Android Debug Build"** workflow
4. Click **"Run workflow"** button
5. Select branch (usually `master`)
6. Click green **"Run workflow"** button
7. Wait for completion and download APK from artifacts

### Option 3: Release Build (Signed APK)

For production builds:

1. Go to **Actions** → **"Android Release Build"**
2. Click **"Run workflow"**
3. Choose build type:
   - `assembleRelease` - for APK
   - `bundleRelease` - for AAB (Google Play)
4. Download from artifacts

**Note:** Release builds require keystore secrets (see Setup below)

## 📦 Download Built APK

After a successful build:

1. Go to the workflow run page
2. Scroll down to **"Artifacts"** section
3. Click on **"app-debug-apk-XXX"** to download
4. Extract the ZIP file
5. Install `app-debug.apk` on your Android device

## ⚙️ GitHub Actions Workflows

### 1. `android-debug.yml` - Debug Builds

**Triggers:**
- Push to `main` or `master` branch
- Pull requests
- Manual workflow dispatch

**What it does:**
- ✅ Builds debug APK
- ✅ Runs unit tests
- ✅ Runs Android Lint
- ✅ Uploads APK as artifact (30 days retention)
- ✅ Uploads test results
- ✅ Uploads lint reports

**Build time:** ~5-10 minutes

### 2. `android-release.yml` - Release Builds

**Triggers:**
- Manual workflow dispatch only
- GitHub releases

**What it does:**
- ✅ Builds signed release APK/AAB
- ✅ Uploads to artifacts (90 days retention)
- ✅ Attaches to GitHub releases (if release trigger)
- ⚠️ Requires keystore secrets

**Build time:** ~10-15 minutes

## 🔐 Setup Release Signing (Optional)

To build signed release APKs, you need to set up repository secrets:

### 1. Create a Release Keystore

```bash
# On your local machine
keytool -genkey -v \
  -keystore release.keystore \
  -alias my-key-alias \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### 2. Convert Keystore to Base64

```bash
base64 release.keystore > keystore.base64.txt
```

### 3. Add GitHub Secrets

Go to: **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Add these secrets:
- `ANDROID_KEYSTORE_BASE64` - Content of `keystore.base64.txt`
- `KEY_ALIAS` - Your key alias (e.g., `my-key-alias`)
- `KEY_PASSWORD` - Your key password
- `STORE_PASSWORD` - Your keystore password

### 4. Run Release Build

Now you can run the release workflow and it will produce a signed APK.

## 💡 Benefits of GitHub Actions

### vs Building Locally on Your Laptop:

| Feature | Local Build | GitHub Actions |
|---------|-------------|----------------|
| **RAM Usage** | 4GB+ required | 0GB (cloud) |
| **Disk Space** | ~5GB needed | 0GB (cloud) |
| **Build Time** | 5-10 min | 5-10 min |
| **Battery Drain** | High | None |
| **Background Build** | No | Yes ✅ |
| **Build History** | No | Yes ✅ |
| **Artifact Storage** | Manual | Auto 30-90 days |
| **Multiple Builds** | Sequential | Parallel ✅ |

## 📊 Monitoring Builds

### Check Build Status

```bash
# View recent workflow runs
gh run list --workflow=android-debug.yml

# View specific run details
gh run view <run-id>

# Download artifacts
gh run download <run-id>
```

Or use the GitHub web interface:
- Repository → **Actions** tab
- See all workflow runs
- Green ✅ = Success
- Red ❌ = Failed
- Yellow 🟡 = In progress

## 🐛 Troubleshooting

### Build Failed

1. Click on the failed workflow run
2. Expand the failed step
3. Read the error message
4. Common issues:
   - **Gradle sync failed** - Check `build.gradle.kts`
   - **Compile error** - Fix code issues
   - **Lint errors** - Check lint report artifact
   - **Out of memory** - Usually auto-handled

### No Artifacts Available

- Artifacts only appear if build succeeds
- Check if workflow completed successfully
- Artifacts expire (30-90 days)

### Release Build Fails

- Check if all secrets are set correctly
- Verify keystore is valid
- Try debug build first to isolate issue

## 🎯 Best Practices

### For Development

1. **Use debug builds** for testing
2. **Let GitHub Actions build** instead of local
3. **Download APK** from artifacts
4. **Push regularly** to trigger auto-builds

### For Release

1. **Test with debug builds** first
2. **Use release workflow** for production
3. **Tag releases** in GitHub
4. **Store keystore safely** (never commit!)

### For Low-Spec Laptop (4GB RAM)

✅ **DO:**
- Push code to GitHub
- Let Actions build APK
- Download and install
- Work on small code changes locally

❌ **DON'T:**
- Build locally (uses too much RAM)
- Run Android Studio (4GB RAM insufficient)
- Keep emulator running

## 📱 Install APK on Device

### Method 1: USB (ADB)

```bash
# Download artifact from GitHub
unzip app-debug-apk-XXX.zip

# Install on device
adb install app-debug.apk
```

### Method 2: Direct Transfer

1. Download artifact ZIP from GitHub Actions
2. Extract to get `app-debug.apk`
3. Transfer to phone via:
   - USB file transfer
   - Google Drive / Dropbox
   - Email attachment
4. On phone:
   - Open file manager
   - Tap APK file
   - Allow "Install from Unknown Sources"
   - Tap "Install"

## 🔄 Workflow File Locations

- `.github/workflows/android-debug.yml` - Debug builds
- `.github/workflows/android-release.yml` - Release builds

## 📈 Next Steps

1. ✅ **Push code** to trigger build
2. ✅ **Wait for Actions** to complete
3. ✅ **Download APK** from artifacts
4. ✅ **Install on phone** and test
5. 🔄 **Repeat** as needed

## 🆘 Need Help?

- Check **Actions** tab for build logs
- Review error messages in failed steps
- Test locally if needed: `./gradlew assembleDebug`
- Open an issue if stuck

---

**Status:** ✅ GitHub Actions configured and ready to use

**Your laptop:** Free from heavy builds! 🎉

**Next:** Just push your code and download the built APK!
