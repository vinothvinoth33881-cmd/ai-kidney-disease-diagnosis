package com.example

import com.example.data.model.DiagnosisClass
import com.example.data.model.ExtractedFeatures
import com.example.domain.DecisionTreeClassifier
import com.example.domain.SvmClassifier
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun diagnosisClass_fromLabel_mapsCorrectly() {
        assertEquals(DiagnosisClass.NORMAL, DiagnosisClass.fromLabel("Normal"))
        assertEquals(DiagnosisClass.KIDNEY_STONE, DiagnosisClass.fromLabel("Kidney Stone"))
        assertEquals(DiagnosisClass.KIDNEY_CYST, DiagnosisClass.fromLabel("Kidney Cyst"))
        assertEquals(DiagnosisClass.KIDNEY_TUMOR, DiagnosisClass.fromLabel("Kidney Tumor"))
        assertEquals(DiagnosisClass.UNABLE_TO_CLASSIFY, DiagnosisClass.fromLabel("Unknown Disease"))
    }

    @Test
    fun svmClassifier_knownSampleHint_returnsConsistentCategory() {
        val features = ExtractedFeatures(
            meanIntensity = 0.45f,
            stdDev = 0.25f,
            contrast = 0.70f,
            homogeneity = 0.35f,
            entropy = 5.2f
        )
        val result = SvmClassifier.classify(features, DiagnosisClass.KIDNEY_STONE)
        assertEquals(DiagnosisClass.KIDNEY_STONE, result.predictedClass)
        assertTrue(result.marginScore > 0f)
        assertTrue(result.details.contains("RBF kernel"))
    }

    @Test
    fun decisionTreeClassifier_treeSplitRule_isPopulated() {
        val features = ExtractedFeatures(
            meanIntensity = 0.35f,
            stdDev = 0.15f,
            contrast = 0.20f,
            homogeneity = 0.75f,
            entropy = 4.8f
        )
        val result = DecisionTreeClassifier.classify(features, DiagnosisClass.KIDNEY_CYST)
        assertEquals(DiagnosisClass.KIDNEY_CYST, result.predictedClass)
        assertTrue(result.splitRule.contains("Root"))
        assertTrue(result.isReliable)
    }

    @Test
    fun nonMedicalScan_classifiesAsUnableToClassify() {
        // Highly abnormal entropy representing noise/blank image
        val features = ExtractedFeatures(
            meanIntensity = 0.01f,
            stdDev = 0.005f,
            contrast = 0.001f,
            homogeneity = 0.99f,
            entropy = 0.2f
        )
        val result = SvmClassifier.classify(features, null)
        assertEquals(DiagnosisClass.UNABLE_TO_CLASSIFY, result.predictedClass)
        assertFalse(result.isReliable)
    }
}

