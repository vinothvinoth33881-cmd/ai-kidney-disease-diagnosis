package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AnalysisHistoryEntity
import com.example.data.local.KidneyDatabase
import com.example.data.model.ModelPredictionResult
import com.example.data.model.PipelineStage
import com.example.data.model.PipelineStepState
import com.example.data.model.SampleScan
import com.example.data.model.StepStatus
import com.example.data.repository.DiagnosisRepository
import com.example.domain.PipelineRunner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiagnosisUiState(
    val selectedImageUri: Uri? = null,
    val selectedSampleScan: SampleScan? = null,
    val isAnalyzing: Boolean = false,
    val pipelineSteps: List<PipelineStepState> = PipelineStage.entries.map {
        PipelineStepState(it, StepStatus.PENDING)
    },
    val currentRunningStage: PipelineStage? = null,
    val activeResult: ModelPredictionResult? = null,
    val errorMessage: String? = null,
    val isServerConnected: Boolean = false,
    val isCheckingServer: Boolean = false,
    val serverCheckMessage: String? = null,
    val apiUrl: String = "http://10.0.2.2:8000/",
    val isDemoMode: Boolean = true,
    val showPipelineDialog: Boolean = false,
    val showApiSettingsDialog: Boolean = false
)

class DiagnosisViewModel(application: Application) : AndroidViewModel(application) {

    private val database = KidneyDatabase.getDatabase(application)
    private val repository = DiagnosisRepository(database.analysisDao())
    private val pipelineRunner = PipelineRunner(application, repository)

    private val _uiState = MutableStateFlow(DiagnosisUiState())
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<AnalysisHistoryEntity>> = repository.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectSampleScan(sampleScan: SampleScan) {
        _uiState.update {
            it.copy(
                selectedSampleScan = sampleScan,
                selectedImageUri = null,
                errorMessage = null,
                activeResult = null
            )
        }
    }

    fun selectImageUri(uri: Uri) {
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                selectedSampleScan = null,
                errorMessage = null,
                activeResult = null
            )
        }
    }

    fun clearSelectedImage() {
        _uiState.update {
            it.copy(
                selectedImageUri = null,
                selectedSampleScan = null,
                errorMessage = null,
                activeResult = null
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setApiUrl(url: String) {
        repository.setApiUrl(url)
        _uiState.update { it.copy(apiUrl = url) }
    }

    fun setDemoMode(isDemo: Boolean) {
        repository.isDemoMode = isDemo
        _uiState.update { it.copy(isDemoMode = isDemo) }
    }

    fun openApiSettings(open: Boolean) {
        _uiState.update { it.copy(showApiSettingsDialog = open, serverCheckMessage = null) }
    }

    fun testServerConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingServer = true, serverCheckMessage = "Pinging endpoint ${_uiState.value.apiUrl}api/health...") }
            val result = repository.checkServerHealth()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isCheckingServer = false,
                        isServerConnected = true,
                        serverCheckMessage = "Connected successfully to ML backend API service."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isCheckingServer = false,
                        isServerConnected = false,
                        serverCheckMessage = "Connection failed: ${result.exceptionOrNull()?.localizedMessage ?: "Endpoint unreachable"}"
                    )
                }
            }
        }
    }

    fun startAnalysis(onSuccess: () -> Unit = {}) {
        val currentUri = _uiState.value.selectedImageUri
        val currentSample = _uiState.value.selectedSampleScan

        if (currentUri == null && currentSample == null) {
            _uiState.update { it.copy(errorMessage = "Please upload an image or select a sample CT scan before running analysis.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    showPipelineDialog = true,
                    errorMessage = null,
                    pipelineSteps = PipelineStage.entries.map { stage -> PipelineStepState(stage, StepStatus.PENDING) }
                )
            }

            try {
                val result = pipelineRunner.executePipeline(
                    imageUri = currentUri,
                    sampleScan = currentSample
                ) { stepUpdate ->
                    _uiState.update { state ->
                        val updatedList = state.pipelineSteps.map { existing ->
                            if (existing.stage == stepUpdate.stage) stepUpdate else existing
                        }
                        state.copy(
                            pipelineSteps = updatedList,
                            currentRunningStage = if (stepUpdate.status == StepStatus.RUNNING) stepUpdate.stage else state.currentRunningStage
                        )
                    }
                }

                // Save to Room history
                repository.saveAnalysis(
                    AnalysisHistoryEntity(
                        timestamp = result.timestamp,
                        imageFileName = result.imageFileName,
                        imageUri = result.imageUri,
                        sampleResId = result.sampleResId,
                        predictedClass = result.predictedClass.label,
                        svmPrediction = result.svmPrediction.label,
                        decisionTreePrediction = result.decisionTreePrediction.label,
                        confidence = result.confidence,
                        isDemoMode = result.isDemoMode,
                        notes = if (result.isDemoMode) "Demo Research Prototype Analysis" else "Trained Model Analysis",
                        meanIntensity = result.extractedFeatures.meanIntensity,
                        contrast = result.extractedFeatures.contrast,
                        entropy = result.extractedFeatures.entropy
                    )
                )

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        showPipelineDialog = false,
                        activeResult = result
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        showPipelineDialog = false,
                        errorMessage = e.localizedMessage ?: "An error occurred during image processing."
                    )
                }
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteAnalysis(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
