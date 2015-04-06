package com.expedia.bookings.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.ApiException;
import com.expedia.bookings.data.cars.BaseApiResponse;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXCreateTripParams;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LXSortFilterMetadata;
import com.expedia.bookings.data.lx.LXSortType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.LXUtils;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

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
		return lxSearch(searchParams)
			.subscribe(observer);
	}

	public Observable<LXSearchResponse> lxSearch(LXSearchParams searchParams) {
		return lxApi
			.searchLXActivities(searchParams.location, searchParams.toServerStartDate(), searchParams.toServerEndDate())
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.map(HANDLE_SEARCH_ERROR)
			.map(PUT_MONEY_IN_ACTIVITIES)
			.map(PUT_BEST_APPLICABLE_CATEGORY);
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
				ApiError apiError = new ApiError();
				apiError.errorCode = ApiError.Code.LX_SEARCH_NO_RESULTS;
				throw new ApiException(apiError);
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
			.doOnNext(HANDLE_ERRORS)
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.subscribe(observer);
	}

	public Subscription lxCheckout(LXCheckoutParams checkoutParams, Observer<LXCheckoutResponse> observer) {
		return lxApi.
			checkout(checkoutParams.toQueryMap())
			.doOnNext(HANDLE_ERRORS)
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.subscribe(observer);
	}

	private static final Action1<BaseApiResponse> HANDLE_ERRORS = new Action1<BaseApiResponse>() {
		@Override
		public void call(BaseApiResponse response) {
			if (response.hasErrors() && !response.hasPriceChange()) {
				throw new ApiException(response.getFirstError());
			}
		}
	};

	private static final Func1<LXSearchResponse, LXSearchResponse> PUT_MONEY_IN_ACTIVITIES = new Func1<LXSearchResponse, LXSearchResponse>() {
		@Override
		public LXSearchResponse call(LXSearchResponse response) {
			String currencyCode = response.currencyCode;
			for (LXActivity activity : response.activities) {
				activity.price = new Money(activity.fromPriceValue, currencyCode);
			}
			return response;
		}
	};

	private class CombineSearchResponseAndSortFilterStreams implements Func2<LXSearchResponse, LXSortFilterMetadata, LXSearchResponse> {

		private List<LXActivity> unfilteredActivities = new ArrayList<>();

		@Override
		public LXSearchResponse call(LXSearchResponse lxSearchResponse, LXSortFilterMetadata lxSortFilterMetadata) {

			if (lxSortFilterMetadata.lxCategoryMetadataMap == null) {
				unfilteredActivities.addAll(lxSearchResponse.activities);
				return lxSearchResponse;
			}
			else {
				lxSearchResponse.activities = applySortFilter(unfilteredActivities, lxSearchResponse, lxSortFilterMetadata.sort);
				lxSearchResponse.filterCategories = lxSortFilterMetadata.lxCategoryMetadataMap;
				return lxSearchResponse;
			}
		}
	}

	public Subscription lxSearchSortFilter(LXSearchParams lxSearchParams, Observable<LXSortFilterMetadata> lxSortFilterMetadataObservable,
										   Observer<LXSearchResponse> searchResultObserver) {

		Observable<LXSearchResponse> lxSearchResponseObservable = lxSearch(lxSearchParams);

		return Observable.combineLatest(lxSearchResponseObservable, lxSortFilterMetadataObservable, new CombineSearchResponseAndSortFilterStreams())
			.subscribeOn(this.subscribeOn)
			.observeOn(this.observeOn)
			.subscribe(searchResultObserver);
	}

	public List<LXActivity> applySortFilter(List<LXActivity> unfilteredActivities, LXSearchResponse lxSearchResponse, LXSortType sort) {

		Set<LXActivity> filteredSet = new LinkedHashSet<>();
		for (int i = 0; i < unfilteredActivities.size(); i++) {
			for (Map.Entry<String, LXCategoryMetadata> filterCategory : lxSearchResponse.filterCategories.entrySet()) {
				LXCategoryMetadata lxCategoryMetadata = filterCategory.getValue();
				if (lxCategoryMetadata.checked) {
					if (unfilteredActivities.get(i).categories.contains(lxCategoryMetadata.displayValue)) {
						filteredSet.add(unfilteredActivities.get(i));
					}
				}
			}
		}

		lxSearchResponse.activities.clear();

		// Filtering.
		if (filteredSet.size() != 0) {
			lxSearchResponse.activities.addAll(filteredSet);
		}
		else {
			lxSearchResponse.activities.addAll(unfilteredActivities);
		}

		// Sorting.
		if (sort == LXSortType.PRICE) {
			Collections.sort(lxSearchResponse.activities, new Comparator<LXActivity>() {
				@Override
				public int compare(LXActivity lhs, LXActivity rhs) {
					Money leftMoney = lhs.price;
					Money rightMoney = rhs.price;
					return leftMoney.compareTo(rightMoney);
				}
			});
		}
		return lxSearchResponse.activities;
	}
}
