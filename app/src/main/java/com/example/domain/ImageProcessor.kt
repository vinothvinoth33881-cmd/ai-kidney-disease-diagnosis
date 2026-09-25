package com.example.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.model.ExtractedFeatures
import java.io.InputStream
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object ImageProcessor {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val width: Int = 0,
        val height: Int = 0,
        val sizeBytes: Long = 0
    )

    fun validateImage(context: Context, uri: Uri): ValidationResult {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: ""
            val isSupportedMime = mimeType.contains("jpeg", ignoreCase = true) ||
                    mimeType.contains("jpg", ignoreCase = true) ||
                    mimeType.contains("png", ignoreCase = true) ||
                    mimeType.startsWith("image/")

            if (!isSupportedMime) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Unsupported file type ($mimeType). Only JPG, JPEG, and PNG images are supported."
                )
            }

            var size: Long = 0
            contentResolver.openInputStream(uri)?.use { stream ->
                size = stream.available().toLong()
            }

            // Max file size: 15MB
            if (size > 15 * 1024 * 1024) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "File size exceeds 15MB limit. Please upload a smaller medical scan."
                )
            }

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Unable to decode image dimensions. File may be corrupted."
                )
            }

            ValidationResult(
                isValid = true,
                width = options.outWidth,
                height = options.outHeight,
                sizeBytes = size
            )
        } catch (e: Exception) {
            ValidationResult(
                isValid = false,
                errorMessage = "Validation error: ${e.localizedMessage ?: "Unknown file error"}"
            )
        }
    }

    fun loadScaledBitmap(context: Context, uri: Uri, targetSize: Int = 224): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val original = BitmapFactory.decodeStream(stream) ?: return null
                Bitmap.createScaledBitmap(original, targetSize, targetSize, true)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadBitmapFromResource(context: Context, resId: Int, targetSize: Int = 224): Bitmap {
        val original = BitmapFactory.decodeResource(context.resources, resId)
        return Bitmap.createScaledBitmap(original, targetSize, targetSize, true)
    }

    /**
     * Extracts pixel intensity statistics and texture metrics from resized 224x224 bitmap.
     */
    fun extractFeatures(bitmap: Bitmap): ExtractedFeatures {
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height
        val histogram = IntArray(256)
        var sumIntensity = 0.0

        val grayscaleValues = FloatArray(totalPixels)
        var pixelIdx = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = bitmap.getPixel(x, y)
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                // Standard Luminance conversion
                val gray = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
                histogram[gray]++
                sumIntensity += gray
                grayscaleValues[pixelIdx++] = gray / 255f
            }
        }

        val meanGray = (sumIntensity / totalPixels).toFloat()
        val normalizedMean = meanGray / 255f

        // Standard deviation (variance / contrast metric)
        var sumVariance = 0.0
        for (v in grayscaleValues) {
            val diff = v - normalizedMean
            sumVariance += diff * diff
        }
        val stdDev = sqrt(sumVariance / totalPixels).toFloat()

        // Shannon Entropy calculation over normalized histogram
        var entropy = 0f
        for (count in histogram) {
            if (count > 0) {
                val p = count.toFloat() / totalPixels
                entropy -= p * (ln(p) / ln(2f))
            }
        }

        // Texture homogeneity & energy (GLCM-like 1-pixel neighbor difference)
        var diffSum = 0.0
        var energySum = 0.0
        for (y in 0 until height - 1) {
            for (x in 0 until width - 1) {
                val p1 = grayscaleValues[y * width + x]
                val p2 = grayscaleValues[y * width + (x + 1)]
                val d = p1 - p2
                diffSum += d * d
                energySum += p1 * p1
            }
        }
        val contrast = (diffSum / ((width - 1) * (height - 1))).toFloat() * 10f
        val energy = (energySum / (totalPixels)).toFloat()
        val homogeneity = 1f / (1f + contrast)

        return ExtractedFeatures(
            meanIntensity = normalizedMean,
            stdDev = stdDev,
            contrast = contrast,
            homogeneity = homogeneity,
            entropy = entropy,
            energy = energy,
            processedWidth = width,
            processedHeight = height
        )
    }

    /**
     * Checks if the scan resembles medical CT/MRI imaging rather than random photo.
     */
    fun isProbableMedicalScan(features: ExtractedFeatures): Boolean {
        // Medical CT scans have controlled entropy and grayscale consistency
        return features.entropy in 2.0f..7.8f && features.stdDev > 0.04f
    }
}
