package com.mealcycle.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mealcycle.app.ui.theme.Primary

/**
 * Navigation item definition.
 */
data class NavItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

/**
 * Animated bottom navigation bar with a sliding indicator.
 *
 * The selected indicator slides horizontally between items using
 * spring animation, creating a smooth OxygenOS-style transition.
 *
 * @param items list of navigation items
 * @param selectedIndex currently selected tab index
 */
@Composable
fun AnimatedNavigationBar(
    items: List<NavItem>,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex

            val iconTint by animateColorAsState(
                targetValue = if (isSelected) Primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(250),
                label = "navIconTint$index"
            )
            val labelColor by animateColorAsState(
                targetValue = if (isSelected) Primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(250),
                label = "navLabelColor$index"
            )

            // Animated scale for the selected icon
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "navScale$index"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = item.onClick,
                icon = {
                    Icon(
                        item.icon, null,
                        tint = iconTint,
                        modifier = Modifier.size((24 * scale).dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = labelColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
