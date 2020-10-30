package net.sourceforge.ganttproject.ui.chart.scene

import net.sourceforge.ganttproject.TestSetupHelper
import net.sourceforge.ganttproject.model.calendar.GPCalendar
import net.sourceforge.ganttproject.model.calendar.WeekendCalendarImpl
import net.sourceforge.ganttproject.model.time.CalendarFactory
import net.sourceforge.ganttproject.model.time.TimeUnit
import net.sourceforge.ganttproject.model.time.impl.GPTimeUnitStack
import net.sourceforge.ganttproject.ui.chart.canvas.Canvas
import net.sourceforge.ganttproject.ui.chart.canvas.TestTextLengthCalculator
import net.sourceforge.ganttproject.ui.chart.grid.Offset
import net.sourceforge.ganttproject.ui.chart.grid.OffsetBuilderImpl
import net.sourceforge.ganttproject.ui.chart.grid.OffsetList
import net.sourceforge.ganttproject.ui.chart.grid.TestPainter
import net.sourceforge.ganttproject.ui.chart.text.DayTextFormatter
import net.sourceforge.ganttproject.ui.chart.text.TimeFormatter
import net.sourceforge.ganttproject.ui.chart.text.TimeUnitText
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.text.DateFormat
import java.util.*

/**
 * @author dbarashev@bardsoftware.com
 */
class TestBottomUnitSceneBuilder {
    init {
        object : CalendarFactory() {
            init {
                setLocaleApi(object : LocaleApi {
                    override fun getLocale(): Locale {
                        return Locale.US
                    }

                    override fun getShortDateFormat(): DateFormat {
                        return DateFormat.getDateInstance(DateFormat.SHORT, Locale.US)
                    }
                })
            }
        };
    }

    // Tests that label corresponding to weekend days are rendered with empty labels
    @Test
    fun testWeekendLabelsAreEmpty() {
        val calendar = WeekendCalendarImpl()
        val start = TestSetupHelper.newMonday().time

        // Build day offsets
        val builder = OffsetBuilderImpl.FactoryImpl()
            .withStartDate(start).withViewportStartDate(start)
            .withCalendar(calendar).withTopUnit(GPTimeUnitStack.WEEK).withBottomUnit(GPTimeUnitStack.DAY)
            .withAtomicUnitWidth(20).withEndOffset(210).withWeekendDecreaseFactor(10f)
            .build()
        val bottomUnitOffsets = OffsetList()
        builder.constructOffsets(ArrayList(), bottomUnitOffsets)

        // Fill canvas with simple bottom line
        val canvas = Canvas()
        val dumbFormatter = DayTextFormatter()
        val bottomUnitSceneBuilder = BottomUnitSceneBuilder(canvas, object : BottomUnitSceneBuilder.InputApi {
            override fun getTopLineHeight(): Int {
                return 10
            }

            override fun getBottomUnitOffsets(): OffsetList {
                return bottomUnitOffsets
            }

            override fun getFormatter(offsetUnit: TimeUnit?, lowerLine: TimeUnitText.Position?): TimeFormatter {
                return dumbFormatter
            }
        })
        bottomUnitSceneBuilder.build()

        // Get text groups from canvas. The only legal way of doing that is "painting"
        val textLengthCalculator = TestTextLengthCalculator(
            10
        )
        val textGroups = mutableListOf<Canvas.TextGroup>()
        canvas.paint(object : TestPainter(textLengthCalculator) {
            override fun paint(textGroup: Canvas.TextGroup?) {
                textGroups.add(textGroup!!)
            }
        })
        Assertions.assertEquals(1, textGroups.size)

        // Now iterate through all texts and check that those which were built for weekend offsets
        // are empty.
        textGroups[0].getLine(0).forEachIndexed { _, text ->
            val offset = findOffset(bottomUnitOffsets, text.leftX)
            Assertions.assertNotNull(offset)
            val label = text.getLabels(textLengthCalculator)[0].text
            if (offset!!.dayMask.and(GPCalendar.DayMask.WEEKEND) == 0) {
                Assertions.assertFalse(label.isEmpty())
            } else {
                Assertions.assertTrue(label.isEmpty())
            }
        }
    }

    private fun findOffset(offsets: OffsetList, leftX: Int): Offset? {
        var result: Offset? = null
        for (offset in offsets) {
            if (offset.startPixels < leftX && offset.startPixels > result?.startPixels ?: -1) {
                result = offset
            }
        }
        return result
    }
}
