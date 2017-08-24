package com.expedia.bookings.widget.itin;

import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.Insurance;
import com.expedia.bookings.data.trips.Insurance.InsuranceLineOfBusiness;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.ItinCardDataAirAttach;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.ItinCardDataFallback;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.itin.data.ItinCardDataHotel;
import com.expedia.bookings.data.trips.ItinCardDataHotelAttach;
import com.expedia.bookings.data.trips.ItinCardDataLXAttach;
import com.expedia.bookings.data.trips.ItinCardDataRails;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.fragment.ItinConfirmRemoveDialogFragment;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.ClipboardUtils;
import com.expedia.bookings.utils.DebugInfoUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.TravelerIconUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.squareup.phrase.Phrase;

public abstract class ItinContentGenerator<T extends ItinCardData> {

	private final Context mContext;
	public final T mItinCardData;

	private boolean mDetailsSummaryHideTypeIcon = true;
	private boolean mDetailsSummaryHideTitle = true;

	private String mSharableImageURL;
	protected MediaCallback callback;

	private final UserStateManager userStateManager;

	public ItinContentGenerator(Context context, T itinCardData) {
		mContext = context;
		mItinCardData = itinCardData;

		userStateManager = Ui.getApplication(context).appComponent().userStateManager();
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

	public void setCallback(MediaCallback callback) {
		this.callback = callback;
	}

	public static ItinContentGenerator<? extends ItinCardData> createGenerator(Context context, ItinCardData itinCardData) {
		return ItinContentGenerator.createGenerator(context, itinCardData, null);
	}

	// Convenience method
	public static ItinContentGenerator<? extends ItinCardData> createGenerator(Context context, ItinCardData itinCardData, MediaCallback callback) {
		if (itinCardData instanceof ItinCardDataHotel) {
			return new HotelItinContentGenerator(context, (ItinCardDataHotel) itinCardData, callback);
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
		else if (itinCardData instanceof ItinCardDataAirAttach) {
			return new AirAttachItinContentGenerator(context, (ItinCardDataAirAttach) itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataFallback) {
			return new FallbackItinContentGenerator(context, (ItinCardDataFallback) itinCardData);
		}
		else if (itinCardData != null && itinCardData.getTripComponentType() == Type.CRUISE) {
			return new CruiseItinContentGenerator(context, itinCardData);
		}
		else if (itinCardData instanceof ItinCardDataLXAttach) {
			return new LXAttachItinContentGenerator(context, (ItinCardDataLXAttach) itinCardData);
		}
		else if (itinCardData != null && itinCardData.getTripComponentType() == Type.RAILS) {
			return new RailsItinContentGenerator(context, (ItinCardDataRails) itinCardData);
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

	public abstract List<? extends IMedia> getHeaderBitmapDrawable();

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

	public String getListCardContentDescription() {
		return Phrase.from(getContext(), R.string.header_itin_card_content_description_TEMPLATE)
			.put("itin_card_header_text", getHeaderText())
			.put("trip_start_date", getHeaderTextDate())
			.put("line_of_business", getType().toString())
			.format().toString();
	}

	// show check in link for flights only
	public String getCheckInLink() {
		if (getType() == Type.FLIGHT) {
			TripFlight tripFlight = (TripFlight) getItinCardData().getTripComponent();
			return tripFlight.getCheckInLink();
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////
	// Itin sharing

	public boolean isSharedItin() {
		return getItinCardData().getTripComponent().getParentTrip().isShared();
	}

	/**
	 * This method is used to fetch the bitmap icon for imported shared itins.
	 * The icon will consist of the shared user's initials and the background color will change based on LOB (Line of Business)
	 *
	 * @return Bitmap which can be used as the itinCardIcon.
	 */
	public Bitmap getSharedItinCardIcon() {
		String name = getSharedItinName();
		// This is NOT supposed to ever happen, but in case it does let's show a default icon based on LOB
		if (TextUtils.isEmpty(name)) {
			int fallBackIcon;
			if (this instanceof FlightItinContentGenerator) {
				fallBackIcon = R.drawable.ic_itin_shared_placeholder_flights;
			}
			else if (this instanceof HotelItinContentGenerator) {
				fallBackIcon = R.drawable.ic_itin_shared_placeholder_hotel;
			}
			else {
				fallBackIcon = R.drawable.ic_itin_shared_placeholder_generic;
			}
			return BitmapFactory.decodeResource(getResources(), fallBackIcon);
		}
		else {
			return fetchIconBitmap(name);
		}
	}

	/**
	 * Use this method to fetch the shared Itin icon background color.
	 * Activities - #FF462966
	 * Car - #FF3B4266
	 * Cruise - #FF7A2D16
	 * Flight - #FF1F6699
	 * Generic - #FF373F4A
	 * Hotel - #FF3B5866
	 * Packages - #FF2E5539
	 *
	 * @return Hex color for the icon background based on LOB
	 */
	public int getSharedItinIconBackground() {
		return 0xFF373F4A;
	}

	/**
	 * Override this in respective LOB to get the appropriate user name.
	 *
	 * @return The full name of the shared user
	 */
	public String getSharedItinName() {
		return getResources().getString(R.string.sharedItin_card_fallback_name);
	}

	private Bitmap fetchIconBitmap(String displayName) {
		return TravelerIconUtils.generateInitialIcon(mContext, displayName, getSharedItinIconBackground(), true, true, 2.5f);
	}

	//////////////////////////////////////////////////////////////////////////
	// Override-able methods with default implementations

	public String getHeaderTextDate() {
		CharSequence relativeStartDate = getItinRelativeStartDate();
		if (relativeStartDate == null) {
			return "";
		}
		return relativeStartDate.toString();
	}

	public boolean hasDetails() {
		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Itin Content Image URL

	public String getSharableImageURL() {
		if (TextUtils.isEmpty(mSharableImageURL)) {
			return ProductFlavorFeatureConfiguration.getInstance().getSharableFallbackImageURL();
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
			return mDetailsSummaryHideTypeIcon;
		}
	}

	public void setHideDetailsTypeIcon(boolean hide) {
		mDetailsSummaryHideTypeIcon = hide;
	}

	public boolean getHideDetailsTitle() {
		if (isSharedItin()) {
			return false;
		}
		else {
			return mDetailsSummaryHideTitle;
		}
	}

	public void setHideDetailsTitle(boolean hide) {
		mDetailsSummaryHideTitle = hide;
	}

	//////////////////////////////////////////////////////////////////////////
	// Local notifications

	/**
	 * Extend this method to return any local notifications related to this trip component.
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
		final String actionText = getContext().getString(R.string.itin_action_call_support_cont_desc);
		return new SummaryButton(R.drawable.ic_phone, getContext().getString(R.string.itin_action_support), actionText,
			new OnClickListener() {
				@Override
				public void onClick(View v) {
					SocialUtils.call(getContext(), PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser()));
				}
			});
	}

	/**
	 * Itin cards share a lot of gui elements. They don't share layouts, but a container can be passed here and filled with said shared elements.
	 * <p/>
	 * Currently supported shared elemenets (in this order)
	 * - Confirmation Code (selectable)
	 * - Insurance
	 * - Itinerary number
	 * - Special Expedia+ Rewards support phone numbers
	 * - Booking Info (additional information link)
	 * <p/>
	 * These get added to the viewgroup only if they exist (or have fallback behavior defined)
	 */
	protected void addSharedGuiElements(ViewGroup container) {
		boolean addedConfNumber = addConfirmationNumber(container);
		boolean addedInsurance = addInsurance(container);
		boolean addedItinNumber = addItineraryNumber(container);
		boolean addedSupportNumber = addGoldOrSilverSupportNumber(container);
		boolean addedBookingInfo = addBookingInfo(container);
		boolean addedSharedoptions = addSharedOptions(container);
		Log.d("ITIN: ItinCard.addSharedGuiElements - addedConfNumber:" + addedConfNumber + " addedItinNumber:"
			+ addedItinNumber + " addedGoldOrSilverNumber:" + addedSupportNumber + " addedBookingInfo:"
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

			String itineraryLabel = Phrase.from(mContext, R.string.itinerary_TEMPLATE).put("brand",
				BuildConfig.brand).format().toString();

			View view = getClickToCopyItinDetailItem(itineraryLabel, itineraryNumber, false);
			if (view != null) {
				Log.d("ITIN: addItineraryNumber to container");
				container.addView(view);
				return true;
			}
		}
		return false;
	}

	protected boolean addGoldOrSilverSupportNumber(ViewGroup container) {
		Log.d("ITIN: addSupportNumber");
		if (isSharedItin()) {
			return false;
		}

		int labelResId = 0;
		final String supportPhoneNumber;
		final String supportEmail;
		View view = null;
		switch (userStateManager.getCurrentUserLoyaltyTier()) {
		case BASE:
			labelResId = R.string.rewards_base_tier_customer_support;
			supportPhoneNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBaseTier();
			supportEmail = null;
			break;
		case MIDDLE:
			labelResId = R.string.rewards_middle_tier_customer_support;
			supportPhoneNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberMiddleTier();
			supportEmail = PointOfSale.getPointOfSale().getSupportEmailMiddleTier();
			break;
		case TOP:
			labelResId = R.string.rewards_top_tier_customer_support;
			supportPhoneNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberTopTier();
			supportEmail = PointOfSale.getPointOfSale().getSupportEmailTopTier();
			break;
		default:
			supportPhoneNumber = null;
			supportEmail = null;
		}

		if (!TextUtils.isEmpty(supportPhoneNumber)) {
			view = getItinDetailItem(labelResId, supportPhoneNumber, false,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						SocialUtils.call(getContext(), supportPhoneNumber);
					}
				});
		}

		else if (!TextUtils.isEmpty(supportEmail)) {
			view = getItinDetailItem(labelResId, supportEmail, false,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						SocialUtils
							.email(getContext(), supportEmail, getResources().getString(R.string.support_request),
								DebugInfoUtils.generateEmailBody(mContext));
					}
				});
		}

		if (view == null) {
			return false;
		}

		Log.d("ITIN: addSupportNumber to container");
		container.addView(view);
		return true;
	}

	protected boolean addSharedOptions(ViewGroup container) {
		Log.d("ITIN: addSharedOptions");
		if (isSharedItin()) {
			View item = getLayoutInflater().inflate(R.layout.snippet_itin_detail_item_shared_options, null);
			TextView removeTextView = Ui.findView(item, R.id.remove_itin);
			AccessibilityUtil.appendRoleContDesc(removeTextView, removeTextView.getText().toString(), R.string.accessibility_cont_desc_role_button);
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
		Trip parentTrip = getItinCardData().getTripComponent().getParentTrip();
		String itinKey = parentTrip.getItineraryKey();
		ItinConfirmRemoveDialogFragment df = ItinConfirmRemoveDialogFragment.getInstance(itinKey);
		df.show(fragmentManager, ItinConfirmRemoveDialogFragment.TAG);
	}

	protected View getClickToCopyItinDetailItem(int headerResId, final String text, final boolean isConfNumber) {
		return getClickToCopyItinDetailItem(getResources().getString(headerResId), text, isConfNumber);
	}

	protected View getClickToCopyItinDetailItem(String label, final String text, final boolean isConfNumber) {

		return getItinDetailItem(label, text, isConfNumber, new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardUtils.setText(getContext(), text);
				Toast.makeText(getContext(), R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
				if (isConfNumber && getItinCardData().getTripComponent().getType() == Type.FLIGHT) {
					OmnitureTracking.trackItinFlightCopyPNR();
				}
			}
		});
	}

	protected View getItinDetailItem(int headerResId, final String text, final boolean isConfNumber, OnClickListener onClickListener) {
		return getItinDetailItem(getResources().getString(headerResId), text, isConfNumber, onClickListener);
	}

	protected View getItinDetailItem(String label, final String text, final boolean isConfNumber,
		OnClickListener onClickListener) {
		View item = getLayoutInflater().inflate(R.layout.snippet_itin_detail_item_generic, null);
		TextView headingTv = Ui.findView(item, R.id.item_label);
		TextView textTv = Ui.findView(item, R.id.item_text);
		View copyToClipboardContentDescription = Ui.findView(item, R.id.copy_to_clipboard_content_description);

		copyToClipboardContentDescription.setVisibility(AccessibilityUtil.isTalkBackEnabled(getContext())
				? View.VISIBLE : View.GONE);
		headingTv.setText(label);
		textTv.setText(text);

		item.setOnClickListener(onClickListener);
		return item;
	}

	protected View setItinDetailItemText(int headerResId, final String text) {
		View item = getLayoutInflater().inflate(R.layout.snippet_itin_detail_item_generic, null);
		TextView headingTv = Ui.findView(item, R.id.item_label);
		TextView textTv = Ui.findView(item, R.id.item_text);

		headingTv.setText(getResources().getString(headerResId));
		textTv.setText(text);
		return item;
	}

	/**
	 * @param container
	 * @return true: details exist and we can show them, inflated booking details view and populated. false: otherwise
	 */
	protected boolean addBookingInfo(ViewGroup container) {
		Log.d("ITIN: addBookingInfo");
		if (getItinCardData() != null
			&& (!TextUtils.isEmpty(getItinCardData().getDetailsUrl()) || isSharedItin())) {

			View item = getLayoutInflater().inflate(R.layout.snippet_itin_detail_item_booking_info, null);

			TextView bookingInfoTv = Ui.findView(item, R.id.booking_info);
			AccessibilityUtil.appendRoleContDesc(bookingInfoTv, bookingInfoTv.getText().toString(), R.string.accessibility_cont_desc_role_button);

			if (isSharedItin()) {
				//If shared we dont show the additional information button
				View divider = Ui.findView(item, R.id.divider);
				divider.setVisibility(View.GONE);
				bookingInfoTv.setVisibility(View.GONE);
			}
			else {
				bookingInfoTv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						getContext().startActivity(buildWebViewIntent(R.string.itin_card_details_details, getItinCardData().getDetailsUrl()).getIntent());
						OmnitureTracking.trackItinInfoClicked(getItinCardData().getTripComponent()
							.getType());
					}

				});
			}

			// Reload stuff
			TextView reloadTextView = Ui.findView(item, R.id.reload_text_view);
			reloadTextView.setText(getReloadText());
			AccessibilityUtil.appendRoleContDesc(reloadTextView, getReloadText(), R.string.accessibility_cont_desc_role_button);

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

					OmnitureTracking.trackItinReload(getType());
				}
			});

			Log.d("ITIN: addBookingInfo to container");
			container.addView(item);
			return true;
		}
		return false;
	}

	protected WebViewActivity.IntentBuilder buildWebViewIntent(@StringRes int titleResId, String url) {
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getContext());
		builder.setUrl(url);
		builder.setTitle(titleResId);
		builder.setInjectExpediaCookies(true);
		builder.setAllowMobileRedirects(false);
		return builder;
	}

	/**
	 * Add this trip's insurance to the passed in container
	 */
	protected boolean addInsurance(ViewGroup container) {
		if (!ProductFlavorFeatureConfiguration.getInstance().shouldDisplayInsuranceDetailsIfAvailableOnItinCard() ||
			!hasInsurance()) {
			return false;
		}

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
				insuranceName.setText(HtmlCompat.stripHtml(insurance.getPolicyName()));
				AccessibilityUtil.appendRoleContDesc(insuranceName, insuranceName.getText().toString(), R.string.accessibility_cont_desc_role_button);

				insuranceRow.setOnClickListener(ProductFlavorFeatureConfiguration.getInstance().getInsuranceLinkViewClickListener(mContext, insurance.getTermsUrl()));

				insuranceContainer.addView(insuranceRow);
				viewAddedCount++;
			}
		}
		container.addView(item);
		return true;
	}

	/**
	 * Does this particular card have displayable insurance info
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
	 */
	protected View getHorizontalDividerView(int margin) {
		View v = new View(this.getContext());
		v.setBackgroundColor(Ui.obtainThemeColor(getContext(), R.attr.itin_card_detail_horizontal_divider_color));
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
	 * @see #getItinRelativeTimeSpan(Context context, DateTime time, DateTime now) for relative time span
	 */
	public CharSequence getItinRelativeStartDate() {
		long time = getItinCardData().getStartDate().getMillis();
		long now = DateTime.now().getMillis();
		long duration = time - now;

		DateTime dateTime = getItinCardData().getStartDate();
		LocalDate localDate = new LocalDate(getItinCardData().getStartDate());
		LocalDate today = new LocalDate(DateTime.now());
		// We use LocalDate because we don't care about the instant in time just the Year Month and Day
		final int daysBetween = JodaUtils.daysBetween(today, localDate);

		CharSequence ret = null;

		// For cards that happened yesterday, we want "Yesterday"
		if (time < now && daysBetween == -1) {
			ret = getContext().getString(R.string.yesterday);
		}

		// For flight cards coming up in less than one hour, we want "XX Minutes"
		else if (time > now && getType().equals(Type.FLIGHT) && duration <= DateUtils.HOUR_IN_MILLIS) {
			int minutes = (int) (duration / DateUtils.MINUTE_IN_MILLIS);

			// Special case: display <1 minute as "1 minute". Defect# 758
			// TODO: consider changing to something like "less than 1 minute"
			if (minutes < 1) {
				minutes = 1;
			}

			ret = Phrase.from(getResources().getQuantityString(R.plurals.minutes_from_now, minutes))
				.put("minutes", minutes).format().toString();
		}

		// For flight cards coming up in greater than one hour but less than one day, we want "XX Hours"
		else if (time > now && getType().equals(Type.FLIGHT) && duration <= DateUtils.DAY_IN_MILLIS) {
			int hours = (int) (duration / DateUtils.HOUR_IN_MILLIS);
			ret = Phrase.from(getResources().getQuantityString(R.plurals.hours_from_now, hours))
				.put("hours", hours).format().toString();
		}

		// For cards that happen today, we want "Today"
		else if (daysBetween == 0) {
			ret = getContext().getString(R.string.Today);
		}

		// For cards coming up tomorrow, we want "Tomorrow"
		else if (time > now && daysBetween == 1) {
			ret = getContext().getString(R.string.tomorrow);
		}

		// For cards coming up greater than 24 hours but in 3 days or less want "XX Days"
		else if (time > now && daysBetween <= 3) {
			ret = Phrase
				.from(getResources().getQuantityString(R.plurals.days_from_now, daysBetween))
				.put("days", daysBetween).format().toString();
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

	/**
	 * Returns a relative time span according to very specific formatting:
	 * 1. If less than one minute ago, or less than one minute in the future, "in 1 minute"
	 * 2. If between 1 and 24 hours, "in 2 hours and 27 minutes" or "in 5 hours"
	 * 3. Otherwise, we'll use JodaUtils.getRelativeTimeSpanString, "in 5 days" or "in 35 minutes"
	 *
	 * @see #getItinRelativeStartDate() for relative date
	 */
	public CharSequence getItinRelativeTimeSpan(Context context, DateTime time, DateTime now) {
		boolean past = now.isAfter(time);
		Duration duration = past ? new Duration(time, now) : new Duration(now, time);

		int hours = (int) duration.getStandardHours();
		int minutes = (int) duration.getStandardMinutes() % 60;

		// Special case: if the time is between 1 and 24 hours (and minutes != 0)
		// we want to show both Hours and Minutes, which isn't supported by getRelativeTimeSpanString()
		if (hours < 24 && hours >= 1 && minutes != 0) {
			int templateResId = past ? R.string.hours_minutes_past_TEMPLATE
				: R.string.hours_minutes_future_TEMPLATE;
			Resources res = context.getResources();
			String hourStr = Phrase.from(res.getQuantityString(R.plurals.hours_from_now, hours))
				.put("hours", hours).format().toString();
			String minStr = Phrase.from(res.getQuantityString(R.plurals.minutes_from_now, minutes))
				.put("minutes", minutes).format().toString();
			return res.getString(templateResId, hourStr, minStr);
		}

		// Special case: if the time is in less than 1 minute (or less than one minute ago),
		// display it as "in 1 minute" to avoid showing '0 minutes' strings. Defect #758, #2157
		// TODO: consider changing to something like "in less than 1 minute"
		if (hours == 0 && minutes == 0) {
			Resources res = context.getResources();
			int resId = past ? R.plurals.joda_time_android_num_minutes_ago : R.plurals.joda_time_android_in_num_minutes;
			return res.getQuantityString(resId, 1, 1);
		}

		// 1871: Due to the screwed up way DateUtils.getNumberOfDaysPassed() works, this ends up such that
		// the millis must be in the system locale (and hopefully the user has not changed their locale recently)
		return JodaUtils.getRelativeTimeSpanString(context, time, now, DateUtils.MINUTE_IN_MILLIS, 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Facebook Sharing

	public String getFacebookShareName() {
		return getHeaderText();
	}

	public interface MediaCallback {
		void onMediaReady(List<? extends IMedia> media);
	}
}
