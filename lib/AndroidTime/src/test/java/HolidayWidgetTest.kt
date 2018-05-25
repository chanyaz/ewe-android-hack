import android.view.LayoutInflater
import android.widget.TextView
import com.mobiata.android.time.R
import com.mobiata.android.time.widget.HolidayWidget
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class HolidayWidgetTest {

    private val context = RuntimeEnvironment.application

    lateinit var sut: HolidayWidget

    val holidayInfoHashMap = linkedMapOf(
            LocalDate("2018-04-01") to "ABC Holiday",
            LocalDate("2018-04-20") to "DEF Holiday",
            LocalDate("2018-04-21") to "GHI Holiday",
            LocalDate("2018-04-22") to "JKL Holiday",
            LocalDate("2018-06-13") to "MNO Holiday"
    )

    @Before
    fun setup() {
        sut = LayoutInflater.from(context).inflate(R.layout.holiday_calendar_widget, null) as HolidayWidget
        sut.holidayInfo = holidayInfoHashMap
    }

    @Test
    fun testHolidayWidgetContentWhenNoHoliday() {
        sut.setDisplayedYearMonth(YearMonth("2018-02"))
        assertEquals(0, sut.holidayRowLayoutContainer.childCount)
    }

    @Test
    fun testHolidayWidgetWithMoreThanThreeHoliday() {
        sut.setDisplayedYearMonth(YearMonth("2018-04"))
        assertEquals(4, sut.holidayRowLayoutContainer.childCount)
    }

    @Test
    fun testHolidayWidgetWithOneHoliday() {
        sut.setDisplayedYearMonth(YearMonth("2018-06"))
        assertEquals(1, sut.holidayRowLayoutContainer.childCount)
        val holidayRow = sut.holidayRowLayoutContainer.getChildAt(0)
        val holidayRowContent = holidayRow.findViewById<TextView>(R.id.holiday_row_text)
        val expectedHolidayContent = "Wed, Jun 13 - MNO Holiday"
        assertEquals(expectedHolidayContent, holidayRowContent.text)
    }
}
