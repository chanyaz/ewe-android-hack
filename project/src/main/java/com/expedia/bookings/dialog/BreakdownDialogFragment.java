package com.expedia.bookings.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.PassengerCategoryPrice;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.CheckoutPriceType;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.data.trips.TripBucketItemHotel;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

/**
 * Generalized class which displays a breakdown of some sort - i.e., line items.
 * <p/>
 * Use the builder to construct the fragment, then show it.
 */
public class BreakdownDialogFragment extends DialogFragment {

	public static final String TAG = BreakdownDialogFragment.class.toString();

	private static final String ARG_PARAMS = "ARG_PARAMS";

	private Params mParams;

	private int mIndentPixels;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mParams = getArguments().getParcelable(ARG_PARAMS);

		mIndentPixels = getResources().getDimensionPixelSize(R.dimen.breakdown_indentation);

		// TODO - proper style
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ExpediaLoginDialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dialog_breakdown, container, false);

		// Configure the title
		Ui.setText(view, R.id.title_text_view, mParams.mTitle);
		Ui.findView(view, R.id.title_divider_view).setBackgroundResource(mParams.mTitleDividerResId);

		// Construct the rows
		ViewGroup breakdownContainer = Ui.findView(view, R.id.breakdown_container);
		for (LineItem lineItem : mParams.mLineItems) {
			if (lineItem.mIsDivider) {
				View divider = inflater.inflate(R.layout.snippet_breakdown_divider, breakdownContainer, false);
				breakdownContainer.addView(divider);
			}
			else {
				LinearLayout row = Ui.inflate(inflater, R.layout.snippet_breakdown_row, breakdownContainer,
					false);

				if (lineItem.mIndent) {
					LayoutUtils.addPadding(row, mIndentPixels, 0, 0, 0);
				}

				// By default the top margin is there; remove it if desired, or if it's the first row
				// (in that case we let the container's top padding handle the top margin
				if (!lineItem.mAddTopMargin || breakdownContainer.getChildCount() == 0) {
					LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) row.getLayoutParams();
					params.topMargin = 0;
				}

				row.addView(createItemView(inflater, row, lineItem.mLeftItem, true));
				row.addView(createItemView(inflater, row, lineItem.mRightItem, false));
				breakdownContainer.addView(row);
			}
		}

		// Configure the done button
		Ui.setOnClickListener(view, R.id.done_button, new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return view;
	}

	private View createItemView(LayoutInflater inflater, LinearLayout container, Item item, boolean isLeft) {
		TextView itemView = Ui.inflate(inflater, R.layout.snippet_breakdown_text, container, false);

		itemView.setText(item.mText);

		if (item.mTextAppearanceResId != 0) {
			itemView.setTextAppearance(getActivity(), item.mTextAppearanceResId);
		}

		if (isLeft) {
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemView.getLayoutParams();
			params.width = 0;
			params.weight = 1;
			params.rightMargin = getResources().getDimensionPixelSize(R.dimen.breakdown_text_margin);
		}

		return itemView;
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience builders

	public static BreakdownDialogFragment buildHotelRateBreakdownDialog(Context context, TripBucketItemHotel hotel) {
		Resources res = context.getResources();
		HotelSearchParams params = hotel.getHotelSearchParams();
		Rate originalRate = hotel.getRateNoCoupon();
		Rate couponRate = hotel.getCouponRate();

		Builder builder = new Builder();

		// Title
		builder.setTitle(context.getString(R.string.cost_summary));
		builder.setTitleDivider(Ui.obtainThemeResID(context, R.attr.skin_costSummaryDialogStripeDrawable));

		// Breakdown summary
		int numNights = params.getStayDuration();
		builder.addLineItem((new LineItemBuilder())
			.setItemLeft((new ItemBuilder())
				.setText(res.getQuantityString(R.plurals.number_of_nights, numNights, numNights))
				.setTextAppearance(R.style.TextAppearance_Breakdown_Medium_Bold)
				.build())
			.setItemRight((new ItemBuilder())
				.setText(originalRate.getNightlyRateTotal().getFormattedMoney())
				.setTextAppearance(R.style.TextAppearance_Breakdown_Medium_Bold)
				.build())
			.build());

		// Breakdown of each night
		if (originalRate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : originalRate.getRateBreakdownList()) {
				String date = JodaUtils.formatLocalDate(context, breakdown.getDate(), DateFormatUtils.FLAGS_DATE_NUMERIC | DateFormatUtils.FLAGS_MEDIUM_DATE_FORMAT);
				Money amount = breakdown.getAmount();
				CharSequence amountStr = (amount.isZero()) ? context.getString(R.string.free) :
					amount.getFormattedMoney();

				builder.addLineItem((new LineItemBuilder())
					.indent()
					.setItemLeft((new ItemBuilder())
						.setText(date)
						.setTextAppearance(R.style.TextAppearance_Breakdown_Light)
						.build())
					.setItemRight((new ItemBuilder())
						.setText(amountStr)
						.setTextAppearance(R.style.TextAppearance_Breakdown_Light)
						.build())
					.build());
			}
		}

		// Discount
		if (couponRate != null && !couponRate.getTotalPriceAdjustments().isZero()) {
			builder.addLineItem((new LineItemBuilder())
				.setItemLeft((new ItemBuilder())
					.setText(context.getString(R.string.discount))
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.setItemRight((new ItemBuilder())
					.setText("(" + couponRate.getTotalPriceAdjustments().getFormattedMoney() + ")")
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium_Green)
					.build())
				.build());
		}

		// Taxes & Fees
		if (originalRate.getTotalSurcharge() != null) {
			String surcharge;
			if (originalRate.getTaxStatusType() != null && originalRate.getTaxStatusType() == Rate.TaxStatusType.UNKNOWN) {
				surcharge = context.getString(R.string.unknown);
			}
			else {
				surcharge = (originalRate.getTotalSurcharge().isZero()) ? context.getString(R.string.included)
					: originalRate.getTotalSurcharge().getFormattedMoney();
			}

			builder.addLineItem((new LineItemBuilder())
				.setItemLeft((new ItemBuilder())
					.setText(context.getString(R.string.taxes_and_fees))
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.setItemRight((new ItemBuilder())
					.setText(surcharge)
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.build());
		}

		// Extra guest fees
		if (originalRate.getExtraGuestFee() != null && !originalRate.getExtraGuestFee().isZero()) {
			builder.addLineItem((new LineItemBuilder())
				.setItemLeft((new ItemBuilder())
					.setText(context.getString(R.string.extra_guest_charge))
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.setItemRight((new ItemBuilder())
					.setText(originalRate.getExtraGuestFee().getFormattedMoney())
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.build());
		}

		Money total;
		Rate rateWeCareAbout = couponRate == null ? originalRate : couponRate;
		boolean resortCase = rateWeCareAbout.getCheckoutPriceType() == CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES;
		boolean payLaterCase = rateWeCareAbout.isPayLater() && !ExpediaBookingApp.useTabletInterface();
		// #5665: zero deposit for tablet only
		boolean isZeroDepositCaseTablet = ExpediaBookingApp.useTabletInterface() && rateWeCareAbout.isPayLater();

		// Show amount to be paid today in resort or ETP cases
		if (resortCase || payLaterCase || isZeroDepositCaseTablet) {
			Money dueToday;
			if (payLaterCase || isZeroDepositCaseTablet) {
				dueToday = rateWeCareAbout.getDepositAmount();
			}
			else {
				dueToday = rateWeCareAbout.getTotalAmountAfterTax();
			}

			CharSequence dueTodayText;
			if (isZeroDepositCaseTablet) {
				dueTodayText = Phrase.from(context, R.string.due_to_brand_today_today_TEMPLATE)
					.put("brand", BuildConfig.brand)
					.format();
			}
			else {
				dueTodayText = Phrase.from(context, R.string.due_to_brand_today_TEMPLATE)
					.put("brand", BuildConfig.brand)
					.format();
			}

			builder.addLineItem((new LineItemBuilder())
				.setItemLeft((new ItemBuilder())
					.setText(dueTodayText)
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.setItemRight((new ItemBuilder())
					.setText(dueToday.getFormattedMoney())
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.build());
		}
		// Mandatory fees:
		// Show fees to be paid at hotel in resort case
		if (resortCase) {
			total = rateWeCareAbout.getTotalPriceWithMandatoryFees();
			builder.addLineItem((new LineItemBuilder())
				.setItemLeft((new ItemBuilder())
					.setText(context.getString(R.string.fees_paid_at_hotel))
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.setItemRight((new ItemBuilder())
					.setText(rateWeCareAbout.getTotalMandatoryFees().getFormattedMoney())
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.build());
		}

		else {
			total = rateWeCareAbout.getDisplayTotalPrice();
		}

		builder.addDivider();

		// Total
		builder.addLineItem((new LineItemBuilder())
			.setItemLeft((new ItemBuilder())
				.setText(context.getString(R.string.total_price_label))
				.setTextAppearance(R.style.TextAppearance_Breakdown_Heavy_Bold)
				.build())
			.setItemRight((new ItemBuilder())
				.setText(total.getFormattedMoney())
				.setTextAppearance(R.style.TextAppearance_Breakdown_Heavy)
				.build())
			.build());

		return builder.build();
	}

	public static BreakdownDialogFragment buildFlightBreakdownDialog(Context context, TripBucketItemFlight item, BillingInfo billingInfo) {
		FlightTrip trip = item.getFlightTrip();
		Builder builder = new Builder();

		// Title
		builder.setTitle(context.getString(R.string.cost_summary));
		builder.setTitleDivider(R.drawable.dialog_breakdown_stripe);

		Money totalFarePerTraveler;
		Money totalBaseFarePerTraveler;
		Money totalTaxesPerTraveler;

		int numAdultsAdded = 0;
		int numChildrenAdded = 0;
		int numInfantsInSeat = 0;
		int numInfantsInLap = 0;

		int travelerHeaderStringId = 0;
		int index = 0;
		boolean throwUnhandledPassengerCatError = false;

		Collections.sort(trip.getPassengers());
		// Per traveler price
		for (int i = 0; i < trip.getPassengers().size(); i++) {
			PassengerCategoryPrice p = trip.getPassenger(i);
			switch (p.getPassengerCategory()) {
			case ADULT:
			case SENIOR:
				travelerHeaderStringId = R.string.add_adult_number_TEMPLATE;
				index = ++numAdultsAdded;
				break;
			case CHILD:
			case ADULT_CHILD:
				travelerHeaderStringId = R.string.add_child_number_TEMPLATE;
				index = ++numChildrenAdded;
				break;
			case INFANT_IN_LAP:
				travelerHeaderStringId = R.string.add_infant_in_lap_number_TEMPLATE;
				index = ++numInfantsInLap;
				break;
			case INFANT_IN_SEAT:
				travelerHeaderStringId = R.string.add_infant_in_seat_number_TEMPLATE;
				index = ++numInfantsInSeat;
				break;
			default:
				throwUnhandledPassengerCatError = true;
				break;
			}

			if (throwUnhandledPassengerCatError) {
				throw new RuntimeException("PassengerType: " + p.getPassengerCategory() + " not recognized.");
			}

			totalFarePerTraveler = p.getTotalPrice();
			totalBaseFarePerTraveler = p.getBasePrice();
			totalTaxesPerTraveler = p.getTaxes();

			builder.addLineItem((new LineItemBuilder())
				.setItemLeft((new ItemBuilder())
					.setText(context.getString(travelerHeaderStringId, index))
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium_Bold)
					.build())
				.setItemRight((new ItemBuilder())
					.setText(totalFarePerTraveler.getFormattedMoney())
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium_Bold)
					.build())
				.build());

			builder.addLineItem((new LineItemBuilder())
				.setTopPaddingEnabled(false)
				.setItemLeft((new ItemBuilder())
					.setText(context.getString(R.string.flight))
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.setItemRight((new ItemBuilder())
					.setText(totalBaseFarePerTraveler.getFormattedMoney())
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.build());

			int taxesAndFeesLabel = PointOfSale.getPointOfSale()
					.shouldAdjustPricingMessagingForAirlinePaymentMethodFee() ?
					R.string.taxes_without_airline_fees : R.string.taxes_and_airline_fees;

			builder.addLineItem((new LineItemBuilder())
				.setTopPaddingEnabled(false)
				.setItemLeft((new ItemBuilder())
					.setText(context.getString(taxesAndFeesLabel))
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.setItemRight((new ItemBuilder())
					.setText(totalTaxesPerTraveler.getFormattedMoney())
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.build());

			builder.addDivider();
		}

		// LCC card fee
		Money cardFee = item.getPaymentFee(billingInfo);
		if (cardFee != null && trip.showFareWithCardFee(context, billingInfo)) {
			builder.addLineItem((new LineItemBuilder())
				.setItemLeft((new ItemBuilder())
					.setText(context.getString(R.string.airline_card_fee))
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.setItemRight((new ItemBuilder())
					.setText(cardFee.getFormattedMoney())
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.build());

			builder.addDivider();
		}

		// OB fees
		if (trip.getFees() != null) {
			String bookingFee = Phrase.from(context, R.string.brand_booking_fee)
				.put("brand", ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(context))
				.format().toString();

			builder.addLineItem((new LineItemBuilder())
				.setItemLeft((new ItemBuilder())
					.setText(bookingFee)
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.setItemRight((new ItemBuilder())
					.setText(trip.getFees().getFormattedMoney())
					.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
					.build())
				.build());

			builder.addDivider();
		}

		// Total price
		if (trip.getTotalPrice() != null) {
			String text;
			if (trip.showFareWithCardFee(context, billingInfo)) {
				text = trip.getTotalFareWithCardFee(billingInfo, item).getFormattedMoney();
			}
			else {
				text = trip.getTotalPrice().getFormattedMoney();
			}

			builder.addLineItem((new LineItemBuilder())
				.setItemLeft((new ItemBuilder())
					.setText(context.getString(
						PointOfSale.getPointOfSale().shouldAdjustPricingMessagingForAirlinePaymentMethodFee()
							? R.string.total_price_min_label : R.string.total_price_label))
					.setTextAppearance(R.style.TextAppearance_Breakdown_Heavy_Bold)
					.build())
				.setItemRight((new ItemBuilder())
					.setText(text)
					.setTextAppearance(R.style.TextAppearance_Breakdown_Heavy)
					.build())
				.build());
		}

		return builder.build();
	}

	//////////////////////////////////////////////////////////////////////////
	// Builder

	public static class Builder {

		private Params mParams;

		public Builder() {
			mParams = new Params();
		}

		public BreakdownDialogFragment build() {
			BreakdownDialogFragment fragment = new BreakdownDialogFragment();
			Bundle args = new Bundle();
			args.putParcelable(ARG_PARAMS, mParams);
			fragment.setArguments(args);
			return fragment;
		}

		public Builder setTitle(CharSequence title) {
			mParams.mTitle = title;
			return this;
		}

		public Builder setTitleDivider(int resId) {
			mParams.mTitleDividerResId = resId;
			return this;
		}

		public Builder addDivider() {
			LineItem divider = new LineItem();
			divider.mIsDivider = true;
			mParams.mLineItems.add(divider);
			return this;
		}

		public Builder addLineItem(LineItem lineItem) {
			mParams.mLineItems.add(lineItem);
			return this;
		}
	}

	public static class LineItemBuilder {

		private LineItem mLineItem = new LineItem();

		public LineItem build() {
			return mLineItem;
		}

		public LineItemBuilder setItemLeft(Item item) {
			mLineItem.mLeftItem = item;
			return this;
		}

		public LineItemBuilder setItemRight(Item item) {
			mLineItem.mRightItem = item;
			return this;
		}

		public LineItemBuilder indent() {
			mLineItem.mIndent = true;
			return this;
		}

		public LineItemBuilder setTopPaddingEnabled(boolean enabled) {
			mLineItem.mAddTopMargin = enabled;
			return this;
		}
	}

	public static class ItemBuilder {

		private Item mItem = new Item();

		public Item build() {
			return mItem;
		}

		public ItemBuilder setText(CharSequence text) {
			mItem.mText = text;
			return this;
		}

		public ItemBuilder setTextAppearance(int resId) {
			mItem.mTextAppearanceResId = resId;
			return this;
		}
	}

	private static class Params implements Parcelable {

		private CharSequence mTitle;
		private int mTitleDividerResId;
		private List<LineItem> mLineItems = new ArrayList<LineItem>();

		private Params() {
			// Default constructor
		}

		private Params(Parcel in) {
			mTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
			mTitleDividerResId = in.readInt();
			in.readList(mLineItems, getClass().getClassLoader());
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			TextUtils.writeToParcel(mTitle, dest, flags);
			dest.writeInt(mTitleDividerResId);
			dest.writeList(mLineItems);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<Params> CREATOR = new Parcelable.Creator<Params>() {
			public Params createFromParcel(Parcel in) {
				return new Params(in);
			}

			public Params[] newArray(int size) {
				return new Params[size];
			}
		};
	}

	private static class LineItem implements Parcelable {

		// If this row is actually a divider, ignore just about every other var
		private boolean mIsDivider;

		private Item mLeftItem;
		private Item mRightItem;

		// Indent the left text a bit
		private boolean mIndent;

		// Whether to pad the top of this row
		private boolean mAddTopMargin = true;

		private LineItem() {
			// Default constructor
		}

		private LineItem(Parcel in) {
			mIsDivider = in.readByte() == 1;
			mLeftItem = in.readParcelable(getClass().getClassLoader());
			mRightItem = in.readParcelable(getClass().getClassLoader());
			mIndent = in.readByte() == 1;
			mAddTopMargin = in.readByte() == 1;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeByte((byte) (mIsDivider ? 1 : 0));
			dest.writeParcelable(mLeftItem, flags);
			dest.writeParcelable(mRightItem, flags);
			dest.writeByte((byte) (mIndent ? 1 : 0));
			dest.writeByte((byte) (mAddTopMargin ? 1 : 0));
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<LineItem> CREATOR = new Parcelable.Creator<LineItem>() {
			public LineItem createFromParcel(Parcel in) {
				return new LineItem(in);
			}

			public LineItem[] newArray(int size) {
				return new LineItem[size];
			}
		};
	}

	private static class Item implements Parcelable {

		private CharSequence mText;
		private int mTextAppearanceResId;

		private Item() {
			// Default constructor
		}

		private Item(Parcel in) {
			mText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
			mTextAppearanceResId = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			TextUtils.writeToParcel(mText, dest, flags);
			dest.writeInt(mTextAppearanceResId);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
			public Item createFromParcel(Parcel in) {
				return new Item(in);
			}

			public Item[] newArray(int size) {
				return new Item[size];
			}
		};
	}
}
