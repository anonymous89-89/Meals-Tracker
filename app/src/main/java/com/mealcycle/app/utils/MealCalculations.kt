package com.mealcycle.app.utils

/**
 * Pure, stateless calculation functions for meal cycle data.
 * All functions accept [totalMeals] and [pricePerMeal] so the cycle
 * size and price can be configured by the user at runtime.
 */
object MealCalculations {

    // ─── Defaults (used as DataStore fallback values) ──────────────────────────
    const val DEFAULT_TOTAL_MEALS = 90
    const val DEFAULT_PRICE_PER_MEAL = 50

    /**
     * Total meals delivered in the current cycle.
     * Capped at [totalMeals] — can never exceed the configured limit.
     */
    fun deliveredMeals(entries: Int, totalMeals: Int = DEFAULT_TOTAL_MEALS): Int =
        entries.coerceAtMost(totalMeals)

    /**
     * Meals still remaining in the cycle.
     */
    fun remainingMeals(delivered: Int, totalMeals: Int = DEFAULT_TOTAL_MEALS): Int =
        (totalMeals - delivered).coerceAtLeast(0)

    /**
     * Full days remaining (integer division — no rounding).
     */
    fun remainingDays(remaining: Int): Int = remaining / 3

    /**
     * Extra meals beyond complete days (0, 1, or 2).
     */
    fun remainingExtraMeals(remaining: Int): Int = remaining % 3

    /**
     * Human-readable string: "X days and Y meals remaining"
     */
    fun remainingTimeText(remaining: Int): String {
        val days = remainingDays(remaining)
        val extra = remainingExtraMeals(remaining)
        return "$days days and $extra meals remaining"
    }

    /**
     * Rupee amount already spent (delivered × pricePerMeal).
     */
    fun amountSpent(delivered: Int, pricePerMeal: Int = DEFAULT_PRICE_PER_MEAL): Int =
        delivered * pricePerMeal

    /**
     * Rupee refund for undelivered meals (remaining × pricePerMeal).
     */
    fun refundAmount(remaining: Int, pricePerMeal: Int = DEFAULT_PRICE_PER_MEAL): Int =
        remaining * pricePerMeal

    /**
     * Progress as a 0.0–1.0 fraction for the animated progress bar.
     */
    fun progressFraction(delivered: Int, totalMeals: Int = DEFAULT_TOTAL_MEALS): Float =
        (delivered.toFloat() / totalMeals.toFloat()).coerceIn(0f, 1f)

    /**
     * Progress as a 0–100 integer percentage.
     */
    fun progressPercent(delivered: Int, totalMeals: Int = DEFAULT_TOTAL_MEALS): Int =
        (progressFraction(delivered, totalMeals) * 100).toInt()

    /**
     * Returns true when the configured meal cycle is fully complete.
     */
    fun isCycleComplete(delivered: Int, totalMeals: Int = DEFAULT_TOTAL_MEALS): Boolean =
        delivered >= totalMeals
}
