package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarApiError;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ErrorWidget extends FrameLayout {

	public ErrorWidget(Context context) {
		super(context);
	}

	public ErrorWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ErrorWidget(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@InjectView(R.id.main_container)
	ViewGroup root;

	@InjectView(R.id.error_image)
	ImageView errorImage;

	@InjectView(R.id.error_action_button)
	Button errorButton;

	@InjectView(R.id.error_text)
	TextView errorText;

	@InjectView(R.id.error_toolbar)
	Toolbar toolbar;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.error_widget, this);
		ButterKnife.inject(this);

		Drawable nav = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp).mutate();
		nav.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(nav);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = Ui.obtainThemeColor(getContext(), R.attr.primary_color);
			addView(Ui.setUpStatusBar(getContext(), toolbar, root, color));
		}
	}

	public void bind(final CarCheckoutResponse response) {
		if (response == null) {
			showDefaultError();
			return;
		}

		CarApiError error = response.getFirstError();
		switch (error.errorCode) {
		case PAYMENT_FAILED:
			bindText(R.drawable.error_payment,
				R.string.reservation_payment_failed,
				R.string.cars_payment_failed_text,
				R.string.edit_payment);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.CarsPaymentFailed(response));
				}
			});
			break;
		case PRICE_CHANGE:
			bindText(R.drawable.error_price,
				R.string.reservation_price_change,
				R.string.cars_price_change_text,
				R.string.view_price_change);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.CarsPriceChange(response));
				}
			});
			break;
		case SESSION_TIMEOUT:
			bindText(R.drawable.error_timeout,
				R.string.reservation_time_out,
				R.string.cars_session_timeout_text,
				R.string.edit_search);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.CarsSessionTimeout(response));
				}
			});
			break;
		case TRIP_ALREADY_BOOKED:
			bindText(R.drawable.error_trip_booked,
				R.string.reservation_already_exists,
				R.string.cars_dupe_trip_text,
				R.string.my_trips);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					NavUtils.goToItin(getContext());
				}
			});
			break;
		case UNKNOWN_ERROR:
		case OMS_ERROR:
		default:
			showDefaultError();
			break;
		}
	}

	private void showDefaultError() {
		bindText(R.drawable.error_default,
			R.string.oops,
			R.string.cars_error_text,
			R.string.retry);
		errorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((ActionBarActivity) getContext()).onBackPressed();
			}
		});
	}

	private void bindText(int imageId, int textId, int toolbarTextId, int buttonTextId) {
		errorImage.setImageResource(imageId);
		errorText.setText(textId);
		toolbar.setTitle(toolbarTextId);
		errorButton.setText(buttonTextId);
	}
}
