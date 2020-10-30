package net.sourceforge.ganttproject.impex.msproject2

import net.sf.mpxj.ProjectFile
import net.sourceforge.ganttproject.TestSetupHelper
import net.sourceforge.ganttproject.importer.ImporterFromGanttFile
import net.sourceforge.ganttproject.model.calendar.CalendarEvent
import net.sourceforge.ganttproject.model.calendar.WeekendCalendarImpl
import net.sourceforge.ganttproject.model.time.CalendarFactory
import net.sourceforge.ganttproject.ui.GanttProjectImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Color
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

fun initLocale() {
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
  }
}

/**
 * Tests project calendar export and import.
 */
class ProjectCalendarTest {
  fun setUp() {
    initLocale()
  }

  @Test
  fun testExportCalendarEvents() {
    val calendar = WeekendCalendarImpl()
    calendar.publicHolidays = listOf<CalendarEvent>(
      CalendarEvent.newEvent(TestSetupHelper.newMonday().time, false, CalendarEvent.Type.HOLIDAY, "", Color.RED),
      CalendarEvent.newEvent(
        TestSetupHelper.newSaturday().time,
        false,
        CalendarEvent.Type.WORKING_DAY,
        "",
        Color.BLACK
      )
    )
    val mpxjProject = ProjectFile()
    val mpxjCalendar = mpxjProject.defaultCalendar
    ProjectFileExporter.exportHolidays(calendar, mpxjCalendar)
    Assertions.assertTrue(mpxjCalendar.isWorkingDate(TestSetupHelper.newSaturday().time))
    Assertions.assertFalse(mpxjCalendar.isWorkingDate(TestSetupHelper.newMonday().time))
  }

  @Test
  fun testImportCalendarEvents() {
    val project = GanttProjectImpl()
    val columns = ImporterFromGanttFile.VisibleFieldsImpl()
    val fileUrl = ProjectCalendarTest::class.java.getResource("/issue1520.xml")
    Assertions.assertNotNull(fileUrl)
    val importer = ProjectFileImporter(project, columns, File(fileUrl.toURI()))
    importer.setPatchMspdi(false)
    importer.run()

    val parser = SimpleDateFormat("yyyy-MM-dd")
    val calendar = project.activeCalendar
    val publicHolidays = ArrayList(calendar.publicHolidays)
    Assertions.assertEquals(2, publicHolidays.size)
    Assertions.assertTrue(publicHolidays[0].type == CalendarEvent.Type.WORKING_DAY)
    Assertions.assertEquals(parser.parse("2018-04-28"), publicHolidays[0].myDate)

    Assertions.assertTrue(publicHolidays[1].type == CalendarEvent.Type.HOLIDAY)
    Assertions.assertEquals(parser.parse("2018-04-30"), publicHolidays[1].myDate)
  }
}
