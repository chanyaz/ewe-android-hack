package com.expedia.bookings.presenter.car

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.CarFilterWidget
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class CarResultsPresenterTests {

    @Test
    fun testFocusSetForToolbarFilter() {
        val carResultsPresenter = CarResultsPresenter(RuntimeEnvironment.application, null)

        val carFilterWidgetMock = Mockito.mock(CarFilterWidget::class.java);
        carResultsPresenter.filter = carFilterWidgetMock
        carResultsPresenter.detailsToFilter.endTransition(false);
        Mockito.verify(carFilterWidgetMock, Mockito.times(0)).setFocusToToolbarForAccessibility()

        carResultsPresenter.detailsToFilter.endTransition(true);
        Mockito.verify(carFilterWidgetMock, Mockito.times(1)).setFocusToToolbarForAccessibility()

        carResultsPresenter.categoriesToFilter.endTransition(true)
        Mockito.verify(carFilterWidgetMock, Mockito.times(2)).setFocusToToolbarForAccessibility()
    }
}