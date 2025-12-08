# Landmark Bangladesh API Integration

## Overview
This implementation integrates a REST API to display landmarks of Bangladesh in a scrollable RecyclerView with card-style items.

## API Integration
- **API URL**: `https://labs.anontech.info/cse489/t3/api.php`
- **Fallback**: Test data with 8 landmark entries when API is unavailable

## Architecture
The implementation follows a clean, decoupled architecture:

### Data Layer
- **`Landmark.kt`**: Main data model for landmarks
- **`ApiResponse.kt`**: Models for flexible API response parsing
- **`ApiService.kt`**: Retrofit interface for API calls
- **`LandmarkRepository.kt`**: Repository pattern implementation with test data fallback

### UI Layer
- **`RecordsScreen.kt`**: Main screen displaying landmarks in LazyColumn (RecyclerView equivalent)
- **`LandmarkCard.kt`**: Reusable card component for each landmark
- **`LandmarkViewModel.kt`**: ViewModel managing UI state and data loading

## Features Implemented

### RecordsScreen
- ✅ Displays landmarks in scrollable cards (RecyclerView equivalent using LazyColumn)
- ✅ Loading state with progress indicator
- ✅ Error state with retry functionality
- ✅ Success state showing landmark cards

### LandmarkCard Component
- ✅ Card-style design with rounded corners and elevation
- ✅ Title display with proper text styling
- ✅ Location information
- ✅ Category-based color coding for image placeholders
- ✅ Category chips for visual categorization
- ✅ Click handling for future navigation

### Data Features
- ✅ Test data with 8 Bangladesh landmarks including:
  - Sundarbans Mangrove Forest (Natural Heritage)
  - Cox's Bazar Beach (Beach)
  - Lalbagh Fort (Historical)
  - Shat Gombuj Mosque (Religious)
  - Paharpur Buddhist Vihara (Archaeological)
  - Bandarban Hill Tracts (Natural)
  - Ahsan Manzil (Historical)
  - Saint Martin's Island (Island)

## UI States
1. **Loading**: Shows circular progress indicator with loading text
2. **Success**: Displays scrollable list of landmark cards
3. **Error**: Shows error message with retry button

## Reusable Components
- **LandmarkCard**: Fully reusable component in `ui.components` package
- **Category-based styling**: Color-coded placeholders based on landmark category

## Navigation Integration
- Properly integrated with existing navigation system
- RecordsScreen moved to separate file as requested
- Maintains navigation structure with MainScreen

## Dependencies Added
- Retrofit for networking
- Gson for JSON parsing
- Coil for image loading (placeholder implementation ready)
- Coroutines for async operations
- ViewModel and LiveData for state management

## Future Enhancements
- Replace image placeholders with actual AsyncImage when API provides image URLs
- Add detailed landmark view navigation
- Implement search and filtering
- Add offline caching
- Integrate with actual API response format
