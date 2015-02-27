package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXMedia;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Images;
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

	@InjectView(R.id.duration)
	TextView duration;

	@InjectView(R.id.free_cancellation)
	TextView freeCancellation;

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
		freeCancellation.setVisibility(View.GONE);
		offerDatesContainer.setVisibility(View.GONE);
		offers.setVisibility(View.GONE);
	}

	@Subscribe
	public void onShowActivityDetails(Events.LXShowDetails event) {
		ActivityDetailsResponse activityDetails = event.activityDetails;

		buildGallery(activityDetails);
		buildInfo(activityDetails);
		buildSections(activityDetails);
		buildOfferDatesSelector(lxState.searchParams.startDate);
		buildOffersSection(activityDetails);
	}

	private void buildOffersSection(ActivityDetailsResponse activityDetails) {
		offers.setOffers(activityDetails.offersDetail.offers, lxState.searchParams.startDate);
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
		duration.setText(activityDetails.duration);
		if (activityDetails.freeCancellation) {
			freeCancellation.setVisibility(View.VISIBLE);
		}
		perTicketType.setText(activityDetails.fromPriceTicketType);
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

	private void buildOfferDatesSelector(LocalDate startDate) {
		offerDatesContainer.setVisibility(View.VISIBLE);
		int noOfDaysToDisplay = getResources().getInteger(R.integer.lx_default_search_range);

		for (int i = 0; i < noOfDaysToDisplay; i++) {
			LXOfferDatesButton dateButton = Ui.inflate(R.layout.lx_offer_date_button, offerDatesContainer, false);
			dateButton.bind(startDate.plusDays(i));
			offerDatesContainer.addView(dateButton);
		}
	}
}

