package com.expedia.bookings.fragment;

import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.TripBucketItem;
import com.expedia.bookings.data.trips.TripBucketItemHotel;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.graphics.HeaderBitmapColorAveragedDrawable;
import com.expedia.bookings.section.HotelReceiptExtraSection;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.phrase.Phrase;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class TripBucketHotelFragment extends TripBucketItemFragment {

	public static TripBucketHotelFragment newInstance() {
		return new TripBucketHotelFragment();
	}

	private TextView mNowBookingTv;
	private TextView mRoomAndBedTv;
	private TextView mRoomTypeTv;
	private TextView mBedTypeTv;
	private TextView mDatesTv;
	private LinearLayout mExtrasContainer;
	private TextView mNumTravelersTv;
	private ViewGroup mPriceContainer;
	private TextView mPriceTv;
	private TextView mTotalTitleTv;


	@Override
	public CharSequence getBookButtonText() {
		return getString(R.string.trip_bucket_book_hotel);
	}

	@Override
	public void addExpandedView(LayoutInflater inflater, ViewGroup root) {
		ViewGroup vg = Ui.inflate(inflater, R.layout.snippet_trip_bucket_expanded_dates_view, root, false);

		// Title stuff
		mRoomTypeTv = Ui.findView(vg, R.id.room_type_text_view);
		if (mRoomTypeTv != null) {
			mRoomTypeTv.setVisibility(View.VISIBLE);
		}

		mBedTypeTv = Ui.findView(vg, R.id.bed_type_text_view);
		if (mBedTypeTv != null) {
			mBedTypeTv.setVisibility(View.VISIBLE);
		}

		// Portrait only
		mNowBookingTv = Ui.findView(vg, R.id.now_booking_text_view);
		if (mNowBookingTv != null) {
			mNowBookingTv.setVisibility(View.VISIBLE);
		}

		mRoomAndBedTv = Ui.findView(vg, R.id.room_and_bed_text_view);
		if (mRoomAndBedTv != null) {
			mRoomAndBedTv.setVisibility(View.VISIBLE);
		}

		mDatesTv = Ui.findView(vg, R.id.dates_text_view);
		mNumTravelersTv = Ui.findView(vg, R.id.num_travelers_text_view);

		mExtrasContainer = Ui.findView(vg, R.id.extras_layout);
		mTotalTitleTv = Ui.findView(vg, R.id.total_text);

		// Price
		mPriceContainer = Ui.findView(vg, R.id.price_expanded_bucket_container);
		mPriceTv = Ui.findView(vg, R.id.price_expanded_bucket_text_view);

		mPriceContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showBreakdownDialog(LineOfBusiness.HOTELS);
			}
		});

		if (!getResources().getBoolean(R.bool.show_tripbucket_date)) {
			mDatesTv.setVisibility(View.GONE);
		}

		bindExpandedView(Db.getTripBucket().getHotel());

		root.addView(vg);
	}

	@Override
	public void bind() {
		if (mRootC != null) {
			refreshRate();
		}
		super.bind();
	}

	@Override
	public void bindExpandedView(TripBucketItem item) {
		if (item != null && item instanceof TripBucketItemHotel) {
			TripBucketItemHotel itemHotel = (TripBucketItemHotel) item;
			if (itemHotel.getRate() != null) {
				Rate rate = itemHotel.getRate();

				if (mRoomTypeTv != null) {
					mRoomTypeTv.setText(rate.getRoomDescription());
				}
				if (mBedTypeTv != null) {
					mBedTypeTv.setText(rate.getFormattedBedNames());
				}
				if (mRoomAndBedTv != null) {
					mRoomAndBedTv.setText(HtmlCompat.fromHtml(getString(R.string.room_and_bed_type_TEMPLATE, rate.getRoomDescription(), rate.getFormattedBedNames())));
				}
				if (mNowBookingTv != null) {
					String hotelName = itemHotel.getProperty().getName();
					mNowBookingTv.setText(HtmlCompat.fromHtml(getString(R.string.now_booking_TEMPLATE, hotelName).toUpperCase(Locale.getDefault())));
				}

				refreshRate();
			}
		}
		bindToDbHotelSearch();
	}

	private void bindToDbHotelSearch() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			HotelSearchParams params = hotel.getHotelSearchParams();
			if (params != null) {
				String dateRange = DateFormatUtils.formatDateRange(getActivity(), params,
					DateFormatUtils.FLAGS_DATE_NO_YEAR_ABBREV_MONTH_ABBREV_WEEKDAY);
				mDatesTv.setText(dateRange);

				//Guests
				int numGuests = params.getNumTravelers();
				String numGuestsStr = getResources()
					.getQuantityString(R.plurals.number_of_guests, numGuests, numGuests);
				mNumTravelersTv.setText(numGuestsStr);

			}
		}
	}

	private void addDueToBrandRow(Rate rate, boolean showDepositAmount) {
		boolean isTabletPayLater = AndroidUtils.isTablet(this.getContext()) && rate.isPayLater();
		String formattedMoney =
			isTabletPayLater ? new Money(0, rate.getTotalAmountAfterTax().getCurrency()).getFormattedMoney()
				: (showDepositAmount ? rate.getDepositAmount().getFormattedMoney()
					: rate.getTotalAmountAfterTax().getFormattedMoney());
		String totalDueToOurBrandToday;
		if (showDepositAmount) {
			totalDueToOurBrandToday = Phrase.from(getActivity(), R.string.due_to_brand_today_today_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.format()
				.toString();
		}
		else {
			totalDueToOurBrandToday = Phrase.from(getActivity(), R.string.due_to_brand_today_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.format()
				.toString();
		}
		addExtraRow(totalDueToOurBrandToday, formattedMoney, false);
	}

	private void addResortFeeRows(Rate rate) {
		String feesPaidAtHotel = getResources().getString(R.string.fees_paid_at_hotel);
		addExtraRow(feesPaidAtHotel, rate.getTotalMandatoryFees().getFormattedMoney(), false);
	}

	private static final int LANDSCAPE_EXTRAS_LIMIT = 3;
	private static final int PORTRAIT_EXTRAS_LIMIT = 2;

	private void addPrioritizedAmenityRows(Rate rate) {
		int extrasSizeLimit = getResources().getBoolean(R.bool.landscape) ?
			LANDSCAPE_EXTRAS_LIMIT : PORTRAIT_EXTRAS_LIMIT;

		if (rate.shouldShowFreeCancellation() && mExtrasContainer.getChildCount() < extrasSizeLimit) {
			addExtraRow(HotelUtils.getRoomCancellationText(getActivity(), rate).toString(), null, true);
		}
		if (PointOfSale.getPointOfSale().displayBestPriceGuarantee() && mExtrasContainer.getChildCount() < extrasSizeLimit) {
			addExtraRow(getResources().getString(Ui.obtainThemeResID(getActivity(), R.attr.skin_bestPriceGuaranteeString)), null, true);
		}
	}

	public void addExtraRow(String title, String optionalPrice, boolean addToTop) {
		HotelReceiptExtraSection row = Ui.inflate(R.layout.snippet_hotel_receipt_price_extra, mExtrasContainer, false);
		row.bind(title, optionalPrice);
		if (addToTop) {
			mExtrasContainer.addView(row, 0);
		}
		else {
			mExtrasContainer.addView(row);
		}
	}

	@Override
	public void addTripBucketImage(ImageView imageView, HeaderBitmapColorAveragedDrawable headerBitmapDrawable) {
		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.skin_HotelRowThumbPlaceHolderDrawable);
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null && hotel.getProperty() != null) {
			if (hotel.getProperty().getThumbnail() != null) {
				hotel.getProperty().getThumbnail().fillHeaderBitmapDrawable(imageView, headerBitmapDrawable, placeholderResId);
			}
			else {
				new PicassoHelper.Builder(getActivity()).setTarget(
					headerBitmapDrawable.getCallBack()).build().load(placeholderResId);
				imageView.setImageDrawable(headerBitmapDrawable);
			}
		}
	}

	@Override
	public boolean doTripBucketImageRefresh() {
		return true;
	}

	@Override
	public String getNameText() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			return hotel.getProperty().getName();
		}
		else {
			return null;
		}
	}

	@Override
	public String getDateRangeText() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			HotelSearchParams params = hotel.getHotelSearchParams();
			return DateFormatUtils.formatDateRange(getActivity(), params, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
		}
		else {
			return null;
		}
	}

	@Override
	public CharSequence getTripPrice() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			Rate rate = hotel.getRate();
			return rate.getDisplayTotalPrice().getFormattedMoney(Money.F_NO_DECIMAL);
		}
		else {
			return null;
		}
	}


	@Override
	public OnClickListener getOnBookClickListener() {
		return mBookOnClick;
	}

	@Override
	public boolean isSelected() {
		if (Db.getTripBucket().getHotel() != null) {
			return Db.getTripBucket().getHotel().isSelected();
		}
		else {
			return false;
		}
	}

	@Override
	public void setSelected(boolean isSelected) {
		Db.getTripBucket().getHotel().setSelected(isSelected);
		if (Db.getTripBucket().getFlight() != null) {
			Db.getTripBucket().getFlight().setSelected(!isSelected);
		}
	}

	private OnClickListener mBookOnClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			triggerTripBucketBookAction(LineOfBusiness.HOTELS);
		}
	};

	public void refreshRate() {
		// Update the price in the expanded tripbucket.
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			Rate rate = hotel.getRate();
			String totalTitle = getResources().getString(R.string.trip_total);
			String price = rate.getTotalPriceWithMandatoryFees().getFormattedMoney();

			mExtrasContainer.removeAllViews();
			if (PointOfSale.getPointOfSale().showFTCResortRegulations() && rate.showResortFeesMessaging()) {
				addResortFeeRows(rate);
				addDueToBrandRow(rate, false);
			}
			else if (rate.isPayLater() && rate.depositRequired()) {
				boolean showDepositAmount = true;
				addDueToBrandRow(rate, showDepositAmount);
			}
			else {
				totalTitle = getResources().getString(R.string.total_with_tax);
				price = rate.getDisplayTotalPrice().getFormattedMoney();
			}
			mTotalTitleTv.setText(totalTitle);
			mPriceTv.setText(price);
			addPrioritizedAmenityRows(rate);
			refreshPriceChange();
		}
	}

	@Override
	public CharSequence getPriceChangeMessage() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			Rate oldRate = hotel.getOldRate();
			Money weCareAbout = hotel.getRate().getCheckoutPriceType() == Rate.CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES ?
				oldRate.getTotalPriceWithMandatoryFees() : oldRate.getDisplayTotalPrice();
			String amount = weCareAbout.getFormattedMoney();
			return getString(R.string.price_changed_from_TEMPLATE, amount);
		}

		return null;
	}

	@Override
	public int getPriceChangeDrawable() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null && hotel.hasAirAttachRate()) {
			return R.drawable.plane_gray;
		}
		return super.getPriceChangeDrawable();
	}

	@Override
	public int getPriceChangeTextColor() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null && hotel.hasAirAttachRate()) {
			return R.color.price_change_air_attach;
		}
		return super.getPriceChangeTextColor();
	}

	@Override
	public TripBucketItemHotel getItem() {
		return Db.getTripBucket().getHotel();
	}

}
