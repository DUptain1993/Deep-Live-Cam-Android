# 🚀 Quick Start - Build via GitHub Actions

## Why GitHub Actions?
If your local machine doesn't have the Android SDK or sufficient resources, GitHub Actions can build the APK in the cloud for free.

## ✅ Solution: Use GitHub Actions

**You don't need to build locally!** GitHub Actions will build your APK in the cloud for free.

## 📝 Simple 3-Step Process

### Step 1: Push Your Code
```bash
cd Deep-Live-Cam-Android
git add .
git commit -m "Your changes"
git push origin master
```

### Step 2: Wait for Build
1. Go to your repository's **Actions** tab on GitHub
2. Click on the latest workflow run
3. Wait ~5-10 minutes for completion
4. ✅ Green checkmark = Success!

### Step 3: Download APK
1. Scroll down to **"Artifacts"** section
2. Click **"app-debug-apk-XXX"** to download
3. Extract ZIP file
4. Install `app-debug.apk` on your Android device

## 🎯 What's configured

### 1. Auto Debug Builds
- **When:** Every push to `master` branch
- **What:** Builds debug APK automatically
- **Time:** ~5-10 minutes
- **Artifact:** Available for 30 days

### 2. Manual Builds
You can also trigger builds manually:
1. Go to **Actions** tab
2. Select **"Android Debug Build"**
3. Click **"Run workflow"**
4. Select `master` branch
5. Click green **"Run workflow"** button

### 3. Enhanced Workflows
- ✅ Better caching (faster builds)
- ✅ Proper error handling
- ✅ Build summaries
- ✅ Test results included
- ✅ Lint reports included
- ✅ APK size reporting

## 💡 Why This Works Better

| Building Locally | GitHub Actions |
|------------------|----------------|
| Need 8GB+ RAM | Uses cloud (0 RAM) |
| Need ~10GB disk | Uses cloud (0 disk) |
| Install JDK 17 | Pre-installed ✅ |
| Install Android SDK | Pre-installed ✅ |
| Drains battery | No impact ✅ |
| 5-10 min build | 5-10 min build |
| Manual download | Auto artifacts ✅ |

## 🔧 What Changed

The GitHub Actions workflows include:

1. **`.github/workflows/android-debug.yml`**
   - Added better caching
   - Added build summaries
   - Added APK size reporting
   - Better artifact naming
   - Test results upload
   - Lint report upload
   - Works on both `main` and `master` branches

2. **`.github/workflows/android-release.yml`**
   - Added fallback for unsigned builds
   - Better secret handling
   - Release automation
   - Improved artifact retention

3. **`gradle.properties`**
   - Optimized for CI/CD
   - Better memory management
   - Faster builds

## 📱 Install APK on Your Phone

### USB Method (Recommended)
```bash
# If you have adb installed
adb install app-debug.apk

# If not, just transfer the file
```

### Direct Transfer Method
1. Download artifact ZIP from GitHub
2. Extract `app-debug.apk`
3. Transfer to phone:
   - USB file transfer, or
   - Upload to Google Drive/Dropbox, or
   - Email to yourself
4. On phone:
   - Open file manager
   - Find the APK
   - Tap to install
   - Allow "Unknown Sources" if asked
   - Done! 🎉

## 🎮 First Run

1. Open "Deep Live Cam" app
2. Grant permissions:
   - ✅ Camera
   - ✅ Photos/Media
3. Tap "Select Source Face"
4. Choose a face image
5. Tap "Start Camera"
6. Point at your face
7. See real-time face swap! 🤳

## ⚡ Next Time You Make Changes

```bash
# 1. Make your code changes
vim app/src/main/...

# 2. Commit and push
git add .
git commit -m "Fixed bug / Added feature"
git push origin master

# 3. Go to GitHub Actions
# 4. Download new APK
# 5. Install on phone
# 6. Test!
```

## 🐛 If Build Fails

1. Click on the failed workflow run
2. Click on the red ❌ step
3. Read the error message
4. Fix the issue in your code
5. Push again

Common errors:
- **Compile error** → Fix Java/Kotlin syntax
- **Dependency issue** → Check `build.gradle.kts`
- **Lint warnings** → Review lint report artifact

## 📊 Monitor Builds

### Via GitHub Web
- Go to **Actions** tab
- See all builds
- Green ✅ = Success
- Red ❌ = Failed
- Yellow 🟡 = Running

### Via CLI (if you have gh installed)
```bash
# List recent runs
gh run list

# View specific run
gh run view <run-id>

# Download artifacts
gh run download <run-id>
```

## 💰 Cost

**FREE!** 🎉

GitHub Actions is free for public repositories with:
- 2,000 minutes/month
- Your builds take ~10 minutes each
- You can do ~200 builds/month for free

## ✅ Current Status

- **Workflows:** ✅ Configured
- **Auto-build:** ✅ Enabled
- **Artifacts:** ✅ Enabled
- **Tests:** ✅ Enabled
- **Lint:** ✅ Enabled
- **Local build needed:** ❌ NO!

## 🎯 TL;DR

1. **Optional:** skip local builds and use GitHub Actions instead
2. **Just push code** to GitHub
3. **Wait 5-10 minutes** for Actions to build
4. **Download APK** from artifacts
5. **Install on phone** and enjoy!

---

**You're all set!** 🚀

Just push your code and GitHub will build it for you!

**Next:** Make a small change and push to test it out.
