package com.expedia.bookings.widget.itin;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.trips.Insurance;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.ItinCardDataFallback;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.Insurance.InsuranceLineOfBusiness;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.widget.LinearLayout;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public abstract class ItinContentGenerator<T extends ItinCardData> {

	private Context mContext;
	private T mItinCardData;

	public ItinContentGenerator(Context context, T itinCardData) {
		mContext = context;
		mItinCardData = itinCardData;
	}

	public T getItinCardData() {
		return mItinCardData;
	}

	protected Context getContext() {
		return mContext;
	}

	protected Resources getResources() {
		return mContext.getResources();
	}

	protected LayoutInflater getLayoutInflater() {
		return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	// Convenience method
	public static ItinContentGenerator<? extends ItinCardData> createGenerator(Context context,
			ItinCardData itinCardData) {
		if (itinCardData instanceof ItinCardDataHotel) {
			return new HotelItinContentGenerator(context, (ItinCardDataHotel) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataFlight) {
			return new FlightItinContentGenerator(context, (ItinCardDataFlight) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataActivity) {
			return new ActivityItinContentGenerator(context, (ItinCardDataActivity) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataCar) {
			return new CarItinContentGenerator(context, (ItinCardDataCar) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataFallback) {
			return new FallbackItinContentGenerator(context, (ItinCardDataFallback) itinCardData);
		}
		else if (itinCardData != null && itinCardData.getTripComponentType() == Type.CRUISE) {
			return new CruiseItinContentGenerator(context, itinCardData);
		}

		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Abstract methods to implement

	// Type

	public abstract int getTypeIconResId();

	public abstract Type getType();

	// Title share button

	public abstract String getShareSubject();

	public abstract String getShareTextShort();

	public abstract String getShareTextLong();

	// Header image

	public abstract int getHeaderImagePlaceholderResId();

	public abstract String getHeaderImageUrl();

	public abstract String getHeaderText();

	// Views

	public abstract View getTitleView(ViewGroup container);

	public abstract View getSummaryView(ViewGroup container);

	public abstract View getDetailsView(ViewGroup container);

	// Action buttons

	public abstract SummaryButton getSummaryLeftButton();

	public abstract SummaryButton getSummaryRightButton();

	//////////////////////////////////////////////////////////////////////////
	// Override-able methods with default implementations

	public String getHeaderTextWithDate() {
		return getContext().getString(R.string.Title_Date_TEMPLATE, getHeaderText(), getRelativeStartDate());
	}

	public boolean hasDetails() {
		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Shared generation methods

	/**
	 * Itin cards share a lot of gui elements. They don't share layouts, but a container can be passed here and filled with said shared elements.
	 * 
	 * Currently supported shared elemenets (in this order)
	 * - Confirmation Code (selectable)
	 * - Itinerary number
	 * - Booking Info (additional information link)
	 * - Insurance
	 * 
	 * These get added to the viewgroup only if they exist (or have fallback behavior defined)
	 * @param container
	 * @param infalter
	 */
	protected void addSharedGuiElements(ViewGroup container) {
		boolean addedConfNumber = addConfirmationNumber(container);
		boolean addedItinNumber = addItineraryNumber(container);
		boolean addedBookingInfo = addBookingInfo(container);
		boolean addedInsurance = addInsurance(container);
		Log.d("ITIN: ItinCard.addSharedGuiElements - addedConfNumber:" + addedConfNumber + " addedItinNumber:"
				+ addedItinNumber + " addedBookingInfo:" + addedBookingInfo + " addedInsurance:" + addedInsurance);
	}

	protected boolean addConfirmationNumber(ViewGroup container) {
		Log.d("ITIN: addConfirmationNumber");
		if (hasConfirmationNumber()) {
			String confirmationText = ((ConfirmationNumberable) this.getItinCardData())
					.getFormattedConfirmationNumbers();
			View view = getClickToCopyItinDetailItem(R.string.confirmation_code_label, confirmationText, true);
			if (view != null) {
				Log.d("ITIN: addConfirmationNumber to container");
				container.addView(view);
				return true;
			}
		}
		return false;
	}

	protected boolean addItineraryNumber(ViewGroup container) {
		Log.d("ITIN: addItineraryNumber");
		if (hasItinNumber()) {
			String itineraryNumber = this.getItinCardData().getTripComponent().getParentTrip().getTripNumber();
			View view = getClickToCopyItinDetailItem(R.string.itinerary_number, itineraryNumber, false);
			if (view != null) {
				Log.d("ITIN: addItineraryNumber to container");
				container.addView(view);
				return true;
			}
		}
		return false;
	}

	//helper
	private View getClickToCopyItinDetailItem(int headerResId, final String text,
			final boolean isConfNumber) {
		View item = getLayoutInflater().inflate(R.layout.snippet_itin_detail_item_generic, null);
		TextView headingTv = Ui.findView(item, R.id.item_label);
		TextView textTv = Ui.findView(item, R.id.item_text);

		headingTv.setText(headerResId);
		textTv.setText(text);

		item.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardUtils.setText(getContext(), text);
				Toast.makeText(getContext(), R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
				if (isConfNumber && getItinCardData().getTripComponent().getType() == Type.FLIGHT) {
					OmnitureTracking.trackItinFlightCopyPNR(getContext());
				}
			}
		});
		return item;
	}

	protected boolean addBookingInfo(ViewGroup container) {
		Log.d("ITIN: addBookingInfo");
		if (this.getItinCardData() != null && !TextUtils.isEmpty(this.getItinCardData().getDetailsUrl())) {
			View item = getLayoutInflater().inflate(R.layout.snippet_itin_detail_item_booking_info, null);
			TextView bookingInfoTv = Ui.findView(item, R.id.booking_info);
			bookingInfoTv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getContext());
					builder.setUrl(getItinCardData().getDetailsUrl());
					builder.setTitle(R.string.booking_info);
					builder.setTheme(R.style.FlightTheme);
					getContext().startActivity(builder.getIntent());

					OmnitureTracking.trackItinInfoClicked(getContext(), getItinCardData().getTripComponent().getType());
				}

			});
			Log.d("ITIN: addBookingInfo to container");
			container.addView(item);
			return true;
		}
		return false;
	}

	/**
	 * Add this trip's insurance to the passed in container
	 * @param inflater
	 * @param insuranceContainer
	 */
	protected boolean addInsurance(ViewGroup container) {
		if (hasInsurance()) {
			View item = getLayoutInflater().inflate(R.layout.snippet_itin_detail_item_insurance, null);
			ViewGroup insuranceContainer = Ui.findView(item, R.id.insurance_container);

			int divPadding = getResources().getDimensionPixelSize(R.dimen.itin_flight_segment_divider_padding);
			int viewAddedCount = 0;
			List<Insurance> insuranceList = this.getItinCardData().getTripComponent().getParentTrip()
					.getTripInsurance();

			for (final Insurance insurance : insuranceList) {
				//Air insurance should only be added for flights, other types should be added to all itin card details
				if (!insurance.getLineOfBusiness().equals(InsuranceLineOfBusiness.AIR) || getType().equals(Type.FLIGHT)) {
					if (viewAddedCount > 0) {
						insuranceContainer.addView(getHorizontalDividerView(divPadding));
					}
					View insuranceRow = getLayoutInflater().inflate(R.layout.snippet_itin_insurance_row, null);
					TextView insuranceName = Ui.findView(insuranceRow, R.id.insurance_name);
					insuranceName.setText(Html.fromHtml(insurance.getPolicyName()).toString());

					View insuranceLinkView = Ui.findView(insuranceRow, R.id.insurance_button);
					insuranceLinkView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getContext());
							builder.setUrl(insurance.getTermsUrl());
							builder.setTheme(R.style.FlightTheme);
							builder.setTitle(R.string.insurance);
							getContext().startActivity(builder.getIntent());
						}
					});

					insuranceContainer.addView(insuranceRow);
					viewAddedCount++;
				}
			}
			container.addView(item);
			return true;
		}
		return false;
	}

	/**
	 * Does this particular card have displayable insurance info
	 * @return
	 */
	protected boolean hasInsurance() {
		boolean hasInsurance = false;
		if (this.getItinCardData() != null && this.getItinCardData().getTripComponent() != null
				&& this.getItinCardData().getTripComponent().getParentTrip() != null) {

			List<Insurance> insuranceList = this.getItinCardData().getTripComponent().getParentTrip()
					.getTripInsurance();
			if (insuranceList != null && insuranceList.size() > 0) {
				for (int i = 0; i < insuranceList.size(); i++) {
					Insurance ins = insuranceList.get(i);
					if (ins.getLineOfBusiness().equals(InsuranceLineOfBusiness.AIR) && getType().equals(Type.FLIGHT)) {
						hasInsurance = true;
					}
					else if (!ins.getLineOfBusiness().equals(InsuranceLineOfBusiness.AIR)) {
						hasInsurance = true;
					}
				}
			}
		}
		return hasInsurance;
	}

	protected boolean hasItinNumber() {
		boolean hasItinNum = false;
		if (this.getItinCardData() != null && this.getItinCardData().getTripComponent() != null
				&& this.getItinCardData().getTripComponent().getParentTrip() != null) {
			hasItinNum = !TextUtils.isEmpty(this.getItinCardData().getTripComponent().getParentTrip().getTripNumber());
		}
		return hasItinNum;
	}

	protected boolean hasConfirmationNumber() {
		if (this.getItinCardData() != null && this.getItinCardData() instanceof ConfirmationNumberable) {
			return ((ConfirmationNumberable) this.getItinCardData()).hasConfirmationNumber();
		}
		return false;
	}

	/**
	 * Get a horizontal divider view with the itin divider color 
	 * @return
	 */
	protected View getHorizontalDividerView(int margin) {
		View v = new View(this.getContext());
		v.setBackgroundColor(getResources().getColor(R.color.itin_divider_color));
		int divHeight = getResources().getDimensionPixelSize(R.dimen.one_px_hdpi_two_px_xhdpi);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, divHeight);
		lp.leftMargin = margin;
		lp.rightMargin = margin;
		lp.topMargin = margin;
		lp.bottomMargin = margin;
		v.setLayoutParams(lp);
		return v;
	}

	/**
	 * Returns a descriptive CharSequence of the start date relative to today.
	 * (Examples: "Today" or "Tomorrow" or "May 15" or "10/25/2022")
	 * @param context
	 * @return
	 */
	public CharSequence getRelativeStartDate() {
		long time = this.getItinCardData().getStartDate().getMillisFromEpoch();
		long now = System.currentTimeMillis();
		long duration = Math.abs(now - time);

		CharSequence ret;
		if (DateUtils.isToday(time)) {
			ret = this.getContext().getText(R.string.today);
		}
		else if (duration < DateUtils.WEEK_IN_MILLIS) {
			ret = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.DAY_IN_MILLIS, 0);
		}
		else {
			ret = DateUtils.getRelativeTimeSpanString(this.getContext(), time, false);
		}

		ret = ret.subSequence(0, 1).toString().toUpperCase(Locale.getDefault()) + ret.subSequence(1, ret.length());
		return ret;
	}

}
