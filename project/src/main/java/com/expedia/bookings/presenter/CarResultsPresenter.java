package com.expedia.bookings.presenter;

import javax.inject.Inject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.RetrofitUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.widget.CarCategoryDetailsWidget;
import com.expedia.bookings.widget.CarCategoryListWidget;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class CarResultsPresenter extends Presenter implements UserAccountRefresher.IUserAccountRefreshListener {

	public CarResultsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Inject
	CarServices carServices;

	@InjectView(R.id.categories)
	CarCategoryListWidget categories;

	@InjectView(R.id.details)
	CarCategoryDetailsWidget details;

	// This is here just for an animation
	@InjectView(R.id.toolbar_background)
	View toolbarBackground;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	UserAccountRefresher userAccountRefresher;
	View carTransitionView;

	private ProgressDialog createTripDialog;
	private Subscription searchSubscription;
	private Subscription createTripSubscription;
	private CarSearchParams mParams;
	private CategorizedCarOffers mOffer;
	private SearchCarOffer offer;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Ui.getApplication(getContext()).carComponent().inject(this);

		addTransition(categoriesToDetails);
		addDefaultTransition(setUpLoading);

		createTripDialog = new ProgressDialog(getContext());
		createTripDialog.setMessage(getResources().getString(R.string.preparing_checkout_message));
		createTripDialog.setIndeterminate(true);

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setTitleTextColor(Color.WHITE);
		toolbar.setSubtitleTextColor(Color.WHITE);
		toolbar.inflateMenu(R.menu.cars_results_menu);
		toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance);
		toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_search:
					Events.post(new Events.CarsGoToOverlay());
					return true;
				}
				return false;
			}
		});
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});

		details.offerList.setOnScrollListener(parallaxScrollListener);

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		toolbarBackground.getLayoutParams().height += statusBarHeight;
		toolbar.setPadding(0, statusBarHeight, 0, 0);

		userAccountRefresher = new UserAccountRefresher(getContext(), LineOfBusiness.CARS, this);
	}

	@Override
	protected void onDetachedFromWindow() {
		cleanup();
		super.onDetachedFromWindow();
	}

	public void cleanup() {
		if (searchSubscription != null) {
			searchSubscription.unsubscribe();
			searchSubscription = null;
		}
		if (createTripSubscription != null) {
			createTripSubscription.unsubscribe();
			createTripSubscription = null;
		}
	}

	private Observer<CarSearch> searchObserver = new Observer<CarSearch>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			OmnitureTracking.trackAppCarNoResults(getContext(), e.getMessage());
			Log.e("CarSearch - onError", e);
			show(categories, FLAG_CLEAR_BACKSTACK);

			if (RetrofitUtils.isNetworkError(e)) {
				showSearchErrorDialog(R.string.error_no_internet);
				return;
			}

			if (e instanceof ApiError) {
				handleInputValidationErrors((ApiError) e);
				return;
			}
		}

		@Override
		public void onNext(CarSearch carSearch) {
			Events.post(new Events.CarsShowSearchResults(carSearch));
			show(categories, FLAG_CLEAR_BACKSTACK);
			OmnitureTracking.trackAppCarSearch(getContext(), mParams, carSearch.categories.size());
		}
	};

	/* handle CAR_SEARCH_WINDOW_VIOLATION detail errors and show default error screen
	* for all the other search error codes.
	*/
	private void handleInputValidationErrors(ApiError apiError) {
		if (apiError.errorDetailCode == null) {
			showDefaultError(apiError);
			return;
		}

		switch (apiError.errorDetailCode) {
		case DROP_OFF_DATE_TOO_LATE:
			showInvalidInputErrorDialog(R.string.drop_off_too_far_error);
			break;
		case SEARCH_DURATION_TOO_SMALL:
			showInvalidInputErrorDialog(R.string.reservation_time_too_short);
			break;
		case SEARCH_DURATION_TOO_LARGE:
			showInvalidInputErrorDialog(R.string.date_out_of_range);
			break;
		case PICKUP_DATE_TOO_EARLY:
			showInvalidInputErrorDialog(R.string.pick_up_time_error);
			break;
		case PICKUP_DATE_IN_THE_PAST:
			showInvalidInputErrorDialog(R.string.pick_up_time_error);
			break;
		case PICKUP_DATE_AND_DROP_OFF_DATE_ARE_THE_SAME:
			showInvalidInputErrorDialog(R.string.pick_up_time_error);
			break;
		default:
			showInvalidInputErrorDialog(R.string.error_server);
			break;
		}
	}

	private void showDefaultError(ApiError apiError) {
		Events.post(new Events.CarsShowSearchResultsError(apiError));
	}

	private void showSearchErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsKickOffSearchCall(mParams));
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsGoToSearch());
				}
			})
			.show();
	}

	private void showInvalidInputErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsGoToSearch());
				}
			})
			.show();
	}

	Transition categoriesToDetails = new Transition(CarCategoryListWidget.class,
		CarCategoryDetailsWidget.class, new DecelerateInterpolator(), Transition.DEFAULT_ANIMATION_DURATION) {

		@Override
		public void startTransition(boolean forward) {
			toolbarBackground.setVisibility(VISIBLE);
			toolbarBackground.setAlpha(1f);

			categories.setVisibility(VISIBLE);

			details.setVisibility(VISIBLE);
			details.hideForInitAnimation(true);

			// Gradient on the top of the details image view.
			View imageGradient = Ui.findView(carTransitionView, R.id.cars_details_gradient);
			imageGradient.setVisibility(VISIBLE);
			imageGradient.setAlpha(0f);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float translationY = forward ? -carTransitionView.getTop() * f : carTransitionView.getTop() * (f - 1);
			carTransitionView.setTranslationY(translationY);
			toolbarBackground.setAlpha(forward ? (Math.abs(f - 1) * 1) : (f * 1));

			// Gradient at the bottom of the imageview in the categories list item.
			View gradientMask = Ui.findView(carTransitionView, R.id.gradient_mask);
			gradientMask.setAlpha(forward ? (Math.abs(f - 1) * 1) : (f * 1));

			// Price container at the bottom in categories list item.
			LinearLayout priceContainer = Ui.findView(carTransitionView, R.id.price_container);
			float px = getResources().getDimensionPixelSize(R.dimen.car_search_details_container_height);
			RelativeLayout.LayoutParams priceLayoutParams = (RelativeLayout.LayoutParams) priceContainer.getLayoutParams();
			priceLayoutParams.height = forward ? (int) (Math.abs(f - 1) * (px)) : (int) (f * (px));
			priceContainer.setLayoutParams(priceLayoutParams);

			// Car details container.
			LinearLayout detailsContainer = Ui.findView(carTransitionView, R.id.details_container);
			detailsContainer.setAlpha(forward ? (Math.abs(f - 1) * 1) : (f * 1));

			// Categories image view.
			ImageView headerImage = Ui.findView(carTransitionView, R.id.background_image_view);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) headerImage.getLayoutParams();
			px = getResources().getDimensionPixelSize(R.dimen.car_details_image_size);
			int listHeightPX = getResources().getDimensionPixelSize(R.dimen.car_search_list_image_container_height);
			layoutParams.height = forward ? (int) (f * (px - listHeightPX) + listHeightPX) : (int) (Math.abs(f - 1) * (px - listHeightPX) + listHeightPX);
			headerImage.setLayoutParams(layoutParams);

			// Altering the right, left margin on the categories list item card. To make it's width expand.
			RecyclerView.LayoutParams cardLayoutParams = (RecyclerView.LayoutParams) carTransitionView.getLayoutParams();
			px = getResources().getDimensionPixelSize(R.dimen.car_search_list_image_container_margin);
			int margin = forward ? (int) (Math.abs(f - 1) * (px)) : (int) (f * (px));
			cardLayoutParams.leftMargin = margin;
			cardLayoutParams.rightMargin = margin;
			carTransitionView.setLayoutParams(cardLayoutParams);

			// Car details offers list - translating-in and alpha-in
			RecyclerView offersList = Ui.findView(details, R.id.offer_list);
			translationY = forward ? -offersList.getHeight() * (f - 1) : offersList.getHeight() * f;
			offersList.setTranslationY(translationY);
			offersList.setAlpha(forward ? (f * 1) : (Math.abs(f - 1) * 1));

			// Gradient on the top of the details image view - Alpha-in
			View imageGradient = Ui.findView(carTransitionView, R.id.cars_details_gradient);
			imageGradient.setAlpha(forward ? (f * 1) : (Math.abs(f - 1) * 1));

			// We need to fade-in the rest of the items on the categories list.
			for (int i = 0; i < categories.recyclerView.getChildCount(); i++) {
				View v = categories.recyclerView.getChildAt(i);
				if (v != carTransitionView) {
					v.setAlpha(forward ? (Math.abs(f - 1) * 1) : (f * 1));
				}
			}
		}

		@Override
		public void endTransition(boolean forward) {
		}

		@Override
		public void finalizeTransition(boolean forward) {
			toolbarBackground.setVisibility(VISIBLE);
			toolbarBackground.setAlpha(forward ? 0f : 1f);

			categories.setVisibility(forward ? GONE : VISIBLE);

			details.setVisibility(forward ? VISIBLE : GONE);

			details.reset();
			details.hideForInitAnimation(false);

			if (forward) {
				setToolBarDetailsText();
			}
			else {
				setToolBarResultsText();
			}
		}
	};

	private void setToolBarDetailsText() {
		if (mOffer != null) {
			toolbar.setTitle(mOffer.carCategoryDisplayLabel);
		}
	}

	private void setToolBarResultsText() {
		if (mParams != null) {
			String dateTimeRange = DateFormatUtils.formatCarSearchDateRange(getContext(), mParams,
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT);
			toolbar.setTitle(mParams.originDescription);
			toolbar.setSubtitle(dateTimeRange);
		}
	}

	DefaultTransition setUpLoading = new DefaultTransition(CarCategoryListWidget.class.getName()) {
		@Override
		public void finalizeTransition(boolean forward) {
			if (!ExpediaBookingApp.sIsAutomation) {
				Events.post(new Events.CarsShowLoadingAnimation());
			}
			categories.setVisibility(View.VISIBLE);
			details.setVisibility(View.GONE);
		}
	};

	// Create Trip network handling

	private Observer<CarCreateTripResponse> createTripObserver = new Observer<CarCreateTripResponse>() {
		@Override
		public void onCompleted() {
			cleanup();
		}

		@Override
		public void onError(Throwable e) {
			Log.e("CarCreateTrip - onError", e);
			createTripDialog.dismiss();

			if (RetrofitUtils.isNetworkError(e)) {
				showOnCreateNoInternetErrorDialog(R.string.error_no_internet);
			}
			else {
				showCreateTripErrorDialog();
			}
		}

		@Override
		public void onNext(CarCreateTripResponse response) {
			createTripDialog.dismiss();
			Events.post(new Events.CarsShowCheckout(response));
		}
	};

	private void showCreateTripErrorDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(R.string.error_server))
			.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsGoToSearch());
				}
			})
			.show();
	}

	private void showOnCreateNoInternetErrorDialog(@StringRes int message) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(getResources().getString(message))
			.setPositiveButton(getResources().getString(R.string.retry), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Events.post(new Events.CarsKickOffCreateTrip(offer));
				}
			})
			.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
	}

	/**
	 * Events
	 */

	@Subscribe
	public void onNewCarSearchParams(Events.CarsNewSearchParams event) {
		if (!ExpediaBookingApp.sIsAutomation) {
			Events.post(new Events.CarsShowLoadingAnimation());
		}
		show(categories, FLAG_CLEAR_BACKSTACK);
		cleanup();
		mParams = event.carSearchParams;
		searchSubscription = carServices
			.carSearch(event.carSearchParams, searchObserver);
		setToolBarResultsText();
	}

	@Subscribe
	public void onShowDetails(Events.CarsShowDetails event) {
		carTransitionView = event.carOfferView;
		show(details);
		mOffer = event.categorizedCarOffers;
		setToolBarDetailsText();
	}

	@Subscribe
	public void onOfferSelected(Events.CarsKickOffCreateTrip event) {
		createTripDialog.show();
		cleanup();
		offer = event.offer;
		userAccountRefresher.ensureAccountIsRefreshed();
	}

	@Subscribe
	public void onCarSearchParams(Events.CarsKickOffSearchCall event) {
		cleanup();
		searchSubscription = carServices
			.carSearch(event.carSearchParams, searchObserver);
		setToolBarResultsText();
	}


	public void animationStart(boolean forward) {
		toolbarBackground.setTranslationY(forward ? 0 : -toolbarBackground.getHeight());
		toolbar.setTranslationY(forward ? 0 : 50);
		toolbar.setVisibility(VISIBLE);
	}

	public void animationUpdate(float f, boolean forward) {
		toolbarBackground
			.setTranslationY(forward ? -toolbarBackground.getHeight() * f : -toolbarBackground.getHeight() * (1 - f));
		toolbar.setTranslationY(forward ? 50 * f : 50 * (1 - f));
	}

	public void animationFinalize(boolean forward) {
		toolbarBackground.setTranslationY(forward ? -toolbarBackground.getHeight() : 0);
		toolbar.setTranslationY(forward ? 50 : 0);
		toolbar.setVisibility(forward ? GONE : VISIBLE);
		toolbarBackground.setAlpha(
			Strings.equals(getCurrentState(), CarCategoryDetailsWidget.class.getName()) ? toolbarBackground.getAlpha()
				: 1f);
	}

	RecyclerView.OnScrollListener parallaxScrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);

			float ratio = details.parallaxScrollHeader();
			toolbarBackground.setAlpha(ratio);
		}
	};

	@Override
	public void onUserAccountRefreshed() {
		createTripSubscription = carServices.createTrip(offer, createTripObserver);
	}
}
