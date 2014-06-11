package com.expedia.bookings.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CollectionStack;
import com.expedia.bookings.widget.HorizontalScrollView;

public class DestinationTilesFragment extends MeasurableFragment implements HorizontalScrollView.OnScrollListener {

	private static final int ALL_DIVIDERS = LinearLayout.SHOW_DIVIDER_MIDDLE | LinearLayout.SHOW_DIVIDER_BEGINNING | LinearLayout.SHOW_DIVIDER_END;

	private HorizontalScrollView mScrollView;
	private LinearLayout mItemsContainer;

	public static DestinationTilesFragment newInstance() {
		DestinationTilesFragment frag = new DestinationTilesFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_destination_stacks, container, false);

		mScrollView = Ui.findView(root, R.id.destinations_scrollview);
		mScrollView.addOnScrollListener(this);

		mItemsContainer = Ui.findView(root, R.id.destinations_container);
		mItemsContainer.setShowDividers(ALL_DIVIDERS);

		root.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				root.getViewTreeObserver().removeOnPreDrawListener(this);
				CollectionStack c;

				c = Ui.findView(root, R.id.stack_seattle);
				c.disableStack();
				c.setStackBackgroundDrawable(Color.rgb(216, 164, 154), "http://media.expedia.com/mobiata/mobile/destination/sea_360_520.jpg");
				c.setText("Destination Spotlight", "Flights from $80");
				c.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Events.post(new Events.LaunchTileClicked());
					}
				});

				c = Ui.findView(root, R.id.stack_portland);
				c.disableStack();
				c.setStackBackgroundDrawable(Color.rgb(216, 164, 154), "http://media.expedia.com/mobiata/mobile/destination/pdx_360_520.jpg");
				c.setText("City of Roses", "Accommodations from $78");

				c = Ui.findView(root, R.id.stack_toronto);
				c.disableStack();
				c.setStackBackgroundDrawable(Color.rgb(216, 164, 154), "http://media.expedia.com/mobiata/mobile/destination/yyz_360_520.jpg");
				c.setText("Pleasant getaway", "Packages from $730");

				c = Ui.findView(root, R.id.stack_asia);
				c.setStackBackgroundDrawable(Color.rgb(216, 164, 154), "http://media.expedia.com/mobiata/mobile/destination/pek_360_520.jpg");
				c.setText("Adventures in Asia", "Flights from $340");

				c = Ui.findView(root, R.id.stack_historic);
				c.setStackBackgroundDrawable(Color.rgb(108, 85, 114), "http://media.expedia.com/hotels/1000000/20000/18200/18200/18200_146_z.jpg");
				c.setText("Historic Hotels", "From $140 per night");

				c = Ui.findView(root, R.id.stack_beaches);
				c.setStackBackgroundDrawable(Color.rgb(117, 129, 187), "http://media.expedia.com/mobiata/mobile/destination/cun_720_1140.jpg");
				c.setText("Hotels on the Beach", "From $110 per night");

				onScrollChanged(mScrollView, 0, 0, 0, 0);

				return true;
			}
		});

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	public void onScrollChanged(HorizontalScrollView scrollView, int x, int y, int oldx, int oldy) {
		int scrollCenter = mScrollView.getWidth() / 2 + x;
		float scrollHalfWidth = mScrollView.getWidth() / 2.0f;
		int paddingLeft = 0;
		int paddingRight = 0;
		int scrollViewLeft = x;
		int scrollViewRight = scrollViewLeft + mScrollView.getWidth();

		if (mItemsContainer.getChildCount() > 0) {
			paddingLeft = mItemsContainer.getChildAt(0).getLeft();
			paddingRight = mItemsContainer.getWidth() - mItemsContainer.getChildAt(mItemsContainer.getChildCount() - 1).getRight();
		}

		for (int i = 0; i < mItemsContainer.getChildCount(); i++) {
			View child = mItemsContainer.getChildAt(i);
			int left = child.getLeft();
			int right = child.getRight();
			if (right < scrollViewLeft) {
				// Not on screen, skip
				continue;
			}
			if (left > scrollViewRight) {
				// The remaining guys are not onscreen, terminate
				break;
			}

			if (child instanceof CollectionStack) {
				CollectionStack stack = (CollectionStack) child;
				int stackCenter = left + (right - left) / 2;
				int distance = (scrollCenter - stackCenter);
				float amount = -1.0f * distance / scrollHalfWidth;
				stack.setStackPosition(amount);
			}
		}
	}
}
