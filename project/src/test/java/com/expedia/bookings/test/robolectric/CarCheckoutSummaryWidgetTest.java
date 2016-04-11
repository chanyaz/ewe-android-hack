package com.expedia.bookings.test.robolectric;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowAlertDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CarLocation;
import com.expedia.bookings.data.cars.CarType;
import com.expedia.bookings.data.cars.CarVendor;
import com.expedia.bookings.data.cars.CreateTripCarFare;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.RateBreakdownItem;
import com.expedia.bookings.data.cars.RentalFareBreakdownType;
import com.expedia.bookings.data.cars.SearchCarFare;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.widget.CarCheckoutSummaryWidget;
import com.expedia.bookings.widget.TextView;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class CarCheckoutSummaryWidgetTest {

	private CarCreateTripResponse carCreateTripResponse;
	private CarCheckoutSummaryWidget checkoutSummaryWidget;
	private Activity activity;

	@Before
	public void before() {
		CreateTripCarOffer carProduct = new CreateTripCarOffer();

		CarLocation pickup = new CarLocation();
		pickup.airportInstructions = "Shuttle to counter and car";
		pickup.locationDescription = "San Francisco (SFO)";
		pickup.countryCode = "USA";

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
		RateBreakdownItem baseFare = new RateBreakdownItem();
		baseFare.type = RentalFareBreakdownType.CAR_RENTAL;
		baseFare.price = new Money(42, "USD");
		RateBreakdownItem dropOffCharge = new RateBreakdownItem();
		dropOffCharge.type = RentalFareBreakdownType.DROP_OFF_CHARGE;
		dropOffCharge.price = new Money(5, "USD");
		RateBreakdownItem taxesAndFees = new RateBreakdownItem();
		taxesAndFees.type = RentalFareBreakdownType.TAXES_AND_FEES;
		taxesAndFees.price = new Money(15, "USD");
		RateBreakdownItem insurance = new RateBreakdownItem();
		insurance.type = RentalFareBreakdownType.INSURANCE;
		insurance.price = new Money(20, "USD");
		fare.priceBreakdownOfTotalDueToday = Arrays.asList(baseFare, dropOffCharge, taxesAndFees, insurance);

		fare.totalDueAtPickup = baseFare.price;
		fare.totalDueToday = new Money(0, "USD");

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
	public void costSummaryAlertDialog() {
		givenWeHaveACheckoutSummaryWidget();

		checkoutSummaryWidget.showCarCostBreakdown();
		AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
		LinearLayout parentLinearLayout = (LinearLayout) latestAlertDialog.findViewById(R.id.parent);

		assertCostSummaryView(parentLinearLayout, 2, "$42", "Car rental");
		assertCostSummaryView(parentLinearLayout, 3, "$5", "Drop off charge");
		assertCostSummaryView(parentLinearLayout, 4, "$15", "Taxes & Fees");
		assertCostSummaryView(parentLinearLayout, 5, "$20", "Insurance");
	}

	@Test
	public void testCheckoutSummaryViews() {
		givenWeHaveACheckoutSummaryWidget();

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
		assertEquals("Free Cancellation", freeCancellation.getText());
		assertEquals(View.GONE, unlimitedMileage.getVisibility());
		assertEquals("$50", tripTotal.getText());
		assertEquals(View.GONE, priceChangeContainer.getVisibility());
		String expedtedDate = DateFormatUtils
			.formatCarDateTimeRange(activity, carCreateTripResponse.carProduct.getPickupTime(),
				carCreateTripResponse.carProduct.getDropOffTime());
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

	private void givenWeHaveACheckoutSummaryWidget() {
		activity = Robolectric.buildActivity(Activity.class).create().get();
		checkoutSummaryWidget = (CarCheckoutSummaryWidget) LayoutInflater.from(activity)
			.inflate(R.layout.car_checkout_summary_widget, null);

		checkoutSummaryWidget.bind(carCreateTripResponse.carProduct, "");
	}

	private void assertCostSummaryView(LinearLayout dialogParentView, int rowIndex, String expectedPrice, String expectedPriceType) {
		LinearLayout costSummaryRow = (LinearLayout) dialogParentView.getChildAt(rowIndex);
		TextView priceTextView = (TextView) costSummaryRow.findViewById(R.id.price_text_view);
		assertEquals(expectedPrice, priceTextView.getText());
		TextView priceTypeTextView = (TextView) costSummaryRow.findViewById(R.id.price_type_text_view);
		assertEquals(expectedPriceType, priceTypeTextView.getText());
	}
}
