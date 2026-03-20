# Security Policy

## Supported Versions

We provide security updates for the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take the security of Deep Live Cam Android seriously. If you discover a security vulnerability, please follow these steps:

### 1. **Do NOT** Create a Public Issue

Please do not report security vulnerabilities through public GitHub issues, discussions, or pull requests.

### 2. Report Privately

Instead, please report security vulnerabilities by:

- **Email**: Send details to the repository owner (check GitHub profile for contact)
- **GitHub Security Advisory**: Use the "Security" tab > "Report a vulnerability" feature

### 3. Include in Your Report

Please include the following information:

- **Description**: Clear description of the vulnerability
- **Impact**: What an attacker could potentially do
- **Steps to Reproduce**: Detailed steps to reproduce the issue
- **Affected Versions**: Which versions are affected
- **Proof of Concept**: Code or screenshots if possible
- **Suggested Fix**: If you have ideas for fixing it

### 4. Response Timeline

- **Initial Response**: Within 48 hours
- **Status Update**: Within 7 days
- **Fix Timeline**: Depends on severity (critical: 1-7 days, high: 7-30 days)

### 5. Disclosure Policy

- We follow **responsible disclosure** practices
- We will work with you to understand and resolve the issue
- We will credit you in the security advisory (unless you prefer to remain anonymous)
- Please allow us reasonable time to fix the issue before public disclosure

## Security Considerations

### Data Privacy

- **All processing is on-device** - No data is sent to external servers
- **No cloud storage** - All files remain on the device
- **No analytics or tracking** - We do not collect user data

For more details, see [PRIVACY_POLICY.md](PRIVACY_POLICY.md).

### Permissions

This app requires the following permissions:

- **CAMERA**: For real-time face swapping
- **READ_EXTERNAL_STORAGE**: To process images/videos from gallery
- **WRITE_EXTERNAL_STORAGE**: To save processed results (Android 9 and below)

We do not request network permissions, ensuring your data stays private.

### Model Security

- Models are loaded from the app's assets directory
- No dynamic model downloading (prevents model poisoning attacks)
- Models are verified during build time

### Known Limitations

- **Face Recognition**: The app uses on-device face detection and may have false positives
- **NSFW Filter**: Placeholder implementation - not production-ready
- **Video Processing**: Currently limited implementation

## Security Best Practices for Users

1. **Download from Official Sources**: Only install from Google Play Store or official GitHub releases
2. **Check Permissions**: Review app permissions before installation
3. **Keep Updated**: Install security updates promptly
4. **Report Suspicious Behavior**: If the app behaves unexpectedly, report it
5. **Respect Privacy**: Do not create deepfakes of people without their consent

## Acknowledgments

We appreciate security researchers who help keep Deep Live Cam Android safe. Responsible disclosures will be acknowledged in our security advisories.

---

**Thank you for helping keep Deep Live Cam Android and our users safe!**
