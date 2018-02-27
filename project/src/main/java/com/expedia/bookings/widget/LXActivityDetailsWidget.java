package com.expedia.bookings.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.DefaultMedia;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.ActivityImages;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.OffersDetail;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.features.Features;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.ApiDateUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.phrase.Phrase;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;
import kotlin.Unit;

public class LXActivityDetailsWidget extends LXDetailsScrollView implements RecyclerGallery.GalleryItemListener, RecyclerGallery.IImageViewBitmapLoadedListener {

	public static final int DURATION = 500;

	@InjectView(R.id.activity_details_container)
	LinearLayout activityContainer;

	@InjectView(R.id.activity_details)
	LXDetailsScrollView activityDetailsContainer;

	@InjectView(R.id.gallery_container)
	FrameLayout galleryContainer;

	@InjectView(R.id.activity_gallery)
	RecyclerGallery activityGallery;

	@InjectView(R.id.highlights)
	LXDetailSectionDataWidget highlights;

	@InjectView(R.id.mini_map_view)
	LocationMapImageView miniMapView;

	@InjectView(R.id.map_divider)
	View mapDivider;

	@InjectView(R.id.map_click_container)
	View miniMapContainer;

	@InjectView(R.id.offers)
	LXOffersListWidget offers;

	@InjectView(R.id.description)
	LXDetailSectionDataWidget description;

	@InjectView(R.id.location)
	LXDetailSectionDataWidget location;

	@InjectView(R.id.event_location)
	LXDetailSectionDataWidget eventLocation;

	@InjectView(R.id.redemption_location)
	LXDetailSectionDataWidget redemptionLocation;

	@InjectView(R.id.offer_dates_container)
	LinearLayout offerDatesContainer;

	@InjectView(R.id.inclusions)
	LXDetailSectionDataWidget inclusions;

	@InjectView(R.id.exclusions)
	LXDetailSectionDataWidget exclusions;

	@InjectView(R.id.know_before_you_book)
	LXDetailSectionDataWidget knowBeforeYouBook;

	@InjectView(R.id.cancellation)
	LXDetailSectionDataWidget cancellation;

	@InjectView(R.id.offer_dates_scroll_view)
	HorizontalScrollView offerDatesScrollView;

	@InjectView(R.id.recommendation_percentage_container)
	LinearLayout recommendPercentageLayout;

	@InjectView(R.id.recommended_percentage)
	TextView recommendedPercentage;

	@InjectView(R.id.recommended_divider)
	View recommendedDivider;

	@InjectView(R.id.discount_container)
	FrameLayout discountContainer;

	@InjectView(R.id.discount_percentage)
	TextView discountPercentageView;

	@Inject
	LXState lxState;

	private ActivityDetailsResponse activityDetails;
	private float offset;
	private int dateButtonWidth;
	private int mGalleryHeight = 0;
	private int mInitialScrollTop = 0;
	private boolean mHasBeenTouched = false;
	SegmentedLinearInterpolator mIGalleryScroll;
	public PublishSubject<Unit> mapClickSubject =  PublishSubject.create();

	private boolean userBucketedForRTRTest;
	private boolean isGroundTransport;

	public LXActivityDetailsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);

		highlights.setVisibility(View.GONE);
		description.setVisibility(View.GONE);
		location.setVisibility(View.GONE);
		inclusions.setVisibility(View.GONE);
		exclusions.setVisibility(View.GONE);
		eventLocation.setVisibility(View.GONE);
		redemptionLocation.setVisibility(View.GONE);
		knowBeforeYouBook.setVisibility(View.GONE);
		cancellation.setVisibility(View.GONE);
		offerDatesContainer.setVisibility(View.GONE);
		offers.setVisibility(View.GONE);
		discountContainer.setVisibility(View.GONE);
		offset = Ui.toolbarSizeWithStatusBar(getContext());
		offers.getOfferPublishSubject().subscribe(lxOfferObserever);
		defaultScroll();
		galleryContainer.getViewTreeObserver().addOnScrollChangedListener(
			new ViewTreeObserver.OnScrollChangedListener() {
				@Override
				public void onScrollChanged() {
					setA11yScrolling();
					updateGalleryPosition();
				}
			});
	}

	public void defaultScroll() {
		smoothScrollTo(0, mInitialScrollTop);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}
	
	private void setA11yScrolling() {
		if (getScrollY() == getHeight() - mGalleryHeight) {
			((RecyclerGallery.A11yLinearLayoutManager) activityGallery.getLayoutManager()).setCanA11yScroll(false);
		}

		if (getScrollY() == 0) {
			((RecyclerGallery.A11yLinearLayoutManager) activityGallery.getLayoutManager()).setCanA11yScroll(true);
		}
	}

	@OnClick(R.id.transparent_view_over_mini_map)
	public void openFullMap() {
		mapClickSubject.onNext(Unit.INSTANCE);
	}

	public void onShowActivityDetails(ActivityDetailsResponse activityDetails) {
		//  Track Product Information on load of this Local Expert Information screen.
		OmnitureTracking.trackAppLXProductInformation(activityDetails, lxState.searchParams, isGroundTransport);
		this.activityDetails = activityDetails;

		buildRecommendationPercentage(activityDetails.recommendationScore);
		buildGallery(activityDetails);
		if (Features.Companion.getAll().getActivityMap().enabled()) {
			buildMapSection(activityDetails);
		}
		buildSections(activityDetails);
		buildDiscountSection(activityDetails.offersDetail.offers);
		buildOfferDatesSelector(activityDetails.offersDetail, lxState.searchParams.getActivityStartDate());
	}

	private void buildRecommendationPercentage(int recommendationScore) {

		if (recommendationScore > 0 && userBucketedForRTRTest) {
			recommendedPercentage.setText(LXDataUtils.getUserRecommendPercentString(getContext(), recommendationScore));
			recommendPercentageLayout.setVisibility(VISIBLE);
			recommendedDivider.setVisibility(VISIBLE);
		}
		else {
			recommendPercentageLayout.setVisibility(GONE);
			recommendedDivider.setVisibility(GONE);
		}
	}

	public void onDetailsDateChanged(LocalDate dateSelected, LXOfferDatesButton buttonSelected) {

		for (int i = 0; i < offerDatesContainer.getChildCount(); i++) {
			LXOfferDatesButton button = (LXOfferDatesButton) offerDatesContainer.getChildAt(i);
			button.setChecked(false);
			button.setSelected(false);
		}

		buttonSelected.setChecked(true);
		buttonSelected.setSelected(true);
		//  Track Link to track Change of dates.
		OmnitureTracking.trackLinkLXChangeDate(isGroundTransport);
		buildOffersSection(dateSelected);
	}

	public void buildOffersSection(LocalDate startDate) {
		offers.setOffers(activityDetails.offersDetail.offers, startDate);
		offers.setVisibility(View.VISIBLE);
	}

	private void buildGallery(ActivityDetailsResponse activityDetails) {
		final List<DefaultMedia> mediaList = new ArrayList<>();
		for (ActivityImages activityImages : activityDetails.images) {
			List<String> imageURLs = Images
				.getLXImageURLBasedOnWidth(activityImages.getImages(), AndroidUtils.getDisplaySize(getContext()).x);
			DefaultMedia media = new DefaultMedia(imageURLs, Phrase.from(getContext(),
					R.string.lx_carousal_cont_desc_TEMPLATE).put("caption",
					activityImages.getImageCaption()).format().toString());
			mediaList.add(media);
		}

		activityGallery.setDataSource(mediaList);
		activityGallery.setOnItemClickListener(this);
		activityGallery.addImageViewCreatedListener(this);
		activityGallery.scrollToPosition(0);
	}

	public void buildSections(ActivityDetailsResponse activityDetailsResponse) {

		// Display readmore for description and highlights if content is more than 5 lines.
		int maxLines = getResources().getInteger(R.integer.lx_detail_content_description_max_lines);

		resetSections();
		if (Strings.isNotEmpty(activityDetailsResponse.description)) {
			String descriptionContent = StrUtils.stripHTMLTags(activityDetailsResponse.description);
			description.bindData(getResources().getString(R.string.description_activity_details), descriptionContent,
				maxLines);
			description.setVisibility(View.VISIBLE);
		}
		if (Strings.isNotEmpty(activityDetailsResponse.location)) {
			String locationContent = StrUtils.stripHTMLTags(activityDetailsResponse.location);
			location.bindData(getResources().getString(R.string.location_activity_details), locationContent, 0);
			location.setVisibility(View.VISIBLE);
		}
		if (CollectionUtils.isNotEmpty(activityDetailsResponse.highlights)) {
			CharSequence highlightsContent = StrUtils.generateBulletedList(activityDetailsResponse.highlights);
			highlights
				.bindData(getResources().getString(R.string.highlights_activity_details), highlightsContent, maxLines);
			highlights.setVisibility(View.VISIBLE);
		}
		if (CollectionUtils.isNotEmpty(activityDetailsResponse.inclusions)) {
			CharSequence inclusionsContent = StrUtils.generateBulletedList(activityDetailsResponse.inclusions);
			inclusions.bindData(getResources().getString(R.string.inclusions_activity_details), inclusionsContent, 0);
			inclusions.setVisibility(View.VISIBLE);
		}
		if (CollectionUtils.isNotEmpty(activityDetailsResponse.exclusions)) {
			CharSequence exclusionsContent = StrUtils.generateBulletedList(activityDetailsResponse.exclusions);
			exclusions.bindData(getResources().getString(R.string.exclusions_activity_details), exclusionsContent, 0);
			exclusions.setVisibility(View.VISIBLE);
		}
		if (CollectionUtils.isNotEmpty(activityDetailsResponse.knowBeforeYouBook)) {
			CharSequence knowBeforeYouBookContent = StrUtils.generateBulletedList(
				activityDetailsResponse.knowBeforeYouBook);
			knowBeforeYouBook.bindData(getResources().getString(R.string.know_before_you_book_activity_details),
				knowBeforeYouBookContent, 0);
			knowBeforeYouBook.setVisibility(View.VISIBLE);
		}
		String cancellationPolicyText = LXDataUtils
				.getCancelationPolicyDisplayText(getContext(), activityDetailsResponse.freeCancellationMinHours);
		cancellation.bindData(getResources().getString(R.string.cancellation_policy),
				cancellationPolicyText, 0);

		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXOfferLevelCancellationPolicySupport)) {
			cancellation.setVisibility(View.GONE);
		}
		else {
			cancellation.setVisibility(View.VISIBLE);
		}

		int datesScrollerDrawable =
			CollectionUtils.isNotEmpty(activityDetailsResponse.highlights) ? R.drawable.lx_dates_container_background
				: R.drawable.lx_dates_container_background_no_top_border;
		offerDatesScrollView.setBackground(getResources().getDrawable(datesScrollerDrawable));
	}

	public void buildMapSection(ActivityDetailsResponse activityDetailsResponse) {
		mapDivider.setVisibility(VISIBLE);
		miniMapContainer.setVisibility(VISIBLE);
		miniMapView.setLocation(ActivityDetailsResponse.LXLocation.getLocation(activityDetails.eventLocation.latLng));
		if (Strings.isNotEmpty(activityDetailsResponse.eventLocation.city)) {
			List<String> eventLocationCity = new ArrayList<>();
			eventLocationCity.add(activityDetailsResponse.eventLocation.city);
			CharSequence eventLocationBullet = StrUtils.generateBulletedList(eventLocationCity);
			eventLocation
				.bindData(getResources().getString(R.string.event_location_activity_details), eventLocationBullet, 0);
			eventLocation.setVisibility(View.VISIBLE);
		}
		if (CollectionUtils.isNotEmpty(activityDetailsResponse.redemptionLocation)) {
			List<String> redemptionLocationList = StrUtils
				.getRedemptionLocationList(activityDetailsResponse.redemptionLocation);
			CharSequence redemptionLocations = StrUtils.generateBulletedList(redemptionLocationList);
			redemptionLocation
				.bindData(getResources().getString(R.string.redemption_location_activity_details), redemptionLocations,
					6);
			redemptionLocation.setVisibility(View.VISIBLE);
		}
	}

	// Not all activities have all the sections. Reset before building details.
	private void resetSections() {
		description.setVisibility(View.GONE);
		location.setVisibility(View.GONE);
		highlights.setVisibility(View.GONE);
		inclusions.setVisibility(View.GONE);
		exclusions.setVisibility(View.GONE);
		knowBeforeYouBook.setVisibility(View.GONE);
		cancellation.setVisibility(View.GONE);
	}

	public void buildOfferDatesSelector(OffersDetail offersDetail, LocalDate startDate) {
		offerDatesContainer.removeAllViews();

		offerDatesContainer.setVisibility(View.VISIBLE);

		addOfferDateViews(offersDetail, startDate);
		selectFirstDateWithAvailabilities(startDate);
	}

	private void addOfferDateViews(OffersDetail offersDetail, LocalDate startDate) {
		int numOfDaysToDisplay = getResources().getInteger(R.integer.lx_default_search_range);

		for (int iDay = 0; iDay <= numOfDaysToDisplay; iDay++) {
			LXOfferDatesButton dateButton = Ui.inflate(R.layout.lx_offer_date_button, offerDatesContainer, false);
			LocalDate offerDate = startDate.plusDays(iDay);
			dateButton.bind(offerDate, offersDetail.isAvailableOnDate(offerDate));
			offerDatesContainer.addView(dateButton);
		}
	}

	private void selectFirstDateWithAvailabilities(LocalDate startDate) {
		final int numOfDaysToDisplay = getResources().getInteger(R.integer.lx_default_search_range);
		dateButtonWidth = (int) getResources().getDimension(R.dimen.lx_offer_dates_container_width);
		int selectedDateX = 0;

		for (int iDay = 0; iDay <= numOfDaysToDisplay; iDay++) {
			if (offerDatesContainer.getChildAt(iDay).isEnabled()) {
				LXOfferDatesButton child = (LXOfferDatesButton) offerDatesContainer.getChildAt(iDay);
				child.setChecked(true);
				buildOffersSection(startDate.plusDays(iDay));
				selectedDateX = dateButtonWidth * iDay;
				break;
			}
		}

		// Scroll to end.
		offerDatesScrollView.scrollTo((dateButtonWidth * numOfDaysToDisplay), 0);
		offerDatesScrollView.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					offerDatesScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					offerDatesScrollView.scrollTo(dateButtonWidth * numOfDaysToDisplay, 0);
				}
			});

		final int finalSelectedDateX = selectedDateX;
		// Scroll from end to the selected date.
		postDelayed(new Runnable() {
			public void run() {
				ObjectAnimator scrollAnimation = ObjectAnimator
					.ofInt(offerDatesScrollView, "scrollX", finalSelectedDateX);
				scrollAnimation.setDuration(DURATION);
				scrollAnimation.setInterpolator(new DecelerateInterpolator());
				scrollAnimation.start();
			}
		}, DURATION);
	}

	public float parallaxScrollHeader(int scrollY) {
		return (float) (scrollY - mInitialScrollTop) * 3.2f / (activityContainer.getTop() - offset);
	}

	public float getArrowRotationRatio(int scrollY) {
		return (float) scrollY / (mInitialScrollTop);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		initGallery(h);
	}

	private void initGallery(int h) {

		if (galleryContainer != null) {
			ViewGroup.LayoutParams lp = galleryContainer.getLayoutParams();
			lp.height = h;
			galleryContainer.setLayoutParams(lp);

			mInitialScrollTop = getInitialScrollTop();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mIGalleryScroll = null;

		if (activityGallery != null) {
			mGalleryHeight = getResources().getDimensionPixelSize(R.dimen.gallery_height);
			mInitialScrollTop = getInitialScrollTop();
		}

		if (!mHasBeenTouched) {
			scrollTo(0, mInitialScrollTop);
			doCounterscroll();
		}
	}

	public void doCounterscroll() {
		int t = getScrollY();
		if (galleryContainer != null) {
			galleryCounterscroll(t);
		}
	}

	private Observer<Offer> lxOfferObserever = new DisposableObserver<Offer>() {
		@Override
		public void onComplete() {
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(Offer offer) {
			LocalDate availabilityDate = ApiDateUtils
				.yyyyMMddHHmmssToLocalDate(offer.availabilityInfoOfSelectedDate.availabilities.valueDate);
			String lowestTicketAmount = offer.availabilityInfoOfSelectedDate.getLowestTicket().money.getAmount()
				.toString();

			for (Ticket ticket : offer.availabilityInfoOfSelectedDate.tickets) {
				if (ticket.code == LXTicketType.Adult) {
					lowestTicketAmount = ticket.money.getAmount().toString();
					break;
				}
			}
			if (lxState.activity != null) {
				AdTracker.trackLXDetails(lxState.activity.id, lxState.activity.destination, availabilityDate,
					lxState.activity.regionId, lxState.activity.price.currencyCode, lowestTicketAmount);
			}
		}
	};

	private void galleryCounterscroll(int parentScroll) {
		// Setup interpolator for Gallery counterscroll (if needed)
		if (mIGalleryScroll == null) {
			int screenHeight = getHeight();
			PointF p1 = new PointF(0, screenHeight - mGalleryHeight / 2);
			PointF p2 = new PointF(mGalleryHeight, screenHeight - mGalleryHeight);
			PointF p3 = new PointF(screenHeight, (screenHeight - mGalleryHeight) / 2);
			mIGalleryScroll = new SegmentedLinearInterpolator(p1, p2, p3);
		}

		// The number of y-pixels available to the gallery
		int availableHeight = getHeight() - parentScroll;

		int counterscroll = (int) mIGalleryScroll.get(availableHeight);

		galleryContainer.setPivotX(getWidth() / 2);
		galleryContainer.setPivotY(counterscroll + mGalleryHeight / 2);

		galleryContainer.scrollTo(0, -counterscroll);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean result = super.onTouchEvent(ev);
		mHasBeenTouched = true;
		return result;
	}

	@Override
	public void onGalleryItemClicked(Object item) {
		mHasBeenTouched = true;
		toggleFullScreenGallery();
	}

	public void toggleFullScreenGallery() {
		int from = getScrollY();
		int to = from != 0 ? 0 : mInitialScrollTop;
		animateScrollY(from, to);
	}

	public void setUserBucketedForRTRTest(boolean userBucketedForRTRTest) {
		this.userBucketedForRTRTest = userBucketedForRTRTest;
	}

	public void setIsFromGroundTransport(boolean isGroundTransport) {
		this.isGroundTransport = isGroundTransport;
		offers.setIsFromGroundTransport(isGroundTransport);
	}

	@VisibleForTesting
	protected ActivityDetailsResponse getActivityDetails() {
		return activityDetails;
	}

	@VisibleForTesting
	protected void setActivityDetails(ActivityDetailsResponse activityDetails) {
		this.activityDetails = activityDetails;
	}

	public void updateGalleryPosition() {
		updateGalleryChildrenHeights(activityGallery.getSelectedItem());
		activityGallery.setTranslationY(-getInitialScrollTop());
	}

	public void updateGalleryChildrenHeights(int index) {
		resizeImageViews(index);
		resizeImageViews(index - 1);
		resizeImageViews(index + 1);
	}

	private void resizeImageViews(int index) {
		if (index >= 0 && index < activityGallery.getAdapter().getItemCount()) {
			RecyclerGallery.RecyclerAdapter.GalleryViewHolder holder = (RecyclerGallery.RecyclerAdapter.GalleryViewHolder)activityGallery.findViewHolderForAdapterPosition(index);
			if (holder != null && holder.mImageView != null) {
				holder.mImageView.setIntermediateValue(getHeight() - mInitialScrollTop, getHeight(),
					(float) activityDetailsContainer.getScrollY() / mInitialScrollTop);
			}
		}
	}

	@Override
	public void onImageViewBitmapLoaded(int index) {
		updateGalleryPosition();
	}

	public void buildDiscountSection(List<Offer> offers) {
		if (offers.get(0).discountPercentage >= Constants.LX_MIN_DISCOUNT_PERCENTAGE) {
			discountPercentageView.setText(Phrase.from(getContext(), R.string.lx_discount_percentage_text_TEMPLATE)
					.put("discount", offers.get(0).discountPercentage)
					.format());
			discountPercentageView.setContentDescription(Phrase.from(getContext(), R.string.lx_discount_percentage_description_TEMPLATE)
					.put("discount", offers.get(0).discountPercentage)
					.format());
			discountContainer.setVisibility(View.VISIBLE);
		}
		else {
			discountContainer.setVisibility(View.GONE);
		}
	}
}
