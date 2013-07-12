package com.expedia.bookings.dialog;

import java.text.DateFormat;
import java.util.ArrayList;
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

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.LayoutUtils;
import com.mobiata.android.util.Ui;

/**
 * Generalized class which displays a breakdown of some sort - i.e., line items.
 * 
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
				LinearLayout row = (LinearLayout) inflater.inflate(R.layout.snippet_breakdown_row, breakdownContainer,
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
		TextView itemView = (TextView) inflater.inflate(R.layout.snippet_breakdown_text, container, false);

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
	// Convenience buildres

	public static BreakdownDialogFragment buildHotelRateBreakdownDialog(Context context, HotelSearch search) {
		Resources res = context.getResources();
		HotelSearchParams params = search.getSearchParams();
		Rate originalRate = search.getSelectedRate();
		Rate couponRate = search.getCouponRate();

		Builder builder = new Builder();

		// Title
		builder.setTitle(context.getString(R.string.cost_summary));
		builder.setTitleDivider(R.drawable.dialog_breakdown_stripe);

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
		DateFormat breakdownFormat = android.text.format.DateFormat.getDateFormat(context);
		if (originalRate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : originalRate.getRateBreakdownList()) {
				String date = breakdownFormat.format(breakdown.getDate().getCalendar().getTime());
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
		if (couponRate != null) {
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
			String surcharge = (originalRate.getTotalSurcharge().isZero()) ? context.getString(R.string.included)
					: originalRate.getTotalSurcharge().getFormattedMoney();

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

		builder.addDivider();

		// Mandatory fees
		if (PointOfSale.getPointOfSale().displayMandatoryFees()) {
			builder.addLineItem((new LineItemBuilder())
					.setItemLeft((new ItemBuilder())
							.setText(context.getString(R.string.total_due_today))
							.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
							.build())
					.setItemRight((new ItemBuilder())
							.setText(originalRate.getTotalAmountAfterTax().getFormattedMoney())
							.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
							.build())
					.build());

			builder.addLineItem((new LineItemBuilder())
					.setItemLeft((new ItemBuilder())
							.setText(context.getString(R.string.MandatoryFees))
							.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
							.build())
					.setItemRight((new ItemBuilder())
							.setText(originalRate.getTotalMandatoryFees().getFormattedMoney())
							.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
							.build())
					.build());
		}

		// Total
		Money total = couponRate == null ? originalRate.getDisplayTotalPrice() : couponRate.getDisplayTotalPrice();
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

	public static BreakdownDialogFragment buildHotelRateBreakdownDialog(Context context, FlightSearch search,
			BillingInfo billingInfo) {
		FlightSearchParams params = search.getSearchParams();
		FlightTrip trip = search.getSelectedFlightTrip();

		Builder builder = new Builder();

		// Title
		builder.setTitle(context.getString(R.string.cost_summary));
		builder.setTitleDivider(R.drawable.border_horizontal_expedia_striped);

		// Per traveler price
		for (int i = 0; i < params.getNumAdults(); i++) {
			builder.addLineItem((new LineItemBuilder())
					.setItemLeft((new ItemBuilder())
							.setText(context.getString(R.string.traveler_num_and_category_TEMPLATE, i + 1))
							.setTextAppearance(R.style.TextAppearance_Breakdown_Medium_Bold)
							.build())
					.setItemRight((new ItemBuilder())
							.setText(trip.getTotalFare().getFormattedMoneyPerTraveler())
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
							.setText(trip.getBaseFare().getFormattedMoneyPerTraveler())
							.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
							.build())
					.build());

			builder.addLineItem((new LineItemBuilder())
					.setTopPaddingEnabled(false)
					.setItemLeft((new ItemBuilder())
							.setText(context.getString(R.string.taxes_and_airline_fees))
							.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
							.build())
					.setItemRight((new ItemBuilder())
							.setText(trip.getTaxes().getFormattedMoneyPerTraveler())
							.setTextAppearance(R.style.TextAppearance_Breakdown_Medium)
							.build())
					.build());

			builder.addDivider();
		}

		// LCC card fee
		Money cardFee = trip.getCardFee(billingInfo);
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
			builder.addLineItem((new LineItemBuilder())
					.setItemLeft((new ItemBuilder())
							.setText(context.getString(R.string.expedia_booking_fee))
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
		if (trip.getTotalFare() != null) {
			String text;
			if (trip.showFareWithCardFee(context, billingInfo)) {
				text = trip.getTotalFareWithCardFee(billingInfo).getFormattedMoney();
			}
			else {
				text = trip.getTotalFare().getFormattedMoney();
			}

			builder.addLineItem((new LineItemBuilder())
					.setItemLeft((new ItemBuilder())
							.setText(context.getString(R.string.total_price_label))
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
			in.readList(mLineItems, null);
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
			mLeftItem = in.readParcelable(null);
			mRightItem = in.readParcelable(null);
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
