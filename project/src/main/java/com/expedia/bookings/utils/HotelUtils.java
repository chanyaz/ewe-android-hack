package com.expedia.bookings.utils;

import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.mobiata.android.util.ViewUtils;

public class HotelUtils {

	/**
	 * Tries to return the best "room" picture, but falls back to property
	 * images/thumbnails if none exists.  May return null if all fails.
	 */
	public static Media getRoomMedia(Property property, Rate rate) {
		if (rate != null && rate.getThumbnail() != null) {
			return rate.getThumbnail();
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

	/**
	 * Sets up the "checkmark" action bar item
	 */
	public static Button setupActionBarCheckmark(final FragmentActivity activity, final MenuItem menuItem,
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
	 * @return
	 */
	public static String getSlideToPurchaseString(Context context, Property property, Rate rate) {
		int chargeTypeMessageId = 0;
		if (!property.isMerchant()) {
			chargeTypeMessageId = R.string.collected_by_the_hotel_TEMPLATE;
		}
		else if (rate.getCheckoutPriceType() == Rate.CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES) {
			chargeTypeMessageId = R.string.Amount_to_be_paid_now_TEMPLATE;
		}
		else {
			chargeTypeMessageId = R.string.your_card_will_be_charged_TEMPLATE;
		}
		return context.getString(chargeTypeMessageId, rate.getTotalAmountAfterTax().getFormattedMoney());
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

	// Convenience method for getting resort fee text that goes at the bottom of checkout,
	// for either device type.
	public static Spanned getCheckoutResortFeesText(Context context, Rate rate) {
		String fees = rate.getTotalMandatoryFees().getFormattedMoney();
		String grandTotal = rate.getTotalPriceWithMandatoryFees().getFormattedMoney();
		return Html.fromHtml(context.getString(R.string.resort_fee_disclaimer_TEMPLATE, fees, grandTotal));
	}
}
