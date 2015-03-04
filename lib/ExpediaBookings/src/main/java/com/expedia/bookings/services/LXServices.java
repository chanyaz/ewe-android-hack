package com.expedia.bookings.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.expedia.bookings.data.lx.ActivityDetailsParams;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Func1;

public class LXServices {

	LXApi lxApi;

	private OkHttpClient client;
	private Scheduler observeOn;
	private Scheduler subscribeOn;

	public LXServices(String endPoint, OkHttpClient client, Scheduler observeOn, Scheduler subscribeOn) {
		this.client = client;
		this.observeOn = observeOn;
		this.subscribeOn = subscribeOn;

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endPoint)
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.setClient(new OkClient(this.client))
			.build();

		lxApi = adapter.create(LXApi.class);
	}

	public Subscription lxSearch(LXSearchParams searchParams, Observer<List<LXActivity>> observer) {
		return lxApi
			.searchLXActivities(searchParams.location, searchParams.toServerStartDate(), searchParams.toServerEndDate())
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.map(sToList)
			.subscribe(observer);
	}

	private Func1<LXSearchResponse, List<LXActivity>> sToList = new Func1<LXSearchResponse, List<LXActivity>>() {
		@Override
		public List<LXActivity> call(LXSearchResponse lxSearchResponse) {
			return lxSearchResponse.activities;
		}
	};

	public Subscription lxDetails(ActivityDetailsParams searchParams, Observer<ActivityDetailsResponse> observer) {
		return lxApi
			.activityDetails(searchParams.activityId, searchParams.toServerStartDate(), searchParams.toServerEndDate())
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.subscribe(observer);
	}

	public Subscription lxCheckout(LXCheckoutParams checkoutParams, Observer<LXCheckoutResponse> observer) {
		return lxApi.
			checkout(createCheckoutParams(checkoutParams))
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.subscribe(observer);
	}

	private Map<String, Object> createCheckoutParams(LXCheckoutParams checkoutParams) {
		Map<String, Object> params = new HashMap<>();
		params.put("streetAddress", checkoutParams.streetAddress);
		params.put("firstName", checkoutParams.firstName);
		params.put("lastName", checkoutParams.lastName);
		params.put("phone", checkoutParams.phone);
		params.put("checkInDate", checkoutParams.checkInDate);
		params.put("phoneCountryCode", checkoutParams.phoneCountryCode);
		params.put("tripId", checkoutParams.tripId);
		params.put("state", checkoutParams.state);
		params.put("city", checkoutParams.city);
		params.put("country", checkoutParams.country);
		params.put("postalCode", checkoutParams.postalCode);
		params.put("expectedFareCurrencyCode", checkoutParams.expectedFareCurrencyCode);
		params.put("expectedFareCurrencyCode", checkoutParams.expectedFareCurrencyCode);
		params.put("expectedTotalFare", checkoutParams.expectedTotalFare);
		params.put("nameOnCard", checkoutParams.nameOnCard);
		params.put("creditCardNumber", checkoutParams.creditCardNumber);
		params.put("expirationDateYear", checkoutParams.expirationDateYear);
		params.put("expirationDateMonth", checkoutParams.expirationDateMonth);
		params.put("cvv", checkoutParams.cvv);
		params.put("email", checkoutParams.email);
		return params;
	}

}
