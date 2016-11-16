package com.expedia.bookings.presenter.car

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.CarCategoryDetailsWidget
import com.expedia.bookings.widget.CarFilterWidget
import android.support.v7.widget.Toolbar
import com.expedia.bookings.widget.CarCategoryListWidget
import com.expedia.bookings.widget.FilterButtonWithCountWidget
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
        val carCategoryDetailsWidgetMock = Mockito.mock(CarCategoryDetailsWidget::class.java);
        val toolBarMock = Mockito.mock(Toolbar::class.java);
        val filterButtonWithCountWidgetMock = Mockito.mock(FilterButtonWithCountWidget::class.java);
        val carCategoryListWidgetMock = Mockito.mock(CarCategoryListWidget::class.java);
        carResultsPresenter.filter = carFilterWidgetMock
        carResultsPresenter.toolbar = toolBarMock
        carResultsPresenter.details = carCategoryDetailsWidgetMock
        carResultsPresenter.filterToolbar = filterButtonWithCountWidgetMock
        carResultsPresenter.categories = carCategoryListWidgetMock
        carResultsPresenter.detailsToFilter.endTransition(false);
        Mockito.verify(carFilterWidgetMock, Mockito.times(0)).setFocusToToolbarForAccessibility()

        carResultsPresenter.detailsToFilter.endTransition(true);
        Mockito.verify(carFilterWidgetMock, Mockito.times(1)).setFocusToToolbarForAccessibility()

        carResultsPresenter.categoriesToFilter.endTransition(true)
        Mockito.verify(carFilterWidgetMock, Mockito.times(2)).setFocusToToolbarForAccessibility()
    }
}