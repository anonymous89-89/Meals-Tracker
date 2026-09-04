package com.mealcycle.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mealcycle.app.R
import com.mealcycle.app.ui.theme.*
import com.mealcycle.app.utils.MealCalculations

/**
 * ModalBottomSheet shown when user taps "End Plan".
 * Displays a summary of the current cycle and offers Cancel / Confirm buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndPlanDialog(
    deliveredCount: Int,
    totalMeals: Int = MealCalculations.DEFAULT_TOTAL_MEALS,
    pricePerMeal: Int = MealCalculations.DEFAULT_PRICE_PER_MEAL,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val remaining = MealCalculations.remainingMeals(deliveredCount, totalMeals)
    val spent = MealCalculations.amountSpent(deliveredCount, pricePerMeal)
    val refund = MealCalculations.refundAmount(remaining, pricePerMeal)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.plan_summary),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider()

            SummaryRow(
                label = stringResource(R.string.delivered_meals),
                value = "$deliveredCount"
            )
            SummaryRow(
                label = stringResource(R.string.remaining_meals),
                value = "$remaining"
            )
            SummaryRow(
                label = stringResource(R.string.amount_spent),
                value = "₹$spent",
                valueColor = Amber
            )
            SummaryRow(
                label = stringResource(R.string.refund_amount),
                value = "₹$refund",
                valueColor = Delivered
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(stringResource(R.string.confirm_save))
                }
            }
        }
    }
}

/**
 * AlertDialog shown when user taps "Reset Plan".
 */
@Composable
fun ResetPlanDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.reset_plan),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(stringResource(R.string.reset_plan_message))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.reset), color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
