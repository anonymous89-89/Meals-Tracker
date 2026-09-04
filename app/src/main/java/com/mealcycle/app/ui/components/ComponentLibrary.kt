package com.mealcycle.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Reusable components library index for the premium UI system.
 *
 * COMPONENTS AVAILABLE:
 * ─────────────────────
 *
 * 1. GlassmorphicCard - Frosted glass effect with gradient and border
 *    Usage: @Composable fun MyScreen() { GlassmorphicCard { content() } }
 *
 * 2. NeumorphicCard - Soft UI design with subtle shadows
 *    Usage: NeumorphicCard { content() }
 *
 * 3. GradientCard - Customizable gradient background card
 *    Usage: GradientCard(colors = listOf(Color1, Color2)) { content() }
 *
 * 4. InteractiveStatCard - Stats with hover/tap animations
 *    Usage: InteractiveStatCard(icon = {}, label = "Label", value = "123")
 *
 * 5. AnimatedProgressBar - Spring-animated progress bar
 *    Usage: AnimatedProgressBar(progress = 0.75f)
 *
 * 6. PremiumFloatingActionButton - Enhanced FAB with scale animation
 *    Usage: PremiumFloatingActionButton(icon = {}, onClick = {})
 *
 * 7. ShimmerLoadingEffect - Animated shimmer skeleton loading
 *    Usage: ShimmerLoadingEffect(width = 200, height = 20)
 *
 * 8. RippleEffect - Animated ripple for interactions
 *    Usage: RippleEffect(color = Primary)
 *
 * 9. PulseAnimation - Attention-grabbing pulse effect
 *    Usage: PulseAnimation(color = Primary)
 *
 * 10. ParallaxCard - Parallax scrolling effect container
 *     Usage: ParallaxCard(offset = scrollOffset) { content() }
 *
 * 11. PremiumNavigationItem - Individual nav item with animations
 *     Usage: PremiumNavigationItem(icon = Icons.Home, label = "Home", isSelected = true)
 *
 * 12. PremiumBottomNavigationBar - Modern bottom nav with glassmorphism
 *     Usage: PremiumBottomNavigationBar(selectedIndex = 0, items = navItems)
 *
 * 13. ModernHeaderCard - Gradient header with shadow
 *     Usage: ModernHeaderCard(title = "Title", subtitle = "Sub")
 *
 * 14. ModernStatCard - Icon + stat display card
 *     Usage: ModernStatCard(icon = {}, title = "Delivered", value = "25")
 *
 * 15. EnhancedCard - Card with loading state support
 *     Usage: EnhancedCard(isLoading = isLoading) { content() }
 *
 * USAGE TIPS:
 * ──────────
 * - All components support custom modifiers for sizing and spacing
 * - Use spring animations for natural, premium feel
 * - Combine multiple components for complex layouts
 * - Apply consistent color scheme using theme colors
 * - Test animations at 120fps for smoothness
 *
 * ANIMATION SPECS:
 * ───────────────
 * - Spring: dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
 * - Tween: durationMillis = 300-500ms, easing = FastOutSlowInEasing
 * - Infinite: durationMillis = 1000-1500ms for smooth continuous effects
 */

@Composable
fun ComponentLibraryShowcase() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Premium UI Components",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Glassmorphic, Neumorphic & Interactive Components",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
