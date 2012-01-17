package com.expedia.bookings.utils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.widget.RoomTypeWidget;
import com.mobiata.android.ImageCache;
import com.mobiata.android.util.SettingUtils;

public class BookingReceiptUtils {

	public static void configureTicket(Activity activity, View receipt, Property property, SearchParams searchParams,
			Rate rate, RoomTypeWidget roomTypeWidget, BookingResponse bookingResponse, BillingInfo billingInfo) {
		// Configure the booking summary at the top of the page
		ImageView thumbnailView = (ImageView) receipt.findViewById(R.id.thumbnail_image_view);
		if (property.getThumbnail() != null) {
			ImageCache.loadImage(property.getThumbnail().getUrl(), thumbnailView);
		}
		else {
			thumbnailView.setVisibility(View.GONE);
		}

		TextView nameView = (TextView) receipt.findViewById(R.id.name_text_view);
		nameView.setText(property.getName());

		Location location = property.getLocation();
		TextView address1View = (TextView) receipt.findViewById(R.id.address1_text_view);
		address1View.setText(Html.fromHtml(StrUtils.formatAddressStreet(location)));
		TextView address2View = (TextView) receipt.findViewById(R.id.address2_text_view);
		address2View.setText(Html.fromHtml(StrUtils.formatAddressCity(location)));

		// Configure the details
		ViewGroup detailsLayout = (ViewGroup) receipt.findViewById(R.id.details_layout);
		if (billingInfo != null && bookingResponse != null) {
			BookingReceiptUtils.addDetail(activity, detailsLayout, R.string.confirmation_number,
					bookingResponse.getConfNumber());
			BookingReceiptUtils.addDetail(activity, detailsLayout, R.string.itinerary_number,
					bookingResponse.getItineraryId());
			BookingReceiptUtils.addDetail(activity, detailsLayout, R.string.confirmation_email, billingInfo.getEmail());
		}

		detailsLayout.addView(roomTypeWidget.getView());

		addRateDetails(activity.getApplicationContext(), detailsLayout,
				searchParams, property, rate, roomTypeWidget);

		// Configure the total cost and (if necessary) total cost paid to Expedia
		Money displayedTotal;
		ViewGroup totalPaidView = (ViewGroup) receipt.findViewById(R.id.below_total_details_layout);
		if (BookingReceiptUtils.shouldDisplayMandatoryFees(activity)) {
			totalPaidView.setVisibility(View.VISIBLE);
			BookingReceiptUtils.addDetail(activity, totalPaidView, R.string.PayToExpedia, rate.getTotalAmountAfterTax()
					.getFormattedMoney());
			displayedTotal = rate.getTotalPriceWithMandatoryFees();
		}
		else {
			totalPaidView.setVisibility(View.GONE);
			displayedTotal = rate.getTotalAmountAfterTax();
		}
		TextView totalView = (TextView) receipt.findViewById(R.id.total_cost_text_view);
		if (displayedTotal != null && displayedTotal.getFormattedMoney() != null
				&& displayedTotal.getFormattedMoney().length() > 0) {
			totalView.setText(displayedTotal.getFormattedMoney());
		}
		else {
			totalView.setText("Dan didn't account for no total info, tell him");
		}
	}

	public static void configureTicket(Activity activity, View receipt, Property property, SearchParams searchParams,
			Rate rate, RoomTypeWidget roomTypeWidget) {
		configureTicket(activity, receipt, property, searchParams, rate, roomTypeWidget, null, null);
	}

	public static void addRateDetails(Context context, ViewGroup detailsLayout, SearchParams searchParams,
			Property property, Rate rate, RoomTypeWidget roomTypeWidget) {
		View bedTypeRow = BookingReceiptUtils.addDetail(context, detailsLayout, R.string.bed_type,
				rate.getRatePlanName());

		if (roomTypeWidget != null) {
			roomTypeWidget.addClickableView(bedTypeRow);
		}

		BookingReceiptUtils.addDetail(context, detailsLayout, R.string.GuestsLabel,
				StrUtils.formatGuests(context, searchParams));

		String start = BookingReceiptUtils.formatCheckInOutDate(context, searchParams.getCheckInDate());
		String end = BookingReceiptUtils.formatCheckInOutDate(context, searchParams.getCheckOutDate());
		String timeLoader = "--:--";
		int numDays = searchParams.getStayDuration();
		BookingReceiptUtils.addDetail(context, detailsLayout, context.getString(R.string.CheckIn),
				context.getString(R.string.check_in_out_time_template, timeLoader, start), R.id.check_in_time);
		BookingReceiptUtils.addDetail(context, detailsLayout, context.getString(R.string.CheckOut),
				context.getString(R.string.check_in_out_time_template, timeLoader, end), R.id.check_out_time);
		BookingReceiptUtils.addDetail(context, detailsLayout, R.string.stay_duration, context.getResources()
				.getQuantityString(R.plurals.length_of_stay, numDays, numDays));
		BookingReceiptUtils.addSpace(context, detailsLayout, 8);

		// If there's a breakdown list, show that; otherwise, show the nightly mRate
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		if (rate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dateFormat.format(date);
				Money amount = breakdown.getAmount();
				if (amount.getAmount() == 0) {
					BookingReceiptUtils.addDetail(context, detailsLayout,
							context.getString(R.string.room_rate_template, dateStr), context.getString(R.string.free));
				}
				else {
					BookingReceiptUtils.addDetail(context, detailsLayout, context.getString(
							R.string.room_rate_template, dateStr), breakdown.getAmount().getFormattedMoney());
				}
			}
		}
		else if (rate.getDailyAmountBeforeTax() != null) {
			BookingReceiptUtils.addDetail(context, detailsLayout, R.string.RatePerRoomPerNight, rate
					.getDailyAmountBeforeTax().getFormattedMoney());
		}

		Money totalSurcharge = rate.getTotalSurcharge();
		Money extraGuestFee = rate.getExtraGuestFee();
		Money totalMandatoryFees = rate.getTotalMandatoryFees();

		if (extraGuestFee != null) {
			BookingReceiptUtils.addDetail(context, detailsLayout, R.string.extra_guest_charge,
					extraGuestFee.getFormattedMoney());
		}
		if (totalSurcharge != null) {
			BookingReceiptUtils.addDetail(context, detailsLayout, R.string.TaxesAndFees,
					totalSurcharge.getFormattedMoney());
		}

		if (totalMandatoryFees != null && BookingReceiptUtils.shouldDisplayMandatoryFees(context)) {
			BookingReceiptUtils.addDetail(context, detailsLayout, R.string.MandatoryFees,
					totalMandatoryFees.getFormattedMoney());
		}
	}

	public static View addDetail(Context context, ViewGroup parent, int labelStrId, CharSequence value) {
		return BookingReceiptUtils.addDetail(context, parent, context.getString(labelStrId), value, -1);
	}

	public static View addDetail(Context context, ViewGroup parent, CharSequence label, CharSequence value) {
		return BookingReceiptUtils.addDetail(context, parent, label, value, -1);
	}

	public static View addDetail(Context context, ViewGroup parent, CharSequence label, CharSequence value, int valueId) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View detailRow = inflater.inflate(R.layout.snippet_booking_detail, parent, false);
		TextView labelView = (TextView) detailRow.findViewById(R.id.label_text_view);
		labelView.setText(label);
		TextView valueView = (TextView) detailRow.findViewById(R.id.value_text_view);
		valueView.setText(value);
		if (valueId != -1) {
			valueView.setId(valueId);
		}
		parent.addView(detailRow);

		return detailRow;
	}

	public static String formatCheckInOutDate(Context context, Calendar cal) {
		DateFormat medDf = android.text.format.DateFormat.getMediumDateFormat(context);
		medDf.setTimeZone(CalendarUtils.getFormatTimeZone());
		return DateUtils.getDayOfWeekString(cal.get(Calendar.DAY_OF_WEEK), DateUtils.LENGTH_MEDIUM) + ", "
				+ medDf.format(cal.getTime());
	}

	public static void addSpace(Context context, ViewGroup parent, int spaceInDp) {
		int height = (int) context.getResources().getDisplayMetrics().density * spaceInDp;
		View v = new View(context);
		v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, height));
		parent.addView(v);
	}

	// Mandatory fees should only be displayed in IT and DE
	private static boolean shouldDisplayMandatoryFees(Context context) {
		String pos = SettingUtils.get(context, context.getString(R.string.PointOfSaleKey), null);
		if (pos == null) {
			return false;
		}
		return pos.equals(context.getString(R.string.point_of_sale_it))
				|| pos.equals(context.getString(R.string.point_of_sale_de));
	}

}
