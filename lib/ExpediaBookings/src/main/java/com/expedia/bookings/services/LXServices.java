package com.expedia.bookings.services;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXCreateTripParams;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.LXUtils;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.LocalDate;
import retrofit.RequestInterceptor;
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

	public LXServices(String endPoint, OkHttpClient client, RequestInterceptor requestInterceptor, Scheduler observeOn, Scheduler subscribeOn, RestAdapter.LogLevel logLevel) {
		this.client = client;
		this.observeOn = observeOn;
		this.subscribeOn = subscribeOn;

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endPoint)
			.setRequestInterceptor(requestInterceptor)
			.setLogLevel(logLevel)
			.setClient(new OkClient(this.client))
			.build();

		lxApi = adapter.create(LXApi.class);
	}

	public Subscription lxSearch(LXSearchParams searchParams, Observer<LXSearchResponse> observer) {
		return lxApi
			.searchLXActivities(searchParams.location, searchParams.toServerStartDate(), searchParams.toServerEndDate())
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.map(HANDLE_SEARCH_ERROR)
			.map(PUT_BEST_APPLICABLE_CATEGORY)
			.subscribe(observer);
	}

	private static final Func1<LXSearchResponse, LXSearchResponse> PUT_BEST_APPLICABLE_CATEGORY = new Func1<LXSearchResponse, LXSearchResponse>() {
		@Override
		public LXSearchResponse call(LXSearchResponse lxSearchResponse) {
			for (LXActivity activity : lxSearchResponse.activities) {
				activity.bestApplicableCategoryEN = LXUtils.bestApplicableCategory(activity.categories);
				activity.bestApplicableCategoryLocalized = lxSearchResponse.filterCategories.get(activity.bestApplicableCategoryEN).displayValue;
			}
			return lxSearchResponse;
		}
	};

	private static final Func1<LXSearchResponse, LXSearchResponse> HANDLE_SEARCH_ERROR = new Func1<LXSearchResponse, LXSearchResponse>() {
		@Override
		public LXSearchResponse call(LXSearchResponse lxSearchResponse) {
			if (lxSearchResponse.searchFailure) {
				throw new RuntimeException();
			}
			return lxSearchResponse;
		}
	};

	public Subscription lxDetails(final LXActivity lxActivity, LocalDate startDate, LocalDate endDate, Observer<ActivityDetailsResponse> observer) {
		return lxApi
			.activityDetails(lxActivity.id, DateUtils.convertToLXDate(startDate), DateUtils.convertToLXDate(endDate))
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.map(HANDLE_ACTIVITY_DETAILS_ERROR)
			.map(PUT_MONEY_IN_TICKETS)
			.map(new Func1<ActivityDetailsResponse, ActivityDetailsResponse>() {
				@Override
				public ActivityDetailsResponse call(ActivityDetailsResponse response) {
					response.bestApplicableCategoryEN = lxActivity.bestApplicableCategoryEN;
					response.bestApplicableCategoryLocalized = lxActivity.bestApplicableCategoryLocalized;
					return response;
				}
			})
			.subscribe(observer);
	}

	private static final Func1<ActivityDetailsResponse, ActivityDetailsResponse> HANDLE_ACTIVITY_DETAILS_ERROR = new Func1<ActivityDetailsResponse, ActivityDetailsResponse>() {
		@Override
		public ActivityDetailsResponse call(ActivityDetailsResponse response) {
			if (response == null || response.offersDetail == null || response.offersDetail.offers == null) {
				throw new RuntimeException();
			}
			return response;
		}
	};

	private static final Func1<ActivityDetailsResponse, ActivityDetailsResponse> PUT_MONEY_IN_TICKETS = new Func1<ActivityDetailsResponse, ActivityDetailsResponse>() {
		@Override
		public ActivityDetailsResponse call(ActivityDetailsResponse response) {
			for (Offer offer : response.offersDetail.offers) {
				for (AvailabilityInfo availabilityInfo : offer.availabilityInfo) {
					for (Ticket ticket : availabilityInfo.tickets) {
						ticket.money = new Money(ticket.amount, response.currencyCode);
					}
				}
			}
			return response;
		}
	};

	public Subscription createTrip(LXCreateTripParams createTripParams, Observer<LXCreateTripResponse> observer) {
		return lxApi.
			createTrip(createTripParams)
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.subscribe(observer);
	}

	public Subscription lxCheckout(LXCheckoutParams checkoutParams, Observer<LXCheckoutResponse> observer) {
		return lxApi.
			checkout(checkoutParams.toQueryMap())
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.subscribe(observer);
	}
}
