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
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
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
import com.expedia.bookings.data.Rate.UserPriceType;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.TypefaceSpan;
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

		setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bg_row_state_pressed));
		getBackground().setAlpha(0);

		setRate(rate);
		android.widget.TextView description = Ui.findView(this, R.id.text_room_description);
		android.widget.TextView price = Ui.findView(this, R.id.text_price_per_night);
		android.widget.TextView bedType = Ui.findView(this, R.id.text_bed_type);
		android.widget.TextView cancellationBedType = Ui.findView(this, R.id.text_bed_type_with_cancellation);

		// Buttons / Clicks
		setOnClickListener(selectRateClickListener);
		Ui.findView(this, R.id.room_rate_button_add).setOnClickListener(addRoomClickListener);
		Ui.findView(this, R.id.room_rate_button_select).setOnClickListener(selectRateClickListener);

		// Description
		description.setText(rate.getRoomDescription());

		Set<BedType> bedTypes = rate.getBedTypes();
		if (bedTypes != null && bedTypes.iterator().hasNext()) {
			bedType.setVisibility(View.VISIBLE);
			String bedTypeText = bedTypes.iterator().next().getBedTypeDescription();
			bedType.setText(bedTypeText);
			setCancellationText(cancellationBedType, bedTypeText);
		}

			if (mRate.shouldShowFreeCancellation()) {
				bedType.setVisibility(View.GONE);
				cancellationBedType.setVisibility(VISIBLE);
			}

		if (price != null) {
			price.setText(getStyledPrice(getContext(), rate));
		}

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
		boolean hasResortFeesMessage = property.getMandatoryFeesText() != null
			&& !TextUtils.isEmpty(property.getMandatoryFeesText().getContent());

		if (rate.showResortFeesMessaging() && hasResortFeesMessage) {
			final String resortFeesTemplate = HotelUtils.getTabletResortFeeBannerText(getContext(), rate);
			final String resortFeesMoreInfoTitle = res.getString(R.string.additional_fees);
			final String resortFeesMoreInfoText = property.getMandatoryFeesText().getContent();

			TextView resortFeesNoticeText = Ui.findView(this, R.id.room_rate_resort_fees_text);
			resortFeesNoticeText.setText(HtmlCompat.fromHtml(resortFeesTemplate));
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
			urgencyMessagingView.setText(HtmlCompat.fromHtml(res.getString(R.string.value_add_template,
				FormatUtils.series(getContext(), unique, ",", null).toLowerCase(Locale.getDefault()))));
			urgencyMessagingView.setVisibility(View.VISIBLE);
		}
		else if (showUrgencyMessaging()) {
			String urgencyString = res.getQuantityString(R.plurals.n_rooms_left_TEMPLATE,
				mRate.getNumRoomsLeft(), mRate.getNumRoomsLeft());
			urgencyMessagingView.setText(urgencyString);
			urgencyMessagingView.setVisibility(View.VISIBLE);
		}
		else {
			urgencyMessagingView.setVisibility(View.GONE);
		}

		ImageView roomDetailImageView = Ui.findView(this, R.id.room_rate_image_view);
		final TextView roomLongDescriptionTextView = Ui.findView(this, R.id.room_rate_description_text);
		TextView refundableCancellationTv = Ui.findView(this, R.id.room_rate_refundable_cancellation_text);
		TextView nonRefundableTv = Ui.findView(this, R.id.room_rate_non_refundable_text);
		android.widget.TextView roomRateDiscountRibbon = Ui.findView(this, R.id.room_rate_discount_text);

		mIsDescriptionTextSpanned = false;
		final String roomLongDescription;
		if (mRate.getRoomLongDescription() != null) {
			roomLongDescription = mRate.getRoomLongDescription().trim();
		}
		else {
			roomLongDescription = null;
		}
		String descriptionReduced;
		int lengthCutOff;
		// Let's try to show as much text to begin with as possible, without exceeding the row height.
		if (Ui.findView(this, R.id.room_rate_urgency_text).getVisibility() == View.VISIBLE) {
			lengthCutOff = res.getInteger(R.integer.room_rate_description_body_length_cutoff_less);
		}
		else {
			lengthCutOff = res.getInteger(R.integer.room_rate_description_body_length_cutoff_more);
		}

		if (roomLongDescription != null && roomLongDescription.length() > lengthCutOff) {
			descriptionReduced = roomLongDescription.substring(0, lengthCutOff);
			descriptionReduced += "...";
			SpannableBuilder builder = new SpannableBuilder();
			builder.append(descriptionReduced);
			builder.append(" ");
			builder.append(res.getString(R.string.more), new ForegroundColorSpan(0xFF245FB3),
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
		if (mRate.isNonRefundable()) {
			nonRefundableTv.setVisibility(View.VISIBLE);
			nonRefundableTv.setText(R.string.non_refundable);
		}
		else if (mRate.shouldShowFreeCancellation()) {
			refundableCancellationTv.setVisibility(View.VISIBLE);
			refundableCancellationTv.setText(HotelUtils.getRoomCancellationText(getContext(), mRate));

		}

		// Rooms and Rates detail image media
		int placeholderResId = Ui.obtainThemeResID(getContext(), R.attr.skin_hotelImagePlaceHolderDrawable);
		if (mRate.getThumbnail() != null) {
			int width = res.getDimensionPixelSize(R.dimen.hotel_room_rate_thumbnail_width);
			mRate.getThumbnail().fillImageView(roomDetailImageView, width, placeholderResId);
		}
		else {
			roomDetailImageView.setImageResource(placeholderResId);
		}

		// Room discount ribbon
		if (mRate.getDiscountPercent() > 0) {
			roomRateDiscountRibbon.setVisibility(View.VISIBLE);
			roomRateDiscountRibbon.setText(res.getString(R.string.percent_minus_template,
				(float) mRate.getDiscountPercent()));

			if (mRate.isAirAttached()) {
				roomRateDiscountRibbon.setBackgroundResource(R.drawable.bg_air_attach_sale_text_view);
			}
			else {
				roomRateDiscountRibbon.setBackgroundResource(R.drawable.bg_sale_text_view);
			}
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

		if (mRate.shouldShowFreeCancellation()) {
			Ui.findView(this, R.id.text_bed_type_with_cancellation).setVisibility(GONE);
			Ui.findView(this, R.id.text_bed_type).setVisibility(VISIBLE);
		}

		// Animate children
		final View addRoomButton = Ui.findView(this, R.id.room_rate_button_add);
		addRoomButton.setVisibility(View.VISIBLE);
		addRoomButton.setAlpha(0f);
		ObjectAnimator addButtonAnimator = ObjectAnimator.ofFloat(addRoomButton, "alpha", 1f);

		final View selectRoomButton = Ui.findView(this, R.id.room_rate_button_select);
		selectRoomButton.setVisibility(View.VISIBLE);
		selectRoomButton.setAlpha(1f);
		ObjectAnimator selectButtonAnimator = ObjectAnimator.ofFloat(selectRoomButton, "alpha", 0f);

		Animator colorDrawableAnimator = ObjectAnimator.ofInt(getBackground(), "alpha", 0, 180);

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
		if (mRate.shouldShowFreeCancellation()) {
			Ui.findView(this, R.id.text_bed_type_with_cancellation).setVisibility(GONE);
			Ui.findView(this, R.id.text_bed_type).setVisibility(VISIBLE);
		}
		getBackground().setAlpha(180);
		setHeight(LayoutParams.WRAP_CONTENT);
	}

	private void collapse() {
		setHeight(getResources().getDimensionPixelSize(R.dimen.hotel_room_rate_list_height));

		if (mRate.shouldShowFreeCancellation()) {
			Ui.findView(this, R.id.text_bed_type).setVisibility(GONE);
			Ui.findView(this, R.id.text_bed_type_with_cancellation).setVisibility(VISIBLE);
		}

		// Animate children
		View addRoomButton = Ui.findView(this, R.id.room_rate_button_add);
		addRoomButton.setVisibility(View.VISIBLE);
		addRoomButton.setAlpha(1f);
		ObjectAnimator addButtonAnimator = ObjectAnimator.ofFloat(addRoomButton, "alpha", 0f);

		View selectRoomButton = Ui.findView(this, R.id.room_rate_button_select);
		selectRoomButton.setVisibility(View.VISIBLE);
		selectRoomButton.setAlpha(0f);
		ObjectAnimator selectButtonAnimator = ObjectAnimator.ofFloat(selectRoomButton, "alpha", 1f);

		Animator colorDrawableAnimator = ObjectAnimator.ofInt(getBackground(), "alpha", 180, 0);

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
		if (mRate.shouldShowFreeCancellation()) {
			Ui.findView(this, R.id.text_bed_type).setVisibility(GONE);
			Ui.findView(this, R.id.text_bed_type_with_cancellation).setVisibility(VISIBLE);
		}
		getBackground().setAlpha(0);
		setHeight(getResources().getDimensionPixelSize(R.dimen.hotel_room_rate_list_height));
	}

	private void openWebView(String title, String text) {
		Context context = getContext();
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
		Intent intent = builder
			.setTitle(title)
			.setHtmlData(HtmlUtils.wrapInHeadAndBodyWithStandardTabletMargins(text))
			.setTheme(R.style.V2_Theme_Activity_TabletWeb)
			.getIntent();
		context.startActivity(intent);
	}

	public static CharSequence getStyledPrice(Context context, Rate rate) {
		final String formattedRoomRate = rate.getDisplayPrice().getFormattedMoney(Money.F_NO_DECIMAL);
		TypefaceSpan typefaceSpan = new TypefaceSpan(FontCache.getTypeface(FontCache.Font.ROBOTO_BOLD));
		ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.hotel_room_rate_select_room_button));

		if (rate.getUserPriceType() == UserPriceType.PER_NIGHT_RATE_NO_TAXES) {
			String built = context.getString(R.string.room_rate_per_night_template, formattedRoomRate);
			int rateOffset = built.indexOf(formattedRoomRate);
			int rateLength = formattedRoomRate.length();
			SpannableStringBuilder builder = new SpannableStringBuilder(built);
			builder.setSpan(typefaceSpan, rateOffset, rateLength, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			builder.setSpan(colorSpan, rateOffset, rateLength, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			return builder;
		}
		else {
			SpannableBuilder span = new SpannableBuilder();
			span.append(formattedRoomRate, typefaceSpan, colorSpan);
			return span.build();
		}
	}

	private void setCancellationText (android.widget.TextView bedType, String bedTypeText) {
		TextAppearanceSpan cancellationSpan = new TextAppearanceSpan(getContext(), R.style.FreeCancellationTextAppearance);
		TypefaceSpan cancellationFontSpan = FontCache.getSpan(FontCache.Font.ROBOTO_REGULAR);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(bedTypeText);
		sb.append(" — ");
		sb.append(getResources().getString(R.string.free_cancellation), cancellationSpan, cancellationFontSpan);
		bedType.setText(sb.build(), android.widget.TextView.BufferType.SPANNABLE);
	}

}
