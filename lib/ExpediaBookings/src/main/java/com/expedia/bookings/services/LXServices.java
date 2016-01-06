package com.expedia.bookings.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.BaseApiResponse;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.data.lx.LXCategorySortOrder;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXCreateTripParams;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXRedemptionType;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LXSortFilterMetadata;
import com.expedia.bookings.data.lx.LXSortType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.utils.Strings;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func2;

public class LXServices {

	private static LXSearchResponse cachedLXSearchResponse = new LXSearchResponse();
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

	public Subscription lxCategorySearch(LXSearchParams searchParams, Observer<LXSearchResponse> observer) {
		return lxApi
			.searchLXActivities(searchParams.location, searchParams.toServerStartDate(), searchParams.toServerEndDate())
			.doOnNext(HANDLE_SEARCH_ERROR)
			.doOnNext(ACTIVITIES_MONEY_TITLE)
			.doOnNext(PUT_POPULARITY_COUNTER_FOR_SORT)
			.doOnNext(CACHE_SEARCH_RESPONSE)
			.doOnNext(PUT_ACTIVITIES_IN_CATEGORY)
			.doOnNext(PUT_CATEGORY_KEY_IN_CATEGORY_METADATA)
			.doOnNext(PUT_CATEGORY_SORT_IN_CATEGORY_METADATA)
			.subscribeOn(subscribeOn)
			.observeOn(observeOn)
			.subscribe(observer);
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
			.doOnNext(HANDLE_SEARCH_ERROR)
			.doOnNext(ACTIVITIES_MONEY_TITLE)
			.doOnNext(CACHE_SEARCH_RESPONSE);
	}

	private static final Action1<LXSearchResponse> HANDLE_SEARCH_ERROR = new Action1<LXSearchResponse>() {
		@Override
		public void call(LXSearchResponse lxSearchResponse) {
			if (lxSearchResponse.searchFailure) {
				ApiError apiError = new ApiError(ApiError.Code.LX_SEARCH_NO_RESULTS);
				apiError.regionId = lxSearchResponse.regionId;
				ApiError.ErrorInfo errorInfo = new ApiError.ErrorInfo();
				errorInfo.cause = "No results from api.";
				apiError.errorInfo = errorInfo;
				throw apiError;
			}
		}
	};

	private static final Action1<LXSearchResponse> PUT_POPULARITY_COUNTER_FOR_SORT = new Action1<LXSearchResponse>() {
		@Override
		public void call(LXSearchResponse lxSearchResponse) {
			int popularityForClientSort = 0;
			for (LXActivity activity : lxSearchResponse.activities) {
				activity.popularityForClientSort = popularityForClientSort++;
			}
		}
	};

	private static final Action1<LXSearchResponse> PUT_CATEGORY_KEY_IN_CATEGORY_METADATA = new Action1<LXSearchResponse>() {
		@Override
		public void call(LXSearchResponse lxSearchResponse) {
			for (Map.Entry<String, LXCategoryMetadata> filterCategory : lxSearchResponse.filterCategories.entrySet()) {
				String categoryKeyEN = filterCategory.getKey();
				LXCategoryMetadata categoryValue = filterCategory.getValue();
				categoryValue.categoryKeyEN = categoryKeyEN;
			}
		}
	};

	private static final Action1<LXSearchResponse> PUT_CATEGORY_SORT_IN_CATEGORY_METADATA = new Action1<LXSearchResponse>() {
		@Override
		public void call(LXSearchResponse lxSearchResponse) {
			for (Map.Entry<String, LXCategoryMetadata> filterCategory : lxSearchResponse.filterCategories.entrySet()) {
				String categoryKeyEN = filterCategory.getKey();
				LXCategoryMetadata categoryValue = filterCategory.getValue();
				for (LXCategorySortOrder sortOrder : LXCategorySortOrder.values()) {
					if (LXUtils.whitelistAlphanumericFromCategoryKey(categoryKeyEN)
						.equalsIgnoreCase(sortOrder.toString())) {
						categoryValue.sortOrder = sortOrder;
						break;
					}
				}
			}
		}
	};

	private static final Action1<LXSearchResponse> PUT_ACTIVITIES_IN_CATEGORY = new Action1<LXSearchResponse>() {
		@Override
		public void call(LXSearchResponse lxSearchResponse) {
			for (Map.Entry<String, LXCategoryMetadata> filterCategory : lxSearchResponse.filterCategories.entrySet()) {
				String categoryKeyEN = filterCategory.getKey();
				LXCategoryMetadata categoryValue = filterCategory.getValue();
				for (LXActivity activity: lxSearchResponse.activities) {
					if (CollectionUtils.isNotEmpty(activity.categories) && activity.categories.contains(categoryKeyEN)) {
						categoryValue.activities.add(activity);
					}
				}
			}
		}
	};

	public Subscription lxDetails(String activityId, String location, LocalDate startDate, LocalDate endDate,
		Observer<ActivityDetailsResponse> observer) {
		return lxApi
			.activityDetails(activityId, location, DateUtils.convertToLXDate(startDate),
				DateUtils.convertToLXDate(endDate))
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.doOnNext(HANDLE_ACTIVITY_DETAILS_ERROR)
			.doOnNext(ACCEPT_ONLY_KNOWN_TICKET_TYPES)
			.doOnNext(DETAILS_TITLE_AND_TICKET_MONEY)
			.subscribe(observer);
	}

	private static final Action1<ActivityDetailsResponse> HANDLE_ACTIVITY_DETAILS_ERROR = new Action1<ActivityDetailsResponse>() {
		@Override
		public void call(ActivityDetailsResponse response) {
			if (response == null || response.offersDetail == null || response.offersDetail.offers == null) {
				throw new ApiError(ApiError.Code.LX_DETAILS_FETCH_ERROR);
			}
		}
	};

	// Add money in offer tickets for easier handling and remove &quot; from activity and offer title.
	private static final Action1<ActivityDetailsResponse> DETAILS_TITLE_AND_TICKET_MONEY = new Action1<ActivityDetailsResponse>() {
		@Override
		public void call(ActivityDetailsResponse response) {
			String bags = response.bags;
			String passengers = response.passengers;
			boolean isGroundTransport = response.isGroundTransport;
			LXRedemptionType redemptionType = response.redemptionType;

			response.title = Strings.escapeQuotes(response.title);

			for (Offer offer : response.offersDetail.offers) {
				offer.title = Strings.escapeQuotes(offer.title);
				offer.bags = bags;
				offer.passengers = passengers;
				offer.isGroundTransport = isGroundTransport;
				offer.redemptionType = redemptionType;
				for (AvailabilityInfo availabilityInfo : offer.availabilityInfo) {
					for (Ticket ticket : availabilityInfo.tickets) {
						ticket.money = new Money(ticket.amount, response.currencyCode);
					}
				}
			}
		}
	};

	private static final Action1<ActivityDetailsResponse> ACCEPT_ONLY_KNOWN_TICKET_TYPES = new Action1<ActivityDetailsResponse>() {
		@Override
		public void call(ActivityDetailsResponse response) {
			Iterator<Offer> offerIterator = response.offersDetail.offers.iterator();
			//Iterate over offers
			while (offerIterator.hasNext()) {
				Offer offer = offerIterator.next();
				Iterator<AvailabilityInfo> availabilityInfoIterator = offer.availabilityInfo.iterator();
				//Iterate over Offers's Availability Infos
				while (availabilityInfoIterator.hasNext()) {
					AvailabilityInfo availabilityInfo = availabilityInfoIterator.next();
					Iterator<Ticket> ticketIterator = availabilityInfo.tickets.iterator();
					//Iterate over Offers's Availability Info's Tickets
					while (ticketIterator.hasNext()) {
						if (ticketIterator.next().code == null) {
							//Remove Unknown Ticket Code
							ticketIterator.remove();
						}
					}

					//In case no tickets are known, rid off the availability info
					if (availabilityInfo.tickets.size() == 0) {
						availabilityInfoIterator.remove();
					}
				}

				//In case no availability info exists, rid off the offer
				if (offer.availabilityInfo.size() == 0) {
					offerIterator.remove();
				}
			}
		}
	};

	public Subscription createTrip(LXCreateTripParams createTripParams, Money originalPrice, Observer<LXCreateTripResponse> observer) {
		return lxApi.
			createTrip(createTripParams)
			.doOnNext(HANDLE_ERRORS)
			.doOnNext(new SearchOriginalPriceInjector(originalPrice))
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.subscribe(observer);
	}

	public Subscription lxCheckout(LXCheckoutParams checkoutParams , Observer<LXCheckoutResponse> observer) {
		Money originalPrice = new Money(checkoutParams.expectedTotalFare, checkoutParams.expectedFareCurrencyCode);
		return lxApi.
			checkout(checkoutParams.toQueryMap())
			.doOnNext(HANDLE_ERRORS)
			.doOnNext(new CreateTripOriginalPriceInjector(originalPrice))
			.observeOn(this.observeOn)
			.subscribeOn(this.subscribeOn)
			.subscribe(observer);
	}

	private static final Action1<BaseApiResponse> HANDLE_ERRORS = new Action1<BaseApiResponse>() {
		@Override
		public void call(BaseApiResponse response) {
			if (response.hasErrors() && !response.hasPriceChange()) {
				throw response.getFirstError();
			}
		}
	};

	private class SearchOriginalPriceInjector implements Action1<LXCreateTripResponse> {
		private Money originalPrice;

		public SearchOriginalPriceInjector(Money originalPrice) {
			this.originalPrice = originalPrice;
		}

		@Override
		public void call(LXCreateTripResponse lxCreateTripResponse) {
			//Set Original Price in case there was a Price Change
			if (lxCreateTripResponse.hasPriceChange()) {
				lxCreateTripResponse.originalPrice = originalPrice;
			}
		}
	}

	private class CreateTripOriginalPriceInjector implements Action1<LXCheckoutResponse> {
		Money originalPrice;

		public CreateTripOriginalPriceInjector(Money originalPrice) {
			this.originalPrice = originalPrice;
		}

		@Override
		public void call(LXCheckoutResponse lxCheckoutResponse) {
			if (lxCheckoutResponse.hasPriceChange()) {
				lxCheckoutResponse.originalPrice = originalPrice;
			}
		}
	}

	// Add money in tickets for easier handling and remove &quot; from activity title.
	private static final Action1<LXSearchResponse> ACTIVITIES_MONEY_TITLE = new Action1<LXSearchResponse>() {
		@Override
		public void call(LXSearchResponse response) {
			String currencyCode = response.currencyCode;
			for (LXActivity activity : response.activities) {
				activity.price = new Money(activity.fromPriceValue, currencyCode);
				activity.originalPrice = new Money(activity.fromOriginalPriceValue, currencyCode);
				activity.title = Strings.escapeQuotes(activity.title);
			}
		}
	};

	private class CombineSearchResponseAndSortFilterStreams implements Func2<LXSearchResponse, LXSortFilterMetadata, LXSearchResponse> {

		@Override
		public LXSearchResponse call(LXSearchResponse lxSearchResponse, LXSortFilterMetadata lxSortFilterMetadata) {

			if (lxSortFilterMetadata.lxCategoryMetadataMap == null) {
				// No filters Applied.
				lxSearchResponse.activities.clear();
				lxSearchResponse.activities.addAll(lxSearchResponse.unFilteredActivities);
				for (Map.Entry<String, LXCategoryMetadata> filterCategory : lxSearchResponse.filterCategories
					.entrySet()) {
					LXCategoryMetadata lxCategoryMetadata = filterCategory.getValue();
					lxCategoryMetadata.checked = false;
				}
			}
			else {
				lxSearchResponse.activities = applySortFilter(lxSearchResponse.unFilteredActivities, lxSearchResponse,
					lxSortFilterMetadata);
			}
			return lxSearchResponse;
		}
	}

	private class SortCategorySearchResponse implements Func2<LXCategoryMetadata, LXSortType, LXCategoryMetadata> {

		@Override
		public LXCategoryMetadata call(LXCategoryMetadata lxCategoryMetadata, LXSortType lxSortType) {
			if (lxSortType == LXSortType.PRICE) {
				Collections.sort(lxCategoryMetadata.activities, new Comparator<LXActivity>() {
					@Override
					public int compare(LXActivity lhs, LXActivity rhs) {
						Money leftMoney = lhs.price;
						Money rightMoney = rhs.price;
						return leftMoney.compareTo(rightMoney);
					}
				});
			}
			else if (lxSortType == LXSortType.POPULARITY) {
				Collections.sort(lxCategoryMetadata.activities, new Comparator<LXActivity>() {
					@Override
					public int compare(LXActivity lhs, LXActivity rhs) {
						return (lhs.popularityForClientSort < rhs.popularityForClientSort) ? -1 :
							((lhs.popularityForClientSort == rhs.popularityForClientSort) ? 0 : 1);
					}
				});
			}
			return lxCategoryMetadata;
		}
	}

	public Subscription lxCategorySort(LXCategoryMetadata category, LXSortType lxSortType, Observer<LXCategoryMetadata> categorySortObserver) {

		return (Observable.combineLatest(Observable.just(category), Observable.just(lxSortType),
			new SortCategorySearchResponse())
			.subscribeOn(this.subscribeOn)
				.observeOn(this.observeOn)
				.subscribe(categorySortObserver));
	}

	public Subscription lxSearchSortFilter(LXSearchParams lxSearchParams, LXSortFilterMetadata lxSortFilterMetadata,
		Observer<LXSearchResponse> searchResultFilterObserver) {
		Observable<LXSearchResponse> lxSearchResponseObservable =
			lxSearchParams == null ? Observable.just(cachedLXSearchResponse) : lxSearch(lxSearchParams);

		return (lxSortFilterMetadata != null ?
			Observable.combineLatest(lxSearchResponseObservable, Observable.just(lxSortFilterMetadata),
				new CombineSearchResponseAndSortFilterStreams()) :
			lxSearch(lxSearchParams))
			.doOnNext(new IsFromCachedResponseInjector(lxSearchParams == null))
			.subscribeOn(this.subscribeOn)
			.observeOn(this.observeOn)
			.subscribe(searchResultFilterObserver);
	}

	private class IsFromCachedResponseInjector implements Action1<LXSearchResponse> {
		private boolean isFromCachedResponse;

		IsFromCachedResponseInjector(boolean isFromCachedResponse) {
			this.isFromCachedResponse = isFromCachedResponse;
		}

		@Override
		public void call(LXSearchResponse lxSearchResponse) {
			lxSearchResponse.isFromCachedResponse = isFromCachedResponse;
		}
	}
	public List<LXActivity> applySortFilter(List<LXActivity> unfilteredActivities, LXSearchResponse lxSearchResponse, LXSortFilterMetadata lxSortFilterMetadata) {

		Set<LXActivity> filteredSet = new LinkedHashSet<>();
		for (int i = 0; i < unfilteredActivities.size(); i++) {
			for (Map.Entry<String, LXCategoryMetadata> filterCategory : lxSortFilterMetadata.lxCategoryMetadataMap.entrySet()) {
				LXCategoryMetadata lxCategoryMetadata = filterCategory.getValue();
				String lxCategoryMetadataKey = filterCategory.getKey();
				if (lxCategoryMetadata.checked) {
					if (unfilteredActivities.get(i).categories.contains(lxCategoryMetadataKey)) {
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
		if (lxSortFilterMetadata.sort == LXSortType.PRICE) {
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

	private static final Action1<LXSearchResponse> CACHE_SEARCH_RESPONSE = new Action1<LXSearchResponse>() {
		@Override
		public void call(LXSearchResponse response) {
			cachedLXSearchResponse = response;
			cachedLXSearchResponse.unFilteredActivities.clear();
			cachedLXSearchResponse.unFilteredActivities.addAll(response.activities);
		}
	};
}
