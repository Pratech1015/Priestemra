package com.priestemra.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * RealAiSuperResolutionEngine:
 * Deep learning generative super-resolution powered by Real-ESRGAN (SRVGGNetCompact).
 * Uses real on-device neural network inference via TensorFlow Lite to synthesize authentic
 * high-frequency textures, sharp micro-edges, and flagship photorealism.
 *
 * Features:
 * - Overlapping tile inference with linear blending for seamless stitching
 * - NCHW output decoding for [1, 3, 512, 512] tensor layout
 * - Post-ESRGAN flagship color science: USM sharpening, contrast, vibrance
 */
object RealAiSuperResolutionEngine {
    private const val TAG = "RealAiEngine"
    private const val MODEL_PATH = "models/realesr_general_x4v3.tflite"
    private const val TILE_SIZE = 128
    private const val UPSCALE_FACTOR = 4
    private const val OUTPUT_TILE_SIZE = TILE_SIZE * UPSCALE_FACTOR // 512
    private const val TILE_OVERLAP = 16 // overlap in input pixels (= 64 in output space)
    private const val OUTPUT_OVERLAP = TILE_OVERLAP * UPSCALE_FACTOR // 64

    private var interpreter: Interpreter? = null
    private var isInitialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (isInitialized && interpreter != null) return

        try {
            val modelBuffer = loadModelFile(context, MODEL_PATH)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                useXNNPACK = true
            }
            interpreter = Interpreter(modelBuffer, options)
            isInitialized = true
            Log.i(TAG, "Real-ESRGAN neural network loaded successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Real-ESRGAN model", e)
        }
    }

    private fun loadModelFile(context: Context, path: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(path)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Executes Real-ESRGAN 4x deep neural network super-resolution on the input bitmap.
     * Uses overlapping tiles with linear blending for seamless output.
     * Applies flagship post-processing (sharpening, contrast, vibrance) after SR.
     */
    fun superResolve(context: Context, input: Bitmap): Bitmap {
        if (!isInitialized || interpreter == null) {
            initialize(context)
        }
        val tflite = interpreter ?: return input

        val srcW = input.width
        val srcH = input.height
        val targetW = srcW * UPSCALE_FACTOR
        val targetH = srcH * UPSCALE_FACTOR

        Log.i(TAG, "Starting Real-ESRGAN: ${srcW}x${srcH} -> ${targetW}x${targetH}")
        val startTime = System.currentTimeMillis()

        // Accumulator buffers for blending overlapping tiles
        val accR = FloatArray(targetW * targetH)
        val accG = FloatArray(targetW * targetH)
        val accB = FloatArray(targetW * targetH)
        val accW = FloatArray(targetW * targetH) // weight accumulator

        // Reusable I/O buffers
        val inputBuffer = ByteBuffer.allocateDirect(1 * TILE_SIZE * TILE_SIZE * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }
        val outputBuffer = ByteBuffer.allocateDirect(1 * 3 * OUTPUT_TILE_SIZE * OUTPUT_TILE_SIZE * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        val tilePixels = IntArray(TILE_SIZE * TILE_SIZE)

        // Step size = tile size - overlap
        val step = TILE_SIZE - TILE_OVERLAP // 112

        // Compute tile positions
        val tilesX = mutableListOf<Int>()
        var x = 0
        while (x < srcW) {
            val tileStart = min(x, max(0, srcW - TILE_SIZE))
            tilesX.add(tileStart)
            if (tileStart + TILE_SIZE >= srcW) break
            x += step
        }

        val tilesY = mutableListOf<Int>()
        var y = 0
        while (y < srcH) {
            val tileStart = min(y, max(0, srcH - TILE_SIZE))
            tilesY.add(tileStart)
            if (tileStart + TILE_SIZE >= srcH) break
            y += step
        }

        val totalTiles = tilesX.size * tilesY.size
        Log.i(TAG, "Processing $totalTiles tiles with ${TILE_OVERLAP}px overlap")

        var tileIdx = 0
        for (startY in tilesY) {
            for (startX in tilesX) {
                val curTileW = min(TILE_SIZE, srcW - startX)
                val curTileH = min(TILE_SIZE, srcH - startY)

                // 1. Extract tile (pad if needed)
                val tileBitmap = Bitmap.createBitmap(input, startX, startY, curTileW, curTileH)
                val squareTile = if (curTileW != TILE_SIZE || curTileH != TILE_SIZE) {
                    Bitmap.createScaledBitmap(tileBitmap, TILE_SIZE, TILE_SIZE, true)
                } else {
                    tileBitmap
                }
                squareTile.getPixels(tilePixels, 0, TILE_SIZE, 0, 0, TILE_SIZE, TILE_SIZE)

                // 2. Fill NHWC float input [1, 128, 128, 3] normalized to [0, 1]
                inputBuffer.rewind()
                for (p in tilePixels) {
                    inputBuffer.putFloat(((p shr 16) and 0xFF) / 255.0f)
                    inputBuffer.putFloat(((p shr 8) and 0xFF) / 255.0f)
                    inputBuffer.putFloat((p and 0xFF) / 255.0f)
                }

                // 3. Run inference
                inputBuffer.rewind()
                outputBuffer.rewind()
                synchronized(this) {
                    tflite.run(inputBuffer, outputBuffer)
                }

                // 4. Decode NCHW output [1, 3, 512, 512] and accumulate with blending weights
                outputBuffer.rewind()
                val totalPixels = OUTPUT_TILE_SIZE * OUTPUT_TILE_SIZE

                // Read all 3 planes
                val rPlane = FloatArray(totalPixels)
                val gPlane = FloatArray(totalPixels)
                val bPlane = FloatArray(totalPixels)
                for (i in 0 until totalPixels) rPlane[i] = outputBuffer.float
                for (i in 0 until totalPixels) gPlane[i] = outputBuffer.float
                for (i in 0 until totalPixels) bPlane[i] = outputBuffer.float

                // Compute output region in target image
                val outStartX = startX * UPSCALE_FACTOR
                val outStartY = startY * UPSCALE_FACTOR
                val outW = curTileW * UPSCALE_FACTOR
                val outH = curTileH * UPSCALE_FACTOR

                // Accumulate with linear ramp weights at overlap borders
                for (oy in 0 until outH) {
                    for (ox in 0 until outW) {
                        // Source pixel in the 512x512 output tile
                        val srcOx = if (curTileW == TILE_SIZE) ox else (ox * OUTPUT_TILE_SIZE / outW)
                        val srcOy = if (curTileH == TILE_SIZE) oy else (oy * OUTPUT_TILE_SIZE / outH)
                        val srcIdx = srcOy * OUTPUT_TILE_SIZE + srcOx

                        // Target pixel in the full output image
                        val dstX = outStartX + ox
                        val dstY = outStartY + oy
                        if (dstX >= targetW || dstY >= targetH) continue
                        val dstIdx = dstY * targetW + dstX

                        // Linear blending weight: ramp down at edges in overlap zone
                        var weight = 1.0f
                        // Left edge ramp
                        if (ox < OUTPUT_OVERLAP && outStartX > 0) {
                            weight *= ox.toFloat() / OUTPUT_OVERLAP
                        }
                        // Right edge ramp
                        if (ox >= outW - OUTPUT_OVERLAP && outStartX + outW < targetW) {
                            weight *= (outW - 1 - ox).toFloat() / OUTPUT_OVERLAP
                        }
                        // Top edge ramp
                        if (oy < OUTPUT_OVERLAP && outStartY > 0) {
                            weight *= oy.toFloat() / OUTPUT_OVERLAP
                        }
                        // Bottom edge ramp
                        if (oy >= outH - OUTPUT_OVERLAP && outStartY + outH < targetH) {
                            weight *= (outH - 1 - oy).toFloat() / OUTPUT_OVERLAP
                        }
                        weight = max(weight, 0.001f) // avoid zero weight

                        accR[dstIdx] += rPlane[srcIdx].coerceIn(0f, 1f) * weight
                        accG[dstIdx] += gPlane[srcIdx].coerceIn(0f, 1f) * weight
                        accB[dstIdx] += bPlane[srcIdx].coerceIn(0f, 1f) * weight
                        accW[dstIdx] += weight
                    }
                }

                if (squareTile != tileBitmap) squareTile.recycle()
                tileBitmap.recycle()
                tileIdx++
            }
        }

        // 5. Normalize accumulated pixels and write to output bitmap
        val outPixels = IntArray(targetW * targetH)
        for (i in outPixels.indices) {
            val w = if (accW[i] > 0f) accW[i] else 1f
            val r = ((accR[i] / w) * 255.0f).roundToInt().coerceIn(0, 255)
            val g = ((accG[i] / w) * 255.0f).roundToInt().coerceIn(0, 255)
            val b = ((accB[i] / w) * 255.0f).roundToInt().coerceIn(0, 255)
            outPixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }

        val srBitmap = Bitmap.createBitmap(outPixels, targetW, targetH, Bitmap.Config.ARGB_8888)

        val srElapsed = System.currentTimeMillis() - startTime
        Log.i(TAG, "Real-ESRGAN inference completed in ${srElapsed}ms.")

        // 6. Apply flagship post-processing color science
        val finalBitmap = applyFlagshipPostProcessing(srBitmap)
        srBitmap.recycle()

        val totalElapsed = System.currentTimeMillis() - startTime
        Log.i(TAG, "Real-ESRGAN + post-processing completed in ${totalElapsed}ms.")
        return finalBitmap
    }

    /**
     * Flagship-grade post-processing applied after neural super-resolution.
     * Implements:
     * 1. Black pedestal dehazing to eliminate milky sensor fog
     * 2. Multi-scale acutance (fine texture + edge contour + micro-clarity) scaled to 4x resolution
     * 3. S-curve local contrast expansion
     * 4. Smart vibrance boosting
     */
    private fun applyFlagshipPostProcessing(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val total = w * h
        val pixels = IntArray(total)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        // --- Stage 1: Black Pedestal Dehazing ---
        // Strip the milky fog characteristic of webcams and small sensors
        var minLum = 255
        var maxLum = 0
        // Sample 1% of pixels for speed
        val step = max(1, total / 10000)
        for (i in 0 until total step step) {
            val p = pixels[i]
            val lum = (19595 * ((p shr 16) and 0xFF) + 38470 * ((p shr 8) and 0xFF) + 7471 * (p and 0xFF)) shr 16
            if (lum < minLum) minLum = lum
            if (lum > maxLum) maxLum = lum
        }

        val blackFloor = max(0, minLum - 5)
        val whiteCeil = min(255, maxLum + 5)
        val dynRange = max(1, whiteCeil - blackFloor).toFloat()

        if (blackFloor > 10) {
            for (i in pixels.indices) {
                val p = pixels[i]
                val r = (((((p shr 16) and 0xFF) - blackFloor).coerceAtLeast(0) / dynRange) * 255.0f).roundToInt().coerceIn(0, 255)
                val g = (((((p shr 8) and 0xFF) - blackFloor).coerceAtLeast(0) / dynRange) * 255.0f).roundToInt().coerceIn(0, 255)
                val b = ((((p and 0xFF) - blackFloor).coerceAtLeast(0) / dynRange) * 255.0f).roundToInt().coerceIn(0, 255)
                pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        // --- Stage 2: Multi-Scale Acutance & Clarity (Matching 4x Upscale Factor) ---
        // Fine details (hair, texture, weave): radius 3
        val fineBlur = boxBlur(pixels, w, h, radius = 3)
        // Edge contours (object outlines, text boundaries): radius 8
        val midBlur = boxBlur(pixels, w, h, radius = 8)

        val fineStrength = 1.15f
        val midStrength = 0.65f

        for (i in pixels.indices) {
            val orig = pixels[i]
            val f = fineBlur[i]
            val m = midBlur[i]

            val oR = (orig shr 16) and 0xFF
            val oG = (orig shr 8) and 0xFF
            val oB = orig and 0xFF

            val fDiffR = oR - ((f shr 16) and 0xFF)
            val fDiffG = oG - ((f shr 8) and 0xFF)
            val fDiffB = oB - (f and 0xFF)

            val mDiffR = oR - ((m shr 16) and 0xFF)
            val mDiffG = oG - ((m shr 8) and 0xFF)
            val mDiffB = oB - (m and 0xFF)

            // Combined multi-band acutance synthesis
            val sR = (oR + fineStrength * fDiffR + midStrength * mDiffR).roundToInt().coerceIn(0, 255)
            val sG = (oG + fineStrength * fDiffG + midStrength * mDiffG).roundToInt().coerceIn(0, 255)
            val sB = (oB + fineStrength * fDiffB + midStrength * mDiffB).roundToInt().coerceIn(0, 255)

            pixels[i] = (0xFF shl 24) or (sR shl 16) or (sG shl 8) or sB
        }

        // --- Stage 3: S-Curve Contrast ---
        val contrastFactor = 1.15f
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = applyContrast((p shr 16) and 0xFF, contrastFactor)
            val g = applyContrast((p shr 8) and 0xFF, contrastFactor)
            val b = applyContrast(p and 0xFF, contrastFactor)
            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }

        // --- Stage 4: Flagship Vibrance Boost ---
        val vibranceAmount = 0.22f
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = ((p shr 16) and 0xFF) / 255.0f
            val g = ((p shr 8) and 0xFF) / 255.0f
            val b = (p and 0xFF) / 255.0f

            val maxC = maxOf(r, g, b)
            val minC = minOf(r, g, b)
            val sat = if (maxC > 0f) (maxC - minC) / maxC else 0f

            val boost = vibranceAmount * (1.0f - sat)
            val lum = 0.299f * r + 0.587f * g + 0.114f * b

            val nr = (r + (r - lum) * boost).coerceIn(0f, 1f)
            val ng = (g + (g - lum) * boost).coerceIn(0f, 1f)
            val nb = (b + (b - lum) * boost).coerceIn(0f, 1f)

            val ir = (nr * 255.0f).roundToInt().coerceIn(0, 255)
            val ig = (ng * 255.0f).roundToInt().coerceIn(0, 255)
            val ib = (nb * 255.0f).roundToInt().coerceIn(0, 255)

            pixels[i] = (0xFF shl 24) or (ir shl 16) or (ig shl 8) or ib
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, w, 0, 0, w, h)
        return result
    }

    /** Simple box blur for USM. Efficient two-pass (horizontal + vertical). */
    private fun boxBlur(pixels: IntArray, w: Int, h: Int, radius: Int): IntArray {
        val temp = IntArray(w * h)
        val result = IntArray(w * h)
        val diameter = 2 * radius + 1

        // Horizontal pass
        for (y in 0 until h) {
            var sumR = 0; var sumG = 0; var sumB = 0
            // Initialize window
            for (kx in -radius..radius) {
                val sx = kx.coerceIn(0, w - 1)
                val p = pixels[y * w + sx]
                sumR += (p shr 16) and 0xFF
                sumG += (p shr 8) and 0xFF
                sumB += p and 0xFF
            }
            temp[y * w] = (0xFF shl 24) or
                    ((sumR / diameter) shl 16) or
                    ((sumG / diameter) shl 8) or
                    (sumB / diameter)

            for (x in 1 until w) {
                val addX = min(x + radius, w - 1)
                val remX = max(x - radius - 1, 0)
                val addP = pixels[y * w + addX]
                val remP = pixels[y * w + remX]
                sumR += ((addP shr 16) and 0xFF) - ((remP shr 16) and 0xFF)
                sumG += ((addP shr 8) and 0xFF) - ((remP shr 8) and 0xFF)
                sumB += (addP and 0xFF) - (remP and 0xFF)
                temp[y * w + x] = (0xFF shl 24) or
                        ((sumR / diameter).coerceIn(0, 255) shl 16) or
                        ((sumG / diameter).coerceIn(0, 255) shl 8) or
                        (sumB / diameter).coerceIn(0, 255)
            }
        }

        // Vertical pass
        for (x in 0 until w) {
            var sumR = 0; var sumG = 0; var sumB = 0
            for (ky in -radius..radius) {
                val sy = ky.coerceIn(0, h - 1)
                val p = temp[sy * w + x]
                sumR += (p shr 16) and 0xFF
                sumG += (p shr 8) and 0xFF
                sumB += p and 0xFF
            }
            result[x] = (0xFF shl 24) or
                    ((sumR / diameter) shl 16) or
                    ((sumG / diameter) shl 8) or
                    (sumB / diameter)

            for (y in 1 until h) {
                val addY = min(y + radius, h - 1)
                val remY = max(y - radius - 1, 0)
                val addP = temp[addY * w + x]
                val remP = temp[remY * w + x]
                sumR += ((addP shr 16) and 0xFF) - ((remP shr 16) and 0xFF)
                sumG += ((addP shr 8) and 0xFF) - ((remP shr 8) and 0xFF)
                sumB += (addP and 0xFF) - (remP and 0xFF)
                result[y * w + x] = (0xFF shl 24) or
                        ((sumR / diameter).coerceIn(0, 255) shl 16) or
                        ((sumG / diameter).coerceIn(0, 255) shl 8) or
                        (sumB / diameter).coerceIn(0, 255)
            }
        }

        return result
    }

    /** Applies centered contrast curve: value = ((value/255 - 0.5) * factor + 0.5) * 255 */
    private fun applyContrast(value: Int, factor: Float): Int {
        val normalized = value / 255.0f
        val adjusted = (normalized - 0.5f) * factor + 0.5f
        return (adjusted * 255.0f).roundToInt().coerceIn(0, 255)
    }
}
