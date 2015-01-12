package com.expedia.bookings.widget;

import org.joda.time.DateTime;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarSearchResponse;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.services.DateTimeTypeAdapter;
import com.expedia.bookings.utils.Ui;
import com.google.gson.GsonBuilder;
import com.mobiata.android.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TestCarSearchWidget extends FrameLayout {

	public TestCarSearchWidget(Context context) {
		super(context);
	}

	public TestCarSearchWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TestCarSearchWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@InjectView(R.id.download_now_btn)
	Button downloadButton;

	@InjectView(R.id.text_scroll)
	android.widget.ScrollView scrollView;

	@InjectView(R.id.display_text)
	TextView displayText;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		scrollView.setVisibility(View.GONE);
	}

	@OnClick(R.id.download_now_btn)
	public void startDownload() {
		CarServices.getInstance().doBoringCarSearch(mCallback);
	}

	Callback<CarSearchResponse> mCallback = new Callback<CarSearchResponse>() {
		@Override
		public void success(CarSearchResponse carSearchResponse, Response response) {
			Ui.showToast(getContext(), "YOLO");
			displayText.setText(new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
				.create().toJson(carSearchResponse));
			downloadButton.setVisibility(View.GONE);
			scrollView.setVisibility(View.VISIBLE);

		}

		@Override
		public void failure(RetrofitError error) {
			Ui.showToast(getContext(), "NOLO");
			Log.d("SWAG", error.getMessage());
		}
	};
}
