package com.mealcycle.app.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mealcycle.app.ui.theme.Primary
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Built-in image crop dialog — circle crop for profile photos.
 * Supports pinch-to-zoom, pan, and rotate.
 * Saves cropped result to app cache and returns the new URI.
 */
@Composable
fun ImageCropDialog(
    sourceUri: Uri,
    onDismiss: () -> Unit,
    onCropComplete: (Uri) -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(sourceUri) { loadBitmapFromUri(context, sourceUri) }

    if (bitmap == null) {
        onDismiss()
        return
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Top bar ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Cancel", tint = Color.White)
                    }
                    Text("Crop Photo", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = {
                        // Perform crop and save
                        val cropped = performCrop(bitmap, scale, offsetX, offsetY, rotation)
                        val savedUri = saveBitmapToCache(context, cropped)
                        if (savedUri != null) {
                            onCropComplete(savedUri)
                        } else {
                            onDismiss()
                        }
                    }) {
                        Icon(Icons.Filled.Check, "Confirm", tint = Primary)
                    }
                }

                // ── Image preview with gesture handling ──
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 5f)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val imageBitmap = remember(bitmap, rotation) {
                        if (rotation != 0f) {
                            val matrix = Matrix().apply { postRotate(rotation) }
                            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        } else {
                            bitmap
                        }.asImageBitmap()
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val circleRadius = min(canvasW, canvasH) * 0.4f

                        // Draw image centered with pan/zoom
                        val imgW = imageBitmap.width.toFloat()
                        val imgH = imageBitmap.height.toFloat()
                        val fitScale = min(canvasW / imgW, canvasH / imgH)
                        val totalScale = fitScale * scale
                        val dstW = (imgW * totalScale).toInt()
                        val dstH = (imgH * totalScale).toInt()
                        val dstX = ((canvasW - dstW) / 2 + offsetX).roundToInt()
                        val dstY = ((canvasH - dstH) / 2 + offsetY).roundToInt()

                        drawImage(
                            image = imageBitmap,
                            dstOffset = IntOffset(dstX, dstY),
                            dstSize = IntSize(dstW, dstH)
                        )

                        // Dark overlay outside circle
                        drawRect(Color.Black.copy(alpha = 0.6f))
                        drawCircle(
                            color = Color.Transparent,
                            radius = circleRadius,
                            center = Offset(canvasW / 2, canvasH / 2),
                            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                        )

                        // Circle border
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = circleRadius,
                            center = Offset(canvasW / 2, canvasH / 2),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }
                }

                // ── Bottom controls ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { rotation = (rotation + 90f) % 360f },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Filled.RotateRight, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rotate")
                    }
                    OutlinedButton(
                        onClick = { scale = 1f; offsetX = 0f; offsetY = 0f; rotation = 0f },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Filled.ZoomIn, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Reset")
                    }
                }
            }
        }
    }
}

// ─── Helper functions ────────────────────────────────────────────────────────

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: Exception) {
        null
    }
}

private fun performCrop(
    source: Bitmap,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    rotation: Float
): Bitmap {
    // Apply rotation first
    val rotated = if (rotation != 0f) {
        val matrix = Matrix().apply { postRotate(rotation) }
        Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    } else {
        source
    }

    // Calculate the visible crop region from pan/zoom state
    // The canvas shows the image at fitScale * userScale, offset by (offsetX, offsetY)
    // We need to reverse-map the circle crop area back to image coordinates
    val imgW = rotated.width.toFloat()
    val imgH = rotated.height.toFloat()

    // Assume a square canvas area (the preview is ~80% of min dimension)
    val canvasSize = min(imgW, imgH) * 1.2f  // approximate canvas
    val fitScale = min(canvasSize / imgW, canvasSize / imgH)
    val totalScale = fitScale * scale
    val circleRadius = canvasSize * 0.4f

    // Center of circle in canvas coordinates
    val circleCenterX = canvasSize / 2f
    val circleCenterY = canvasSize / 2f

    // Image top-left in canvas coordinates
    val imgLeft = (canvasSize - imgW * totalScale) / 2f + offsetX
    val imgTop = (canvasSize - imgH * totalScale) / 2f + offsetY

    // Map circle bounds back to image coordinates
    val cropLeft = ((circleCenterX - circleRadius - imgLeft) / totalScale).coerceIn(0f, imgW)
    val cropTop = ((circleCenterY - circleRadius - imgTop) / totalScale).coerceIn(0f, imgH)
    val cropRight = ((circleCenterX + circleRadius - imgLeft) / totalScale).coerceIn(0f, imgW)
    val cropBottom = ((circleCenterY + circleRadius - imgTop) / totalScale).coerceIn(0f, imgH)

    val cropW = (cropRight - cropLeft).toInt().coerceAtLeast(1)
    val cropH = (cropBottom - cropTop).toInt().coerceAtLeast(1)
    val size = min(cropW, cropH)

    // Ensure we stay within bitmap bounds
    val x = cropLeft.toInt().coerceIn(0, (rotated.width - size).coerceAtLeast(0))
    val y = cropTop.toInt().coerceIn(0, (rotated.height - size).coerceAtLeast(0))
    val safeSize = size.coerceAtMost(min(rotated.width - x, rotated.height - y)).coerceAtLeast(1)

    val cropped = Bitmap.createBitmap(rotated, x, y, safeSize, safeSize)

    // Scale to 512×512 for efficiency
    return Bitmap.createScaledBitmap(cropped, 512, 512, true)
}

private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheDir, "cropped_profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }
}
