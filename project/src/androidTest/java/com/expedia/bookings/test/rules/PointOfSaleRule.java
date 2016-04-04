package com.expedia.bookings.test.rules;

import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;

import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;

public class PointOfSaleRule implements TestRule {
	@Override
	public Statement apply(final Statement base, org.junit.runner.Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				PointOfSaleId[] pointsOfSale = new PointOfSaleId[] {
					PointOfSaleId.ARGENTINA,
					PointOfSaleId.AUSTRALIA,
					PointOfSaleId.AUSTRIA,
					PointOfSaleId.BELGIUM,
					PointOfSaleId.BRAZIL,
					PointOfSaleId.CANADA,
					PointOfSaleId.DENMARK,
					PointOfSaleId.FRANCE,
					PointOfSaleId.GERMANY,
					PointOfSaleId.HONG_KONG,
					PointOfSaleId.INDIA,
					PointOfSaleId.INDONESIA,
					PointOfSaleId.IRELAND,
					PointOfSaleId.ITALY,
					PointOfSaleId.JAPAN,
					PointOfSaleId.MALAYSIA,
					PointOfSaleId.MEXICO,
					PointOfSaleId.NETHERLANDS,
					PointOfSaleId.NEW_ZEALND,
					PointOfSaleId.NORWAY,
					PointOfSaleId.PHILIPPINES,
					PointOfSaleId.SINGAPORE,
					PointOfSaleId.SOUTH_KOREA,
					PointOfSaleId.SPAIN,
					PointOfSaleId.SWEDEN,
					PointOfSaleId.SWITZERLAND,
					PointOfSaleId.TAIWAN,
					PointOfSaleId.THAILAND,
					PointOfSaleId.UNITED_KINGDOM,
					PointOfSaleId.UNITED_STATES,
					PointOfSaleId.VIETNAM,
				};

				for (PointOfSaleId pos : pointsOfSale) {
					Common.setPOS(pos);
					beforeTest();
					base.evaluate();
				}

				Common.setPOS(PointOfSaleId.UNITED_STATES);
			}
		};
	}

	public PointOfSaleId get() {
		return PointOfSale.getPointOfSale().getPointOfSaleId();
	}

	// Called after the point of sale is set but before the test is run
	protected void beforeTest() {
		// ignore
	}
}
