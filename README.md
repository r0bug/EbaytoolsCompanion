# eBay Tools Companion - Android App

Mobile companion app for eBay Tools desktop application. Capture photos and item information on the go, then export for processing on desktop.

## Features

- 📸 **Multi-photo capture** - Take multiple photos per item
- 📝 **Quick data entry** - Voice-to-text support for notes
- 📁 **Project organization** - Group items into collections
- 🏷️ **Barcode scanning** - Quick SKU/product entry
- 📤 **Flexible export** - USB, cloud, or share options
- 🔄 **Desktop integration** - Seamless import into eBay Tools

## Requirements

- Android 7.0 (API 24) or higher
- Camera permission for photo capture
- Storage permission for saving data

## Building the App

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK 34

### Build Steps

1. Clone the repository:
```bash
git clone https://github.com/r0bug/EbaytoolsCompanion.git
cd EbaytoolsCompanion
```

2. Open in Android Studio:
- File → Open → Select project directory
- Let Gradle sync complete

3. Build the app:
- Build → Build APK (for testing)
- Build → Generate Signed Bundle/APK (for release)

## Development Setup

### Project Structure
```
app/
├── src/main/java/com/ebaytools/companion/
│   ├── MainActivity.kt
│   ├── data/              # Data models and database
│   ├── ui/                # UI components
│   └── utils/             # Utility classes
├── src/main/res/          # Resources
└── build.gradle.kts       # Build configuration
```

### Key Dependencies
- AndroidX libraries
- Material Design 3
- CameraX for photo capture
- Room for local database
- ML Kit for barcode scanning

## Usage

1. **Create Project**: Start a new collection for your items
2. **Add Items**: Tap (+) to add new items
3. **Capture Photos**: Take multiple photos per item
4. **Add Details**: Title, notes, category, etc.
5. **Export**: Choose export method (ZIP, folder, share)
6. **Import on Desktop**: Use Mobile Import tool in eBay Tools

## Data Format

Exports follow the eBay Tools mobile data exchange specification:
- JSON manifest with item metadata
- Organized photo files
- Compatible with desktop import tool

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## License

This project is part of eBay Tools and follows the same license.

## Related Projects

- [eBay Tools Desktop](https://github.com/r0bug/Ebaytools) - Main desktop application
- [Mobile Data Spec](https://github.com/r0bug/Ebaytools/blob/main/mobile/MOBILE_API_SPEC.md) - Data exchange format

## Support

Report issues on the [GitHub Issues](https://github.com/r0bug/EbaytoolsCompanion/issues) page.