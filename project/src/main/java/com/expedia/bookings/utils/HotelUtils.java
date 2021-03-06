package com.expedia.bookings.utils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelRate;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.ViewUtils;
import com.squareup.phrase.Phrase;

public class HotelUtils {

	public static boolean checkPhoneFinishConditionsAndFinish(Activity activity) {
		if (Db.getHotelSearch().getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			activity.finish();
			return true;
		}

		return false;
	}


	/**
	 * Tries to return the best "room" picture, but falls back to property
	 * images/thumbnails if none exists.  May return null if all fails.
	 */
	public static HotelMedia getRoomMedia(TripBucketItemHotel hotel) {
		Rate rate = hotel.getRate();
		Rate oldRate = hotel.getOldRate();
		Property property = hotel.getProperty();
		if (rate != null && rate.getThumbnail() != null) {
			return rate.getThumbnail();
		}

		if (oldRate != null && oldRate.getThumbnail() != null) {
			return oldRate.getThumbnail();
		}

		if (property != null) {
			if (property.getMediaCount() > 0) {
				return property.getMedia(0);
			}
			else {
				return property.getThumbnail();
			}
		}

		return null;
	}

	public static void loadHotelOffersAsSearchResponse(HotelOffersResponse offersResponse) {
		Property property = offersResponse.getProperty();
		property.setIsFromSearchByHotel(true);
		HotelSearchResponse searchResponse = new HotelSearchResponse();

		List<Rate> rates = offersResponse.getRates();
		if (property != null && rates != null) {
			Rate lowestRate = null;
			for (Rate rate : rates) {
				Money temp = rate.getDisplayPrice();
				if (lowestRate == null) {
					lowestRate = rate;
				}
				else if (lowestRate.getDisplayPrice().getAmount().compareTo(temp.getAmount()) > 0) {
					lowestRate = rate;
				}
			}
			property.setLowestRate(lowestRate);
		}

		searchResponse.addProperty(property);

		Db.getHotelSearch().setSearchResponse(searchResponse);
		Db.getHotelSearch().updateFrom(offersResponse);
	}

	public static void setupActionBarHotelNameAndRating(Activity activity, Property property) {
		ViewGroup actionBarView = Ui.inflate(activity, R.layout.actionbar_hotel_name_with_stars, null);

		TextView titleView = Ui.findView(actionBarView, R.id.title);
		titleView.setText(property.getName());

		RatingBar ratingBar;
		if (PointOfSale.getPointOfSale().shouldShowCircleForRatings()) {
			ratingBar = Ui.findView(actionBarView, R.id.rating_circles);
		}
		else {
			ratingBar = Ui.findView(actionBarView, R.id.rating_stars);
		}
		ratingBar.setRating((float) property.getHotelRating());
		ratingBar.setVisibility(View.VISIBLE);

		activity.getActionBar().setCustomView(actionBarView);
	}

	/**
	 * Sets up the "checkmark" action bar item
	 */
	public static Button setupActionBarCheckmark(final Activity activity, final MenuItem menuItem,
			boolean enabled) {
		Button tv = Ui.inflate(activity, R.layout.actionbar_checkmark_item, null);
		ViewUtils.setAllCaps(tv);

		if (enabled) {
			tv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.onOptionsItemSelected(menuItem);
				}
			});
		}
		else {
			tv.setClickable(false);
			tv.setFocusable(false);
			tv.setTextColor(activity.getResources().getColor(R.color.actionbar_text_disabled));
			tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_bar_checkmark_disabled, 0, 0, 0);
		}

		menuItem.setActionView(tv);

		return tv;
	}

	/**
	 * Returns a string meant to be displayed along with a room description, describing the terms on
	 * which the room can be cancelled.
	 * @param context
	 * @param rate
	 * @return
	 */
	public static CharSequence getRoomCancellationText(Context context, final Rate rate) {
		DateTime window = rate.getFreeCancellationWindowDate();
		if (window != null) {
			CharSequence formattedDate = JodaUtils.formatDateTime(context, window, DateUtils.FORMAT_SHOW_TIME
				| DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
			String formattedString = context.getString(R.string.free_cancellation_date_TEMPLATE, formattedDate);
			return Html.fromHtml(formattedString);
		}
		else {
			return context.getString(R.string.free_cancellation);
		}
	}

	/**
	 * Returns the string meant to be displayed below the slide-to-purchase view; i.e. the final
	 * prompt displayed before the card is actually charged. We want this message to be consistent
	 * between phone and tablet.
	 * @param context
	 * @param property
	 * @param rate
	 * @param isTablet
	 * @return
	 */
	public static String getSlideToPurchaseString(Context context, Property property, Rate rate, boolean isTablet) {
		int chargeTypeMessageId = 0;

		// Determine price to be paid now
		Money sliderCharge;
		boolean isTabletPayLater = isTablet && rate.isPayLater();
		if (isTabletPayLater) {
			sliderCharge = new Money(0, rate.getTotalAmountAfterTax().getCurrency());
		}
		else if (rate.isPayLater() && !isTablet && property.isMerchant()) {
			sliderCharge = rate.getDepositAmount();
		}
		else {
			sliderCharge = rate.getTotalAmountAfterTax();
		}
		// Determine the slider message template
		if (isTabletPayLater) {
			chargeTypeMessageId = R.string.your_card_will_be_charged_template;
		}
		else if (!property.isMerchant()) {
			chargeTypeMessageId = R.string.to_be_collected_by_the_hotel_TEMPLATE;
		}
		else if (rate.getCheckoutPriceType() == Rate.CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES || rate.isPayLater()) {
			chargeTypeMessageId = R.string.amount_to_be_paid_now_TEMPLATE;
		}
		else {
			chargeTypeMessageId = R.string.your_card_will_be_charged_template;
		}
		return Phrase.from(context, chargeTypeMessageId)
			.put("dueamount", sliderCharge.getFormattedMoney()).format().toString();
	}

	// Convenience method for getting secondary resort fee banner text for phone
	public static String getPhoneResortFeeBannerText(Context context, Rate rate) {
		int stringId = rate.resortFeeInclusion() ? R.string.included_in_the_price : R.string.not_included_in_the_price;
		return context.getString(stringId);
	}

	// Convenience method for getting secondary resort fee banner text for tablet
	public static String getTabletResortFeeBannerText(Context context, Rate rate) {
		int stringId = rate.resortFeeInclusion() ? R.string.tablet_room_rate_resort_fees_included_template :
			R.string.tablet_room_rate_resort_fees_not_included_template;
		String mandatoryFees = rate.getTotalMandatoryFees().getFormattedMoney();
		return context.getString(stringId, mandatoryFees);
	}

	public static Spanned getDepositPolicyText(Context context, String[] depositPolicy) {
		String depositStatement = depositPolicy[0];
		String depositAmount = depositPolicy[1].trim();
		return Html.fromHtml(context.getString(R.string.pay_later_deposit_policy, depositStatement, depositAmount));
	}

	// Convenience method for getting resort fee text that goes at the bottom of checkout,
	// for either device type.
	public static Spanned getCheckoutResortFeesText(Context context, Rate rate) {
		String fees = rate.getTotalMandatoryFees().getFormattedMoney();
		String tripTotal = rate.getTotalPriceWithMandatoryFees().getFormattedMoney();
		int templateId;
		if (!AndroidUtils.isTablet(context) && rate.isPayLater()) {
			if (!rate.getDisplayDeposit().isZero()) {
				templateId = R.string.pay_later_deposit_resort_disclaimer_TEMPLATE;
			}
			else {
				templateId = R.string.pay_later_resort_disclaimer_TEMPLATE;
			}
		}
		else {
			templateId = R.string.resort_fee_disclaimer_TEMPLATE;
		}
		return Html.fromHtml(context.getString(templateId, fees, tripTotal));
	}

	// Convenience method for getting pay later text that goes at the bottom of checkout.
	public static Spanned getCheckoutPayLaterText(Context context, Rate rate) {
		if (rate.depositRequired()) {
			String deposit = rate.getDepositAmount().getFormattedMoney();
			return Html.fromHtml(context.getString(R.string.pay_later_deposit_disclaimer_TEMPLATE, deposit));
		}
		else {
			String tripTotal = rate.getDisplayTotalPrice().getFormattedMoney();
			return Html.fromHtml(context.getString(R.string.pay_later_disclaimer_TEMPLATE, tripTotal));
		}
	}
	/*
	 * Helper method to check if it's valid to start the hotel search.
	 */
	public static boolean dateRangeSupportsHotelSearch(Context context) {
		// TODO should we be referring to Db.getHotelSearch() or Sp.toHotelSearch() ??
		return Sp.getParams().toHotelSearchParams().getStayDuration() <= context.getResources()
			.getInteger(R.integer.calendar_max_days_hotel_stay);
	}

	// Distance formatting
	public static boolean isDistanceUnitInMiles() {
		if (Locale.getDefault().getCountry().toLowerCase(Locale.ENGLISH).equals("us")) {
			return true;
		}
		return false;
	}

	public static String formatDistanceForNearby(Resources resources, Hotel offer, boolean abbreviated) {
		boolean isMiles = isDistanceUnitInMiles();
		double distance = isMiles ? offer.proximityDistanceInMiles : offer.proximityDistanceInKiloMeters;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);

		// This skirts the pluralization problem, while also being more precise.
		// We will display "1.0 miles away" instead of "1 miles away".
		nf.setMinimumFractionDigits(abbreviated ? 0 : 1);

		int unitStrId;
		if (!isMiles) {
			unitStrId = abbreviated ? R.string.unit_kilometers : R.string.unit_kilometers_full;
		}
		else {
			unitStrId = abbreviated ? R.string.unit_miles : R.string.unit_miles_full;
		}
		int templateResId = (abbreviated) ? R.string.distance_template_short : R.string.distance_template;
		return resources.getString(templateResId, nf.format(distance), resources.getString(unitStrId));
	}

	public static float getDiscountPercent(HotelRate rate) {
		if (rate == null) {
			return 0;
		}
		return Math.abs(rate.discountPercent);
	}

	public static boolean isDiscountTenPercentOrBetter(HotelRate rate) {
		float discountPercent = getDiscountPercent(rate);
		if (discountPercent <= 100 && discountPercent >= 0) {
			return discountPercent > 9.5;
		}
		return false;
	}

	public static String formattedReviewCount(int numberOfReviews) {
		NumberFormat nf = NumberFormat.getInstance();
		return nf.format(numberOfReviews);
	}
}
