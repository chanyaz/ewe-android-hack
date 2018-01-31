package com.expedia.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class SuggestionAdapterViewModelTest {

    private val context = RuntimeEnvironment.application
    private val mockSuggestionService = Mockito.mock(SuggestionV4Services::class.java)

    @Test
    fun testLanguagesTriggeringEss() {
        val languageMap = createLanguageMap()
        for ((_, list) in languageMap) {
            val vm = MockSuggestionAdapterViewModel(context, mockSuggestionService, null, false, false)
            vm.queryObserver.onNext(list[0])
            assertNull(vm.receivedSuggestion)
            vm.queryObserver.onNext(list[1])
            assertEquals(list[1], vm.receivedSuggestion)
        }
    }

    private class MockSuggestionAdapterViewModel(context: Context, suggestionsService: SuggestionV4Services, locationObservable: Observable<Location>?, shouldShowCurrentLocation: Boolean, rawQueryEnabled: Boolean) :
            BaseSuggestionAdapterViewModel(context, suggestionsService, locationObservable, shouldShowCurrentLocation, rawQueryEnabled) {

        var receivedSuggestion: String? = null

        override fun getSuggestionService(query: String) {
            receivedSuggestion = query
        }

        override fun getSuggestionHistoryFile(): String {
            return ""
        }

        override fun getLineOfBusinessForGaia(): String {
            return ""
        }

        override fun getNearbySortTypeForGaia(): String {
            return ""
        }
    }

    private fun createLanguageMap(): Map<String, List<String>> {
        var languageMap = HashMap<String, List<String>>()
        languageMap["Arabic"] = listOf("ا", "ال", "القاهرة") // Cairo
        languageMap["Armenian"] = listOf("Ե", "Եր", "Երևան") // Yerevan
        languageMap["Burmese"] = listOf("", "န", "နေပြည်တော်") // Naypyidaw
        languageMap["Chinese"] = listOf("", "北", "北京") // Beijing
        languageMap["Cherokee"] = listOf("", "Ꮣ", "ᏓᎵᏆ") // Tahlequah
        languageMap["English"] = listOf("Lo", "Lon", "London")
        languageMap["French"] = listOf("N", "Nî", "Nîmes")
        languageMap["German"] = listOf("K", "Kö" , "Köln") // Cologne
        languageMap["Greek"] = listOf("Α", "Αθ", "Αθήνα") // Athens
        languageMap["Hindi"] = listOf("", "न", "नई दिल्ली") // New Delhi
        languageMap["Hebrew"] = listOf("י", "יר", "ירושלים") // Jerusalem
        languageMap["JapaneseHiragana"] = listOf("", "と", "とうきょう") // Tokyo
        languageMap["JapaneseKatakana"] = listOf("", "シ", "シカゴ") // Chicago
        languageMap["Korean"] = listOf("", "서", "서울") // Seoul
        languageMap["Lao"] = listOf("", "ວ", "ວຽງຈັນ") // Vientiane
        languageMap["Mongolian"] = listOf("", "ᠤ", "ᠤᠯᠠᠭᠠᠨᠪᠠᠭᠠᠲᠤ") // Ulaanbaatar
        languageMap["Nepali"] = listOf("", "क", "काठमाडौं") // Kathmandu
        languageMap["Russian"] = listOf("М", "Мо", "Москва") // Moscow
        languageMap["Sinhalese"] = listOf("", "ක", "කොළඹ") // Colombo
        languageMap["Spanish"] = listOf("L", "L’", "L’Hospitalet de Llobregat")
        languageMap["Tamil"] = listOf("", "க", "கோலாலம்பூர்") // Kuala Lumpur
        languageMap["Tibetan"] = listOf("", "ལ", "ལྷ་ས") // Lhasa
        languageMap["Thai"] = listOf("", "ก", "กรุงเทพฯ") // Bangkok
        languageMap["Vietnam"] = listOf("H", "Hà", "Hà Nội") // Ha Noi

        return languageMap
    }
}
