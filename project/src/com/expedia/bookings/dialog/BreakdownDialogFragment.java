package com.expedia.bookings.dialog;

import java.util.ArrayList;
import java.util.List;

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

		mIndentPixels = (int) Math.round(17 * getResources().getDisplayMetrics().density);

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
			params.rightMargin = (int) Math.round(8 * getResources().getDisplayMetrics().density);
		}

		return itemView;
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
