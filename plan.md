1. Project setup and dependencies:
   - Configure Gradle dependencies: CameraX, ML Kit (Text Recognition Latin & Japanese, Translation), Tesseract4Android (for Thai), Jetpack Compose, Room, Hilt. (Done)
   - Add permissions for Camera and Internet in AndroidManifest.xml (Done).
2. Domain & Data Layers:
   - Configure Room Database for translation history.
   - Setup Hilt for Dependency Injection.
   - Implement Translation Engine abstracting ML Kit Translation, and text caching.
   - Implement OCR Provider abstracting ML Kit Text Recognition and Tesseract.
   - Create models for Bounding Boxes and Language.
3. UI Layer (Jetpack Compose):
   - Scaffold the Main Screen with Camera Preview and Top/Bottom bars.
   - Create Overlay to render translated texts using bounded boxes correctly mapped to screen coordinates.
   - Create Settings Screen and History Screen.
   - Build Image Picker screen for translating photos from Gallery.
4. Camera Integration:
   - Implement CameraX integration in Compose.
   - Use `ImageAnalysis` to stream frames.
   - Process frames with OCR Provider in the background.
5. OCR & Translation Logic (The core):
   - Translate text sequentially or concurrently with coroutines, avoiding blocking.
   - Manage the translation overlay state in a ViewModel.
   - Only translate when text has stabilized (debounce).
   - Display translation near the recognized text.
6. CI/CD:
   - Setup GitHub Actions for building debug and release APK.
7. Pre-commit check.
8. Submit.
