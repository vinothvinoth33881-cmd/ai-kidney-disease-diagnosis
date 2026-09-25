package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CystColor
import com.example.ui.theme.NormalColor
import com.example.ui.theme.StoneColor
import com.example.ui.theme.TumorColor
import com.example.ui.theme.UncertainColor

enum class DiagnosisClass(val label: String, val badgeColor: Color, val clinicalDescription: String) {
    NORMAL(
        label = "Normal",
        badgeColor = NormalColor,
        clinicalDescription = "Bilateral kidneys appear normal with preserved parenchymal thickness and no focal lesions, calculi, or hydronephrosis."
    ),
    KIDNEY_STONE(
        label = "Kidney Stone",
        badgeColor = StoneColor,
        clinicalDescription = "High attenuation calcification identified, indicative of nephrolithiasis / renal calculus in the collecting system or parenchyma."
    ),
    KIDNEY_CYST(
        label = "Kidney Cyst",
        badgeColor = CystColor,
        clinicalDescription = "Well-circumscribed hypoattenuating fluid density consistent with simple or minimally complex renal cortical cyst."
    ),
    KIDNEY_TUMOR(
        label = "Kidney Tumor",
        badgeColor = TumorColor,
        clinicalDescription = "Solid soft-tissue attenuating renal mass with irregular enhancement pattern suspicious for renal cell carcinoma / neoplasm."
    ),
    UNABLE_TO_CLASSIFY(
        label = "Unable to classify",
        badgeColor = UncertainColor,
        clinicalDescription = "Unable to classify this image reliably. Please consult a qualified medical professional."
    );

    companion object {
        fun fromLabel(label: String): DiagnosisClass {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: UNABLE_TO_CLASSIFY
        }
    }
}
