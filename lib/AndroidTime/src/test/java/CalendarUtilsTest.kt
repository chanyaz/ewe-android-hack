import com.mobiata.android.time.util.CalendarConstants
import com.mobiata.android.time.util.CalendarUtils
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class CalendarUtilsTest {

    private val ROWS = CalendarConstants.ROWS
    private val COLS = CalendarConstants.COLS

    @Test
    fun testPreComputeGrid() {
        val receivedVisibleDatesArray = CalendarUtils.computeVisibleDays(YearMonth("2018-05"), ROWS, COLS)
        val expectedVisibleDatesArray = expectedVisibleDays(LocalDate("2018-04-29"))
        Assert.assertArrayEquals(expectedVisibleDatesArray, receivedVisibleDatesArray)
    }

    @Test
    fun testFormatLocalDateBasedOnLocale() {
        val receivedDateFormat = CalendarUtils.formatLocalDateBasedOnLocale(LocalDate("2018-09-09"), "E, MMM d")
        val expectedDateFormat = "Sun, Sep 9"
        assertEquals(expectedDateFormat, receivedDateFormat)
    }

    private fun expectedVisibleDays(mFirstDayOfGrid: LocalDate): Array<Array<LocalDate?>> {
        val visibleDays = Array<Array<LocalDate?>>(ROWS) { arrayOfNulls(COLS) }
        for (week in 0 until ROWS) {
            for (dayOfWeek in 0 until COLS) {
                visibleDays[week][dayOfWeek] = mFirstDayOfGrid.plusDays(week * COLS + dayOfWeek)
            }
        }
        return visibleDays
    }
}
