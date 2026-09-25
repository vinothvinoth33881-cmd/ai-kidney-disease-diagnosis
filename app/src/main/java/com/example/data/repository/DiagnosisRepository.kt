package com.example.data.repository

import com.example.data.api.MedicalDiagnosisApiService
import com.example.data.api.PredictApiResponse
import com.example.data.api.RetrofitClient
import com.example.data.local.AnalysisDao
import com.example.data.local.AnalysisHistoryEntity
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class DiagnosisRepository(private val analysisDao: AnalysisDao) {

    var serverBaseUrl: String = "http://10.0.2.2:8000/"
        private set

    var isDemoMode: Boolean = true

    fun setApiUrl(url: String) {
        serverBaseUrl = url.trim()
    }

    private fun getService(): MedicalDiagnosisApiService {
        return RetrofitClient.getApiService(serverBaseUrl)
    }

    suspend fun checkServerHealth(): Result<Boolean> {
        return try {
            val response = getService().checkHealth()
            if (response.isSuccessful && response.body() != null) {
                Result.success(true)
            } else {
                Result.failure(Exception("Server returned HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun predictViaApi(imageBytes: ByteArray, filename: String): Result<PredictApiResponse> {
        return try {
            val requestFile = imageBytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, imageBytes.size)
            val body = MultipartBody.Part.createFormData("image", filename, requestFile)
            val response = getService().predictImage(body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Prediction API error ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Room Database access
    fun getAllHistory(): Flow<List<AnalysisHistoryEntity>> = analysisDao.getAllHistory()

    suspend fun saveAnalysis(entity: AnalysisHistoryEntity): Long = analysisDao.insert(entity)

    suspend fun deleteAnalysis(id: Long) = analysisDao.deleteById(id)

    suspend fun clearHistory() = analysisDao.clearAll()
}
