# 🏛️ Landmark Bangladesh

A modern Android application built with Jetpack Compose for managing and visualizing landmarks across Bangladesh. The app provides an intuitive interface to create, view, update, and delete landmark records with GPS coordinates and images, featuring both list and map views.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)

---

## 📱 App Summary

**Landmark Bangladesh** is a full-featured mobile application that allows users to discover, document, and explore landmarks throughout Bangladesh. The app integrates with a REST API backend for data persistence and uses OpenStreetMap for interactive map visualization.

### Key Highlights

- ✅ **Modern UI** - Built entirely with Jetpack Compose and Material Design 3
- ✅ **Interactive Map** - OpenStreetMap integration with custom markers
- ✅ **Complete CRUD** - Create, Read, Update, Delete landmark records
- ✅ **Image Support** - Capture photos or select from gallery
- ✅ **GPS Integration** - Auto-detect current location for new landmarks
- ✅ **REST API** - Cloud-based backend for data synchronization
- ✅ **Swipe Gestures** - Intuitive swipe-to-edit/delete actions
- ✅ **Offline-Ready** - Cached map tiles for better performance

---

## ✨ Features

### 🗺️ Map Overview Screen
- **Interactive OpenStreetMap** display centered on Bangladesh
- **Custom markers** showing all landmark locations
- **Zoom and pan** controls for easy navigation
- **Marker info** - Tap markers to see landmark details
- **Real-time updates** - Automatically refreshes when data changes
- **Manual refresh** button for on-demand updates

### 📋 List View Screen
- **Scrollable card layout** displaying all landmarks
- **High-quality images** loaded asynchronously with Coil
- **Swipe gestures**:
  - Swipe **right** → Edit landmark
  - Swipe **left** → Delete with confirmation
- **Delete confirmation** dialog to prevent accidental deletions
- **Live count** showing total number of landmarks
- **Pull-to-refresh** functionality
- **Empty state** handling

### ✏️ Add/Edit Form Screen
- **Dual mode** - Add new landmarks or edit existing ones
- **Auto GPS detection** - Automatically detects current location for new landmarks
- **Manual coordinate entry** - Input latitude/longitude manually if needed
- **Camera integration** - Take photos directly from the app
- **Gallery picker** - Select existing photos from device
- **Image optimization** - Automatic resize to 800×600 for optimal upload
- **Real-time validation** - Instant feedback on form fields
- **Success feedback** - Snackbar notifications on completion

### 🎨 User Experience
- **Material Design 3** - Modern, consistent UI across all screens
- **Smooth animations** - Fluid transitions and gesture feedback
- **Loading states** - Clear visual feedback during operations
- **Error handling** - Helpful error messages with retry options
- **Responsive design** - Adapts to different screen sizes
- **Dark mode ready** - Respects system theme preferences

### 🔐 Technical Features
- **MVVM Architecture** - Clean separation of concerns
- **Kotlin Coroutines** - Efficient asynchronous operations
- **Retrofit** - Type-safe HTTP client for API calls
- **StateFlow** - Reactive state management
- **Coil** - Modern image loading with caching
- **Multipart upload** - Support for image file uploads
- **Permission handling** - Runtime permissions for location and camera
- **Logging** - Comprehensive logging for debugging

---

## 🏗️ Architecture

The app follows the **MVVM (Model-View-ViewModel)** architecture pattern:

```
┌─────────────────────────────────────┐
│  UI Layer (Jetpack Compose)        │
│  - OverviewScreen                   │
│  - RecordsScreen                    │
│  - FormScreen                       │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  ViewModel Layer                    │
│  - LandmarkViewModel                │
│  - State Management                 │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  Repository Layer                   │
│  - LandmarkRepository               │
│  - Data Operations                  │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  API Layer (Retrofit)               │
│  - ApiService Interface             │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  REST API Backend                   │
│  https://labs.anontech.info/...    │
└─────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (Material 3)
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle 8.13.1 with Kotlin DSL

### Key Libraries
| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose | 2024.09.00 | Modern declarative UI |
| Material 3 | Latest | Material Design components |
| Retrofit | 2.9.0 | REST API client |
| Coil | 2.5.0 | Async image loading |
| OSMDroid | 6.1.14 | OpenStreetMap integration |
| Kotlin Coroutines | Latest | Asynchronous programming |
| ViewModel | Latest | State management |
| Navigation Compose | 2.5.3 | Screen navigation |

---

## 📋 Prerequisites

Before building the app, ensure you have:

- ✅ **Android Studio** Hedgehog (2023.1.1) or later
- ✅ **JDK 17** or higher
- ✅ **Android SDK** with API level 34
- ✅ **Git** (for cloning the repository)
- ✅ **Internet connection** (for downloading dependencies and API access)

---

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/LandmarkBangladesh.git
cd LandmarkBangladesh
```

### 2. Open in Android Studio

1. Launch **Android Studio**
2. Select **File → Open**
3. Navigate to the cloned repository folder
4. Click **OK**
5. Wait for Gradle sync to complete

### 3. Configure SDK

Ensure the correct SDK is installed:

1. Go to **Tools → SDK Manager**
2. Install **Android 14.0 (API 34)** if not already installed
3. Install **Android SDK Build-Tools 34.0.0**
4. Click **Apply** and **OK**

### 4. Sync Gradle

If Gradle doesn't auto-sync:

1. Click **File → Sync Project with Gradle Files**
2. Wait for the sync to complete
3. Resolve any dependency issues if prompted

---

## ▶️ Running the App

### Option 1: Using Android Emulator

1. **Create an emulator** (if you don't have one):
   - Click **Tools → Device Manager**
   - Click **Create Device**
   - Select a device (e.g., Pixel 6)
   - Select **System Image**: Android 14 (API 34)
   - Click **Finish**

2. **Run the app**:
   - Click the **Run** button (green play icon) or press `Shift + F10`
   - Select your emulator from the dropdown
   - Wait for the app to build and launch

### Option 2: Using Physical Device

1. **Enable Developer Options** on your Android device:
   - Go to **Settings → About Phone**
   - Tap **Build Number** 7 times
   - Go back to **Settings → Developer Options**
   - Enable **USB Debugging**

2. **Connect your device**:
   - Connect via USB cable
   - Accept the USB debugging prompt on your device

3. **Run the app**:
   - Click the **Run** button
   - Select your device from the dropdown
   - The app will install and launch on your device

### Option 3: Build APK

To generate a debug APK:

```bash
# Windows
.\gradlew assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

The APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

To generate a release APK:

```bash
# Windows
.\gradlew assembleRelease

# macOS/Linux
./gradlew assembleRelease
```

---

## 🔧 Build Commands

### Common Gradle Commands

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Check for dependency updates
./gradlew dependencyUpdates

# Generate build report
./gradlew build --scan
```

---

## 📱 App Permissions

The app requires the following permissions:

| Permission | Purpose |
|------------|---------|
| `INTERNET` | API communication and map tile downloads |
| `ACCESS_FINE_LOCATION` | GPS coordinate auto-detection |
| `ACCESS_COARSE_LOCATION` | Approximate location detection |
| `CAMERA` | Taking photos for landmarks |
| `READ_EXTERNAL_STORAGE` | Selecting images from gallery |
| `WRITE_EXTERNAL_STORAGE` | Saving processed images |

All permissions are requested at runtime when needed.

---

## 🌐 API Configuration

The app connects to a REST API for data persistence:

**Base URL**: `https://labs.anontech.info/cse489/t3/`

### API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/api.php` | Fetch all landmarks |
| `POST` | `/api.php` | Create new landmark |
| `PUT` | `/api.php` | Update existing landmark |
| `DELETE` | `/api.php?id={id}` | Delete landmark |

### API Request Examples

**Create Landmark (POST)**:
```
Content-Type: multipart/form-data

Fields:
- title: String
- lat: Double
- lon: Double
- image: File (optional)
```

**Update Landmark (PUT)**:
```
Content-Type: multipart/form-data

Fields:
- id: Int
- title: String (optional)
- lat: Double (optional)
- lon: Double (optional)
- image: File (optional)
```

**Delete Landmark (DELETE)**:
```
Query Parameter:
- id: Int
```

---

## 📂 Project Structure

```
LandmarkBangladesh/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/landmarkbangladesh/
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/
│   │   │   │   │   │   └── ApiService.kt
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Landmark.kt
│   │   │   │   │   │   └── ApiResponse.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       └── LandmarkRepository.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── AppTopBar.kt
│   │   │   │   │   │   └── LandmarkCard.kt
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── OverviewScreen.kt
│   │   │   │   │   │   ├── RecordsScreen.kt
│   │   │   │   │   │   └── FormScreen.kt
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       └── LandmarkViewModel.kt
│   │   │   │   ├── utils/
│   │   │   │   │   ├── ImageUtils.kt
│   │   │   │   │   └── LocationUtils.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   │   └── androidTest/
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🎮 Using the App

### Adding a New Landmark

1. Navigate to the **Form** screen (bottom navigation)
2. The app will automatically detect your GPS coordinates
3. Enter a **title** for the landmark
4. Tap **"Take Photo"** or **"Choose from Gallery"**
5. Select or capture an image
6. Wait for image processing (resize to 800×600)
7. Review the form details
8. Tap **"Add Landmark"**
9. Wait for success message
10. Navigate to **List** or **Map** to see your new landmark

### Viewing Landmarks

**List View**:
- Scroll through cards showing all landmarks
- Each card displays: image, title, location coordinates
- Swipe right to edit
- Swipe left to delete (with confirmation)

**Map View**:
- See all landmarks as markers on Bangladesh map
- Pinch to zoom in/out
- Drag to pan around
- Tap marker to see landmark info
- Use refresh button to reload data

### Editing a Landmark

1. From **List** screen, swipe a card **right**
2. Form opens with existing data pre-filled
3. Modify title, coordinates, or image
4. Tap **"Update Landmark"**
5. Success message appears
6. Navigate back to see updated landmark

### Deleting a Landmark

1. From **List** screen, swipe a card **left**
2. Confirmation dialog appears
3. Tap **"Delete"** to confirm or **"Cancel"** to abort
4. Success message appears
5. Card disappears from list
6. Map updates automatically

---

## 🐛 Troubleshooting

### Build Issues

**Problem**: Gradle sync fails
```
Solution:
1. File → Invalidate Caches → Invalidate and Restart
2. Delete .gradle folder in project root
3. Sync again
```

**Problem**: SDK not found
```
Solution:
1. Tools → SDK Manager
2. Install Android SDK 34
3. Set ANDROID_HOME environment variable
```

### Runtime Issues

**Problem**: App crashes on launch
```
Solution:
1. Check Logcat for error messages
2. Verify all permissions in AndroidManifest.xml
3. Clear app data: Settings → Apps → Landmark Bangladesh → Clear Data
```

**Problem**: Images not loading
```
Solution:
1. Check internet connection
2. Verify INTERNET permission granted
3. Check Logcat for Coil errors
4. Try clearing app cache
```

**Problem**: GPS not detecting
```
Solution:
1. Enable Location Services on device
2. Grant location permissions
3. Ensure GPS signal (go outdoors if indoors)
4. Check Google Play Services is updated
```

**Problem**: Map not showing
```
Solution:
1. Check INTERNET permission
2. Clear OSMDroid cache: Settings → Apps → Clear Cache
3. Verify map tiles can download (check internet)
4. Check Logcat for OSMDroid errors
```

---

## 🧪 Testing

### Running Unit Tests

```bash
./gradlew test
```

### Running Instrumentation Tests

```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist

- [ ] Add new landmark with photo
- [ ] Add new landmark with gallery image
- [ ] GPS auto-detection works
- [ ] View landmarks in list
- [ ] View landmarks on map
- [ ] Edit existing landmark
- [ ] Delete landmark with confirmation
- [ ] Swipe gestures work smoothly
- [ ] Map markers are accurate
- [ ] Images load correctly
- [ ] Error states display properly
- [ ] Success snackbars appear
- [ ] Offline behavior (cached data)

---

## 📊 Performance Optimization

The app includes several optimizations:

- **LazyColumn** for efficient list rendering (only visible items)
- **Image caching** with Coil (reduces redundant downloads)
- **Image resizing** to 800×600 (reduces upload time and bandwidth)
- **Coroutines** for non-blocking async operations
- **StateFlow** for efficient state updates
- **Map tile caching** with OSMDroid (faster map loads)
- **Multipart upload** for efficient file transfers

---

## 🔒 Security Considerations

- ✅ HTTPS-only API communication
- ✅ Runtime permission requests (not granted by default)
- ✅ Input validation before API submission
- ✅ Proper error handling (no sensitive data in logs)
- ✅ FileProvider for secure file sharing
- ✅ No hardcoded credentials

---

## 🚧 Known Issues

- Map may be slow on first load (downloading tiles)
- GPS detection may fail indoors or with poor signal
- Large images (>5MB) may take time to process
- Delete requires internet connection (no offline queue)
- Problem with edit landmark cards, file edit
- some isssue with auto location detection


---

## 🗺️ Roadmap

Future enhancements planned:

- [ ] Offline mode with local database (Room)
- [ ] Search and filter landmarks
- [ ] Categories and tags
- [ ] User accounts and authentication
- [ ] Favorite landmarks
- [ ] Share landmarks via social media
- [ ] Export data as CSV/JSON
- [ ] Dark theme optimization
- [ ] Multi-language support (Bengali)
- [ ] Landmark directions and navigation
- [ ] Nearby landmarks detection
- [ ] Photo gallery for multiple images
- [ ] Comments and ratings

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Developer

**Ahmed Riasat**  
Android Developer  
Landmark Bangladesh Project  

---

## 🙏 Acknowledgments

- **OpenStreetMap** - Map tiles and data
- **Material Design** - UI/UX guidelines
- **Square** - Retrofit and OkHttp libraries
- **Coil** - Image loading library
- **OSMDroid** - OpenStreetMap Android library
- **JetBrains** - Kotlin language
- **Google** - Android platform and Jetpack libraries


## 🎯 Quick Start Summary

```bash
# 1. Clone repository
git clone https://github.com/yourusername/LandmarkBangladesh.git

# 2. Open in Android Studio
# File → Open → Select project folder

# 3. Sync Gradle
# File → Sync Project with Gradle Files

# 4. Run app
# Click Run button or press Shift + F10

# 5. Build APK (optional)
./gradlew assembleDebug
```

---

**Version**: 1.0.0  
**Last Updated**: December 9, 2024  
**Platform**: Android  
**Language**: Kotlin  
**Framework**: Jetpack Compose

---


