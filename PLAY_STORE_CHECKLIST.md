# Play Store Release Checklist

## App Information
- [x] App name: Deep Live Cam - Real-Time Face Swap
- [x] Package name: com.deeplivecam.android
- [x] Version: 1.0.0 (versionCode: 1)
- [x] Category: Photography / Entertainment
- [x] Content rating: 17+ (Mature content - Deepfake creation)

## Required Assets

### App Icon
- [ ] 512x512 PNG app icon (high-res)
- [ ] Adaptive icon (foreground + background)
- [ ] Round icon variant

### Screenshots (Required: minimum 2)
- [ ] Phone screenshots (JPEG/PNG, 16:9 or 9:16)
  - [ ] Welcome screen
  - [ ] Live face swap demo
  - [ ] Settings screen
  - [ ] Gallery view
  - [ ] Photo processing result
  - [ ] Video processing UI
- [ ] Tablet screenshots (7" and 10") - Optional but recommended
- [ ] All screenshots must show app in use (no generic images)

### Feature Graphic
- [ ] 1024x500 PNG feature graphic (banner)
  - Design: App logo + "Real-Time Face Swap" + device mockup
  - Show face swap in action
  - Include "Privacy First - On Device AI" tagline

### Promotional Video (Optional but highly recommended)
- [ ] YouTube video link (30-120 seconds)
  - Demo of real-time face swap
  - Show photo/video processing
  - Highlight privacy features
  - Show settings options

### Promo Graphic (Optional)
- [ ] 180x120 PNG promo graphic

## Store Listing Text

### Short Description (80 chars)
```
Real-time face swap & deepfake app. On-device AI, no cloud, privacy-focused.
```

### Full Description
- [x] See PLAY_STORE_DESCRIPTION.md
- [x] Includes feature list
- [x] Includes responsible use guidelines
- [x] Includes privacy statement
- [x] Includes technical specs

## Legal Documents

### Privacy Policy
- [x] Privacy policy created (PRIVACY_POLICY.md)
- [ ] Host privacy policy on permanent URL
  - Option 1: GitHub Pages
  - Option 2: Website
- [ ] Add privacy policy URL to Play Console
- [ ] Add privacy policy link in app (Settings screen)

### Terms of Service (Optional but recommended)
- [ ] Create terms of service
- [ ] Host on permanent URL
- [ ] Link from app

## App Configuration

### Build Configuration
- [x] Release build type configured
- [x] ProGuard/R8 rules defined
- [x] Signing configuration ready
- [ ] Generate release keystore
- [ ] Store keystore securely (password protected)
- [ ] Backup keystore to secure location

### Permissions
- [x] Camera permission (runtime)
- [x] Storage permission (runtime, API-level specific)
- [x] All permissions justified in listing

### Age Rating
- [ ] Complete content rating questionnaire
- [x] Expected rating: 17+ (Mature)
  - Reason: Deepfake creation capability
  - Realistic depiction of people

## Testing Requirements

### Pre-Launch Report (Google Play)
- [ ] Run pre-launch report on Firebase Test Lab
- [ ] Fix all critical issues
- [ ] Address warnings

### Manual Testing
- [x] Unit tests passing
- [x] Instrumented tests passing
- [ ] Manual testing on:
  - [ ] Low-end device (2GB RAM, Android 8)
  - [ ] Mid-range device (4GB RAM, Android 11)
  - [ ] High-end device (8GB+ RAM, Android 13+)
  - [ ] Tablet (10" screen)

### Test Cases
- [ ] Cold start (first launch)
- [ ] Permission flow
- [ ] Camera switching
- [ ] Source face selection
- [ ] Real-time face swap (15+ seconds)
- [ ] Photo processing
- [ ] Video processing (1+ minute video)
- [ ] Settings changes (all toggles)
- [ ] Gallery view and delete
- [ ] Thermal throttling (long session)
- [ ] Low memory handling
- [ ] App backgrounding/resuming
- [ ] Device rotation

## Release Build

### APK/AAB Generation
- [ ] Build release AAB (Android App Bundle)
  ```bash
  ./gradlew bundleRelease
  ```
- [ ] Build release APK (for backup)
  ```bash
  ./gradlew assembleRelease
  ```
- [ ] Sign APK/AAB with release keystore
- [ ] Verify signature
- [ ] Test release build on real device

### Size Optimization
- [x] ProGuard enabled
- [x] Resource shrinking enabled
- [x] Unused resources removed
- [ ] Verify APK size < 100MB
- [ ] Enable App Bundle optimization

## Play Console Setup

### App Access
- [ ] Set app access type:
  - [x] All functionality available without restrictions
  - [x] No login required
  - [x] No special hardware required

### Content Rating
- [ ] Complete IARC questionnaire
- [ ] Expected ratings:
  - ESRB: Mature 17+
  - PEGI: 16
  - USK: 16
  - ACB: M

### Target Audience
- [ ] Age groups: 18 and up
- [ ] Not designed for children

### News Apps Declaration
- [ ] Not a news app

### COVID-19 Contact Tracing
- [ ] Not a contact tracing app

### Data Safety
- [ ] Complete data safety form:
  - [x] Data collection: None
  - [x] Data sharing: None
  - [x] Data security: All local processing
  - [x] No user accounts
  - [x] Data deletion: Uninstall app

### Government Apps
- [ ] Not a government app

## Release Strategy

### Internal Testing (Optional but recommended)
- [ ] Upload to internal testing track
- [ ] Invite team members
- [ ] Test for 1-2 weeks
- [ ] Fix reported issues

### Closed Testing (Optional)
- [ ] Create closed testing group
- [ ] Invite beta testers
- [ ] Collect feedback
- [ ] Iterate based on feedback

### Open Testing (Recommended)
- [ ] Release to open testing track
- [ ] Set max testers (e.g., 1000)
- [ ] Run for 2-4 weeks
- [ ] Monitor crash reports
- [ ] Fix critical issues

### Production Release
- [ ] Final testing complete
- [ ] All critical issues resolved
- [ ] Release notes prepared
- [ ] Submit to production
- [ ] Choose rollout:
  - [ ] Staged rollout (recommended): 5% → 10% → 50% → 100%
  - [ ] Full rollout

## Post-Release

### Monitoring
- [ ] Monitor crash reports (Play Console)
- [ ] Monitor ANRs (Application Not Responding)
- [ ] Monitor user reviews
- [ ] Track download metrics
- [ ] Monitor uninstall rate

### Support
- [ ] Set up GitHub Issues for bug reports
- [ ] Respond to user reviews
- [ ] Create FAQ document
- [ ] Set up support email

### Updates
- [ ] Plan update schedule (monthly/quarterly)
- [ ] Monitor feature requests
- [ ] Track bugs to fix in updates
- [ ] Prepare changelog for updates

## Marketing (Optional)

### Social Media
- [ ] Create announcement post
- [ ] Share on relevant communities:
  - [ ] r/Android
  - [ ] r/androidapps
  - [ ] XDA Forums
  - [ ] Android Police tips

### Press Kit
- [ ] App description
- [ ] Screenshots
- [ ] Feature list
- [ ] Privacy highlights
- [ ] Contact information

## Legal Compliance

### GDPR (EU)
- [x] Privacy policy compliant
- [x] No data collection = compliant
- [x] User controls all data

### CCPA (California)
- [x] No personal data sale
- [x] No tracking

### Other Jurisdictions
- [ ] Review local laws regarding:
  - Deepfake creation
  - Face recognition technology
  - Privacy requirements

## Final Checks

- [ ] All features working
- [ ] No crashes in testing
- [ ] Privacy policy accessible
- [ ] Responsible use warnings displayed
- [ ] All Play Store assets uploaded
- [ ] Release build signed and tested
- [ ] Team ready for support
- [ ] Monitoring tools configured

---

## Commands Reference

### Build Release AAB
```bash
cd /home/xmrfk/Deep-Live-Cam-Android
./gradlew bundleRelease
```

### Build Release APK
```bash
./gradlew assembleRelease
```

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

### Sign APK (manual)
```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore release.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  alias_name
```

---

**Target Release Date**: Q2 2026
**Version**: 1.0.0 MVP
