package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;

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

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (carSearchSubscription != null) {
			carSearchSubscription.unsubscribe();
		}
	}

	//////////////////////////
	// Car Search Download

	Subscription carSearchSubscription;

	@OnClick(R.id.download_now_btn)
	public void startDownload() {
		carSearchSubscription = CarServices
			.getInstance()
			.doBoringCarSearch(carSearchSubscriber);
	}

	private Observer<CarSearch> carSearchSubscriber = new Observer<CarSearch>() {
		@Override
		public void onCompleted() {
			Log.d("TestCarSearchWidget - onCompleted");
			Ui.showToast(getContext(), "onComplete");
		}

		@Override
		public void onError(Throwable e) {
			Log.d("TestCarSearchWidget - onError", e);
		}

		@Override
		public void onNext(CarSearch carSearch) {
			Log.d("TestCarSearchWidget - onNext");
			CarDb.carSearch = carSearch;
		}
	};

}
