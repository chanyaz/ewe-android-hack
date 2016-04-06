package com.expedia.bookings.widget;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * A Viewholder for the case where our data are launch collections.
 */
public class CollectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, HeaderBitmapDrawable.CallbackListener {
	private static final int FULL_TILE_TEXT_SIZE = 18;
	private static final int HALF_TILE_TEXT_SIZE = 15;

	@Optional
	@InjectView(R.id.card_view)
	public CardView cardView;

	@Optional
	@InjectView(R.id.title)
	public TextView title;

	@Optional
	@InjectView(R.id.subtitle)
	public TextView subtitle;

	@Optional
	@InjectView(R.id.background_image)
	public ImageView backgroundImage;

	@Optional
	@InjectView(R.id.browse_hotels_label)
	public TextView browseHotelsLabel;

	@Optional
	@InjectView(R.id.gradient)
	public View gradient;

	public String collectionUrl;

	public CollectionViewHolder(View view) {
		super(view);
		ButterKnife.inject(this, itemView);
		itemView.setOnClickListener(this);
	}

	public void bindListData(Object data, boolean fullWidthTile, boolean showBrowseHotelsLabel) {
		itemView.setTag(data);
		cardView.setPreventCornerOverlap(false);

		if (fullWidthTile) {
			title.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE);
		}
		else {
			title.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE);
		}
		browseHotelsLabel.setVisibility(showBrowseHotelsLabel ? View.VISIBLE : View.GONE);

		CollectionLocation location = (CollectionLocation) data;
		bindLocationData(location);
	}

	private void bindLocationData(CollectionLocation location) {
		title.setText(location.title);
		FontCache.setTypeface(title, FontCache.Font.ROBOTO_MEDIUM);
		subtitle.setText(location.subtitle);
		subtitle.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View view) {
		Bundle animOptions = AnimUtils.createActivityScaleBundle(view);
		CollectionLocation location = (CollectionLocation) view.getTag();
		Events.post(new Events.LaunchCollectionItemSelected(location, animOptions, collectionUrl));
		OmnitureTracking.trackNewLaunchScreenTileClick(true);
	}

	@Override
	public void onBitmapLoaded() {
		gradient.setVisibility(View.VISIBLE);
	}

	@Override
	public void onBitmapFailed() {
		gradient.setVisibility(View.GONE);
	}

	@Override
	public void onPrepareLoad() {
		gradient.setVisibility(View.GONE);
	}
}
