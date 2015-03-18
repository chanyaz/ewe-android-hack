package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXMedia;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.OffersDetail;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LXFormatUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LXActivityDetailsWidget extends ScrollView {

	@InjectView(R.id.activity_gallery)
	RecyclerGallery activityGallery;

	@InjectView(R.id.title)
	TextView title;

	@InjectView(R.id.price)
	TextView price;

	@InjectView(R.id.category)
	TextView category;

	@InjectView(R.id.per_ticket_type)
	TextView perTicketType;

	@InjectView(R.id.highlights)
	LXDetailSectionDataWidget highlights;

	@InjectView(R.id.offers)
	LXOffersListWidget offers;

	@InjectView(R.id.description)
	LXDetailSectionDataWidget description;

	@InjectView(R.id.location)
	LXDetailSectionDataWidget location;

	@InjectView(R.id.offer_dates_container)
	RadioGroup offerDatesContainer;

	@Inject
	LXState lxState;

	private ActivityDetailsResponse activityDetails;

	public LXActivityDetailsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);

		highlights.setVisibility(View.GONE);
		description.setVisibility(View.GONE);
		location.setVisibility(View.GONE);
		offerDatesContainer.setVisibility(View.GONE);
		offers.setVisibility(View.GONE);
	}

	@Subscribe
	public void onShowActivityDetails(Events.LXShowDetails event) {
		//  Track Product Information on load of this Local Expert Information screen.
		OmnitureTracking.trackAppLXProductInformation(getContext(), event.activityDetails, lxState.searchParams);
		activityDetails = event.activityDetails;

		buildGallery(activityDetails);
		buildInfo(activityDetails);
		buildSections(activityDetails);
		buildOfferDatesSelector(activityDetails.offersDetail, lxState.searchParams.startDate);
	}

	@Subscribe
	public void onDetailsDateChanged(Events.LXDetailsDateChanged event) {
		//  Track Link to track Change of dates.
		OmnitureTracking.trackLinkLXChangeDate(getContext());
		buildOffersSection(event.dateSelected);
	}

	private void buildOffersSection(LocalDate startDate) {
		offers.setOffers(activityDetails.offersDetail.offers, startDate);
		offers.setVisibility(View.VISIBLE);
	}

	private void buildGallery(ActivityDetailsResponse activityDetails) {
		final List<LXMedia> mediaList = new ArrayList<LXMedia>();
		for (int i = 0; i < activityDetails.images.size(); i++) {
			String url = Images.getLXImageURL(activityDetails.images.get(i).url);
			LXMedia media = new LXMedia(url);
			mediaList.add(media);
		}

		activityGallery.setDataSource(mediaList);
	}

	private void buildInfo(ActivityDetailsResponse activityDetails) {
		title.setText(activityDetails.title);
		price.setText(activityDetails.fromPrice);
		category.setText(activityDetails.bestApplicableCategoryLocalized);
		perTicketType.setText(getResources().getString(LXDataUtils.LX_TICKET_TYPE_NAME_MAP.get(activityDetails.fromPriceTicketCode)));
	}

	public void buildSections(ActivityDetailsResponse activityDetailsResponse) {

		String descriptionContent = LXFormatUtils.stripHTMLTags(activityDetailsResponse.description);
		String locationContent = LXFormatUtils.stripHTMLTags(activityDetailsResponse.location);
		String highlightsContent = LXFormatUtils.formatHighlights(activityDetailsResponse.highlights);

		if (Strings.isNotEmpty(descriptionContent)) {
			description.bindData(getResources().getString(R.string.description_activity_details), descriptionContent);
			description.setVisibility(View.VISIBLE);
		}
		if (Strings.isNotEmpty(locationContent)) {
			location.bindData(getResources().getString(R.string.location_activity_details), locationContent);
			location.setVisibility(View.VISIBLE);
		}
		if (Strings.isNotEmpty(highlightsContent)) {
			highlights.bindData(getResources().getString(R.string.highlights_activity_details), highlightsContent);
			highlights.setVisibility(View.VISIBLE);
		}
	}

	private void buildOfferDatesSelector(OffersDetail offersDetail, LocalDate startDate) {
		offerDatesContainer.removeAllViews();
		offerDatesContainer.setVisibility(View.VISIBLE);
		int noOfDaysToDisplay = getResources().getInteger(R.integer.lx_default_search_range);

		for (int i = 0; i <= noOfDaysToDisplay; i++) {
			LXOfferDatesButton dateButton = Ui.inflate(R.layout.lx_offer_date_button, offerDatesContainer, false);
			dateButton.bind(offersDetail, startDate.plusDays(i));
			offerDatesContainer.addView(dateButton);
		}
		// Set first enabled date as selected.
		for (int i = 0; i <= offerDatesContainer.getChildCount(); i++) {
			if (offerDatesContainer.getChildAt(i).isEnabled()) {
				RadioButton child = (RadioButton) offerDatesContainer.getChildAt(i);
				child.setChecked(true);
				buildOffersSection(startDate.plusDays(i));
				break;
			}
		}
	}
}

