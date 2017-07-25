package com.expedia.bookings.test.robolectric;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CarLocation;
import com.expedia.bookings.data.cars.CarType;
import com.expedia.bookings.data.cars.CarVendor;
import com.expedia.bookings.data.cars.CreateTripCarFare;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.SearchCarFare;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.widget.CarCheckoutSummaryWidget;
import com.expedia.bookings.widget.TextView;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class CarCheckoutSummaryWidgetTest {

	private CarCreateTripResponse carCreateTripResponse;

	@Before
	public void before() {
		CreateTripCarOffer carProduct = new CreateTripCarOffer();

		CarLocation pickup = new CarLocation();
		pickup.airportInstructions = "Shuttle to counter and car";
		pickup.locationDescription = "San Francisco (SFO)";

		CarVendor vendor = new CarVendor();
		vendor.name = "Fox";

		CarInfo carInfo = new CarInfo();
		carInfo.category = CarCategory.COMPACT;
		carInfo.type = CarType.CONVERTIBLE;
		carInfo.makes = Arrays.asList("Toyota Yaris");
		carInfo.carCategoryDisplayLabel = "Category Display";

		CreateTripCarFare fare = new CreateTripCarFare();
		Money totalFare = new Money("50", "USD");
		fare.grandTotal = totalFare;

		carProduct.pickUpLocation = pickup;
		carProduct.vendor = vendor;
		carProduct.vehicleInfo = carInfo;
		carProduct.detailedFare = fare;
		carProduct.hasFreeCancellation = true;
		carProduct.hasUnlimitedMileage = false;
		carProduct.setPickupTime(DateTime.now());
		carProduct.setDropOffTime(DateTime.now().plusHours(2));

		carCreateTripResponse = new CarCreateTripResponse();
		carCreateTripResponse.carProduct = carProduct;
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY })
	public void testCheckoutSummaryViews() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		CarCheckoutSummaryWidget checkoutSummaryWidget = (CarCheckoutSummaryWidget) LayoutInflater.from(activity)
			.inflate(R.layout.car_checkout_summary_widget, null);

		checkoutSummaryWidget.bind(carCreateTripResponse.carProduct, "");

		TextView carCompany = (TextView) checkoutSummaryWidget.findViewById(R.id.car_vendor_text);
		TextView categoryTitle = (TextView) checkoutSummaryWidget.findViewById(R.id.category_title_text);
		TextView carModel = (TextView) checkoutSummaryWidget.findViewById(R.id.car_model_text);
		TextView locationDescription = (TextView) checkoutSummaryWidget.findViewById(R.id.location_description_text);
		TextView airportText = (TextView) checkoutSummaryWidget.findViewById(R.id.airport_text);
		TextView dateTime = (TextView) checkoutSummaryWidget.findViewById(R.id.date_time_text);
		TextView freeCancellation = (TextView) checkoutSummaryWidget.findViewById(R.id.ticked_info_text_1);
		TextView unlimitedMileage = (TextView) checkoutSummaryWidget.findViewById(R.id.ticked_info_text_2);
		TextView tripTotal = (TextView) checkoutSummaryWidget.findViewById(R.id.price_text);
		ViewGroup priceChangeContainer = (ViewGroup) checkoutSummaryWidget.findViewById(R.id.price_change_container);

		assertEquals("Fox", carCompany.getText());
		assertEquals("Category Display", categoryTitle.getText());
		String expectedCarModel = activity.getResources().getString(R.string.car_model_name_template, "Toyota Yaris");
		assertEquals(expectedCarModel, carModel.getText());
		assertEquals("San Francisco (SFO)", airportText.getText());
		assertEquals("Shuttle to counter and car", locationDescription.getText());
		assertEquals(View.VISIBLE, freeCancellation.getVisibility());
		assertEquals("Free cancellation", freeCancellation.getText());
		assertEquals(View.GONE, unlimitedMileage.getVisibility());
		assertEquals("$50", tripTotal.getText());
		assertEquals("$50. Cost summary information button.", tripTotal.getContentDescription());
		assertEquals(View.GONE, priceChangeContainer.getVisibility());
		String expedtedDate = DateFormatUtils
			.formatStartEndDateTimeRange(activity, carCreateTripResponse.carProduct.getPickupTime(),
				carCreateTripResponse.carProduct.getDropOffTime(), false);
		assertEquals(expedtedDate, dateTime.getText());
	}

	@Test
	public void testCheckoutSummaryWithPriceChange() {
		ApiError error = new ApiError(ApiError.Code.PRICE_CHANGE);
		carCreateTripResponse.errors = Arrays.asList(error);
		SearchCarOffer searchCarOffer = new SearchCarOffer();
		SearchCarFare searchFare = new SearchCarFare();
		Money money = new Money();
		money.formattedPrice = "8";
		searchFare.total = money;
		searchCarOffer.fare = searchFare;
		carCreateTripResponse.searchCarOffer = searchCarOffer;

		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		CarCheckoutSummaryWidget checkoutSummaryWidget = (CarCheckoutSummaryWidget) LayoutInflater.from(activity)
			.inflate(R.layout.car_checkout_summary_widget, null);

		checkoutSummaryWidget.bind(carCreateTripResponse.carProduct,
			carCreateTripResponse.searchCarOffer.fare.total.formattedPrice);

		ViewGroup priceChangeContainer = (ViewGroup) checkoutSummaryWidget.findViewById(R.id.price_change_container);
		TextView priceChange = (TextView) checkoutSummaryWidget.findViewById(R.id.price_change_text);
		assertEquals(View.VISIBLE, priceChangeContainer.getVisibility());
		String expectedPriceChangeMessage = activity.getResources().getString(R.string.price_changed_from_TEMPLATE,
			carCreateTripResponse.searchCarOffer.fare.total.formattedPrice);
		assertEquals(expectedPriceChangeMessage, priceChange.getText());
	}
}
