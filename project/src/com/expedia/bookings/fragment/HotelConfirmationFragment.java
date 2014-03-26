package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.bitmaps.UrlBitmapDrawable;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SamsungWalletResponse;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable.CornerMode;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.SamsungWalletUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.CalendarAPIUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.util.ViewUtils;

public class HotelConfirmationFragment extends ConfirmationFragment {

	public static final String TAG = HotelConfirmationFragment.class.getName();

	private static final int[] CARD_GRADIENT_COLORS = new int[] { 0, 206 << 24, 255 << 24 };

	private static final float[] CARD_GRADIENT_POSITIONS = new float[] { 0f, .82f, 1f };

	private static final String SAMSUNG_WALLET_DOWNLOAD_KEY = "SAMSUNG_WALLET_DOWNLOAD_KEY";

	private ViewGroup mHotelCard;
	private View mSamsungDivider;
	private TextView mSamsungWalletButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// This can be invoked when the parent activity finishes itself (when it detects missing data, in the case of
		// a background kill). In this case, lets just return a null view because it won't be used anyway. Prevents NPE.
		if (getActivity().isFinishing()) {
			return null;
		}

		View v = super.onCreateView(inflater, container, savedInstanceState);

		Property property = Db.getTripBucket().getHotel().getBookingResponse().getProperty();
		Ui.setText(v, R.id.hotel_name_text_view, property.getName());

		// Construct the hotel card
		ImageView hotelImageView = Ui.findView(v, R.id.hotel_image_view);
		HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
		headerBitmapDrawable.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
		headerBitmapDrawable.setCornerMode(CornerMode.ALL);
		headerBitmapDrawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.itin_card_corner_radius));
		headerBitmapDrawable.setOverlayDrawable(getResources().getDrawable(R.drawable.card_top_lighting));
		hotelImageView.setImageDrawable(headerBitmapDrawable);

		Rate selectedRate = Db.getTripBucket().getHotel().getRate();
		Media media = HotelUtils.getRoomMedia(property, selectedRate);
		if (media != null) {
			headerBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(getResources(), media.getHighResUrls(),
					R.drawable.bg_itin_placeholder));
		}
		else {
			headerBitmapDrawable
					.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_itin_placeholder));
		}

		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		int numGuests = params.getNumAdults() + params.getNumChildren();
		String guests = getResources().getQuantityString(R.plurals.number_of_guests, numGuests, numGuests);
		String duration = CalendarUtils.formatDateRange2(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_ABBREV_MONTH);
		Ui.setText(v, R.id.stay_summary_text_view, getString(R.string.stay_summary_TEMPLATE, guests, duration));

		// Setup a dropping animation with the hotel card.  Only animate on versions of Android
		// that will allow us to make the animation nice and smooth.
		mHotelCard = Ui.findView(v, R.id.hotel_card);
		if (savedInstanceState == null && Build.VERSION.SDK_INT >= 14) {
			mHotelCard.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					mHotelCard.getViewTreeObserver().removeOnPreDrawListener(this);
					animateHotelCard();
					return false;
				}
			});
		}

		Ui.findView(v, R.id.action_container).setBackgroundResource(R.drawable.bg_confirmation_mask_hotels);

		PointOfSale pos = PointOfSale.getPointOfSale();
		// 1373: Need to hide cross sell until we can fix the poor search results
		// TODO: 1370: When you enable this, please make sure to disable flights cross sell if its a VSC build. i.e. use ExpediaBookingApp.IS_VSC to check
		//if (pos.showHotelCrossSell() && pos.supportsFlights()) {
		if (false) {
			ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.get_there_text_view));

			String city = property.getLocation().getCity();
			Ui.setText(v, R.id.flights_action_text_view, getString(R.string.flights_to_TEMPLATE, city));
			Ui.setOnClickListener(v, R.id.flights_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					searchForFlights();
				}
			});
		}
		else {
			Ui.findView(v, R.id.get_there_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.get_there_text_divider).setVisibility(View.GONE);
			Ui.findView(v, R.id.flights_action_text_view).setVisibility(View.GONE);

			// #1391: If there are no additional actions above, then only show "actions" instead of "more actions"
			Ui.setText(v, R.id.more_actions_text_view, R.string.actions);
		}

		ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.more_actions_text_view));

		//Remove share and add to calendar
		if(ExpediaBookingApp.IS_TRAVELOCITY) {
			Ui.findView(v, R.id.share_action_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.calendar_divider).setVisibility(View.GONE);
			Ui.findView(v, R.id.calendar_action_text_view).setVisibility(View.GONE);
			Ui.findView(v, R.id.calendar_divider).setVisibility(View.GONE);
		}
		else {
			Ui.setOnClickListener(v, R.id.share_action_text_view, new OnClickListener() {
				@Override
				public void onClick(View v) {
					share();
				}
			});
			if (CalendarAPIUtils.deviceSupportsCalendarAPI(getActivity())) {
				Ui.setOnClickListener(v, R.id.calendar_action_text_view, new OnClickListener() {
					@Override
					public void onClick(View v) {
						addToCalendar();
					}
				});
			}
			else {
				Ui.findView(v, R.id.calendar_action_text_view).setVisibility(View.GONE);
				Ui.findView(v, R.id.calendar_divider).setVisibility(View.GONE);
			}
		}

		mSamsungDivider = Ui.findView(v, R.id.samsung_divider);
		mSamsungWalletButton = Ui.findView(v, R.id.samsung_wallet_action_text_view);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (SamsungWalletUtils.isSamsungWalletAvailable(getActivity())) {
			Log.d("SamsungWallet: onResume samsung wallet was found");
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (bd.isDownloading(SAMSUNG_WALLET_DOWNLOAD_KEY)) {
				Log.d("SamsungWallet: is available, resuming download");
				bd.registerDownloadCallback(SAMSUNG_WALLET_DOWNLOAD_KEY, mWalletCallback);
			}
			else if (!TextUtils.isEmpty(Db.getSamsungWalletTicketId())) {
				Log.d("SamsungWallet: is available, already have ticketId=" + Db.getSamsungWalletTicketId());
				handleSamsungWalletTicketId(Db.getSamsungWalletTicketId());
			}
			else {
				Log.d("SamsungWallet: is available, starting download");
				bd.startDownload(SAMSUNG_WALLET_DOWNLOAD_KEY, mWalletDownload, mWalletCallback);
			}
		}
		else if (SamsungWalletUtils.isSamsungAvailable(getActivity())) {
			Log.d("SamsungWallet: onResume show Download Samsung Wallet button");
			showDownloadSamsungWalletButton();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity() != null && getActivity().isFinishing()) {
			bd.cancelDownload(SAMSUNG_WALLET_DOWNLOAD_KEY);
		}
		else {
			bd.unregisterDownloadCallback(SAMSUNG_WALLET_DOWNLOAD_KEY, mWalletCallback);
		}

		if (getActivity() != null && getActivity().isFinishing()) {
			Db.setSamsungWalletTicketId(null);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// ConfirmationFragment

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_hotel_confirmation;
	}

	@Override
	protected int getActionsLayoutId() {
		return R.layout.include_confirmation_actions_hotels;
	}

	@Override
	protected String getItinNumber() {
		return Db.getTripBucket().getHotel().getBookingResponse().getItineraryId();
	}

	//////////////////////////////////////////////////////////////////////////
	// Animation

	private void animateHotelCard() {
		// Animate the card from -height to 0
		mHotelCard.setTranslationY(-mHotelCard.getHeight());

		ViewPropertyAnimator animator = mHotelCard.animate();
		animator.translationY(0);
		animator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
		animator.setInterpolator(new OvershootInterpolator());

		if (Build.VERSION.SDK_INT >= 16) {
			animator.withLayer();
		}
		else {
			mHotelCard.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			animator.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mHotelCard.setLayerType(View.LAYER_TYPE_NONE, null);
				}
			});
		}

		animator.start();
	}

	//////////////////////////////////////////////////////////////////////////
	// Cross-sell

	private void searchForFlights() {
		// Load the search params
		FlightSearchParams flightSearchParams = Db.getFlightSearch().getSearchParams();
		flightSearchParams.reset();

		Property property = Db.getTripBucket().getHotel().getBookingResponse().getProperty();

		Location loc = new Location();
		loc.setDestinationId(property.getLocation().toLongFormattedString());
		flightSearchParams.setArrivalLocation(loc);

		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		flightSearchParams.setDepartureDate(params.getCheckInDate());
		flightSearchParams.setReturnDate(params.getCheckOutDate());

		// Go to flights
		NavUtils.goToFlights(getActivity(), true);

		OmnitureTracking.trackHotelConfirmationFlightsXSell(getActivity());
	}

	//////////////////////////////////////////////////////////////////////////
	// Share booking

	private void share() {
		Context context = getActivity();

		HotelSearchParams searchParams = Db.getTripBucket().getHotel().getHotelSearchParams();
		Property property = Db.getTripBucket().getHotel().getBookingResponse().getProperty();

		ShareUtils socialUtils = new ShareUtils(context);
		LocalDate checkIn = searchParams.getCheckInDate();
		LocalDate checkOut = searchParams.getCheckOutDate();
		String address = StrUtils.formatAddress(property.getLocation());
		String phone = Db.getTripBucket().getHotel().getBookingResponse().getPhoneNumber();

		//In this screen isShared & travelerName would not be relevant. So just set to false and null and pass it on to ShareUtils.
		String subject = socialUtils.getHotelShareSubject(property.getLocation().getCity(), checkIn, checkOut, false,
				null);
		String body = socialUtils.getHotelShareTextLong(property.getName(), address, phone, checkIn, checkOut, null,
				false, null);

		SocialUtils.email(context, subject, body);

		OmnitureTracking.trackHotelConfirmationShareEmail(getActivity());
	}

	//////////////////////////////////////////////////////////////////////////
	// Add to Calendar

	private void addToCalendar() {
		// Go in reverse order, so that "check in" is shown to the user first
		startActivity(generateHotelCalendarIntent(false));
		startActivity(generateHotelCalendarIntent(true));

		OmnitureTracking.trackHotelConfirmationAddToCalendar(getActivity());
	}

	private Intent generateHotelCalendarIntent(boolean checkIn) {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		HotelBookingResponse bookingResponse = Db.getTripBucket().getHotel().getBookingResponse();
		Property property = bookingResponse.getProperty();
		LocalDate date = checkIn ? hotel.getHotelSearchParams().getCheckInDate() :
			hotel.getHotelSearchParams().getCheckOutDate();
		String confNumber = bookingResponse.getHotelConfNumber();
		String itinNumber = bookingResponse.getItineraryId();

		return AddToCalendarUtils.generateHotelAddToCalendarIntent(getActivity(), property, date, checkIn, confNumber,
				itinNumber);
	}

	//////////////////////////////////////////////////////////////////////////
	// Add to Samsung Wallet

	private final Download<SamsungWalletResponse> mWalletDownload = new Download<SamsungWalletResponse>() {
		@Override
		public SamsungWalletResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			return services.getSamsungWalletTicketId(getItinNumber());

			// TEST: SamsungWalletResponse response = new SamsungWalletResponse();
			// TEST: response.setTicketId("TEST");
			// TEST: return response;
		}
	};

	private final OnDownloadComplete<SamsungWalletResponse> mWalletCallback = new OnDownloadComplete<SamsungWalletResponse>() {
		@Override
		public void onDownload(SamsungWalletResponse response) {
			if (response != null && response.isSuccess()) {
				Db.setSamsungWalletTicketId(response.getTicketId());
				handleSamsungWalletTicketId(Db.getSamsungWalletTicketId());
			}
		}
	};

	private final View.OnClickListener mSamsungWalletClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int result = (Integer) v.getTag();
			Context context = getActivity();
			if (context == null) {
				return;
			}

			String ticketId = Db.getSamsungWalletTicketId();
			Intent intent = null;
			if (result == SamsungWalletUtils.RESULT_TICKET_EXISTS) {
				Log.d("SamsungWallet: Starting view ticket activity");
				OmnitureTracking.trackSamsungWalletViewClicked(getActivity());
				intent = SamsungWalletUtils.viewTicketIntent(context, ticketId);
			}
			else if (result == SamsungWalletUtils.RESULT_TICKET_NOT_FOUND) {
				Log.d("SamsungWallet: Starting download ticket activity");
				OmnitureTracking.trackSamsungWalletLoadClicked(getActivity());
				intent = SamsungWalletUtils.downloadTicketIntent(context, ticketId);
			}

			if (intent != null) {
				startActivity(intent);
			}
		}
	};

	private final View.OnClickListener mDownloadSamsungWalletClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			OmnitureTracking.trackSamsungWalletDownloadClicked(getActivity());
			SocialUtils.openSite(getActivity(), SamsungWalletUtils.SAMSUNG_WALLET_DOWNLOAD_URL);
		}
	};

	private void handleSamsungWalletTicketId(String ticketId) {
		// We have the samsung ticketId, now we need to check against the wallet
		SamsungWalletUtils.Callback callback = new SamsungWalletUtils.Callback() {
			@Override
			public void onResult(int result) {
				Log.d("SamsungWallet: Got result from checkTicket: " + result);
				handleSamsungWalletResult(result);
			}
		};

		SamsungWalletUtils.checkTicket(getActivity(), callback, ticketId);
	}

	private void handleSamsungWalletResult(int result) {
		Log.d("SamsungWallet: Handle samsung wallet result: " + result);
		// Ready to let the user click the button
		if (result == SamsungWalletUtils.RESULT_TICKET_EXISTS ||
				result == SamsungWalletUtils.RESULT_TICKET_NOT_FOUND) {

			setSamsungWalletVisibility(View.VISIBLE);
			mSamsungWalletButton.setTag(result);
			mSamsungWalletButton.setOnClickListener(mSamsungWalletClickListener);

			if (result == SamsungWalletUtils.RESULT_TICKET_EXISTS) {
				OmnitureTracking.trackSamsungWalletViewShown(getActivity());
				mSamsungWalletButton.setText(R.string.view_in_samsung_wallet);
			}
			else if (result == SamsungWalletUtils.RESULT_TICKET_NOT_FOUND) {
				OmnitureTracking.trackSamsungWalletLoadShown(getActivity());
				mSamsungWalletButton.setText(R.string.load_to_samsung_wallet);
			}
		}
	}

	private void showDownloadSamsungWalletButton() {
		OmnitureTracking.trackSamsungWalletDownloadShown(getActivity());
		setSamsungWalletVisibility(View.VISIBLE);
		mSamsungWalletButton.setText(getString(R.string.download_samsung_wallet));
		mSamsungWalletButton.setOnClickListener(mDownloadSamsungWalletClickListener);
	}

	private void setSamsungWalletVisibility(int visibility) {
		mSamsungDivider.setVisibility(visibility);
		mSamsungWalletButton.setVisibility(visibility);
	}

}
