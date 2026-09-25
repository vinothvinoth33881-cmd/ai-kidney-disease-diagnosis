package com.example.data.model

data class ExtractedFeatures(
    val meanIntensity: Float = 0f,
    val stdDev: Float = 0f,
    val contrast: Float = 0f,
    val homogeneity: Float = 0f,
    val entropy: Float = 0f,
    val energy: Float = 0f,
    val processedWidth: Int = 224,
    val processedHeight: Int = 224
)

enum class PipelineStage(val stageNumber: Int, val title: String, val shortDescription: String) {
    IMAGE_UPLOAD(1, "Image Upload", "Validating file format (JPG/PNG) & file size constraints"),
    PREPROCESSING(2, "Image Preprocessing", "Grayscale conversion and color channel standardisation"),
    RESIZE(3, "Resize", "Resampling to standard 224x224 pixel input matrix"),
    NORMALIZATION(4, "Normalization", "Min-max intensity scaling pixel values to [0.0, 1.0] range"),
    NOISE_REDUCTION(5, "Noise Reduction", "Applying 3x3 Gaussian smoothing kernel filtering"),
    FEATURE_EXTRACTION(6, "Feature Extraction", "Calculating texture, GLCM contrast, intensity & entropy metrics"),
    SVM_CLASSIFICATION(7, "SVM Classification", "Radial basis hyperplane margin separation"),
    DECISION_TREE_CLASSIFICATION(8, "Decision Tree", "Evaluating hierarchical entropy/Gini branching rules"),
    RESULT_DISPLAY(9, "Result Display", "Synthesizing classification output & clinical disclaimer")
}

enum class StepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}

data class PipelineStepState(
    val stage: PipelineStage,
    val status: StepStatus = StepStatus.PENDING,
    val detailMessage: String = "",
    val durationMs: Long = 0L
)

data class ModelPredictionResult(
    val predictedClass: DiagnosisClass,
    val svmPrediction: DiagnosisClass,
    val svmMarginScore: Float,
    val svmDetails: String,
    val decisionTreePrediction: DiagnosisClass,
    val decisionTreeSplitRule: String,
    val decisionTreeDetails: String,
    val confidence: Float?, // null if not available or demo mode without real confidence
    val isDemoMode: Boolean,
    val isReliable: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val extractedFeatures: ExtractedFeatures,
    val imageFileName: String,
    val imageUri: String? = null,
    val sampleResId: Int? = null,
    val disclaimer: String = "This application is an AI-assisted educational/research prototype and is not a medical diagnosis. Results must be reviewed by a qualified healthcare professional."
)

data class SampleScan(
    val id: String,
    val title: String,
    val category: DiagnosisClass,
    val drawableResId: Int,
    val scanType: String,
    val notes: String,
    val approximateSizeKb: Int
)

data class UserSession(
    val email: String,
    val name: String,
    val role: String,
    val hospitalDept: String,
    val isDemoAccount: Boolean = true
)
