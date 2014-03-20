package com.expedia.bookings.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ExpediaImage;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable.CornerMode;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AddToCalendarUtils;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.ShareUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.Ui;

public class TabletFlightConfirmationFragment extends TabletConfirmationFragment {

	public static final String TAG = TabletFlightConfirmationFragment.class.getName();

	private static final int[] CARD_GRADIENT_COLORS = new int[] { 0, 206 << 24, 255 << 24 };

	private static final float[] CARD_GRADIENT_POSITIONS = new float[] { 0f, .82f, 1f };

	private static final String DESTINATION_IMAGE_INFO_DOWNLOAD_KEY = "DESTINATION_IMAGE_INFO_DOWNLOAD_KEY";

	private ViewGroup mFlightCard;
	private TextView mConfirmationTitleText;
	private TextView mShareButtonText;

	private ImageView mDestinationImageView;
	private HeaderBitmapDrawable mHeaderBitmapDrawable;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		mConfirmationTitleText = Ui.findView(v, R.id.confirmation_title_text);
		mConfirmationTitleText.setText(R.string.tablet_confirmation_flight_booked);

		mShareButtonText = Ui.findView(v, R.id.share_action_text_view);
		mShareButtonText.setText(R.string.tablet_confirmation_share_flight);

		// Construct the destination card
		mDestinationImageView = Ui.findView(v, R.id.hotel_image_view);
		mHeaderBitmapDrawable = new HeaderBitmapDrawable();
		mHeaderBitmapDrawable.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
		mHeaderBitmapDrawable.setCornerMode(CornerMode.ALL);
		mHeaderBitmapDrawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.itin_card_corner_radius));
		mHeaderBitmapDrawable.setOverlayDrawable(getResources().getDrawable(R.drawable.card_top_lighting));
		mDestinationImageView.setImageDrawable(mHeaderBitmapDrawable);

		mHeaderBitmapDrawable.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_itin_placeholder));

		ViewTreeObserver vto = mDestinationImageView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				startDestinationImageDownload();
			}
		});

		return v;
	}

	private void startDestinationImageDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY)) {
			bd.startDownload(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY, mDestinationImageInfoDownload,
					mDestinationImageInfoDownloadCallback);
		}
	}

	private Download<ExpediaImage> mDestinationImageInfoDownload = new Download<ExpediaImage>() {
		@Override
		public ExpediaImage doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY, services);
			String code = Db.getFlightSearch().getSearchParams().getArrivalLocation().getDestinationId();
			return ExpediaImageManager.getInstance().getDestinationImage(code,
					mDestinationImageView.getMeasuredWidth(), mDestinationImageView.getMeasuredHeight(), true);
		}
	};

	private OnDownloadComplete<ExpediaImage> mDestinationImageInfoDownloadCallback = new OnDownloadComplete<ExpediaImage>() {
		@Override
		public void onDownload(ExpediaImage image) {
			if (image != null) {
				mHeaderBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(getResources(), image.getUrl(),
						R.drawable.bg_itin_placeholder));
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// TabletConfirmationFragment

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_tablet_hotel_confirmation;
	}

	@Override
	protected int getActionsLayoutId() {
		return R.layout.include_tablet_confirmation_actions_hotels;
	}

	@Override
	protected String getItinNumber() {
		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getSelectedFlightTrip();
		Itinerary itinerary = Db.getItinerary(trip.getItineraryNumber());
		return itinerary.getItineraryNumber();
	}

	@Override
	protected String getConfirmationSummaryText() {
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		int numGuests = params.getNumAdults() + params.getNumChildren();
		String duration = CalendarUtils.formatDateRange(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_ABBREV_MONTH);
		String fromTo = getString(R.string.tablet_confirmation_flights_from_to,
				params.getDepartureLocation().getCity(), params.getArrivalLocation().getCity());
		return getString(R.string.tablet_confirmation_summary, fromTo, duration, numGuests);
	}

	@Override
	protected void shareItinerary() {
		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getSelectedFlightTrip();

		int travelerCount = Db.getTravelers() == null ? 1 : Db.getTravelers().size();
		ShareUtils shareUtils = new ShareUtils(getActivity());
		String subject = shareUtils.getFlightShareSubject(trip, travelerCount);
		String body = shareUtils.getFlightShareEmail(trip, Db.getTravelers());

		SocialUtils.email(getActivity(), subject, body);

		OmnitureTracking.trackFlightConfirmationShareEmail(getActivity());
	}

	@Override
	protected void addItineraryToCalendar() {
		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
		for (int a = 0; a < trip.getLegCount(); a++) {
			Intent intent = generateCalendarInsertIntent(trip.getLeg(a));
			startActivity(intent);
		}

		OmnitureTracking.trackFlightConfirmationAddToCalendar(getActivity());
	}

	@SuppressLint("NewApi")
	private Intent generateCalendarInsertIntent(FlightLeg leg) {
		PointOfSale pointOfSale = PointOfSale.getPointOfSale();
		String itineraryNumber = Db.getFlightSearch().getSelectedFlightTrip().getItineraryNumber();
		return AddToCalendarUtils.generateFlightAddToCalendarIntent(getActivity(), pointOfSale, itineraryNumber, leg);
	}

	@Override
	protected LineOfBusiness getNextBookingItem() {
		if (Db.getTripBucket().getHotel() != null
				&& Db.getTripBucket().getHotel().getState() == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
			return LineOfBusiness.HOTELS;
		}
		else {
			return null;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY)) {
			bd.registerDownloadCallback(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY, mDestinationImageInfoDownloadCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			bd.cancelDownload(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY);
		}
		else {
			bd.unregisterDownloadCallback(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY);
		}
	}
}
