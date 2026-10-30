package com.priestemra.processing

import android.graphics.*
import androidx.compose.ui.graphics.Color

enum class FilterPreset(
    val id: String,
    val title: String,
    val description: String,
    val previewColor: Color
) {
    ORIGINAL("original", "Original", "Natural sensor color science", Color(0xFFEEEEEE)),
    LEICA_CLASSIC("leica", "Leica Look", "High micro-contrast, deep shadow roll-off", Color(0xFFD32F2F)),
    KODAK_PORTRA("portra", "Portra 400", "Warm pastel tones, soft skin science", Color(0xFFFFB74D)),
    FUJI_VELVIA("velvia", "Velvia 50", "Vivid landscape saturation, rich blues & greens", Color(0xFF4CAF50)),
    CINE_TEAL_ORANGE("teal_orange", "Cine Teal", "Hollywood block-buster color grading", Color(0xFF00ACC1)),
    MONO_NOIR("mono_noir", "Noir B&W", "High-contrast dynamic monochrome", Color(0xFF424242)),
    VINTAGE_CHROME("vintage", "Slide 70s", "Warm nostalgic retro slide film", Color(0xFFFF7043)),
    NIGHT_CYBER("cyber", "Cyber Neon", "Enhanced blue-violet shadow with warm highlights", Color(0xFF7E57C2))
}

object ColorFilterEngine {

    /**
     * Generates a 4x5 ColorMatrix for real-time Compose/AndroidView preview and image processing.
     * @param preset The chosen filter profile.
     * @param intensity Blend intensity between 0.0 (original) and 1.0 (full effect).
     */
    fun getColorMatrix(preset: FilterPreset, intensity: Float = 1.0f): ColorMatrix {
        val targetMatrix = when (preset) {
            FilterPreset.ORIGINAL -> ColorMatrix()

            FilterPreset.LEICA_CLASSIC -> {
                // High contrast, slightly muted colors, pronounced blacks
                ColorMatrix(floatArrayOf(
                    1.25f, -0.05f, -0.05f, 0f, -15f,
                    -0.05f, 1.25f, -0.05f, 0f, -15f,
                    -0.05f, -0.05f, 1.30f, 0f, -10f,
                    0f,     0f,     0f,    1f,   0f
                ))
            }

            FilterPreset.KODAK_PORTRA -> {
                // Warm golden skin tones, soft shadows, warm highlight lift
                ColorMatrix(floatArrayOf(
                    1.12f, 0.05f, 0.02f, 0f,  12f,
                    0.02f, 1.05f, 0.02f, 0f,   6f,
                    -0.03f, 0.00f, 0.95f, 0f, -10f,
                    0f,    0f,    0f,    1f,   0f
                ))
            }

            FilterPreset.FUJI_VELVIA -> {
                // Saturated rich colors, deep sky blues, punchy greens
                ColorMatrix(floatArrayOf(
                    1.22f, -0.08f, -0.04f, 0f,  5f,
                    -0.05f, 1.25f, -0.05f, 0f,  8f,
                    -0.04f, -0.06f, 1.30f, 0f, 10f,
                    0f,     0f,     0f,    1f,  0f
                ))
            }

            FilterPreset.CINE_TEAL_ORANGE -> {
                // Teal shadows, orange/gold skin tones
                ColorMatrix(floatArrayOf(
                    1.28f, -0.10f, -0.12f, 0f,  18f,
                    -0.05f, 1.08f,  0.02f, 0f,   0f,
                    -0.15f, 0.05f,  1.35f, 0f,  20f,
                    0f,     0f,     0f,    1f,   0f
                ))
            }

            FilterPreset.MONO_NOIR -> {
                // High dynamic range black and white with steep contrast curve
                val mono = ColorMatrix()
                mono.setSaturation(0f)
                val contrast = ColorMatrix(floatArrayOf(
                    1.35f, 0f,    0f,    0f, -25f,
                    0f,    1.35f, 0f,    0f, -25f,
                    0f,    0f,    1.35f, 0f, -25f,
                    0f,    0f,    0f,    1f,   0f
                ))
                contrast.preConcat(mono)
                contrast
            }

            FilterPreset.VINTAGE_CHROME -> {
                // 1970s slide film: yellowed highlights, cyan-tinted shadows, faded contrast
                ColorMatrix(floatArrayOf(
                    1.15f, 0.08f, -0.05f, 0f,  10f,
                    0.05f, 1.10f,  0.02f, 0f,  12f,
                    -0.08f, -0.02f, 0.90f, 0f, -15f,
                    0f,     0f,     0f,    1f,   0f
                ))
            }

            FilterPreset.NIGHT_CYBER -> {
                // Cool violet/cyan shadows with warm highlights
                ColorMatrix(floatArrayOf(
                    1.10f, 0.00f, 0.15f, 0f,  10f,
                    0.00f, 1.05f, 0.10f, 0f,  -5f,
                    0.10f, 0.05f, 1.45f, 0f,  25f,
                    0f,     0f,     0f,    1f,  0f
                ))
            }
        }

        if (intensity >= 0.99f) return targetMatrix

        // Interpolate between Identity and Target Matrix
        val identity = ColorMatrix()
        val identityArr = identity.array
        val targetArr = targetMatrix.array
        val resultArr = FloatArray(20)

        for (i in 0 until 20) {
            resultArr[i] = identityArr[i] + (targetArr[i] - identityArr[i]) * intensity.coerceIn(0f, 1f)
        }

        return ColorMatrix(resultArr)
    }

    /**
     * Applies the chosen filter matrix directly onto a Bitmap.
     */
    fun applyFilterToBitmap(src: Bitmap, preset: FilterPreset, intensity: Float = 1.0f): Bitmap {
        if (preset == FilterPreset.ORIGINAL || intensity <= 0.01f) {
            return src
        }

        val output = Bitmap.createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(getColorMatrix(preset, intensity))
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return output
    }
}
