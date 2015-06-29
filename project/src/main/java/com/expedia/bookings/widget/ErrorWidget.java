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

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ErrorWidget extends FrameLayout {

	public ErrorWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.error_widget, this);
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

	public void setToolbarVisibility(int visibility) {
		toolbar.setVisibility(visibility);
	}

	public void bind(final ApiError error) {
		if (error == null) {
			showDefaultError();
			return;
		}

		switch (error.errorCode) {
		case CAR_SEARCH_ERROR:
			showDefaultSearchError();
			break;
		case CAR_FILTER_NO_RESULTS:
			showNoFilterSearchResultError();
			break;
		case CAR_PRODUCT_NOT_AVAILABLE:
			showNoProductSearchError();
			break;
		case CAR_SERVICE_ERROR:
			showDefaultSearchError();
			break;
		case PAYMENT_FAILED:
			bindText(R.drawable.error_payment,
				getResources().getString(R.string.reservation_payment_failed),
				R.string.cars_payment_failed_text,
				R.string.edit_payment);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.CarsPaymentFailed());
				}
			});
			break;
		case INVALID_INPUT:
			String message = Phrase.from(getContext(), R.string.error_server_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.format().toString();
			if (error.errorInfo.field.equals("mainMobileTraveler.lastName")) {
				message = getResources().getString(R.string.reservation_invalid_name);
			}
			else if (error.errorInfo.field.equals("mainMobileTraveler.firstName")) {
				message = getResources().getString(R.string.reservation_invalid_name);
			}
			else if (error.errorInfo.field.equals("mainMobileTraveler.phone")) {
				message = getResources().getString(R.string.reservation_invalid_phone);
			}
			bindText(R.drawable.error_default,
				message,
				R.string.cars_invalid_input_text,
				R.string.edit_info);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.CarsInvalidInput(error.errorInfo.field));
				}
			});
			break;
		case PRICE_CHANGE:
			bindText(R.drawable.error_price,
				getResources().getString(R.string.reservation_price_change),
				R.string.cars_price_change_text,
				R.string.view_price_change);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.CarsPriceChange());
				}
			});
			break;
		case SESSION_TIMEOUT:
			bindText(R.drawable.error_timeout,
				getResources().getString(R.string.reservation_time_out),
				R.string.cars_session_timeout_text,
				R.string.edit_search);
			errorButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.CarsSessionTimeout());
				}
			});
			break;
		case TRIP_ALREADY_BOOKED:
			bindText(R.drawable.error_trip_booked,
				getResources().getString(R.string.reservation_already_exists),
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
		bindText(Ui.obtainThemeResID(getContext(), R.attr.skin_carsErrorDefaultDrawable),
			Phrase.from(getContext(), R.string.error_server_TEMPLATE).put("brand", BuildConfig.brand).format()
				.toString(),
			R.string.cars_error_text,
			R.string.retry);
		errorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((ActionBarActivity) getContext()).onBackPressed();
			}
		});
	}

	public void showDefaultSearchError() {
		bindText(Ui.obtainThemeResID(getContext(), R.attr.skin_carsErrorDefaultDrawable),
			Phrase.from(getContext(), R.string.error_server_TEMPLATE).put("brand", BuildConfig.brand).format()
				.toString(),
			R.string.cars_error_text,
			R.string.retry);
		errorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Events.post(new Events.CarsSearchFailed());
			}
		});
	}

	public void showNoProductSearchError() {
		bindText(R.drawable.car,
			getResources().getString(R.string.error_car_search_message),
			R.string.cars_no_results_text,
			R.string.edit_search);
		errorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Events.post(new Events.CarsGoToSearch());
			}
		});
	}

	public void showNoFilterSearchResultError() {
		bindText(R.drawable.car,
			getResources().getString(R.string.filter_no_car_search_results_message),
			R.string.cars_no_results_text,
			R.string.edit_filters);
		errorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Events.post(new Events.CarsShowFilteredSearchResults());
			}
		});
	}

	private void bindText(int imageId, String text, int toolbarTextId, int buttonTextId) {
		errorImage.setImageResource(imageId);
		errorText.setText(text);
		toolbar.setTitle(toolbarTextId);
		errorButton.setText(buttonTextId);
	}
}
