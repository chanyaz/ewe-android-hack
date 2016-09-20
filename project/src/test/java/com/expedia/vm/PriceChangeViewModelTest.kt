package com.expedia.vm

import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal

@RunWith(RobolectricRunner::class)
class PriceChangeViewModelTest {

    val context = RuntimeEnvironment.application
    lateinit var vm: PriceChangeViewModel

    @Test
    fun testPriceChangeVisibility() {
        vm = PriceChangeViewModel(context, LineOfBusiness.FLIGHTS)

        vm.originalPrice.onNext(Money(BigDecimal(1000.00), "USD"))
        vm.newPrice.onNext(Money(BigDecimal(991.00), "USD"))
        Assert.assertEquals(vm.priceChangeVisibility.value, false)

        vm.originalPrice.onNext(Money(BigDecimal(1000.00), "USD"))
        vm.newPrice.onNext(Money(BigDecimal(990.00), "USD"))
        Assert.assertEquals(vm.priceChangeVisibility.value, true)

        vm.originalPrice.onNext(Money(BigDecimal(1000.00), "USD"))
        vm.newPrice.onNext(Money(BigDecimal(989.00), "USD"))
        Assert.assertEquals(vm.priceChangeVisibility.value, true)

        vm.originalPrice.onNext(Money(BigDecimal(1000.00), "USD"))
        vm.newPrice.onNext(Money(BigDecimal(1001.00), "USD"))
        Assert.assertEquals(vm.priceChangeVisibility.value, true)

        vm.originalPrice.onNext(Money(BigDecimal(1000.00), "USD"))
        vm.newPrice.onNext(Money(BigDecimal(1000.00), "USD"))
        Assert.assertEquals(vm.priceChangeVisibility.value, false)
    }
}