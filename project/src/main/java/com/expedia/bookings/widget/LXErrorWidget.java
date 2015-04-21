package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXErrorWidget extends FrameLayout {

	public LXErrorWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.lx_error_widget, this);
	}

	@InjectView(R.id.error_main_container)
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
		toolbar.setPadding(0, statusBarHeight, 0, 0);
	}

	public void bind(final ApiError error) {
		bind(error, SearchType.EXPLICIT_SEARCH);
	}

	public void bind(final ApiError error, final SearchType searchType) {
		if (error == null || error.errorCode == null) {
			showDefaultError();
			return;
		}

		switch (error.errorCode) {

		case INVALID_INPUT:
			int resID = R.string.error_server;
			if (error.errorInfo.field.equals("lastName")) {
				resID = R.string.reservation_invalid_name;
			}
			else if (error.errorInfo.field.equals("firstName")) {
				resID = R.string.reservation_invalid_name;
			}
			else if (error.errorInfo.field.equals("phone")) {
				resID = R.string.reservation_invalid_phone;
			}
			bindText(R.drawable.error_default,
				resID,
				R.string.lx_invalid_input_text,
				R.string.edit_info);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.LXInvalidInput(error.errorInfo.field));
				}
			});
			break;

		case LX_SEARCH_NO_RESULTS:
			switch (searchType) {
			case DEFAULT_SEARCH:
				showSearchError(R.string.error_lx_current_location_search_message);
				break;
			case EXPLICIT_SEARCH:
				showSearchError(R.string.error_lx_search_message);
				break;
			}
			break;

		case SESSION_TIMEOUT:
			bindText(R.drawable.error_timeout,
				R.string.reservation_time_out,
				R.string.lx_session_timeout_text,
				R.string.edit_search);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.LXSessionTimeout());
				}
			});
			break;

		case TRIP_ALREADY_BOOKED:
			bindText(R.drawable.error_trip_booked,
				R.string.reservation_already_exists,
				R.string.lx_duplicate_trip_text,
				R.string.my_trips);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					NavUtils.goToItin(getContext());
				}
			});
			break;

		case PAYMENT_FAILED:
			bindText(R.drawable.error_payment,
				R.string.reservation_payment_failed,
				R.string.lx_payment_failed_text,
				R.string.edit_payment);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.LXPaymentFailed());
				}
			});
			break;

		case PRICE_CHANGE:
			bindText(R.drawable.error_price,
				R.string.lx_error_price_changed,
				R.string.lx_price_change_text,
				R.string.view_price_change);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.LXActivitySelectedRetry());
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
			R.string.error_server,
			R.string.lx_error_current_location_toolbar_text,
			R.string.retry);
		errorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((ActionBarActivity) getContext()).onBackPressed();
			}
		});
	}

	public void showSearchError(int errorMessageResId) {
		bindText(R.drawable.error_lx,
			errorMessageResId,
			R.string.lx_error_text,
			R.string.edit_search);
		errorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Events.post(new Events.LXShowSearchWidget());
			}
		});
	}

	private void bindText(int imageId, int textId, int toolbarTextId, int buttonTextId) {
		errorImage.setImageResource(imageId);
		errorText.setText(textId);
		toolbar.setTitle(toolbarTextId);
		errorButton.setText(buttonTextId);
	}

	public void setToolbarVisibility(int visibility) {
		toolbar.setVisibility(visibility);
	}
}
