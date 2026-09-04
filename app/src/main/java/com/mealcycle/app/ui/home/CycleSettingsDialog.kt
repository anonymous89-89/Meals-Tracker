package com.mealcycle.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mealcycle.app.ui.theme.*
import com.mealcycle.app.utils.MealCalculations

/**
 * Dialog for adjusting cycle settings:
 *  - Total meals in the cycle (e.g. 90)
 *  - Price per meal in ₹ (e.g. 50)
 */
@Composable
fun CycleSettingsDialog(
    currentTotalMeals: Int,
    currentPricePerMeal: Int,
    onDismiss: () -> Unit,
    onSave: (totalMeals: Int, pricePerMeal: Int) -> Unit
) {
    var totalMealsText by remember { mutableStateOf(currentTotalMeals.toString()) }
    var pricePerMealText by remember { mutableStateOf(currentPricePerMeal.toString()) }

    val totalMealsValue = totalMealsText.toIntOrNull()
    val pricePerMealValue = pricePerMealText.toIntOrNull()

    val totalMealsError = totalMealsValue == null || totalMealsValue !in 3..999
    val priceError = pricePerMealValue == null || pricePerMealValue !in 1..9999
    val canSave = !totalMealsError && !priceError

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = null,
                tint = Primary
            )
        },
        title = {
            Text("Cycle Settings", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // ── Total Meals ──────────────────────────────────────────────
                OutlinedTextField(
                    value = totalMealsText,
                    onValueChange = { totalMealsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Total Meals in Cycle") },
                    placeholder = { Text("${MealCalculations.DEFAULT_TOTAL_MEALS}") },
                    supportingText = {
                        if (totalMealsError && totalMealsText.isNotEmpty())
                            Text("Must be between 3 and 999", color = MaterialTheme.colorScheme.error)
                        else
                            Text("Default: ${MealCalculations.DEFAULT_TOTAL_MEALS} meals")
                    },
                    isError = totalMealsError && totalMealsText.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Price Per Meal ───────────────────────────────────────────
                OutlinedTextField(
                    value = pricePerMealText,
                    onValueChange = { pricePerMealText = it.filter { c -> c.isDigit() } },
                    label = { Text("Price Per Meal (₹)") },
                    placeholder = { Text("${MealCalculations.DEFAULT_PRICE_PER_MEAL}") },
                    supportingText = {
                        if (priceError && pricePerMealText.isNotEmpty())
                            Text("Must be between ₹1 and ₹9999", color = MaterialTheme.colorScheme.error)
                        else
                            Text("Default: ₹${MealCalculations.DEFAULT_PRICE_PER_MEAL}")
                    },
                    isError = priceError && pricePerMealText.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Live preview ─────────────────────────────────────────────
                if (canSave) {
                    val totalCost = totalMealsValue!! * pricePerMealValue!!
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = PrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Preview",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total cycle cost",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnPrimaryContainer
                                )
                                Text(
                                    "₹$totalCost",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Days in cycle",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnPrimaryContainer
                                )
                                Text(
                                    "${totalMealsValue / 3} days + ${totalMealsValue % 3} meals",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSave) onSave(totalMealsValue!!, pricePerMealValue!!)
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
