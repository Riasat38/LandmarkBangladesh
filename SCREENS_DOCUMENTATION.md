# Landmark Bangladesh App - Screens Documentation

## 📱 App Overview

The Landmark Bangladesh app is a complete Android application built with Jetpack Compose that allows users to manage and visualize landmarks across Bangladesh. The app integrates with a REST API to perform CRUD operations (Create, Read, Update, Delete) and displays landmarks on an interactive map.

**Tech Stack:**
- Jetpack Compose (Modern UI)
- MVVM Architecture
- Kotlin Coroutines
- Retrofit (API calls)
- Coil (Image loading)
- OpenStreetMap (Map display)
- Material Design 3

---

## 🗂️ App Screens

The app consists of three main screens:

1. **OverviewScreen** - Map view with all landmarks
2. **RecordsScreen** - List view with swipeable cards
3. **FormScreen** - Add/Edit landmark form

---

# 1️⃣ OverviewScreen - Map Overview

**File**: `OverviewScreen.kt`  
**Purpose**: Displays all landmarks on an interactive OpenStreetMap with markers

## 🎯 What This Screen Does

- Shows an interactive map of Bangladesh
- Displays markers for each landmark at their GPS coordinates
- Allows users to zoom, pan, and interact with the map
- Shows landmark details when clicking markers
- Provides refresh button to reload data
- Displays success/error states with appropriate UI

---

## 📋 Code Breakdown

### 1. Component Declaration (Lines 28-31)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    viewModel: LandmarkViewModel = viewModel()
)
```

**What it does:**
- `@Composable`: Marks this as a Compose UI function
- `viewModel`: Gets shared ViewModel instance to access landmark data
- `@OptIn`: Uses experimental Material3 features (TopAppBar)

---

### 2. State Management (Lines 32-35)

```kotlin
val context = LocalContext.current
val uiState by viewModel.uiState.collectAsState()
val snackbarHostState = remember { SnackbarHostState() }
```

**What each line does:**

| Line | Variable | Purpose |
|------|----------|---------|
| 32 | `context` | Android context for OSM configuration |
| 33 | `uiState` | Observes data state (Loading/Success/Error) |
| 34 | `snackbarHostState` | Manages snackbar display |

**How it works:**
- `collectAsState()` converts Flow to Compose State
- When `uiState` changes, UI automatically recomposes
- `remember` prevents state loss during recomposition

---

### 3. Screen Initialization (Lines 37-41)

```kotlin
LaunchedEffect(Unit) {
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
    Log.d("OverviewScreen", "Screen loaded, refreshing landmark data for map...")
    viewModel.loadLandmarks()
}
```

**What it does:**
1. **Runs once** when screen opens (`Unit` key)
2. **Configures OSMDroid** (OpenStreetMap library)
3. **Loads map preferences** from SharedPreferences
4. **Fetches landmarks** from API via ViewModel

**Why LaunchedEffect:**
- Provides coroutine scope for async operations
- Automatically cancelled if screen is disposed

---

### 4. Success Snackbar (Lines 43-51)

```kotlin
LaunchedEffect(uiState) {
    if (uiState is LandmarkUiState.Success) {
        snackbarHostState.showSnackbar(
            message = "Map Successfully Loaded  ",
            duration = SnackbarDuration.Short
        )
    }
}
```

**What it does:**
- **Watches** `uiState` for changes
- When data loads successfully → Shows snackbar
- Automatically disappears after ~4 seconds

**Flow:**
```
Data loads → uiState = Success → 
LaunchedEffect detects change → Shows snackbar
```

---

### 5. Layout Structure (Lines 53-69)

```kotlin
Scaffold(
    topBar = {
        AppTopBar(
            title = "Landmarks Map Overview",
            actions = {
                IconButton(
                    onClick = {
                        Log.d("OverviewScreen", "Refresh button clicked")
                        viewModel.loadLandmarks()
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
) { paddingValues ->
```

**Component Breakdown:**

| Component | Purpose | Location |
|-----------|---------|----------|
| `Scaffold` | Material Design layout container | Wraps entire screen |
| `topBar` | Top app bar with title + actions | Fixed at top |
| `AppTopBar` | Reusable top bar component | Shows title + refresh button |
| `IconButton` | Refresh button | Top-right corner |
| `snackbarHost` | Container for snackbars | Bottom of screen |
| `paddingValues` | Spacing to avoid overlaps | Applied to content |

**Why Scaffold:**
- Provides consistent Material Design layout
- Automatically manages spacing between components
- Handles snackbar positioning

---

### 6. State-Based UI (Lines 71-188)

The screen shows different UIs based on data state:

#### A. Loading State (Lines 72-88)

```kotlin
is LandmarkUiState.Loading -> {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading landmarks on map...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

**What it shows:**
- Spinning circle (CircularProgressIndicator)
- "Loading landmarks on map..." message
- Centered on screen

**When shown:**
- Initial screen load
- After clicking refresh button
- While API request is in progress

---

#### B. Success State - Map Display (Lines 90-159)

```kotlin
is LandmarkUiState.Success -> {
    val landmarks = currentState.landmarks

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        factory = { context -> ... },
        update = { mapView -> ... }
    )
}
```

**What it does:**
- Extracts landmark list from success state
- Creates interactive map using AndroidView
- Places markers at landmark coordinates

##### Map Factory (Lines 104-133) - Initial Setup

```kotlin
factory = { context ->
    Log.d("OverviewScreen", "Creating MapView with ${landmarks.size} landmarks")

    MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)

        val mapController: IMapController = controller

        // Center map on Bangladesh
        val bangladeshCenter = GeoPoint(23.6850, 90.3563) // Dhaka center
        mapController.setCenter(bangladeshCenter)
        mapController.setZoom(7.0)

        // Add markers for each landmark
        landmarks.forEach { landmark ->
            val marker = Marker(this).apply {
                position = GeoPoint(landmark.latitude, landmark.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = landmark.title
                snippet = landmark.location
            }
            overlays.add(marker)
            Log.d("OverviewScreen", "Added marker for ${landmark.title} at ...")
        }

        // Refresh map
        invalidate()
    }
}
```

**Step-by-step execution:**

1. **Create MapView** instance
   ```kotlin
   MapView(context)
   ```

2. **Configure map tiles**
   ```kotlin
   setTileSource(TileSourceFactory.MAPNIK)
   ```
   - Uses OpenStreetMap MAPNIK tiles (standard map style)
   - Downloads map tiles from OSM servers

3. **Enable touch controls**
   ```kotlin
   setMultiTouchControls(true)
   ```
   - Allows pinch-to-zoom
   - Enables drag to pan

4. **Center map on Bangladesh**
   ```kotlin
   val bangladeshCenter = GeoPoint(23.6850, 90.3563)
   mapController.setCenter(bangladeshCenter)
   mapController.setZoom(7.0)
   ```
   - GeoPoint: Dhaka coordinates (23.6850°N, 90.3563°E)
   - Zoom level 7: Shows entire Bangladesh

5. **Add markers**
   ```kotlin
   landmarks.forEach { landmark ->
       val marker = Marker(this).apply {
           position = GeoPoint(landmark.latitude, landmark.longitude)
           title = landmark.title
           snippet = landmark.location
       }
       overlays.add(marker)
   }
   ```
   - Creates one marker per landmark
   - `position`: GPS coordinates from database
   - `title`: Shows when marker clicked
   - `snippet`: Additional info when marker clicked
   - `overlays.add()`: Adds marker to map

6. **Refresh display**
   ```kotlin
   invalidate()
   ```
   - Forces map to redraw with new markers

##### Map Update (Lines 135-155) - Dynamic Updates

```kotlin
update = { mapView ->
    Log.d("OverviewScreen", "Updating map with ${landmarks.size} landmarks")

    // Clear existing markers
    mapView.overlays.clear()

    // Add updated markers
    landmarks.forEach { landmark ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(landmark.latitude, landmark.longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = landmark.title
            snippet = landmark.location
        }
        mapView.overlays.add(marker)
    }

    // Refresh
    mapView.invalidate()
}
```

**When it runs:**
- When `landmarks` list changes (new/deleted landmarks)
- Automatically called by Compose

**What it does:**
1. **Clears old markers** to prevent duplicates
2. **Adds new markers** from updated landmark list
3. **Refreshes map** to show changes

**Example flow:**
```
User deletes landmark → landmarks list updates →
Compose detects change → Calls update lambda →
Old markers cleared → New markers added → Map refreshes
```

---

#### C. Error State (Lines 161-187)

```kotlin
is LandmarkUiState.Error -> {
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error loading landmarks",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.loadLandmarks() }) {
                Text("Retry")
            }
        }
    }
}
```

**What it shows:**
- Large red "Error loading landmarks" title
- Specific error message (e.g., "Network error", "HTTP 500")
- Retry button to try again

**When shown:**
- API request fails
- Network connection lost
- Server error (4xx, 5xx)

---

## 🔄 Data Flow Diagram

```
Screen Opens
    ↓
LaunchedEffect(Unit) triggers
    ↓
Configure OSMDroid + Load landmarks
    ↓
uiState = Loading
    ↓
Shows: CircularProgressIndicator + "Loading..." text
    ↓
API Request to: https://labs.anontech.info/cse489/t3/api.php
    ↓
┌─────────────────────────┬───────────────────────┐
│   Success Response      │   Error Response       │
└─────────────────────────┴───────────────────────┘
         ↓                           ↓
uiState = Success(landmarks)   uiState = Error(message)
         ↓                           ↓
Shows: Interactive map         Shows: Error screen + Retry
       with markers
         ↓
LaunchedEffect(uiState) detects Success
         ↓
Shows: "Map Successfully Loaded" snackbar
```

---

## 🎨 Visual Layout

```
┌─────────────────────────────────────────┐
│ ┌─────────────────────────────────────┐ │ ← TopAppBar
│ │ Landmarks Map Overview         🔄   │ │
│ └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤
│                                         │
│           [Map of Bangladesh]           │
│                                         │
│              📍 Marker 1                │
│                                         │
│         📍 Marker 2                     │
│                                         │
│                        📍 Marker 3      │
│                                         │
│                                         │
├─────────────────────────────────────────┤
│ Map Successfully Loaded ✓               │ ← Snackbar
└─────────────────────────────────────────┘
```

---

## 🎯 Key Features

1. **Auto-refresh on load** - Fetches latest data when screen opens
2. **Manual refresh** - Refresh button in top-right corner
3. **Interactive map** - Pinch to zoom, drag to pan
4. **Marker clustering** - Shows all landmarks with markers
5. **Marker info** - Click marker to see title and location
6. **Success feedback** - Snackbar confirms data loaded
7. **Error handling** - Shows error message with retry option
8. **Centered view** - Always starts centered on Bangladesh

---

## 📊 State Management

| State | Variable | Type | Purpose |
|-------|----------|------|---------|
| UI State | `uiState` | `LandmarkUiState` | Loading/Success/Error |
| Context | `context` | `Context` | OSM configuration |
| Snackbar | `snackbarHostState` | `SnackbarHostState` | Success messages |

**State Transitions:**
```
Initial → Loading → (API Call) → Success/Error
                                      ↓
                                  User clicks Retry
                                      ↓
                                   Loading...
```

---

## 🐛 Common Issues & Solutions

### Issue: Map doesn't show

**Possible causes:**
- OSMDroid not configured
- Internet permission missing
- Network connection unavailable

**Solution:**
- Check `AndroidManifest.xml` for INTERNET permission
- Verify OSMDroid configuration in `LaunchedEffect`

### Issue: Markers not appearing

**Possible causes:**
- Landmarks have invalid coordinates (0,0 or null)
- Zoom level too high/low
- Map not invalidated after adding markers

**Debug:**
Check Logcat:
```
Filter: "OverviewScreen"
Look for: "Added marker for [Title] at ..."
```

### Issue: Map doesn't update after delete

**Possible causes:**
- `update` lambda not triggered
- Landmarks list not changing reference

**Solution:**
- Verify `landmarks` is from immutable list
- Check ViewModel creates new list on updates

---

---

# 2️⃣ RecordsScreen - List View

**File**: `RecordsScreen.kt`  
**Purpose**: Displays all landmarks in a scrollable list with swipe-to-delete/edit functionality

## 🎯 What This Screen Does

- Shows all landmarks as cards in a scrollable list
- Displays landmark image, title, and location
- Swipe right → Edit landmark
- Swipe left → Delete landmark (with confirmation)
- Real-time data refresh after operations
- Shows success/error feedback

---

## 📋 Code Breakdown

### 1. Component Declaration (Lines 22-25)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    viewModel: LandmarkViewModel = viewModel()
)
```

**What it does:**
- Composable function for the records/list screen
- Shares ViewModel instance across app
- Manages landmark list state

---

### 2. State Management (Lines 26-29)

```kotlin
val uiState by viewModel.uiState.collectAsState()
val crudOperationState by viewModel.crudOperationState.collectAsState()
val snackbarHostState = remember { SnackbarHostState() }
```

**State Variables:**

| Variable | Type | Purpose |
|----------|------|---------|
| `uiState` | `LandmarkUiState` | Main data state (Loading/Success/Error) |
| `crudOperationState` | `CrudOperationState` | Operation state (Create/Update/Delete) |
| `snackbarHostState` | `SnackbarHostState` | Success/error messages |

**Why two state variables:**
- `uiState`: For fetching/displaying data
- `crudOperationState`: For create/update/delete operations
- Separation allows independent tracking

---

### 3. Auto-Refresh on Load (Lines 31-35)

```kotlin
LaunchedEffect(Unit) {
    Log.d("RecordsScreen", "🔄 Screen loaded, refreshing landmark data...")
    viewModel.loadLandmarks()
}
```

**What it does:**
- Runs once when screen opens
- Fetches fresh landmark data from API
- Ensures list is always up-to-date

---

### 4. Operation Feedback Handler (Lines 37-57)

```kotlin
LaunchedEffect(crudOperationState) {
    when (val state = crudOperationState) {
        is CrudOperationState.Success -> {
            Log.d("RecordsScreen", "✅ Operation successful: ${state.message}")
            snackbarHostState.showSnackbar(
                message = state.message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearCrudOperationState()
        }
        is CrudOperationState.Error -> {
            Log.e("RecordsScreen", "❌ Operation failed: ${state.message}")
            snackbarHostState.showSnackbar(
                message = "Error: ${state.message}",
                duration = SnackbarDuration.Long
            )
            viewModel.clearCrudOperationState()
        }
        else -> { /* Idle or Loading - no action needed */ }
    }
}
```

**What it does:**
- Watches `crudOperationState` for changes
- When operation succeeds → Shows success snackbar
- When operation fails → Shows error snackbar
- Clears state after showing feedback

**Example flow:**
```
User deletes landmark → crudOperationState = Success →
LaunchedEffect detects → Shows "Deleted successfully" →
Clears state → Ready for next operation
```

---

### 5. Layout Structure (Lines 59-69)

```kotlin
Scaffold(
    topBar = {
        AppTopBar(
            title = "Landmarks of Bangladesh"
        )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
) { paddingValues ->
    when (val currentState = uiState) {
```

**Components:**
- `Scaffold`: Material Design container
- `topBar`: Shows screen title
- `snackbarHost`: Shows success/error messages
- `when`: Switches between Loading/Success/Error states

---

### 6. State-Based UI

#### A. Loading State (Lines 71-88)

```kotlin
is LandmarkUiState.Loading -> {
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading landmarks from API...",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "https://labs.anontech.info/cse489/t3/api.php",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

**What it shows:**
- Spinning circle
- "Loading landmarks from API..."
- API URL for transparency

---

#### B. Success State - List View (Lines 90-133)

```kotlin
is LandmarkUiState.Success -> {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Summary card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "📍 Found ${currentState.landmarks.size} landmarks",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Landmark cards
        items(currentState.landmarks) { landmark ->
            LandmarkCard(
                landmark = landmark,
                onClick = {
                    // TODO: Navigate to landmark details
                },
                onEdit = {
                    // TODO: Navigate to FormScreen with landmark data
                    Log.d("RecordsScreen", "Edit landmark: ${landmark.title}")
                },
                onDelete = {
                    Log.d("RecordsScreen", "Delete landmark: ${landmark.title}")
                    viewModel.deleteLandmark(landmark.id)
                }
            )
        }
    }
}
```

**Component Breakdown:**

##### LazyColumn (Lines 91-94)
```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize().padding(paddingValues),
    contentPadding = PaddingValues(vertical = 8.dp)
)
```
**What it does:**
- Like RecyclerView in traditional Android
- Only renders visible items (efficient for long lists)
- Scrolls vertically
- 8dp padding top and bottom

##### Summary Card (Lines 96-110)
```kotlin
item {
    Card(...) {
        Text(text = "📍 Found ${currentState.landmarks.size} landmarks", ...)
    }
}
```
**What it does:**
- Shows total count at top of list
- Blue background (primaryContainer)
- Example: "📍 Found 12 landmarks"

##### Landmark Cards (Lines 112-130)
```kotlin
items(currentState.landmarks) { landmark ->
    LandmarkCard(
        landmark = landmark,
        onClick = { /* View details */ },
        onEdit = { /* Navigate to edit */ },
        onDelete = {
            viewModel.deleteLandmark(landmark.id)
        }
    )
}
```

**What each parameter does:**

| Parameter | Action | What Happens |
|-----------|--------|--------------|
| `landmark` | Data | Passes landmark info to card |
| `onClick` | Tap card | TODO: Navigate to details |
| `onEdit` | Swipe right | TODO: Navigate to edit form |
| `onDelete` | Swipe left + confirm | Deletes via API |

**Delete Flow:**
```
User swipes card left → Confirmation dialog →
User taps "Delete" → onDelete() called →
viewModel.deleteLandmark(id) →
API DELETE request →
Success → crudOperationState = Success →
LaunchedEffect shows snackbar →
viewModel.loadLandmarks() refreshes list →
Card disappears from UI
```

---

#### C. Error State (Lines 135-160)

```kotlin
is LandmarkUiState.Error -> {
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error loading landmarks",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.loadLandmarks() }) {
                Text("Retry")
            }
        }
    }
}
```

**What it shows:**
- Red "Error loading landmarks" title
- Specific error message
- Retry button

---

## 🔄 Data Flow

```
Screen Opens
    ↓
LaunchedEffect(Unit) → viewModel.loadLandmarks()
    ↓
uiState = Loading → Shows spinner
    ↓
API GET Request
    ↓
┌──────────────────┬───────────────────┐
│  Success         │  Error            │
└──────────────────┴───────────────────┘
       ↓                   ↓
uiState = Success    uiState = Error
       ↓                   ↓
LazyColumn with      Error screen +
landmark cards       Retry button

User swipes card left → Delete confirmation →
Confirm → viewModel.deleteLandmark(id) →
API DELETE → crudOperationState = Success →
LaunchedEffect → Snackbar "Deleted successfully" →
viewModel.loadLandmarks() → Fresh data → List updates
```

---

## 🎨 Visual Layout

```
┌─────────────────────────────────────────┐
│ Landmarks of Bangladesh                 │ ← TopAppBar
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ 📍 Found 12 landmarks               │ │ ← Summary card
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ [Image]                             │ │
│ │ National Parliament House           │ │ ← Landmark card 1
│ │ 📍 Lat: 23.76, Lon: 90.37           │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ [Image]                             │ │
│ │ Ahsan Manzil                        │ │ ← Landmark card 2
│ │ 📍 Lat: 23.70, Lon: 90.40           │ │
│ └─────────────────────────────────────┘ │
│                ...                      │
├─────────────────────────────────────────┤
│ Landmark deleted successfully ✓         │ ← Snackbar
└─────────────────────────────────────────┘
```

---

## 🎯 Key Features

1. **Scrollable list** - LazyColumn for efficient rendering
2. **Real images** - Loads from API with Coil
3. **Swipe gestures** - Right for edit, left for delete
4. **Delete confirmation** - Prevents accidental deletions
5. **Auto-refresh** - Updates after any operation
6. **Feedback** - Snackbars for success/error
7. **Count display** - Shows total landmarks
8. **Error handling** - Retry button on failures

---

---

# 3️⃣ FormScreen - Add/Edit Landmark

**File**: `FormScreen.kt`  
**Purpose**: Form for adding new landmarks or editing existing ones

## 🎯 What This Screen Does

- Add new landmarks to database
- Edit existing landmark information
- Auto-detect GPS coordinates (for new landmarks)
- Take photo with camera or select from gallery
- Resize images to 800×600 before upload
- Validate input fields
- Upload to REST API
- Navigate to list after success

---

## 📋 Code Breakdown

### 1. Component Declaration (Lines 45-50)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    landmark: Landmark? = null,
    onNavigateBack: () -> Unit,
    onSuccessNavigation: (() -> Unit)? = null,
    viewModel: LandmarkViewModel = viewModel()
)
```

**Parameters:**

| Parameter | Type | Purpose |
|-----------|------|---------|
| `landmark` | `Landmark?` | null = Add mode, non-null = Edit mode |
| `onNavigateBack` | `() -> Unit` | Navigate back (cancel or after edit) |
| `onSuccessNavigation` | `(() -> Unit)?` | Navigate to list (after add) |
| `viewModel` | `LandmarkViewModel` | Shared ViewModel |

---

### 2. State Management (Lines 51-59)

```kotlin
var title by remember { mutableStateOf(landmark?.title ?: "") }
var latitude by remember { mutableStateOf(landmark?.latitude?.toString() ?: "") }
var longitude by remember { mutableStateOf(landmark?.longitude?.toString() ?: "") }
var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
var resizedImageUri by remember { mutableStateOf<Uri?>(null) }
var isLocationLoading by remember { mutableStateOf(false) }
var locationError by remember { mutableStateOf<String?>(null) }
var isImageProcessing by remember { mutableStateOf(false) }

val context = LocalContext.current
val crudOperationState by viewModel.crudOperationState.collectAsState()
val coroutineScope = rememberCoroutineScope()
val snackbarHostState = remember { SnackbarHostState() }
```

**Form State:**

| Variable | Type | Purpose |
|----------|------|---------|
| `title` | String | Landmark name input |
| `latitude` | String | GPS latitude input |
| `longitude` | String | GPS longitude input |
| `selectedImageUri` | Uri? | Original image from camera/gallery |
| `resizedImageUri` | Uri? | Processed 800×600 image for upload |
| `isLocationLoading` | Boolean | GPS detection in progress |
| `locationError` | String? | GPS error message |
| `isImageProcessing` | Boolean | Image resize in progress |

**Why two image URIs:**
- `selectedImageUri`: Original (may be large, e.g., 4000×3000)
- `resizedImageUri`: Resized to 800×600 (reduces upload size/time)

---

### 3. Permission Launchers (Lines 68-85)

```kotlin
val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = { permissions ->
        val allPermissionsGranted = permissions.values.all { it }
        if (allPermissionsGranted && landmark == null) {
            coroutineScope.launch {
                detectCurrentLocation(context) { location, error ->
                    location?.let {
                        latitude = it.latitude.toString()
                        longitude = it.longitude.toString()
                        locationError = null
                    }
                    error?.let {
                        locationError = it
                    }
                    isLocationLoading = false
                }
            }
        }
    }
)
```

**What it does:**
- Requests location permissions (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
- Only for **new landmarks** (`landmark == null`)
- If granted → Auto-detects GPS coordinates
- Fills latitude/longitude fields automatically
- Shows error if detection fails

**Flow:**
```
New landmark mode → Request permissions →
User grants → Detect GPS → Fill coordinates →
User can submit or edit manually
```

---

### 4. Image Launchers (Lines 89-127)

#### Gallery Launcher (Lines 89-104)

```kotlin
val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri: Uri? ->
        Log.d("FormScreen", "📷 Gallery result: URI = $uri")
        uri?.let {
            Log.d("FormScreen", "✅ Image selected from gallery: $it")
            selectedImageUri = it
            processImage(context, it) { processedUri ->
                resizedImageUri = processedUri
                isImageProcessing = false
                Log.d("FormScreen", "✅ Gallery image processing completed")
            }
        } ?: run {
            Log.w("FormScreen", "⚠️ No image selected from gallery")
            isImageProcessing = false
        }
    }
)
```

**What it does:**
1. Opens device gallery
2. User selects image
3. `selectedImageUri` = original image
4. Calls `processImage()` to resize to 800×600
5. `resizedImageUri` = processed image ready for upload

#### Camera Launcher (Lines 108-125)

```kotlin
var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }

val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
    onResult = { success ->
        Log.d("FormScreen", "📸 Camera result: success = $success")
        if (success && currentPhotoUri != null) {
            Log.d("FormScreen", "✅ Photo captured successfully: ${currentPhotoUri}")
            selectedImageUri = currentPhotoUri
            processImage(context, currentPhotoUri!!) { processedUri ->
                resizedImageUri = processedUri
                isImageProcessing = false
                Log.d("FormScreen", "✅ Image processing completed")
            }
        } else {
            Log.w("FormScreen", "⚠️ Camera capture failed or cancelled")
            isImageProcessing = false
        }
    }
)
```

**What it does:**
1. Opens camera app
2. User takes photo
3. Saves to `currentPhotoUri`
4. If success → Process image
5. Resize to 800×600
6. Ready for upload

---

### 5. Auto-Location Detection (Lines 128-134)

```kotlin
LaunchedEffect(landmark) {
    if (landmark == null) { // Only for new landmarks
        isLocationLoading = true
        locationPermissionLauncher.launch(LocationUtils.getRequiredPermissions())
    }
}
```

**What it does:**
- Runs when screen opens
- Only for **Add mode** (`landmark == null`)
- Requests location permissions
- Auto-detects current GPS coordinates

**Why only for new landmarks:**
- Editing existing landmark keeps original coordinates
- User can manually change if needed

---

### 6. Success Handler (Lines 136-171)

```kotlin
LaunchedEffect(crudOperationState) {
    when (val currentState = crudOperationState) {
        is CrudOperationState.Success -> {
            Log.d("FormScreen", "✅ Landmark operation successful!")

            // Show success snackbar
            val message = if (landmark == null) {
                "✅ Landmark created successfully and marked on map!"
            } else {
                "✅ Landmark updated successfully!"
            }

            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )

            // Wait a bit for snackbar to show, then navigate
            kotlinx.coroutines.delay(1000)

            // Clear the operation state
            viewModel.clearCrudOperationState()

            // Navigate to records screen if callback provided (for new landmarks)
            if (landmark == null && onSuccessNavigation != null) {
                Log.d("FormScreen", "Navigating to records screen...")
                onSuccessNavigation()
            } else {
                // Navigate back for edit operations
                onNavigateBack()
            }
        }
        else -> { /* Handle other states if needed */ }
    }
}
```

**What it does:**

1. **Watches** `crudOperationState` for Success
2. **Shows snackbar** with appropriate message
   - "Landmark created..." (new)
   - "Landmark updated..." (edit)
3. **Waits 1 second** (so user sees snackbar)
4. **Clears state** (ready for next operation)
5. **Navigates**:
   - New landmark → List screen (to see it in list)
   - Edit landmark → Back (to detail/list)

---

### 7. Layout Structure (Lines 173-192)

```kotlin
Scaffold(
    topBar = {
        AppTopBar(
            title = if (landmark == null) "Add New Landmark" else "Edit Landmark",
            showBackButton = true,
            onBackClick = onNavigateBack
        )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = Modifier.fillMaxSize()
) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
```

**Components:**

| Component | Purpose |
|-----------|---------|
| `Scaffold` | Material Design container |
| `topBar` | Shows "Add New" or "Edit" with back button |
| `snackbarHost` | Success/error messages |
| `Column` | Scrollable form container |
| `verticalScroll` | Allows scrolling if content too tall |
| `spacedBy(16.dp)` | 16dp gap between form fields |

---

### 8. Form Fields

#### Title Field (Lines 194-200)

```kotlin
OutlinedTextField(
    value = title,
    onValueChange = { title = it },
    label = { Text("Landmark Title") },
    modifier = Modifier.fillMaxWidth(),
    enabled = crudOperationState !is CrudOperationState.Loading
)
```

**What it does:**
- Text input for landmark name
- `value = title`: Shows current value
- `onValueChange`: Updates when user types
- `enabled`: Disabled during API request (prevents changes)

---

#### GPS Coordinates Card (Lines 202-290)

```kotlin
Card(modifier = Modifier.fillMaxWidth(), ...) {
    Column(modifier = Modifier.padding(16.dp), ...) {
        // Header
        Row(...) {
            Icon(Icons.Default.LocationOn, ...)
            Text("GPS Coordinates", ...)
        }

        // Auto-detect button
        if (landmark == null) {
            Button(
                onClick = {
                    isLocationLoading = true
                    locationPermissionLauncher.launch(...)
                },
                enabled = !isLocationLoading && crudOperationState !is Loading
            ) {
                if (isLocationLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detecting...")
                } else {
                    Icon(Icons.Default.MyLocation, ...)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-detect My Location")
                }
            }
        }

        // Location error message
        locationError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Latitude field
        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            enabled = crudOperationState !is Loading
        )

        // Longitude field
        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            enabled = crudOperationState !is Loading
        )
    }
}
```

**Features:**

1. **Auto-detect button** (only for new landmarks)
   - Shows "Detecting..." with spinner during GPS detection
   - Automatically fills latitude/longitude
   
2. **Error display** (if GPS fails)
   - Red text with error message
   - e.g., "Unable to detect GPS coordinates"

3. **Manual input fields**
   - User can enter coordinates manually
   - Decimal keyboard for easier input
   - Editable even after auto-detection

---

#### Image Selection Card (Lines 292-400)

```kotlin
Card(modifier = Modifier.fillMaxWidth(), ...) {
    Column(modifier = Modifier.padding(16.dp), ...) {
        // Header
        Text("Landmark Photo", ...)

        // Camera button
        Button(
            onClick = {
                isImageProcessing = true
                val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                currentPhotoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                cameraLauncher.launch(currentPhotoUri)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isImageProcessing && crudOperationState !is Loading
        ) {
            Icon(Icons.Default.CameraAlt, ...)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Take Photo")
        }

        // Gallery button
        Button(
            onClick = {
                isImageProcessing = true
                galleryLauncher.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isImageProcessing && crudOperationState !is Loading
        ) {
            Icon(Icons.Default.PhotoCamera, ...)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose from Gallery")
        }

        // Image preview
        if (isImageProcessing) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), ...) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Processing image...")
            }
        } else {
            val imageUri = resizedImageUri ?: selectedImageUri ?: landmark?.image
            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
```

**Features:**

1. **Take Photo** button
   - Opens camera app
   - Saves to temporary file
   - Processes and resizes

2. **Choose from Gallery** button
   - Opens photo picker
   - User selects image
   - Processes and resizes

3. **Processing indicator**
   - Shows spinner with "Processing image..."
   - Displays while resizing to 800×600

4. **Image preview**
   - Shows selected/processed image
   - 200dp height with rounded corners
   - Crop scaling to fit

---

#### Submit Button (Lines 402-450)

```kotlin
Button(
    onClick = {
        Log.d("FormScreen", "Submit button clicked")
        
        // Validate inputs
        if (title.isBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please enter a title")
            }
            return@Button
        }
        
        val lat = latitude.toDoubleOrNull()
        val lon = longitude.toDoubleOrNull()
        
        if (lat == null || lon == null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please enter valid coordinates")
            }
            return@Button
        }

        // Create or update
        if (landmark == null) {
            Log.d("FormScreen", "Creating new landmark: $title at ($lat, $lon)")
            viewModel.createLandmark(
                title = title,
                latitude = lat,
                longitude = lon,
                imageUri = resizedImageUri,
                context = context
            )
        } else {
            Log.d("FormScreen", "Updating landmark ${landmark.id}: $title")
            viewModel.updateLandmark(
                id = landmark.id,
                title = title,
                latitude = lat,
                longitude = lon,
                imageUri = resizedImageUri,
                context = context
            )
        }
    },
    modifier = Modifier.fillMaxWidth(),
    enabled = crudOperationState !is CrudOperationState.Loading
) {
    if (crudOperationState is CrudOperationState.Loading) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (landmark == null) "Creating..." else "Updating...")
    } else {
        Icon(Icons.Default.Add, ...)
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (landmark == null) "Add Landmark" else "Update Landmark")
    }
}
```

**What it does:**

1. **Validates inputs**
   - Title not blank
   - Coordinates are valid numbers
   - Shows snackbar if invalid

2. **Determines mode**
   - `landmark == null` → Create new
   - `landmark != null` → Update existing

3. **Calls ViewModel**
   - `createLandmark()` or `updateLandmark()`
   - Passes title, coordinates, image URI

4. **Loading state**
   - Shows spinner + "Creating..." or "Updating..."
   - Disabled during operation

---

### 9. Helper Functions (Lines 500-538)

#### detectCurrentLocation()

```kotlin
private suspend fun detectCurrentLocation(
    context: Context,
    callback: (LocationCoordinates?, String?) -> Unit
) {
    try {
        val location = LocationUtils.getCurrentLocation(context)
        if (location != null) {
            callback(location, null)
        } else {
            callback(null, "Unable to detect GPS coordinates. Please enter manually.")
        }
    } catch (e: Exception) {
        callback(null, "Error: ${e.message}")
    }
}
```

**What it does:**
- Gets current GPS location
- Returns coordinates or error message
- Uses Android Location APIs

#### processImage()

```kotlin
private fun processImage(
    context: Context,
    imageUri: Uri,
    onComplete: (Uri) -> Unit
) {
    try {
        val resized = ImageUtils.resizeImage(context, imageUri, 800, 600)
        onComplete(resized)
    } catch (e: Exception) {
        Log.e("FormScreen", "Image processing failed: ${e.message}")
        onComplete(imageUri) // Use original if resize fails
    }
}
```

**What it does:**
- Takes original image URI
- Resizes to 800×600 pixels
- Returns new URI
- Falls back to original if fails

**Why resize:**
- Reduces file size (faster uploads)
- Saves bandwidth
- Consistent image dimensions
- Better performance

---

## 🔄 Complete Flow Diagrams

### Add New Landmark Flow

```
Screen Opens (landmark = null)
    ↓
Request location permissions
    ↓
User grants permissions
    ↓
Auto-detect GPS coordinates
    ↓
Fill latitude/longitude fields
    ↓
User enters title
    ↓
User taps "Take Photo" or "Choose from Gallery"
    ↓
Image selected → Resize to 800×600
    ↓
Preview shows processed image
    ↓
User taps "Add Landmark"
    ↓
Validate inputs (title, coordinates)
    ↓
viewModel.createLandmark(title, lat, lon, imageUri)
    ↓
API POST request with multipart form data
    ↓
Success → crudOperationState = Success
    ↓
LaunchedEffect detects Success
    ↓
Show snackbar: "Landmark created successfully"
    ↓
Wait 1 second
    ↓
Navigate to RecordsScreen
    ↓
See new landmark in list + on map
```

### Edit Existing Landmark Flow

```
Screen Opens (landmark = Landmark object)
    ↓
Pre-fill form with existing data:
  - title = landmark.title
  - latitude = landmark.latitude
  - longitude = landmark.longitude
  - image preview = landmark.image
    ↓
User edits title/coordinates
    ↓
User optionally changes image
    ↓
User taps "Update Landmark"
    ↓
Validate inputs
    ↓
viewModel.updateLandmark(id, title, lat, lon, imageUri)
    ↓
API PUT request
    ↓
Success → crudOperationState = Success
    ↓
Show snackbar: "Landmark updated successfully"
    ↓
Navigate back
```

---

## 🎨 Visual Layout

```
┌─────────────────────────────────────────┐
│ ← Add New Landmark                      │ ← TopAppBar
├─────────────────────────────────────────┤
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ Landmark Title                      │ │ ← Title field
│ │ ________________________            │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 📍 GPS Coordinates                  │ │
│ │                                     │ │
│ │ [🎯 Auto-detect My Location]       │ │ ← Auto-detect
│ │                                     │ │
│ │ Latitude                            │ │
│ │ ________________________            │ │
│ │                                     │ │
│ │ Longitude                           │ │
│ │ ________________________            │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ Landmark Photo                      │ │
│ │                                     │ │
│ │ [📷 Take Photo]                     │ │
│ │ [🖼️ Choose from Gallery]            │ │
│ │                                     │ │
│ │ ┌─────────────────────────────────┐ │ │
│ │ │                                 │ │ │
│ │ │   [Image Preview]               │ │ │ ← Image preview
│ │ │                                 │ │ │
│ │ └─────────────────────────────────┘ │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ [➕ Add Landmark]                       │ ← Submit button
│                                         │
└─────────────────────────────────────────┘
```

---

## 🎯 Key Features

1. **Mode detection** - Add vs Edit based on parameter
2. **Auto GPS** - Detects current location automatically
3. **Camera integration** - Take photos directly
4. **Gallery picker** - Choose existing photos
5. **Image resize** - 800×600 for optimal upload
6. **Validation** - Checks required fields
7. **Loading states** - Shows progress during operations
8. **Success feedback** - Snackbar confirmation
9. **Smart navigation** - Goes to list (add) or back (edit)
10. **Error handling** - Shows errors with retry options

---

## 📊 State Transitions

```
Initial State
    ↓
User fills form → Idle
    ↓
User taps Submit → Loading
    ↓
API Request → Loading (button disabled, showing spinner)
    ↓
┌─────────────────┬──────────────────┐
│  Success        │  Error           │
└─────────────────┴──────────────────┘
       ↓                   ↓
Show snackbar        Show error
Navigate away        Stay on form
```

---

---

# 🏗️ Architecture Overview

## MVVM Pattern

```
┌─────────────────────────────────────────┐
│           UI Layer (Screens)            │
│  - OverviewScreen.kt                    │
│  - RecordsScreen.kt                     │
│  - FormScreen.kt                        │
│  └─ Composable functions                │
│     Display data, Handle user input     │
└──────────────────┬──────────────────────┘
                   ↓ Observes State
┌─────────────────────────────────────────┐
│      ViewModel Layer (Business Logic)   │
│  - LandmarkViewModel.kt                 │
│  └─ Manages UI state                    │
│     Handles user actions                │
│     Coordinates operations              │
└──────────────────┬──────────────────────┘
                   ↓ Calls Methods
┌─────────────────────────────────────────┐
│   Repository Layer (Data Management)    │
│  - LandmarkRepository.kt                │
│  └─ CRUD operations                     │
│     Data transformation                 │
│     Error handling                      │
└──────────────────┬──────────────────────┘
                   ↓ Makes API Calls
┌─────────────────────────────────────────┐
│      API Layer (Network Interface)      │
│  - ApiService.kt (Retrofit interface)   │
│  └─ HTTP requests                       │
│     JSON serialization                  │
└──────────────────┬──────────────────────┘
                   ↓ HTTP
┌─────────────────────────────────────────┐
│         REST API (Backend)              │
│  https://labs.anontech.info/.../api.php│
│  └─ Database operations                 │
│     Image storage                       │
│     Response generation                 │
└─────────────────────────────────────────┘
```

---

## 🔄 Complete App Flow

```
App Launch
    ↓
MainActivity
    ↓
Navigation (3 bottom tabs)
    ↓
┌──────────────┬──────────────┬──────────────┐
│  Overview    │  Records     │  Form        │
│  (Map)       │  (List)      │  (Add/Edit)  │
└──────────────┴──────────────┴──────────────┘
       ↓               ↓              ↓
   MapView       LazyColumn      Scrollable
   Markers       Cards           Form Fields
       ↓               ↓              ↓
   Click          Swipe          Submit
   Marker         Card           Button
       ↓               ↓              ↓
   Show Info      Edit/Delete     API Call
                      ↓              ↓
                  Confirm        Success
                      ↓              ↓
                  API Call       Navigate
                      ↓              ↓
                  Success        Refresh
                      ↓
                  Refresh
```

---

## 📁 File Structure

```
app/src/main/java/com/example/landmarkbangladesh/
├── data/
│   ├── api/
│   │   └── ApiService.kt              (Retrofit interface)
│   ├── model/
│   │   ├── Landmark.kt                (Data class)
│   │   └── ApiResponse.kt             (API response models)
│   └── repository/
│       └── LandmarkRepository.kt      (Data operations)
│
├── ui/
│   ├── components/
│   │   ├── AppTopBar.kt               (Reusable top bar)
│   │   └── LandmarkCard.kt            (Swipeable card)
│   ├── screens/
│   │   ├── OverviewScreen.kt          (Map view)
│   │   ├── RecordsScreen.kt           (List view)
│   │   └── FormScreen.kt              (Add/Edit form)
│   └── viewmodel/
│       └── LandmarkViewModel.kt       (Business logic)
│
├── utils/
│   ├── ImageUtils.kt                  (Image processing)
│   └── LocationUtils.kt               (GPS detection)
│
└── MainActivity.kt                    (App entry point)
```

---

## 🎨 UI Components

### Reusable Components

#### AppTopBar (ui/components/AppTopBar.kt)
```kotlin
@Composable
fun AppTopBar(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable () -> Unit = {}
)
```
**Used in:** All 3 screens  
**Purpose:** Consistent top bar across app

#### LandmarkCard (ui/components/LandmarkCard.kt)
```kotlin
@Composable
fun LandmarkCard(
    landmark: Landmark,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
)
```
**Used in:** RecordsScreen  
**Purpose:** Display landmark with swipe gestures

---

## 🔐 Permissions

Required permissions in `AndroidManifest.xml`:

```xml
<!-- Internet for API calls -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Location for GPS detection -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Camera for taking photos -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Storage for reading/writing images -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- OSMDroid map cache -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

## 📦 Dependencies

Key libraries used:

```kotlin
// Jetpack Compose - Modern UI
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Navigation
implementation("androidx.navigation:navigation-compose")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")

// Networking
implementation("com.squareup.retrofit2:retrofit")
implementation("com.squareup.retrofit2:converter-gson")

// Image Loading
implementation("io.coil-kt:coil-compose")

// OpenStreetMap
implementation("org.osmdroid:osmdroid-android")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
```

---

## 🎯 Best Practices Used

1. **MVVM Architecture** - Clean separation of concerns
2. **State Management** - Reactive UI with Flow/StateFlow
3. **Reusable Components** - AppTopBar, LandmarkCard
4. **Error Handling** - Proper try-catch with user feedback
5. **Loading States** - Visual feedback during operations
6. **Validation** - Input validation before submission
7. **Image Optimization** - Resize to reduce upload size
8. **Permissions** - Runtime permission requests
9. **Logging** - Comprehensive logging for debugging
10. **Material Design 3** - Modern, consistent UI

---

## 🐛 Common Issues & Solutions

### Issue: Images not loading

**Solution:**
- Check internet permission
- Verify Coil dependency
- Check image URL format (should be full URL)

### Issue: GPS not detecting

**Solution:**
- Enable location services on device
- Grant location permissions
- Ensure GPS signal (may not work indoors)

### Issue: Map not showing

**Solution:**
- Check internet permission
- Verify OSMDroid configuration
- Clear app cache if corrupted

### Issue: Delete not working

**Solution:**
- Check API endpoint (should use query parameter)
- Verify delete confirmation dialog appears
- Check network connectivity

---

## 📚 Additional Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Coil Image Loading](https://coil-kt.github.io/coil/)
- [OSMDroid Wiki](https://github.com/osmdroid/osmdroid/wiki)
- [Material Design 3](https://m3.material.io/)

---

**App Version**: 1.0  
**Last Updated**: December 9, 2024  
**Platform**: Android (Jetpack Compose)  
**Minimum SDK**: 24 (Android 7.0)  
**Target SDK**: 34 (Android 14)

