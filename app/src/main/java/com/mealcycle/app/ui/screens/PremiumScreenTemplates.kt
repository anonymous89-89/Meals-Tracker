package com.mealcycle.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mealcycle.app.ui.components.*
import com.mealcycle.app.ui.home.CycleProgressBar
import com.mealcycle.app.ui.home.RemainingDaysBar
import com.mealcycle.app.ui.theme.Primary
import com.mealcycle.app.ui.theme.Delivered

/**
 * Template: Premium Dashboard Screen
 * Shows how to combine multiple premium components for a complete screen experience.
 */
@Composable
fun PremiumDashboardTemplate(
    userName: String = "John Doe",
    deliveredMeals: Int = 25,
    totalMeals: Int = 30,
    remainingDays: Int = 7,
    monthlySpent: Int = 3750,
    monthlyRefund: Int = 750
) {
    var showDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── Premium Header ───────────────────────────────────────────
        ModernHeaderCard(
            title = "Welcome, $userName",
            subtitle = "Your meal cycle overview",
            gradientColors = listOf(Primary, Color(0xFF818CF8)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val progressFraction = if (totalMeals > 0) deliveredMeals.toFloat() / totalMeals.toFloat() else 0f
                val progressPercent = if (totalMeals > 0) deliveredMeals * 100 / totalMeals else 0
                PremiumCircularProgress(
                    progress = progressFraction,
                    size = 100,
                    strokeWidth = 6,
                    label = "$progressPercent%"
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = "$deliveredMeals / $totalMeals Delivered",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$remainingDays days remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // ─── Stats Row ────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Animated Progress Bar
            item {
                CycleProgressBar(
                    deliveredCount = deliveredMeals,
                    totalMeals = totalMeals
                )
            }
            
            // Days remaining
            item {
                RemainingDaysBar(
                    deliveredCount = deliveredMeals,
                    totalMeals = totalMeals
                )
            }
            
            // Stats Section
            item {
                Text(
                    text = "Financial Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                PremiumStatsOverview(
                    stats = listOf(
                        StatOverviewItem(
                            label = "Amount Spent",
                            value = "₹$monthlySpent",
                            trend = 15f
                        ),
                        StatOverviewItem(
                            label = "Refund Available",
                            value = "₹$monthlyRefund",
                            trend = -8f
                        ),
                        StatOverviewItem(
                            label = "Average per Meal",
                            value = "₹${if (deliveredMeals > 0) monthlySpent / deliveredMeals else 0}",
                            trend = 0f
                        )
                    )
                )
            }
            
            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumButton(
                        text = "Edit Plan",
                        onClick = { showDialog = true },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Filled.Edit, null, tint = Color.White) }
                    )
                    
                    PremiumButton(
                        text = "More Options",
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.weight(1f),
                        backgroundColor = Color(0xFF8B5CF6),
                        leadingIcon = { Icon(Icons.Filled.MoreVert, null, tint = Color.White) }
                    )
                }
            }
            
            // Timeline Section
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                PremiumTimeline(
                    events = listOf(
                        TimelineEvent(
                            title = "Meal Delivered",
                            description = "Evening meal delivered",
                            timestamp = "Today, 6:30 PM",
                            isActive = true
                        ),
                        TimelineEvent(
                            title = "Payment Processed",
                            description = "Monthly subscription charged",
                            timestamp = "May 1, 2024"
                        ),
                        TimelineEvent(
                            title = "Cycle Started",
                            description = "New meal cycle commenced",
                            timestamp = "May 1, 2024"
                        )
                    )
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Dialog
    PremiumDialog(
        visible = showDialog,
        onDismissRequest = { showDialog = false },
        title = "Edit Your Meal Plan",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumInputField(
                    value = totalMeals.toString(),
                    onValueChange = {},
                    label = "Number of Meals",
                    leadingIcon = { Icon(Icons.Filled.Restaurant, null) }
                )
                
                Text(
                    text = "Price per meal: ₹150",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            PremiumButton(
                text = "Save",
                onClick = { showDialog = false },
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
    
    // Bottom Sheet
    PremiumBottomSheet(
        visible = showBottomSheet,
        onDismissRequest = { showBottomSheet = false },
        title = "More Options"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PremiumButton(
                text = "View History",
                onClick = { showBottomSheet = false },
                backgroundColor = Primary,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.History, null, tint = Color.White) }
            )
            
            PremiumButton(
                text = "Download Report",
                onClick = { showBottomSheet = false },
                backgroundColor = Delivered,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Download, null, tint = Color.White) }
            )
            
            PremiumButton(
                text = "Contact Support",
                onClick = { showBottomSheet = false },
                backgroundColor = Color(0xFF8B5CF6),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.ContactSupport, null, tint = Color.White) }
            )
        }
    }
}

/**
 * Template: Premium Settings Screen
 * Shows interactive form elements with premium styling
 */
@Composable
fun PremiumSettingsTemplate() {
    var emailValue by remember { mutableStateOf("user@example.com") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var privacyLevel by remember { mutableStateOf(0f) }
    var selectedTheme by remember { mutableStateOf("auto") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            ModernHeaderCard(
                title = "Settings",
                subtitle = "Customize your experience",
                gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
            )
        }
        
        // Email Section
        item {
            Text(
                text = "Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            PremiumInputField(
                value = emailValue,
                onValueChange = { emailValue = it },
                label = "Email Address",
                leadingIcon = { Icon(Icons.Filled.Email, null) }
            )
        }
        
        // Notifications
        item {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        item {
            GlassmorphicCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Push Notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Receive meal delivery alerts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            }
        }
        
        item {
            GlassmorphicCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Sound Effects",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Play sounds for interactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }
            }
        }
        
        // Theme Selection
        item {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Light", "Dark", "Auto").forEachIndexed { index, theme ->
                    PremiumChip(
                        text = theme,
                        isSelected = selectedTheme == theme.lowercase(),
                        onClick = { selectedTheme = theme.lowercase() }
                    )
                }
            }
        }
        
        // Privacy Slider
        item {
            Text(
                text = "Privacy Level: ${(privacyLevel.toInt())}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            PremiumSlider(
                value = privacyLevel,
                onValueChange = { privacyLevel = it },
                valueRange = 0f..100f
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
