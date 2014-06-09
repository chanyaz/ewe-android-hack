package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.BedType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.FormatUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.HtmlUtils;

public class RowRoomRateLayout extends FrameLayout {

	private static final int ROOM_RATE_ANIMATION_DURATION = 300;
	private static final int ROOM_COUNT_URGENCY_CUTOFF = 5;

	// The Rate associated with this row and its children
	private Rate mRate;

	private boolean mExpanded = false;

	private boolean mIsDescriptionTextSpanned;

	public RowRoomRateLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		Bundle bundle = new Bundle();
		bundle.putParcelable("super", superState);
		if (mRate != null) {
			JSONUtils.putJSONable(bundle, "rate", mRate);
		}
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// Boilerplate code so parent classes can restore state
		if (!(state instanceof Bundle)) {
			super.onRestoreInstanceState(state);
			return;
		}

		Bundle bundle = (Bundle) state;
		super.onRestoreInstanceState(bundle.getParcelable("super"));
		if (bundle.containsKey("rate")) {
			mRate = JSONUtils.getJSONable(bundle, "rate", Rate.class);
		}
		else {
			mRate = null;
		}
	}

	@Override
	public void setSelected(boolean selected) {
		setSelected(selected, false);
	}

	public void setSelected(boolean selected, boolean animate) {
		super.setSelected(selected);

		if (selected) {
			if (!animate) {
				mExpanded = true;
				expandNow();
			}
			else if (!isExpanded()) {
				mExpanded = true;
				expand();
			}
		}
		else {
			if (!animate) {
				mExpanded = false;
				collapseNow();
			}
			else if (isExpanded()) {
				mExpanded = false;
				collapse();
			}
		}
	}

	// This forces a re-layout. Let's hope it doesn't get called too often.
	public void setHeight(int height) {
		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		layoutParams.height = height;
		setLayoutParams(layoutParams);
	}

	public boolean isExpanded() {
		return mExpanded;
	}

	private boolean showUrgencyMessaging() {
		int roomsLeft = mRate.getNumRoomsLeft();
		return roomsLeft > 0 && roomsLeft < ROOM_COUNT_URGENCY_CUTOFF;
	}

	public void setRate(Rate rate) {
		mRate = rate;
	}

	public Rate getRate() {
		return mRate;
	}

	public void bind(Rate rate, List<String> commonValueAdds,
					 OnClickListener selectRateClickListener, OnClickListener addRoomClickListener) {
		Resources res = getResources();

		setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bg_row_state_pressed)));
		getBackground().setAlpha(0);

		setRate(rate);
		android.widget.TextView description = Ui.findView(this, R.id.text_room_description);
		android.widget.TextView pricePerNight = Ui.findView(this, R.id.text_price_per_night);
		android.widget.TextView bedType = Ui.findView(this, R.id.text_bed_type);

		// Buttons / Clicks
		setOnClickListener(selectRateClickListener);
		Ui.findView(this, R.id.room_rate_button_add).setOnClickListener(addRoomClickListener);
		Ui.findView(this, R.id.room_rate_button_select).setOnClickListener(selectRateClickListener);

		// Description
		description.setText(rate.getRoomDescription());

		Set<BedType> bedTypes = rate.getBedTypes();
		if (bedTypes != null && bedTypes.iterator().hasNext()) {
			bedType.setVisibility(View.VISIBLE);
			bedType.setText(bedTypes.iterator().next().getBedTypeDescription());
		}

		String formattedRoomRate = rate.getDisplayPrice().getFormattedMoney(Money.F_NO_DECIMAL);
		String perNightString = res.getString(R.string.room_rate_per_night_template, formattedRoomRate);
		pricePerNight.setText(Html.fromHtml(perNightString));

		// Show renovation fees notice
		View renovationNoticeContainer = Ui.findView(this, R.id.room_rate_renovation_container);
		Property property = Db.getHotelSearch().getSelectedProperty();
		if (property.getRenovationText() != null && !TextUtils.isEmpty(property.getRenovationText().getContent())) {
			renovationNoticeContainer.setVisibility(View.VISIBLE);
			final String renovationTitle = res.getString(R.string.renovation_notice);
			final String renovationText = property.getRenovationText().getContent();
			Ui.findView(this, R.id.room_rate_renovation_more_info).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openWebView(renovationTitle, renovationText);
				}
			});
		}
		else {
			renovationNoticeContainer.setVisibility(View.GONE);
		}

		// Show resort fees notice
		View resortFeesContainer = Ui.findView(this, R.id.room_rate_resort_fees_container);
		Money mandatoryFees = rate == null ? null : rate.getTotalMandatoryFees();
		boolean hasMandatoryFees = mandatoryFees != null && !mandatoryFees.isZero();
		boolean hasResortFeesMessage = property.getMandatoryFeesText() != null
			&& !TextUtils.isEmpty(property.getMandatoryFeesText().getContent());

		if (hasMandatoryFees && hasResortFeesMessage
			&& rate.getCheckoutPriceType() != Rate.CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES) {

			final String resortFeesTemplate = res.getString(R.string.tablet_room_rate_resort_fees_template,
				mandatoryFees.getFormattedMoney());
			final String resortFeesMoreInfoTitle = res.getString(R.string.additional_fees);
			final String resortFeesMoreInfoText = property.getMandatoryFeesText().getContent();

			TextView resortFeesNoticeText = Ui.findView(this, R.id.room_rate_resort_fees_text);
			resortFeesNoticeText.setText(Html.fromHtml(resortFeesTemplate));
			resortFeesContainer.setVisibility(View.VISIBLE);
			Ui.findView(this, R.id.room_rate_resort_fees_more_info).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openWebView(resortFeesMoreInfoTitle, resortFeesMoreInfoText);
				}
			});
		}
		else {
			resortFeesContainer.setVisibility(View.GONE);
		}

		android.widget.TextView urgencyMessagingView = Ui.findView(this, R.id.room_rate_urgency_text);

		// Value Adds
		List<String> unique = new ArrayList<>(mRate.getValueAdds());
		if (commonValueAdds != null) {
			unique.removeAll(commonValueAdds);
		}

		if (unique.size() > 0) {
			urgencyMessagingView.setText(Html.fromHtml(getResources().getString(R.string.value_add_template,
				FormatUtils.series(getContext(), unique, ",", null).toLowerCase(Locale.getDefault()))));
			urgencyMessagingView.setVisibility(View.VISIBLE);
		}
		else if (showUrgencyMessaging()) {
			String urgencyString = getResources()
				.getQuantityString(R.plurals.n_rooms_left_TEMPLATE, mRate.getNumRoomsLeft(), mRate.getNumRoomsLeft());
			urgencyMessagingView.setText(urgencyString);
			urgencyMessagingView.setVisibility(View.VISIBLE);
		}
		else {
			urgencyMessagingView.setVisibility(View.GONE);
		}

		ImageView roomDetailImageView = Ui.findView(this, R.id.room_rate_image_view);
		final android.widget.TextView roomLongDescriptionTextView = Ui.findView(this, R.id.room_rate_description_text);
		android.widget.TextView refundableTextView = Ui.findView(this, R.id.room_rate_refundable_text);
		android.widget.TextView roomRateDiscountRibbon = Ui.findView(this, R.id.room_rate_discount_text);

		mIsDescriptionTextSpanned = false;
		final String roomLongDescription = mRate.getRoomLongDescription().trim();
		String descriptionReduced;
		int lengthCutOff;
		// Let's try to show as much text to begin with as possible, without exceeding the row height.
		if (Ui.findView(this, R.id.room_rate_urgency_text).getVisibility() == View.VISIBLE) {
			lengthCutOff = getResources().getInteger(R.integer.room_rate_description_body_length_cutoff_less);
		}
		else {
			lengthCutOff = getResources().getInteger(R.integer.room_rate_description_body_length_cutoff_more);
		}

		if (roomLongDescription.length() > lengthCutOff) {
			descriptionReduced = roomLongDescription.substring(0, lengthCutOff);
			descriptionReduced += "...";
			SpannableBuilder builder = new SpannableBuilder();
			builder.append(descriptionReduced);
			builder.append(" ");
			builder.append(getResources().getString(R.string.more), new ForegroundColorSpan(0xFF245FB3),
				FontCache.getSpan(FontCache.Font.ROBOTO_BOLD));
			mIsDescriptionTextSpanned = true;
			roomLongDescriptionTextView.setText(builder.build());
		}
		else {
			roomLongDescriptionTextView.setText(roomLongDescription);
		}

		// #817. Let user tap to expand or contract the room description text.
		roomLongDescriptionTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsDescriptionTextSpanned) {
					roomLongDescriptionTextView.setText(roomLongDescription);
				}
			}
		});

		// Refundable text visibility check
		refundableTextView.setVisibility(mRate.isNonRefundable() ? View.VISIBLE : View.GONE);

		// Rooms and Rates detail image media
		int placeholderResId = Ui.obtainThemeResID(getContext(), R.attr.hotelImagePlaceHolderDrawable);
		if (mRate.getThumbnail() != null) {
			int width = getResources().getDimensionPixelSize(R.dimen.hotel_room_rate_thumbnail_width);
			mRate.getThumbnail().fillImageView(roomDetailImageView, width, placeholderResId, null);
		}
		else {
			roomDetailImageView.setImageResource(placeholderResId);
		}

		// Room discount ribbon
		if (mRate.getDiscountPercent() > 0) {
			roomRateDiscountRibbon.setVisibility(View.VISIBLE);
			roomRateDiscountRibbon.setText(getResources().getString(R.string.percent_minus_template, (float) mRate.getDiscountPercent()));
		}
		else {
			roomRateDiscountRibbon.setVisibility(View.GONE);
		}
	}

	private void expand() {
		setHeight(LayoutParams.WRAP_CONTENT);

		// Show the room rate detail container
		Ui.findView(this, R.id.room_rate_detail_container).setVisibility(View.VISIBLE);
		Ui.findView(this, R.id.notice_container).setVisibility(View.VISIBLE);

		// Animate children
		final View addRoomButton = Ui.findView(this, R.id.room_rate_button_add);
		addRoomButton.setVisibility(View.VISIBLE);
		addRoomButton.setAlpha(0f);
		ObjectAnimator addButtonAnimator = ObjectAnimator.ofFloat(addRoomButton, "alpha", 1f);

		final View selectRoomButton = Ui.findView(this, R.id.room_rate_button_select);
		selectRoomButton.setVisibility(View.VISIBLE);
		selectRoomButton.setAlpha(1f);
		ObjectAnimator selectButtonAnimator = ObjectAnimator.ofFloat(selectRoomButton, "alpha", 0f);

		Animator colorDrawableAnimator = ObjectAnimator.ofInt(getBackground(), "alpha", 0, 255);

		AnimatorSet set = new AnimatorSet();
		set.setDuration(ROOM_RATE_ANIMATION_DURATION);
		set.playTogether(addButtonAnimator, selectButtonAnimator, colorDrawableAnimator);
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animator) {
				expandNow();
			}
		});
		set.start();
	}

	private void expandNow() {
		Ui.findView(this, R.id.room_rate_detail_container).setVisibility(View.VISIBLE);
		Ui.findView(this, R.id.notice_container).setVisibility(View.VISIBLE);
		Ui.findView(this, R.id.room_rate_button_add).setVisibility(View.VISIBLE);
		Ui.findView(this, R.id.room_rate_button_add).setAlpha(1f);
		Ui.findView(this, R.id.room_rate_button_select).setAlpha(0f);
		Ui.findView(this, R.id.room_rate_button_select).setVisibility(View.GONE);
		getBackground().setAlpha(255);
		setHeight(LayoutParams.WRAP_CONTENT);
	}

	private void collapse() {
		setHeight(getResources().getDimensionPixelSize(R.dimen.hotel_room_rate_list_height));

		// Animate children
		View addRoomButton = Ui.findView(this, R.id.room_rate_button_add);
		addRoomButton.setVisibility(View.VISIBLE);
		addRoomButton.setAlpha(1f);
		ObjectAnimator addButtonAnimator = ObjectAnimator.ofFloat(addRoomButton, "alpha", 0f);

		View selectRoomButton = Ui.findView(this, R.id.room_rate_button_select);
		selectRoomButton.setVisibility(View.VISIBLE);
		selectRoomButton.setAlpha(0f);
		ObjectAnimator selectButtonAnimator = ObjectAnimator.ofFloat(selectRoomButton, "alpha", 1f);

		Animator colorDrawableAnimator = ObjectAnimator.ofInt(getBackground(), "alpha", 255, 0);

		AnimatorSet set = new AnimatorSet();
		set.setDuration(ROOM_RATE_ANIMATION_DURATION);
		set.playTogether(addButtonAnimator, selectButtonAnimator, colorDrawableAnimator);
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animator) {
				collapseNow();
			}
		});
		set.start();
	}

	private void collapseNow() {
		Ui.findView(this, R.id.room_rate_detail_container).setVisibility(View.GONE);
		Ui.findView(this, R.id.notice_container).setVisibility(View.GONE);
		Ui.findView(this, R.id.room_rate_button_add).setVisibility(View.GONE);
		Ui.findView(this, R.id.room_rate_button_add).setAlpha(0f);
		Ui.findView(this, R.id.room_rate_button_select).setVisibility(View.VISIBLE);
		Ui.findView(this, R.id.room_rate_button_select).setAlpha(1f);
		getBackground().setAlpha(0);
		setHeight(getResources().getDimensionPixelSize(R.dimen.hotel_room_rate_list_height));
	}

	private void openWebView(String title, String text) {
		Context context = getContext();
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
		Intent intent = builder
			.setTitle(title)
			.setHtmlData(HtmlUtils.wrapInHeadAndBodyWithStandardTabletMargins(text))
			.setTheme(R.style.Theme_Phone_WebView_WithTitle)
			.getIntent();
		context.startActivity(intent);
	}

}
