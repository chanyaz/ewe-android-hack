package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
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
	protected String getShareSubject(ItinCardDataActivity itinCardData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getShareText(ItinCardDataActivity itinCardData) {
		// TODO Auto-generated method stub
		return null;
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
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, ItinCardDataActivity itinCardData) {
		TextView view = (TextView) inflater.inflate(R.layout.include_itin_card_summary_activity, container, false);
		view.setText(Html.fromHtml("Valid starting <strong>" + itinCardData.getLongFormattedActiveDate() + "</strong>"));

		return view;
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, final ItinCardDataActivity itinCardData) {
		View view = inflater.inflate(R.layout.include_itin_card_details_activity, container, false);

		// Find
		TextView activeDateTextView = Ui.findView(view, R.id.active_date_text_view);
		TextView expirationDateTextView = Ui.findView(view, R.id.expiration_date_text_view);
		TextView guestCountTextView = Ui.findView(view, R.id.guest_count_text_view);
		ViewGroup guestsLayout = Ui.findView(view, R.id.guests_layout);
		TextView detailsTextView = Ui.findView(view, R.id.details_text_view);

		// Bind
		activeDateTextView.setText(itinCardData.getFormattedActiveDate());
		expirationDateTextView.setText(itinCardData.getFormattedExpirationDate());
		guestCountTextView.setText(itinCardData.getFormattedGuestCount());

		guestsLayout.removeAllViews();
		for (Traveler travler : itinCardData.getTravelers()) {
			TextView guestView = (TextView) inflate(getContext(), R.layout.include_itin_card_guest, null);
			guestView.setText(travler.getFullName());
			guestView.setCompoundDrawables(createGuestIcon(travler, R.drawable.ic_activity_guest), null, null, null);

			guestsLayout.addView(guestView);
		}

		detailsTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.openSite(getContext(), itinCardData.getDetailsUrl());
			}
		});

		return view;
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

	private Drawable createGuestIcon(Traveler travler, int iconResId) {
		final String text = travler.getFullName().substring(0, 1).toUpperCase();
		final Resources res = getResources();

		final Bitmap bitmap = BitmapFactory.decodeResource(res, iconResId).copy(Bitmap.Config.ARGB_8888, true);
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();

		final Paint paint = new Paint();
		paint.setColor(0xFFFFFFFF);
		paint.setTextSize(height * 0.6f);
		paint.setTypeface(FontCache.getTypeface(Font.ROBOTO_BOLD));
		paint.setAntiAlias(true);

		final Rect bounds = new Rect();
		paint.getTextBounds(text, 0, 1, bounds);

		final int textX = (width - bounds.width()) / 2;
		final int textY = height - ((height - bounds.height()) / 2);

		final Canvas canvas = new Canvas(bitmap);
		canvas.drawText(text, textX, textY, paint);

		BitmapDrawable drawable = new BitmapDrawable(res, bitmap);
		drawable.setBounds(0, 0, width, height);

		return drawable;
	}
}