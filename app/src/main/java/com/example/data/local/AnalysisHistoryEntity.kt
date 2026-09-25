package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_history")
data class AnalysisHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val imageFileName: String,
    val imageUri: String? = null,
    val sampleResId: Int? = null,
    val predictedClass: String,
    val svmPrediction: String,
    val decisionTreePrediction: String,
    val confidence: Float? = null,
    val isDemoMode: Boolean = true,
    val notes: String = "",
    val meanIntensity: Float = 0f,
    val contrast: Float = 0f,
    val entropy: Float = 0f
)
