package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PointF;
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
import com.expedia.bookings.data.LXMedia;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.OffersDetail;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;

public class LXActivityDetailsWidget extends LXDetailsScrollView implements RecyclerGallery.GalleryItemListener {

	public static final int DURATION = 500;

	@InjectView(R.id.activity_details_container)
	LinearLayout activityContainer;

	@InjectView(R.id.gallery_container)
	FrameLayout galleryContainer;

	@InjectView(R.id.activity_gallery)
	RecyclerGallery activityGallery;

	@InjectView(R.id.highlights)
	LXDetailSectionDataWidget highlights;

	@InjectView(R.id.offers)
	LXOffersListWidget offers;

	@InjectView(R.id.description)
	LXDetailSectionDataWidget description;

	@InjectView(R.id.location)
	LXDetailSectionDataWidget location;

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

	@Inject
	LXState lxState;

	private ActivityDetailsResponse activityDetails;
	private float offset;
	private int dateButtonWidth;
	private int mGalleryHeight = 0;
	private int mInitialScrollTop = 0;
	private boolean mHasBeenTouched = false;
	private int mIntroOffset = 0;
	SegmentedLinearInterpolator mIGalleryScroll;

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
		knowBeforeYouBook.setVisibility(View.GONE);
		cancellation.setVisibility(View.GONE);
		offerDatesContainer.setVisibility(View.GONE);
		offers.setVisibility(View.GONE);

		offset = Ui.toolbarSizeWithStatusBar(getContext());
		offers.getOfferPublishSubject().subscribe(lxOfferObserever);
		defaultScroll();
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

	@Subscribe
	public void onShowActivityDetails(Events.LXShowDetails event) {
		//  Track Product Information on load of this Local Expert Information screen.
		OmnitureTracking.trackAppLXProductInformation(event.activityDetails, lxState.searchParams);
		activityDetails = event.activityDetails;

		buildGallery(activityDetails);
		buildSections(activityDetails);
		buildOfferDatesSelector(activityDetails.offersDetail, lxState.searchParams.startDate);
	}

	@Subscribe
	public void onDetailsDateChanged(Events.LXDetailsDateChanged event) {

		for (int i = 0; i < offerDatesContainer.getChildCount(); i++) {
			LXOfferDatesButton button = (LXOfferDatesButton) offerDatesContainer.getChildAt(i);
			button.setChecked(false);
		}

		event.buttonSelected.setChecked(true);
		//  Track Link to track Change of dates.
		OmnitureTracking.trackLinkLXChangeDate();
		buildOffersSection(event.dateSelected);
	}

	private void buildOffersSection(LocalDate startDate) {
		offers.setOffers(activityDetails.offersDetail.offers, startDate);
		offers.setVisibility(View.VISIBLE);
	}

	private void buildGallery(ActivityDetailsResponse activityDetails) {
		final List<LXMedia> mediaList = new ArrayList<LXMedia>();
		for (int i = 0; i < activityDetails.images.size(); i++) {
			List<String> imageURLs = Images
				.getLXImageURLBasedOnWidth(activityDetails.images.get(i).getImages(),
					AndroidUtils.getDisplaySize(getContext()).x);
			LXMedia media = new LXMedia(imageURLs);
			mediaList.add(media);
		}

		activityGallery.setDataSource(mediaList);
		activityGallery.setOnItemClickListener(this);
		activityGallery.scrollToPosition(0);
		if (!activityGallery.isFlipping()) {
			activityGallery.startFlipping();
		}
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
		cancellation.setVisibility(View.VISIBLE);

		int datesScrollerDrawable =
			CollectionUtils.isNotEmpty(activityDetailsResponse.highlights) ? R.drawable.lx_dates_container_background
				: R.drawable.lx_dates_container_background_no_top_border;
		offerDatesScrollView.setBackground(getResources().getDrawable(datesScrollerDrawable));
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

	private void buildOfferDatesSelector(OffersDetail offersDetail, LocalDate startDate) {
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

	private Observer<Offer> lxOfferObserever = new Observer<Offer>() {
		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(Offer offer) {
			LocalDate availabilityDate = DateUtils
				.yyyyMMddHHmmssToLocalDate(offer.availabilityInfoOfSelectedDate.availabilities.valueDate);
			String lowestTicketAmount = offer.availabilityInfoOfSelectedDate.getLowestTicket().money.getAmount()
				.toString();

			for (Ticket ticket : offer.availabilityInfoOfSelectedDate.tickets) {
				if (ticket.code == LXTicketType.Adult) {
					lowestTicketAmount = ticket.money.getAmount().toString();
					break;
				}
			}

			AdTracker.trackLXDetails(lxState.activity.id, lxState.activity.destination, availabilityDate,
				lxState.activity.regionId, lxState.activity.price.currencyCode, lowestTicketAmount);
		}
	};

	private void galleryCounterscroll(int parentScroll) {
		// Setup interpolator for Gallery counterscroll (if needed)
		if (mIGalleryScroll == null) {
			int screenHeight = getHeight();
			PointF p1 = new PointF(0, screenHeight - mGalleryHeight / 2);
			PointF p2 = new PointF(mGalleryHeight, screenHeight - mGalleryHeight);
			PointF p3 = new PointF(screenHeight, (screenHeight - mGalleryHeight + mIntroOffset) / 2);
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
		toggleFullScreenGallery();
	}

	public void toggleFullScreenGallery() {
		int from = getScrollY();
		int to = from != 0 ? 0 : mInitialScrollTop;
		animateScrollY(from, to);
	}
}

