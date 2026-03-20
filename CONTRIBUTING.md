# Contributing to Deep Live Cam - Android

Thank you for your interest in contributing to Deep Live Cam Android! We welcome contributions from the community.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Coding Guidelines](#coding-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Guidelines](#issue-guidelines)

## Code of Conduct

### Our Standards

- Be respectful and inclusive
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy towards others

### Ethical Use

This project is intended for educational and research purposes. Contributors must:

- **NOT** create tools for non-consensual deepfakes
- **NOT** enable harassment, fraud, or misinformation
- **RESPECT** privacy and consent
- **FOLLOW** all applicable laws and regulations

## How Can I Contribute?

### Reporting Bugs

Before creating a bug report:

1. **Check existing issues** to avoid duplicates
2. **Use the latest version** to ensure the bug hasn't been fixed
3. **Collect information**:
   - Android version and device model
   - App version
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots or logs if applicable

### Suggesting Enhancements

Enhancement suggestions are welcome! Please:

1. **Check existing feature requests** to avoid duplicates
2. **Describe the use case** - why is this feature needed?
3. **Propose a solution** - how should it work?
4. **Consider alternatives** - are there other approaches?

### Code Contributions

We welcome:

- **Bug fixes**
- **Performance improvements**
- **New features** (discuss in an issue first for large changes)
- **Documentation improvements**
- **Test coverage**

## Development Setup

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17** or later
- **Android SDK 26-34**
- **Git**

### Setup Steps

1. **Fork the repository**
   ```bash
   # Fork on GitHub, then clone your fork
   git clone https://github.com/YOUR_USERNAME/Deep-Live-Cam-Android.git
   cd Deep-Live-Cam-Android
   ```

2. **Open in Android Studio**
   - Open the project in Android Studio
   - Let Gradle sync complete
   - Wait for indexing to finish

3. **Run the app**
   - Connect an Android device or start an emulator
   - Click "Run" or press Shift+F10
   - Select your device

4. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

For detailed build instructions, see [BUILD_INSTALL.md](BUILD_INSTALL.md).

## Coding Guidelines

### Kotlin Style

We follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html):

- **Naming**:
  - Classes: `PascalCase`
  - Functions/variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  
- **Formatting**:
  - Indentation: 4 spaces (no tabs)
  - Line length: 120 characters maximum
  - Use trailing commas in multi-line declarations

- **Code organization**:
  - One public class per file
  - Order: properties → init blocks → constructors → methods
  - Group related functionality together

### Android Best Practices

- **Use Jetpack Compose** for UI components
- **Follow Material Design 3** guidelines
- **Use Kotlin Coroutines** for async operations (not RxJava or callbacks)
- **Inject dependencies** via constructor (manual DI, no framework)
- **Handle lifecycle** properly (collect flows in lifecycle-aware scopes)
- **Request permissions** at runtime (don't assume granted)

### Architecture

- **MVVM pattern**: UI → ViewModel → Repository → Data Source
- **Unidirectional data flow** with Compose
- **Single source of truth** for state
- **Separation of concerns** - keep layers independent

### Comments and Documentation

- **Code should be self-documenting** - use clear names
- **Add comments** only when necessary:
  - Complex algorithms
  - Non-obvious workarounds
  - Important constraints or assumptions
- **Use KDoc** for public APIs:
  ```kotlin
  /**
   * Performs face swap on the given bitmap.
   *
   * @param sourceFace The source face bitmap
   * @param targetFrame The target frame to swap into
   * @return The processed bitmap with swapped face
   */
  fun swapFace(sourceFace: Bitmap, targetFrame: Bitmap): Bitmap
  ```

### Testing

- **Write tests** for new functionality
- **Test coverage**: Aim for >80% for business logic
- **Test types**:
  - Unit tests for ViewModels, repositories, utilities
  - Integration tests for ML model inference
  - UI tests for critical user flows (optional)

Example test:
```kotlin
@Test
fun `face detection returns faces when present`() = runTest {
    val bitmap = loadTestBitmap("face.jpg")
    val result = faceDetector.detectFaces(bitmap)
    assertTrue(result.isNotEmpty())
}
```

## Pull Request Process

### Before Submitting

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-description
   ```

2. **Make your changes**
   - Write clean, readable code
   - Follow the coding guidelines
   - Add/update tests
   - Update documentation if needed

3. **Test thoroughly**
   ```bash
   ./gradlew test
   ./gradlew lint
   ./gradlew assembleDebug
   ```

4. **Commit with clear messages**
   ```bash
   git commit -m "Add face enhancement quality slider"
   # or
   git commit -m "Fix crash when selecting video without faces"
   ```

### Submitting the PR

1. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create Pull Request** on GitHub
   - Provide a clear title and description
   - Reference related issues (e.g., "Fixes #123")
   - Describe what changed and why
   - Include screenshots for UI changes
   - Note any breaking changes

3. **Respond to feedback**
   - Be open to suggestions
   - Make requested changes promptly
   - Ask questions if anything is unclear

### PR Review Process

- Maintainers will review within 3-7 days
- CI checks must pass (build, lint, tests)
- At least one approval required
- Address all feedback before merge

## Issue Guidelines

### Creating Issues

Use the appropriate template:

- **Bug Report**: For reproducible bugs
- **Feature Request**: For new functionality
- **Question**: For help or clarification

### Good Issue Examples

**Bug Report**:
```
Title: App crashes when selecting 4K video

**Environment**:
- Device: Samsung Galaxy S21
- Android: 13
- App version: 1.0.0

**Steps to reproduce**:
1. Open app
2. Tap "Process Video"
3. Select 4K video from gallery
4. App crashes

**Expected**: Video should load
**Actual**: App crashes with OutOfMemoryError

**Logs**: [attach logcat]
```

**Feature Request**:
```
Title: Add batch processing for multiple images

**Problem**: Currently can only process one image at a time

**Proposed Solution**: Add "Select Multiple" option that processes a batch

**Alternatives**: Could use a file picker with multi-select

**Use Case**: Users want to process entire photo albums
```

## Development Workflow

### Branch Strategy

- `master`: Stable releases
- `develop`: Development branch (if used)
- `feature/*`: New features
- `fix/*`: Bug fixes
- `hotfix/*`: Urgent production fixes

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat: Add face swap strength control`
- `fix: Resolve memory leak in video processing`
- `docs: Update README with new features`
- `test: Add unit tests for FaceDetector`
- `refactor: Simplify camera initialization logic`
- `perf: Optimize face detection model loading`

## Additional Resources

- [README.md](README.md) - Project overview
- [BUILD_INSTALL.md](BUILD_INSTALL.md) - Build instructions
- [PRIVACY_POLICY.md](PRIVACY_POLICY.md) - Privacy information
- [Android Developer Guide](https://developer.android.com/guide)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose Guide](https://developer.android.com/jetpack/compose/documentation)

## Questions?

- **GitHub Issues**: For bug reports and feature requests
- **Discussions**: For questions and community chat
- **Repository Owner**: Check GitHub profile for contact information

---

**Thank you for contributing to Deep Live Cam Android!** 🎉

Your contributions help make this project better for everyone. We appreciate your time and effort!
