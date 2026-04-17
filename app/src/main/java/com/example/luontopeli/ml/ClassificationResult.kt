package com.example.luontopeli.ml

import com.google.mlkit.vision.label.ImageLabel

/**
 * ML Kit -tunnistuksen tulosluokka.
 */
sealed class ClassificationResult {
    data class Success(
        val label: String,
        val confidence: Float,
        val allLabels: List<ImageLabel> = emptyList()
    ) : ClassificationResult()

    data class NotNature(
        val allLabels: List<ImageLabel> = emptyList()
    ) : ClassificationResult()

    data class Error(val message: String) : ClassificationResult()
    object Loading : ClassificationResult()
}
