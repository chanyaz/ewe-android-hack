package com.expedia.bookings.location;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.RetrofitUtils;

import rx.Observer;

public class LXCurrentLocationSuggestionObserver implements Observer<Suggestion> {

	private Context context;
	private LXSearchParams currentLocationSearchParams = null;

	public LXCurrentLocationSuggestionObserver(Context context, LXSearchParams currentLocationSearchParams) {
		this.context = context;
		this.currentLocationSearchParams = currentLocationSearchParams;
	}

	@Override
	public void onCompleted() {
	}

	@Override
	public void onError(Throwable e) {
		if (RetrofitUtils.isNetworkError(e)) {
			showNoInternetErrorDialog(R.string.error_no_internet);
			return;
		}
		else if (e instanceof ApiError) {
			ApiError apiError = (ApiError)e;
			if (apiError.errorCode == ApiError.Code.CURRENT_LOCATION_ERROR
				|| apiError.errorCode == ApiError.Code.SUGGESTIONS_NO_RESULTS) {
				Events.post(new Events.LXShowSearchError(apiError, SearchType.DEFAULT_SEARCH));
			}
			return;
		}

		//Default
		ApiError apiError = new ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS);
		Events.post(new Events.LXShowSearchError(apiError, SearchType.DEFAULT_SEARCH));
	}

	@Override
	public void onNext(Suggestion suggestion) {
		if (currentLocationSearchParams != null) {
			Events.post(new Events.LXNewSearchParamsAvailable(suggestion.fullName, suggestion.airportCode,
				currentLocationSearchParams.startDate,
				currentLocationSearchParams.endDate,
				currentLocationSearchParams.searchType));
			currentLocationSearchParams = null;
		}
	}

	private void showNoInternetErrorDialog(@StringRes int message) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setCancelable(false)
			.setMessage(context.getResources().getString(message))
			.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.LXShowSearchWidget());
				}
			})
			.show();
	}
}
