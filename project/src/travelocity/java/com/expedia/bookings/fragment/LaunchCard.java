package com.expedia.bookings.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.LaunchDb;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LaunchCard extends FrameLayout {

	@InjectView(R.id.launch_title_front_text_view)
	TextView frontTextView;

	@InjectView(R.id.launch_title_front_container)
	FrameLayout frontContainer;

	@InjectView(R.id.launch_title_back_container)
	FrameLayout backContainer;

	@InjectView(R.id.launch_title_front_image_view)
	ImageView frontImageView;

	@InjectView(R.id.launch_card_back_text_title)
	TextView backTextTitle;

	@InjectView(R.id.launch_card_back_text_description)
	TextView backTextDescription;

	@InjectView(R.id.button_explore_now)
	TextView backExploreNow;

	private LaunchLocation launchLocation;
	private static LaunchCard currentToggledCard = null;

	public LaunchCard(Context context) {
		super(context);
	}

	public LaunchCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LaunchCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		FontCache.setTypeface(frontTextView, FontCache.Font.ROBOTO_REGULAR);
		FontCache.setTypeface(backTextTitle, FontCache.Font.ROBOTO_REGULAR);
		FontCache.setTypeface(backTextDescription, FontCache.Font.ROBOTO_REGULAR);
	}

	public void bind(final LaunchLocation launchLocation) {
		if (launchLocation != null) {
			this.launchLocation = launchLocation;
			String cardText = "";
			if (Strings.isEmpty(launchLocation.subtitle)) {
				cardText = launchLocation.title;
			}
			else {
				cardText = String.format(getResources().getString(R.string.destination_list_launch_card_title_text),
					launchLocation.title, launchLocation.subtitle);
			}
			frontTextView.setText(cardText);
			backTextTitle.setText(cardText);

			backTextDescription.setText(launchLocation.description);

			final String imageUrl = TabletLaunchPinDetailFragment.getResizedImageUrl(getContext(), launchLocation);
			new PicassoHelper.Builder(frontImageView).fit().setPlaceholder(R.drawable.bg_launch_link_tile)
				.build().load(imageUrl);
			frontContainer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (null != currentToggledCard) {
						currentToggledCard.toggleView();
					}
					currentToggledCard = LaunchCard.this;
					toggleView();
				}
			});
			backContainer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					toggleView();
					currentToggledCard = null;
				}
			});
			backExploreNow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean fromLastSearch = launchLocation.id.equals(LaunchDb.YOUR_SEARCH_TILE_ID);
					launchLocation.location.setImageCode(launchLocation.imageCode);
					Events.post(new Events.SearchSuggestionSelected(launchLocation.location, fromLastSearch));
				}
			});
		}
	}

	public LaunchLocation getLaunchLocation() {
		return launchLocation;
	}

	public void toggleView() {
		if (backContainer.getVisibility() == GONE) {
			frontContainer.setVisibility(GONE);
			backContainer.setVisibility(VISIBLE);
		}
		else {
			frontContainer.setVisibility(VISIBLE);
			backContainer.setVisibility(GONE);
		}

	}
}
