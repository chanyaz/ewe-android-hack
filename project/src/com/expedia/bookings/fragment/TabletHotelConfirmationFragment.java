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
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SamsungWalletResponse;
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
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.util.CalendarAPIUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.util.ViewUtils;

public class TabletHotelConfirmationFragment extends TabletConfirmationFragment {

	public static final String TAG = TabletHotelConfirmationFragment.class.getName();

	private static final int[] CARD_GRADIENT_COLORS = new int[] { 0, 206 << 24, 255 << 24 };

	private static final float[] CARD_GRADIENT_POSITIONS = new float[] { 0f, .82f, 1f };

	private ViewGroup mHotelCard;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		Property property = Db.getBookingResponse().getProperty();

		// Construct the hotel card
		ImageView hotelImageView = Ui.findView(v, R.id.hotel_image_view);
		HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
		headerBitmapDrawable.setGradient(CARD_GRADIENT_COLORS, CARD_GRADIENT_POSITIONS);
		headerBitmapDrawable.setCornerMode(CornerMode.ALL);
		headerBitmapDrawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.itin_card_corner_radius));
		headerBitmapDrawable.setOverlayDrawable(getResources().getDrawable(R.drawable.card_top_lighting));
		hotelImageView.setImageDrawable(headerBitmapDrawable);
		Rate selectedRate = Db.getHotelSearch().getSelectedRate();
		Media media = HotelUtils.getRoomMedia(property, selectedRate);
		if (media != null) {
			headerBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(getResources(), media.getHighResUrls(),
					R.drawable.bg_itin_placeholder));
		}
		else {
			headerBitmapDrawable
					.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_itin_placeholder));
		}

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

		return v;
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
		return Db.getBookingResponse().getItineraryId();
	}

	@Override
	protected String getConfirmationSummaryText() {
		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		int numGuests = params.getNumAdults() + params.getNumChildren();
		String hotelName = Db.getTripBucket().getHotel().getProperty().getName();
		String duration = CalendarUtils.formatDateRange(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_ABBREV_MONTH);
		return getString(R.string.tablet_confirmation_summary, hotelName, duration, numGuests);
	}

	@Override
	protected void shareItinerary() {
		Context context = getActivity();

		HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
		Property property = Db.getBookingResponse().getProperty();

		ShareUtils socialUtils = new ShareUtils(context);
		LocalDate checkIn = searchParams.getCheckInDate();
		LocalDate checkOut = searchParams.getCheckOutDate();
		String address = StrUtils.formatAddress(property.getLocation());
		String phone = Db.getBookingResponse().getPhoneNumber();

		//In this screen isShared & travelerName would not be relevant. So just set to false and null and pass it on to ShareUtils.
		String subject = socialUtils.getHotelShareSubject(property.getLocation().getCity(), checkIn, checkOut, false,
				null);
		String body = socialUtils.getHotelShareTextLong(property.getName(), address, phone, checkIn, checkOut, null,
				false, null);

		SocialUtils.email(context, subject, body);

		OmnitureTracking.trackHotelConfirmationShareEmail(getActivity());
	}

	@Override
	protected void addItineraryToCalendar() {
		// Go in reverse order, so that "check in" is shown to the user first
		startActivity(generateHotelCalendarIntent(false));
		startActivity(generateHotelCalendarIntent(true));

		OmnitureTracking.trackHotelConfirmationAddToCalendar(getActivity());
	}

	private Intent generateHotelCalendarIntent(boolean checkIn) {
		Property property = Db.getBookingResponse().getProperty();
		LocalDate date = checkIn ? Db.getHotelSearch().getSearchParams().getCheckInDate() : Db.getHotelSearch()
				.getSearchParams().getCheckOutDate();
		BookingResponse bookingResponse = Db.getBookingResponse();
		String confNumber = bookingResponse.getHotelConfNumber();
		String itinNumber = bookingResponse.getItineraryId();

		return AddToCalendarUtils.generateHotelAddToCalendarIntent(getActivity(), property, date, checkIn, confNumber,
				itinNumber);
	}

}
