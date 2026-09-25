package com.example.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class PredictApiResponse(
    val status: String,
    val predicted_class: String,
    val svm_prediction: String,
    val decision_tree_prediction: String,
    val confidence: Float? = null,
    val message: String? = null,
    val model_trained_on: String? = null,
    val features: Map<String, Float>? = null
)

data class HealthApiResponse(
    val status: String,
    val service: String? = null,
    val svm_model_loaded: Boolean = false,
    val decision_tree_loaded: Boolean = false,
    val supported_classes: List<String>? = null
)

interface MedicalDiagnosisApiService {
    @Multipart
    @POST("api/predict")
    suspend fun predictImage(
        @Part image: MultipartBody.Part
    ): Response<PredictApiResponse>

    @GET("api/health")
    suspend fun checkHealth(): Response<HealthApiResponse>
}
