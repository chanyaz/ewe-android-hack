package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;

public class ActivityItinCard extends ItinCard<ItinCardDataActivity> {
	public ActivityItinCard(Context context) {
		this(context, null);
	}

	public ActivityItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public int getTypeIconResId() {
		return R.drawable.ic_type_circle_activity;
	}

	@Override
	public Type getType() {
		return Type.ACTIVITY;
	}

	@Override
	protected String getHeaderImageUrl(ItinCardDataActivity itinCardData) {
		return null;
	}

	@Override
	protected String getHeaderText(ItinCardDataActivity itinCardData) {
		return itinCardData.getTitle();
	}

	@Override
	protected View getTitleView(LayoutInflater inflater, ViewGroup container, ItinCardDataActivity itinCardData) {
		TextView view = (TextView) inflater.inflate(R.layout.include_itin_card_title_generic, container, false);
		view.setText(getHeaderText(itinCardData));
		return view;
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, final ItinCardDataActivity itinCardData) {
		View view = inflater.inflate(R.layout.include_itin_card_details_activity, container, false);

		// Find
		TextView activeDateTextView = Ui.findView(view, R.id.active_date_text_view);
		TextView expirationDateTextView = Ui.findView(view, R.id.expiration_date_text_view);
		TextView guestCountTextView = Ui.findView(view, R.id.guest_count_text_view);
		TextView detailsTextView = Ui.findView(view, R.id.details_text_view);

		// Bind
		activeDateTextView.setText(itinCardData.getFormattedActiveDate());
		expirationDateTextView.setText(itinCardData.getFormattedExpirationDate());
		guestCountTextView.setText(itinCardData.getFormattedGuestCount());

		detailsTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.openSite(getContext(), itinCardData.getDetailsUrl());
			}
		});

		return view;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, ItinCardDataActivity itinCardData) {
		return null;
	}

	@Override
	protected SummaryButton getSummaryLeftButton(ItinCardDataActivity itinCardData) {
		return new SummaryButton(R.drawable.ic_printer_redeem, R.string.itin_action_redeem, new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
	}

	@Override
	protected SummaryButton getSummaryRightButton(ItinCardDataActivity itinCardData) {
		return new SummaryButton(R.drawable.ic_phone, R.string.itin_action_support, new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
	}

	@Override
	protected void onShareButtonClick(ItinCardDataActivity itinCardData) {
	}
}