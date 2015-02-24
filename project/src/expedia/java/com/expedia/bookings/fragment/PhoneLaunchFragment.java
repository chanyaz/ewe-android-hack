package com.expedia.bookings.fragment;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.CarsActivity;
import com.expedia.bookings.data.hotels.NearbyHotelOffer;
import com.expedia.bookings.data.hotels.NearbyHotelParams;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.services.NearbyServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.DbUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;

import retrofit.RequestInterceptor;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	// Used to prevent launching of both flight and hotel activities at once
	// (as it is otherwise possible to quickly click on both sides).
	private boolean mLaunchingActivity;

	private NearbyServices nearbyServices;

	private View nearbyDealsList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_new_phone_launch, container, false);

		Ui.findView(v, R.id.hotels_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.flights_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.cars_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.see_all_hotels_button).setOnClickListener(mHeaderItemOnClickListener);

		nearbyDealsList = Ui.findView(v, R.id.nearby_deals_widget);

		RequestInterceptor requestInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				request.addEncodedQueryParam("clientid", ServicesUtil.generateClientId(getActivity()));
				request.addEncodedQueryParam("sourceType", ServicesUtil.generateSourceType());

				String langid = ServicesUtil.generateLangId();
				if (Strings.isNotEmpty(langid)) {
					request.addEncodedQueryParam("langid", langid);
				}

				if (EndPoint.requestRequiresSiteId(getActivity())) {
					request.addEncodedQueryParam("siteid", ServicesUtil.generateSiteId());
				}
			}
		};
		nearbyServices = new NearbyServices(EndPoint.getE3EndpointUrl(getActivity(), true /*isSecure*/),
			DbUtils.generateOkHttpClient(),
			requestInterceptor,
			AndroidSchedulers.mainThread(),
			Schedulers.io());

		return v;
	}

	private Subscriber<List<NearbyHotelOffer>> downloadListener = new Subscriber<List<NearbyHotelOffer>>() {
		@Override
		public void onCompleted() {
			Log.d("nearbyhotelstatus", "completed");
		}

		@Override
		public void onError(Throwable e) {
			Log.d("nearbyhotelstatus", e.getMessage());
		}

		@Override
		public void onNext(List<NearbyHotelOffer> nearbyHotelResponse) {
			Ui.showToast(getActivity(), "We have hotels!");
			Events.post(new Events.NearbyHotelSearchResults(nearbyHotelResponse));
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		NearbyHotelParams params = new NearbyHotelParams("51.5033630", "-0.1276250", "1", "2015-03-03", "2015-03-04");
		nearbyServices.hotelSearch(params, downloadListener);
		mLaunchingActivity = false;

	}

	// Listeners

	private final View.OnClickListener mHeaderItemOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Bundle animOptions = AnimUtils.createActivityScaleBundle(v);

			switch (v.getId()) {
			case R.id.see_all_hotels_button:
			case R.id.hotels_button:
				if (!mLaunchingActivity) {
					mLaunchingActivity = true;
					NavUtils.goToHotels(getActivity(), animOptions);
					OmnitureTracking.trackLinkLaunchScreenToHotels(getActivity());
				}
				break;
			case R.id.flights_button:
				if (!mLaunchingActivity) {
					mLaunchingActivity = true;
					NavUtils.goToFlights(getActivity(), animOptions);
					OmnitureTracking.trackLinkLaunchScreenToFlights(getActivity());
				}
				break;
			case R.id.cars_button:
				Intent carsIntent = new Intent(getActivity(), CarsActivity.class);
				getActivity().startActivity(carsIntent);
				break;
			}

			cleanUp();
		}
	};

	@Override
	public void startMarquee() {

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void reset() {

	}
}
