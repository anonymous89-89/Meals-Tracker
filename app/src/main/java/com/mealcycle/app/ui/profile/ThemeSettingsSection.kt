package com.mealcycle.app.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mealcycle.app.ui.theme.LocalThemeEngine
import com.mealcycle.app.ui.theme.composables.GlassCard
import com.mealcycle.app.ui.theme.composables.DynamicGradientBackground
import com.mealcycle.app.ui.theme.engine.ThemeEngine
import kotlinx.coroutines.launch

/**
 * Adaptive theming settings section for ProfileScreen.
 *
 * Features:
 * - Theme Source: Default / Wallpaper / Custom Image
 * - Blur Intensity slider (0–100%)
 * - Gradient Intensity slider (0–100%)
 * - Live preview card showing glass + gradient result
 * - Auto/Light/Dark mode toggle
 */
@Composable
fun ThemeSettingsSection(
    themeMode: String,
    onSetThemeMode: suspend (String) -> Unit
) {
    val engine = LocalThemeEngine.current ?: return
    val scope = rememberCoroutineScope()
    val themeSource by engine.themeSource.collectAsState(initial = "default")
    val blurIntensity by engine.blurIntensity.collectAsState(initial = 0.6f)
    val gradientIntensity by engine.gradientIntensity.collectAsState(initial = 0.7f)

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { engine.extractFromImage(it) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ── Live Preview ──
        Text(
            "Theme Preview",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        DynamicGradientBackground(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp)),
            intensityOverride = gradientIntensity
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    "Adaptive Theme Active",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Colors adapt to your wallpaper",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Theme Source ──
        Text(
            "Color Source",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = themeSource == "default",
                onClick = { engine.resetToDefault() },
                label = { Text("Default") },
                leadingIcon = { Icon(Icons.Filled.Palette, null, Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = themeSource == "wallpaper",
                onClick = { engine.extractFromWallpaper() },
                label = { Text("Wallpaper") },
                leadingIcon = { Icon(Icons.Filled.Wallpaper, null, Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = themeSource == "custom",
                onClick = { imagePicker.launch("image/*") },
                label = { Text("Image") },
                leadingIcon = { Icon(Icons.Filled.Image, null, Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
        }

        // ── Theme Mode ──
        Text(
            "Theme Mode",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = themeMode == "auto",
                onClick = { scope.launch { onSetThemeMode("auto") } },
                label = { Text("Auto") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = themeMode == "light",
                onClick = { scope.launch { onSetThemeMode("light") } },
                label = { Text("Light") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = themeMode == "dark",
                onClick = { scope.launch { onSetThemeMode("dark") } },
                label = { Text("Dark") },
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            when (themeMode) {
                "light" -> "Always use light theme"
                "dark" -> "Always use dark theme"
                else -> "Follows your device setting"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ── Blur Intensity ──
        Text(
            "Blur Intensity",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.BlurOff, null, Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = blurIntensity,
                onValueChange = { engine.setBlurIntensity(it) },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Icon(Icons.Filled.BlurOn, null, Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${(blurIntensity * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.width(36.dp)
            )
        }

        // ── Gradient Intensity ──
        Text(
            "Gradient Intensity",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Gradient, null, Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = gradientIntensity,
                onValueChange = { engine.setGradientIntensity(it) },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Icon(Icons.Filled.AutoAwesome, null, Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${(gradientIntensity * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}
