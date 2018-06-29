package com.expedia.bookings.location;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;

import com.expedia.bookings.R;
import com.expedia.bookings.R2;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.LXNavUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Ui;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class LXCurrentLocationSuggestionObserver implements Observer<SuggestionV4> {

	private Context context;
	private LxSearchParams currentLocationSearchParams = null;
	public Disposable disposable;

	public LXCurrentLocationSuggestionObserver(Context context, LxSearchParams currentLocationSearchParams) {
		this.context = context;
		this.currentLocationSearchParams = currentLocationSearchParams;
	}

	@Override
	public void onComplete() {
	}

	@Override
	public void onError(Throwable e) {
		if (RetrofitUtils.isNetworkError(e)) {
			showNoInternetErrorDialog(R.string.error_no_internet);
		}
		else {
			LXNavUtils.handleLXSearchFailure(e, SearchType.DEFAULT_SEARCH);
		}
	}

	@Override
	public void onSubscribe(@NonNull Disposable disposable) {
		this.disposable = disposable;
	}

	@Override
	public void onNext(SuggestionV4 suggestion) {
		boolean modQualified = Ui.getApplication(this.context).appComponent().userStateManager().isUserAuthenticated();
		if (currentLocationSearchParams != null) {
			Events.post(new Events.LXNewSearchParamsAvailable(suggestion.regionNames.fullName,
				currentLocationSearchParams.getActivityStartDate(),
				currentLocationSearchParams.getActivityEndDate(), modQualified));
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
