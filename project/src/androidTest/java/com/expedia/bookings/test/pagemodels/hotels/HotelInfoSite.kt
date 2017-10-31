package com.expedia.bookings.test.pagemodels.hotels

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isClickable
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withParent
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.ViewActions
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not

object HotelInfoSite {
	//Matchers
		//Containers
	private val detailContainer = withId(R.id.detail_container)
		//Individual Objects
	private val plusVIPContainer = withId(R.id.vip_access_message_container)
	private val plusVIPLabel = withId(R.id.vip_access_message)
	private val travelDates = withId(R.id.hotel_search_info)
	private val numberOfGuests = withId(R.id.hotel_search_info_guests)

	// Views
	@JvmStatic
	fun infositeDetailContainer(): ViewInteraction {
		return onView(detailContainer)
	}

	@JvmStatic
	fun plusVIPlabel(): ViewInteraction {
		return onView(plusVIPLabel)
	}
	@JvmStatic
	fun travelDates(): ViewInteraction {
		return onView(travelDates)
	}
	@JvmStatic
	fun numberOfGuests(): ViewInteraction {
		return onView(numberOfGuests)
	}

	// Actions
	@JvmStatic
	fun clickOnVIPAccess() {
		onView(plusVIPContainer).perform(click())
	}

	@JvmStatic
	fun verifyVIPAccessLabelIsPresent() {
		onView(plusVIPLabel).check(matches(isDisplayed()))
		onView(plusVIPLabel).check(matches(not(withText(isEmptyString()))))
	}

	@JvmStatic
	fun waitForPageToLoad() {
		infositeDetailContainer().perform(ViewActions.waitForViewToDisplay())
	}

	@JvmStatic
	fun validateTravelDates(dateString: String) {
		travelDates().check(matches(withText(dateString)))
	}

	@JvmStatic
	fun validateNumberOfGuests(guestsString: String) {
		numberOfGuests().check(matches(withText(guestsString)))
	}

	object VIPAccess {
		private val vipAccessPage = withId(R.id.hotel_vip_access_info)
		private val header = allOf(withId(R.id.toolbar), isDisplayed())
		private val headerCloseButton = allOf(withParent(header), instanceOf<Any>(android.widget.ImageButton::class.java), isClickable())
		private val headerLabel = allOf(withParent(header), instanceOf<Any>(android.widget.TextView::class.java))
		private val viewBody = allOf(withParent(withId(R.id.container)), instanceOf<Any>(android.widget.ScrollView::class.java), isDisplayed())
		private val viewBodyText = allOf(withParent(viewBody), instanceOf<Any>(android.widget.TextView::class.java))

		// View and Data Interactions
		@JvmStatic
		fun headerCloseButton(): ViewInteraction {
			return onView(headerCloseButton)
		}

		@JvmStatic
		fun headerLabel(): ViewInteraction {
			return onView(headerLabel)
		}

		@JvmStatic
		fun vipAccessPage(): ViewInteraction {
			return onView(vipAccessPage)
		}

		@JvmStatic
		fun viewBody(): ViewInteraction {
			return onView(viewBody)
		}

		@JvmStatic
		fun viewBodyText(): ViewInteraction {
			return onView(viewBodyText)
		}

		// Actions
		@JvmStatic
		fun clickHeaderCloseButton() {
			headerCloseButton().perform(click())
		}

		@JvmStatic
		fun waitForViewToLoad() {
			vipAccessPage().perform(ViewActions.waitForViewToDisplay())
		}

		@JvmStatic
		fun verifyHeaderText(text: String) {
			headerLabel().check(matches(withText(text)))
		}

		@JvmStatic
		fun verifyBodyText(text: String) {
			viewBodyText().check(matches(allOf(withText(text), isDisplayed())))
		}
	}
}
