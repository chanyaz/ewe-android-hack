package com.expedia.bookings.test.tests.unit;

import com.expedia.bookings.data.FlightFilter;
import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA.
 * User: brad
 * Date: 7/24/12
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlightFilterTest extends TestCase {

	public void testNewFlightFilterDefaultsSortToPrice() {
		FlightFilter flightFilter = new FlightFilter();
		assertEquals(FlightFilter.Sort.PRICE, flightFilter.getSort());
	}

}
