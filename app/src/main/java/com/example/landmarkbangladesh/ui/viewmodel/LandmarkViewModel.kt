package com.example.landmarkbangladesh.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.landmarkbangladesh.data.model.ApiResponse
import com.example.landmarkbangladesh.data.model.Landmark
import com.example.landmarkbangladesh.data.repository.LandmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LandmarkUiState {
    object Loading : LandmarkUiState()
    data class Success(val landmarks: List<Landmark>) : LandmarkUiState()
    data class Error(val message: String) : LandmarkUiState()
}

sealed class CrudOperationState {
    object Idle : CrudOperationState()
    object Loading : CrudOperationState()
    data class Success(val message: String) : CrudOperationState()
    data class Error(val message: String) : CrudOperationState()
}

class LandmarkViewModel : ViewModel() {
    private val repository = LandmarkRepository()

    private val _uiState = MutableStateFlow<LandmarkUiState>(LandmarkUiState.Loading)
    val uiState: StateFlow<LandmarkUiState> = _uiState.asStateFlow()

    private val _crudOperationState = MutableStateFlow<CrudOperationState>(CrudOperationState.Idle)
    val crudOperationState: StateFlow<CrudOperationState> = _crudOperationState.asStateFlow()

    init {
        Log.d("LandmarkViewModel", "Initializing ViewModel and loading landmarks...")
        loadLandmarks()
    }

    fun loadLandmarks() {
        Log.d("LandmarkViewModel", "Starting to load landmarks from API...")
        _uiState.value = LandmarkUiState.Loading
        viewModelScope.launch {
            repository.getLandmarks()
                .onSuccess { landmarks ->
                    Log.d("LandmarkViewModel", "Successfully loaded ${landmarks.size} landmarks")
                    landmarks.forEach { landmark ->
                        Log.d("LandmarkViewModel", "Landmark: ${landmark.title} at ${landmark.location}")
                    }
                    _uiState.value = LandmarkUiState.Success(landmarks)
                }
                .onFailure { error ->
                    Log.e("LandmarkViewModel", "Failed to load landmarks: ${error.message}")
                    _uiState.value = LandmarkUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun createLandmark(
        title: String,
        latitude: Double,
        longitude: Double,
        imageUri: Uri? = null,
        context: Context? = null
    ) {
        Log.d("LandmarkViewModel", "Creating new landmark: $title")
        _crudOperationState.value = CrudOperationState.Loading

        viewModelScope.launch {
            repository.createLandmark(title, latitude, longitude, imageUri, context)
                .onSuccess { response ->
                    Log.d("LandmarkViewModel", "Successfully created landmark")
                    _crudOperationState.value = CrudOperationState.Success(
                        response.message ?: "Landmark created successfully"
                    )
                    // Reload landmarks to refresh the list
                    loadLandmarks()
                }
                .onFailure { error ->
                    Log.e("LandmarkViewModel", "Failed to create landmark: ${error.message}")
                    _crudOperationState.value = CrudOperationState.Error(
                        error.message ?: "Failed to create landmark"
                    )
                }
        }
    }

    fun updateLandmark(
        id: Int,
        title: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        imageUri: Uri? = null,
        context: Context? = null
    ) {
        Log.d("LandmarkViewModel", "Updating landmark ID: $id")
        _crudOperationState.value = CrudOperationState.Loading

        viewModelScope.launch {
            repository.updateLandmark(id, title, latitude, longitude, imageUri, context)
                .onSuccess { response ->
                    Log.d("LandmarkViewModel", "Successfully updated landmark")
                    _crudOperationState.value = CrudOperationState.Success(
                        response.message ?: "Landmark updated successfully"
                    )
                    // Reload landmarks to refresh the list
                    loadLandmarks()
                }
                .onFailure { error ->
                    Log.e("LandmarkViewModel", "Failed to update landmark: ${error.message}")
                    _crudOperationState.value = CrudOperationState.Error(
                        error.message ?: "Failed to update landmark"
                    )
                }
        }
    }

    fun deleteLandmark(id: Int) {
        Log.d("LandmarkViewModel", "Deleting landmark ID: $id")
        _crudOperationState.value = CrudOperationState.Loading

        viewModelScope.launch {
            repository.deleteLandmark(id)
                .onSuccess { response ->
                    Log.d("LandmarkViewModel", "Successfully deleted landmark")
                    _crudOperationState.value = CrudOperationState.Success(
                        response.message ?: "Landmark deleted successfully"
                    )
                    // Reload landmarks to refresh the list
                    loadLandmarks()
                }
                .onFailure { error ->
                    Log.e("LandmarkViewModel", "Failed to delete landmark: ${error.message}")
                    _crudOperationState.value = CrudOperationState.Error(
                        error.message ?: "Failed to delete landmark"
                    )
                }
        }
    }

    fun clearCrudOperationState() {
        _crudOperationState.value = CrudOperationState.Idle
    }
}
