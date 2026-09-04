package com.mealcycle.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Premium navigation pill with smooth animations and ripple effects.
 */
@Composable
fun PremiumNavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) 
            selectedColor.copy(alpha = 0.15f) 
        else 
            Color.Transparent,
        animationSpec = tween(300),
        label = "nav_bg"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(300),
        label = "nav_color"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .graphicsLayer {
                alpha = 0.99f
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            
            if (isSelected) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Enhanced bottom navigation bar with glassmorphic background.
 */
@Composable
fun PremiumBottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    items: List<NavigationItemData>,
    onItemSelected: (Int) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
) {
    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp),
        backgroundColor = containerColor,
        cornerRadius = 20
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                PremiumNavigationItem(
                    icon = item.icon,
                    label = item.label,
                    isSelected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    selectedColor = item.selectedColor ?: MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

data class NavigationItemData(
    val icon: ImageVector,
    val label: String,
    val selectedColor: Color? = null
)
