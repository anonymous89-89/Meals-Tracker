package com.mealcycle.app.ui.integration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mealcycle.app.ui.components.*
import com.mealcycle.app.ui.theme.*

/**
 * Integration examples showing how to use the new premium components
 * in your existing screens. Copy and adapt these patterns.
 */

// ─────────────────────────────────────────────────────────────────────────────
// EXAMPLE 1: Upgrade Home Screen with Premium Stats
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreenPremiumExample(
    deliveredCount: Int = 15,
    totalMeals: Int = 30,
    pricePerMeal: Int = 150
) {
    // Replace your current stats row with this:
    
    // OLD: StatsCardsRow(deliveredCount, totalMeals, pricePerMeal)
    // NEW:
    /*
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ModernHeaderCard(
            title = "Your Meal Cycle",
            subtitle = "Track your progress with premium analytics",
            gradientColors = listOf(Primary, Color(0xFF818CF8)),
            modifier = Modifier.fillMaxWidth()
        )
        
        StatsCardsRow(deliveredCount, totalMeals, pricePerMeal)
        
        CycleProgressBar(deliveredCount, totalMeals)
        
        RemainingDaysBar(deliveredCount, totalMeals)
    }
    */
}

// ─────────────────────────────────────────────────────────────────────────────
// EXAMPLE 2: Modern Profile Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileCardPremiumExample(
    userName: String = "John Doe",
    mealsPlan: String = "30 Meals/Month",
    joinDate: String = "Jan 2024"
) {
    ModernHeaderCard(
        title = userName,
        subtitle = mealsPlan,
        gradientColors = listOf(Primary, Secondary),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Add profile content here
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXAMPLE 3: Premium Navigation Integration
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NavigationBarPremiumExample(
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {}
) {
    val navItems = listOf(
        NavigationItemData(
            icon = Icons.Filled.Home,
            label = "Home",
            selectedColor = Primary
        ),
        NavigationItemData(
            icon = Icons.Filled.History,
            label = "History",
            selectedColor = Color(0xFF8B5CF6)
        ),
        NavigationItemData(
            icon = Icons.Filled.BarChart,
            label = "Stats",
            selectedColor = Delivered
        ),
        NavigationItemData(
            icon = Icons.Filled.AccountCircle,
            label = "Profile",
            selectedColor = Amber
        )
    )
    
    PremiumBottomNavigationBar(
        selectedIndex = selectedIndex,
        items = navItems,
        onItemSelected = onItemSelected,
        modifier = Modifier.fillMaxWidth()
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// EXAMPLE 4: Stats Dashboard with Modern Cards
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatsDashboardPremiumExample() {
    /*
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ModernStatCard(
            icon = { Icon(Icons.Filled.CheckCircle, null, tint = Delivered) },
            title = "Delivered",
            value = "25",
            subtitle = "out of 30 meals",
            backgroundColor = DeliveredContainer.copy(alpha = 0.2f)
        )
        
        ModernStatCard(
            icon = { Icon(Icons.Filled.CurrencyRupee, null, tint = Amber) },
            title = "Amount Spent",
            value = "₹3,750",
            subtitle = "25 × ₹150",
            backgroundColor = AmberContainer.copy(alpha = 0.2f)
        )
        
        ModernStatCard(
            icon = { Icon(Icons.Filled.Replay, null, tint = Delivered) },
            title = "Refund Amount",
            value = "₹750",
            subtitle = "5 remaining meals",
            backgroundColor = DeliveredContainer.copy(alpha = 0.2f)
        )
    }
    */
}

// ─────────────────────────────────────────────────────────────────────────────
// EXAMPLE 5: Loading State with Shimmer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoadingStateExample(isLoading: Boolean) {
    // OLD: Simple spinner or text
    // NEW: Premium shimmer effect
    
    /*
    if (isLoading) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerLoadingEffect(width = 300, height = 24, cornerRadius = 8)
            ShimmerLoadingEffect(width = 200, height = 16, cornerRadius = 6)
            ShimmerLoadingEffect(width = 250, height = 20, cornerRadius = 8)
        }
    }
    */
}

// ─────────────────────────────────────────────────────────────────────────────
// EXAMPLE 6: Interactive Button with Animations
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ActionButtonPremiumExample() {
    // OLD: Standard FAB
    // NEW: Premium FAB with animations
    
    /*
    PremiumFloatingActionButton(
        icon = { Icon(Icons.Filled.Add, null) },
        onClick = { /* Add meal action */ },
        backgroundColor = Primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        size = 56
    )
    */
}

// ─────────────────────────────────────────────────────────────────────────────
// USAGE TIPS
// ─────────────────────────────────────────────────────────────────────────────

/*
MIGRATION CHECKLIST:

1. Import the new components:
   import com.mealcycle.app.ui.components.*
   import com.mealcycle.app.ui.theme.*

2. Replace old cards with new ones:
   OLD: ElevatedCard {} → NEW: GlassmorphicCard {}
   OLD: Card {} → NEW: EnhancedCard {}

3. Add animations to buttons:
   OLD: Button {} → NEW: PremiumFloatingActionButton {}

4. Upgrade stat displays:
   OLD: StatCard {} → NEW: StatCardPremium {} / ModernStatCard {}

5. Use new progress bars:
   OLD: LinearProgressIndicator {} → NEW: AnimatedProgressBar {}

6. Update navigation:
   OLD: NavigationBar {} → NEW: PremiumBottomNavigationBar {}

7. Theme improvements already integrated in:
   MealCycleTheme() - now uses AppTypography with Poppins font

TESTING:
- Compile and run the app
- Check animations are smooth (60fps+)
- Verify colors display correctly in light/dark mode
- Test on different device sizes
- Check touch response is immediate

PERFORMANCE TIPS:
- Use Modifier.graphicsLayer { alpha = 0.99f } for cards
- Limit animated components on same screen
- Test on mid-range devices (Snapdragon 670+)
- Profile with Android Profiler if needed

CUSTOMIZATION:
- Modify corner radius in component params
- Adjust animation durations in PremiumAnimationSpecs
- Change colors via theme Color.kt
- Update text styles in Typography.kt
*/
