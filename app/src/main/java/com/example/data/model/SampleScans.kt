package com.example.data.model

import com.example.R

object SampleScans {
    val list = listOf(
        SampleScan(
            id = "normal_01",
            title = "Normal Kidney CT",
            category = DiagnosisClass.NORMAL,
            drawableResId = R.drawable.sample_ct_normal_1790311625859,
            scanType = "Axial Abdominal CT (Contrast)",
            notes = "Unremarkable bilateral renal cortex, intact corticomedullary differentiation.",
            approximateSizeKb = 348
        ),
        SampleScan(
            id = "stone_01",
            title = "Nephrolithiasis Scan",
            category = DiagnosisClass.KIDNEY_STONE,
            drawableResId = R.drawable.sample_ct_stone_1790311636225,
            scanType = "Non-Contrast Helical CT (KUB)",
            notes = "Radio-opaque calcific density detected in the renal calyx system.",
            approximateSizeKb = 392
        ),
        SampleScan(
            id = "cyst_01",
            title = "Renal Cortical Cyst",
            category = DiagnosisClass.KIDNEY_CYST,
            drawableResId = R.drawable.sample_ct_cyst_1790311648135,
            scanType = "Abdominal Contrast-Enhanced CT",
            notes = "Thin-walled, non-enhancing fluid attenuation mass in renal parenchymal margin.",
            approximateSizeKb = 365
        ),
        SampleScan(
            id = "tumor_01",
            title = "Renal Mass (Tumor)",
            category = DiagnosisClass.KIDNEY_TUMOR,
            drawableResId = R.drawable.sample_ct_tumor_1790311661094,
            scanType = "Multiphase CT Angiography",
            notes = "Heterogeneous enhancing mass in the renal cortex showing parenchymal distortion.",
            approximateSizeKb = 418
        )
    )
}
