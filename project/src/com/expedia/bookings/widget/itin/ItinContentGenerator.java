package com.expedia.bookings.widget.itin;

import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.Insurance;
import com.expedia.bookings.data.trips.Insurance.InsuranceLineOfBusiness;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.ItinCardDataFallback;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.ItinCardDataHotelAttach;
import com.expedia.bookings.data.trips.ItinCardDataLocalExpert;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.fragment.ConfirmItinRemoveDialogFragment;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.widget.LinearLayout;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.Ui;

public abstract class ItinContentGenerator<T extends ItinCardData> {

	private Context mContext;
	private T mItinCardData;

	private boolean mDetialsSummaryHideTypeIcon = true;
	private boolean mDetailsSummaryHideTitle = true;

	private String mSharableImageURL;

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
		return LayoutInflater.from(mContext);
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
		else if (itinCardData instanceof ItinCardDataHotelAttach) {
			return new HotelAttachItinContentGenerator(context, (ItinCardDataHotelAttach) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataLocalExpert) {
			return new LocalExpertItinContentGenerator(context, (ItinCardDataLocalExpert) itinCardData);
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

	/**
	 * @return a UrlBitmapDrawable to display, or null if we want to use the placeholder
	 */
	public abstract UrlBitmapDrawable getHeaderBitmapDrawable(int width, int height);

	public abstract String getHeaderText();

	public abstract String getReloadText();

	// Views

	public abstract View getTitleView(View convertView, ViewGroup container);

	public abstract View getSummaryView(View convertView, ViewGroup container);

	public abstract View getDetailsView(View convertView, ViewGroup container);

	// Action buttons

	public abstract SummaryButton getSummaryLeftButton();

	public abstract SummaryButton getSummaryRightButton();

	// Add to calendar

	public abstract List<Intent> getAddToCalendarIntents();

	// Itin sharing
	public boolean isSharedItin() {
		return getItinCardData().getTripComponent().getParentTrip().isShared();
	}

	//////////////////////////////////////////////////////////////////////////
	// Override-able methods with default implementations

	public String getHeaderTextDate() {
		CharSequence relativeStartDate = getRelativeStartDate();
		if (relativeStartDate == null) {
			return "";
		}
		return getContext().getString(R.string.Title_Date_TEMPLATE, "", relativeStartDate);
	}

	public boolean hasDetails() {
		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Itin Content Image URL

	public String getSharableImageURL() {
		if (mSharableImageURL == null) {
			// TODO: Check for the desired default image to be used while sharing. Using this temporarily
			return "http://media.expedia.com/media/content/shared/images/navigation/expedia.com.png";
		}
		else {
			return mSharableImageURL;
		}
	}

	public void setSharableImageURL(String imageURL) {
		this.mSharableImageURL = imageURL;
	}

	//////////////////////////////////////////////////////////////////////////
	// Detail Settings

	public boolean getHideDetailsTypeIcon() {
		if (isSharedItin()) {
			return false;
		}
		else {
			return mDetialsSummaryHideTypeIcon;
		}
	}

	public void setHideDetailsTypeIcon(boolean hide) {
		mDetialsSummaryHideTypeIcon = hide;
	}

	public boolean getHideDetailsTitle() {
		return mDetailsSummaryHideTitle;
	}

	public void setHideDetailsTitle(boolean hide) {
		mDetailsSummaryHideTitle = hide;
	}

	//////////////////////////////////////////////////////////////////////////
	// Local notifications

	/**
	 * Extend this method to return any local notifications related to this trip component.
	 * @return
	 */
	public List<Notification> generateNotifications() {
		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Shared generation methods

	/**
	 * Backup, if we don't have data to give to a button.
	 */
	protected SummaryButton getSupportSummaryButton() {
		return new SummaryButton(R.drawable.ic_phone, getContext().getString(R.string.itin_action_support),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						SocialUtils.call(getContext(), PointOfSale.getPointOfSale().getSupportPhoneNumber(mContext));
					}
				});
	}

	/**
	 * Itin cards share a lot of gui elements. They don't share layouts, but a container can be passed here and filled with said shared elements.
	 *
	 * Currently supported shared elemenets (in this order)
	 * - Confirmation Code (selectable)
	 * - Itinerary number
	 * - Elite Plus support phone number
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
		boolean addedElitePlusNumber = addElitePlusNumber(container);
		boolean addedBookingInfo = addBookingInfo(container);
		boolean addedInsurance = addInsurance(container);
		boolean addedSharedoptions = addSharedOptions(container);
		Log.d("ITIN: ItinCard.addSharedGuiElements - addedConfNumber:" + addedConfNumber + " addedItinNumber:"
				+ addedItinNumber + " addedElitePlusNumber:" + addedElitePlusNumber + " addedBookingInfo:"
				+ addedBookingInfo + " addedInsurance:" + addedInsurance + " addedSharedoptions:" + addedSharedoptions);
	}

	protected boolean addConfirmationNumber(ViewGroup container) {
		Log.d("ITIN: addConfirmationNumber");
		if (hasConfirmationNumber() && !isSharedItin()) {
			String confirmationText = ((ConfirmationNumberable) this.getItinCardData())
					.getFormattedConfirmationNumbers();
			int labelResId = ((ConfirmationNumberable) this.getItinCardData()).getConfirmationNumberLabelResId();
			View view = getClickToCopyItinDetailItem(labelResId, confirmationText, true);
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
		if (hasItinNumber() && !isSharedItin()) {
			String itineraryNumber = this.getItinCardData().getTripComponent().getParentTrip().getTripNumber();
			View view = getClickToCopyItinDetailItem(R.string.expedia_itinerary, itineraryNumber, false);
			if (view != null) {
				Log.d("ITIN: addItineraryNumber to container");
				container.addView(view);
				return true;
			}
		}
		return false;
	}

	protected boolean addElitePlusNumber(ViewGroup container) {
		Log.d("ITIN: addElitePlusNumber");
		if (hasElitePlusNumber()) {
			final String elitePlusNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberGold();
			View view = getItinDetailItem(R.string.elite_plus_customer_support, elitePlusNumber, false,
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							SocialUtils.call(getContext(), elitePlusNumber);
						}
					});
			if (view != null) {
				Log.d("ITIN: addElitePlusNumber to container");
				container.addView(view);
				return true;
			}
		}

		return false;
	}

	protected boolean addSharedOptions(ViewGroup container) {
		Log.d("ITIN: addSharedOptions");
		if (isSharedItin()) {
			View item = getLayoutInflater().inflate(R.layout.snippet_itin_detail_item_shared_options, null);
			TextView removeTextView = Ui.findView(item, R.id.remove_itin);
			removeTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showRemoveDialog();
				}
			});
			Log.d("ITIN: addSharedOptions to container");
			container.addView(item);
			return true;
		}
		return false;
	}

	private void showRemoveDialog() {
		final FragmentActivity activity = (FragmentActivity) getContext();
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		ConfirmItinRemoveDialogFragment df = ConfirmItinRemoveDialogFragment.getInstance(getItinCardData()
				.getTripComponent().getParentTrip().getTripNumber());
		df.show(fragmentManager, ConfirmItinRemoveDialogFragment.TAG);
	}

	protected View getClickToCopyItinDetailItem(int headerResId, final String text, final boolean isConfNumber) {
		return getClickToCopyItinDetailItem(getResources().getString(headerResId), text, isConfNumber);
	}

	protected View getClickToCopyItinDetailItem(String label, final String text,
			final boolean isConfNumber) {

		return getItinDetailItem(label, text, isConfNumber, new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardUtils.setText(getContext(), text);
				Toast.makeText(getContext(), R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
				if (isConfNumber && getItinCardData().getTripComponent().getType() == Type.FLIGHT) {
					OmnitureTracking.trackItinFlightCopyPNR(getContext());
				}
			}
		});
	}

	protected View getItinDetailItem(int headerResId, final String text, final boolean isConfNumber,
			OnClickListener onClickListener) {
		return getItinDetailItem(getResources().getString(headerResId), text, isConfNumber, onClickListener);
	}

	protected View getItinDetailItem(String label, final String text, final boolean isConfNumber,
			OnClickListener onClickListener) {
		View item = getLayoutInflater().inflate(R.layout.snippet_itin_detail_item_generic, null);
		TextView headingTv = Ui.findView(item, R.id.item_label);
		TextView textTv = Ui.findView(item, R.id.item_text);

		headingTv.setText(label);
		textTv.setText(text);

		item.setOnClickListener(onClickListener);
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
					builder.setTitle(R.string.itin_card_details_details);
					builder.setTheme(R.style.ItineraryTheme);
					builder.setInjectExpediaCookies(true);
					builder.setAllowMobileRedirects(false);
					getContext().startActivity(builder.getIntent());

					OmnitureTracking.trackItinInfoClicked(getContext(), getItinCardData().getTripComponent().getType());
				}

			});

			// Reload stuff
			TextView reloadTextView = Ui.findView(item, R.id.reload_text_view);
			reloadTextView.setText(getReloadText());

			final ProgressBar progressBar = Ui.findView(item, R.id.itin_details_progress_bar);
			final ImageView reloadImageView = Ui.findView(item, R.id.itin_details_reload_image_view);

			RelativeLayout rl = Ui.findView(item, R.id.itin_details_deep_refresh_container);
			rl.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Trip trip = getItinCardData().getTripComponent().getParentTrip();

					if (ItineraryManager.getInstance().deepRefreshTrip(trip)) {
						// Toggle ProgressBar, mimics GMail refresh behavior
						progressBar.setVisibility(View.VISIBLE);
						reloadImageView.setVisibility(View.INVISIBLE);
					}

					OmnitureTracking.trackItinReload(getContext(), getType());
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
							builder.setTheme(R.style.ItineraryTheme);
							builder.setTitle(R.string.insurance);
							builder.setAllowMobileRedirects(false);
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

	protected boolean hasElitePlusNumber() {
		boolean hasElitePlusNum = false;
		if (User.isLoggedIn(mContext) && Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null
				&& Db.getUser().getPrimaryTraveler().getIsElitePlusMember()) {
			hasElitePlusNum = !TextUtils.isEmpty(PointOfSale.getPointOfSale().getSupportPhoneNumberGold());
		}
		return hasElitePlusNum;
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
	 * (Examples: "Today" or "May 15" or "10/25/2022")
	 * Rules defined here: https://mingle.karmalab.net/projects/eb_ad_app/cards/234
	 * @param context
	 * @return
	 */
	private CharSequence getRelativeStartDate() {
		DateTime dateTime = getItinCardData().getStartDate();
		DateTime today = DateTime.now();
		long time = dateTime.getMillis();
		long now = today.getMillis();
		long duration = time - now;
		int daysBetween = JodaUtils.daysBetween(today.withZone(dateTime.getZone()), dateTime);

		CharSequence ret = null;

		// For cards that happened yesterday, we want "Yesterday"
		if (time < now && daysBetween == -1) {
			ret = getContext().getString(R.string.yesterday);
		}

		// For flight cards coming up in less than one hour, we want "XX Minutes"
		else if (time > now && getType().equals(Type.FLIGHT) && duration <= DateUtils.HOUR_IN_MILLIS) {
			// Explicitly adding a minute in milliseconds to avoid showing '0 minutes' strings. Defect# 758
			int minutes = (int) ((duration + DateUtils.MINUTE_IN_MILLIS - 1) / DateUtils.MINUTE_IN_MILLIS);
			ret = getResources().getQuantityString(R.plurals.minutes_from_now, minutes, minutes);
		}

		// For flight cards coming up in greater than one hour but less than one day, we want "XX Hours"
		else if (time > now && getType().equals(Type.FLIGHT) && duration <= DateUtils.DAY_IN_MILLIS) {
			int hours = (int) (duration / DateUtils.HOUR_IN_MILLIS);
			ret = getResources().getQuantityString(R.plurals.hours_from_now, hours, hours);
		}

		// For flight cards that happen today, we want "Today"
		else if (daysBetween == 0) {
			ret = getContext().getString(R.string.Today);
		}

		// For cards coming up tomorrow, we want "Tomorrow"
		else if (time > now && daysBetween == 1) {
			ret = getContext().getString(R.string.tomorrow);
		}

		// For cards coming up greater than 24 hours but in 3 days or less want "XX Days"
		else if (time > now && daysBetween <= 3) {
			ret = getResources().getQuantityString(R.plurals.days_from_now, daysBetween, daysBetween);
		}

		// Fall back to the date, we want "MMM d" ("Mar 15")
		else {
			int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH;
			ret = JodaUtils.formatDateTime(getContext(), dateTime, flags);
		}

		// Capitalize the first letter
		if (ret != null) {
			ret = ret.subSequence(0, 1).toString().toUpperCase(Locale.getDefault()) + ret.subSequence(1, ret.length());
		}

		return ret;
	}

	//////////////////////////////////////////////////////////////////////////
	// Facebook Sharing

	public String getFacebookShareName() {
		return getHeaderText();
	}
}
