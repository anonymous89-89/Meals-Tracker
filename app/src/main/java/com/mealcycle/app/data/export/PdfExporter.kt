package com.mealcycle.app.data.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.mealcycle.app.data.model.MealEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates a PDF table of meal data using Android built-in PdfDocument API.
 *
 * Table columns: Date | Breakfast | Lunch | Dinner | Total
 */
@Singleton
class PdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    companion object {
        private const val PAGE_WIDTH = 595   // A4 in points
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 40f
        private const val ROW_HEIGHT = 28f
        private const val HEADER_HEIGHT = 80f

        // Column widths: Date | Breakfast | Lunch | Dinner | Total
        private val COL_WIDTHS = floatArrayOf(110f, 95f, 95f, 95f, 70f)
        private val COL_HEADERS = arrayOf("Date", "Breakfast", "Lunch", "Dinner", "Total")
    }

    /**
     * Generate PDF and write to the given SAF URI.
     * @return number of days exported.
     */
    fun exportToPdf(
        uri: Uri,
        userName: String,
        startDate: LocalDate,
        endDate: LocalDate,
        groupedMeals: Map<String, List<MealEntry>>
    ): Int {
        val document = PdfDocument()
        val sortedDays = groupedMeals.entries.sortedBy { it.key }

        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16f; color = Color.parseColor("#4F46E5")
        }
        val subtitlePaint = Paint().apply {
            textSize = 10f; color = Color.GRAY
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 10f; color = Color.WHITE
        }
        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#4F46E5")
        }
        val cellPaint = Paint().apply {
            textSize = 10f; color = Color.DKGRAY
        }
        val checkPaint = Paint().apply {
            textSize = 11f; color = Color.parseColor("#22C55E")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val dashPaint = Paint().apply {
            textSize = 10f; color = Color.LTGRAY
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY; strokeWidth = 0.5f
        }
        val stripePaint = Paint().apply {
            color = Color.parseColor("#F5F5FF")
        }

        val maxRowsPerPage = ((PAGE_HEIGHT - MARGIN * 2 - HEADER_HEIGHT - ROW_HEIGHT) / ROW_HEIGHT).toInt()
        var pageNum = 0
        var rowIndex = 0

        while (rowIndex < sortedDays.size) {
            pageNum++
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            var y = MARGIN

            // ── Page header ──
            if (pageNum == 1) {
                canvas.drawText("Meal Tracker — Export Report", MARGIN, y + 16f, titlePaint)
                y += 28f
                canvas.drawText("User: $userName", MARGIN, y + 10f, subtitlePaint)
                canvas.drawText(
                    "Period: ${startDate.format(dateFormat)} to ${endDate.format(dateFormat)}",
                    MARGIN + 180f, y + 10f, subtitlePaint
                )
                y += 20f
                canvas.drawText(
                    "Exported: ${LocalDate.now().format(dateFormat)}  |  Page $pageNum",
                    MARGIN, y + 10f, subtitlePaint
                )
                y += 22f
            } else {
                canvas.drawText("Meal Tracker — Page $pageNum", MARGIN, y + 12f, subtitlePaint)
                y += 24f
            }

            // ── Table header row ──
            val tableLeft = MARGIN
            canvas.drawRect(tableLeft, y, tableLeft + COL_WIDTHS.sum(), y + ROW_HEIGHT, headerBgPaint)
            var x = tableLeft
            COL_HEADERS.forEachIndexed { i, header ->
                canvas.drawText(header, x + 6f, y + 18f, headerPaint)
                x += COL_WIDTHS[i]
            }
            y += ROW_HEIGHT

            // ── Data rows ──
            var rowsOnPage = 0
            while (rowIndex < sortedDays.size && rowsOnPage < maxRowsPerPage) {
                val (date, meals) = sortedDays[rowIndex]
                val mealTypes = meals.map { it.mealType.uppercase() }.toSet()
                val hasBreakfast = "BREAKFAST" in mealTypes
                val hasLunch = "LUNCH" in mealTypes
                val hasDinner = "DINNER" in mealTypes
                val total = meals.size

                // Zebra stripes
                if (rowsOnPage % 2 == 1) {
                    canvas.drawRect(tableLeft, y, tableLeft + COL_WIDTHS.sum(), y + ROW_HEIGHT, stripePaint)
                }

                // Cell values: Date | B ✓/— | L ✓/— | D ✓/— | Total
                x = tableLeft
                // Date
                canvas.drawText(date, x + 6f, y + 18f, cellPaint)
                x += COL_WIDTHS[0]
                // Breakfast
                if (hasBreakfast) canvas.drawText("✓", x + 30f, y + 18f, checkPaint)
                else canvas.drawText("—", x + 30f, y + 18f, dashPaint)
                x += COL_WIDTHS[1]
                // Lunch
                if (hasLunch) canvas.drawText("✓", x + 24f, y + 18f, checkPaint)
                else canvas.drawText("—", x + 24f, y + 18f, dashPaint)
                x += COL_WIDTHS[2]
                // Dinner
                if (hasDinner) canvas.drawText("✓", x + 24f, y + 18f, checkPaint)
                else canvas.drawText("—", x + 24f, y + 18f, dashPaint)
                x += COL_WIDTHS[3]
                // Total
                canvas.drawText("$total / 3", x + 10f, y + 18f, cellPaint)

                // Bottom line
                canvas.drawLine(tableLeft, y + ROW_HEIGHT, tableLeft + COL_WIDTHS.sum(), y + ROW_HEIGHT, linePaint)

                y += ROW_HEIGHT
                rowIndex++
                rowsOnPage++
            }

            document.finishPage(page)
        }

        // If no data, create a single empty page
        if (sortedDays.isEmpty()) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawText("No meal data in the selected date range.", MARGIN, MARGIN + 40f, cellPaint)
            document.finishPage(page)
        }

        // Write to SAF URI
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            document.writeTo(stream)
        }
        document.close()

        return sortedDays.size
    }
}
