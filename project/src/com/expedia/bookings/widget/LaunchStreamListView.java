package com.expedia.bookings.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class LaunchStreamListView extends MeasureListView implements OnScrollListener {

	private LaunchStreamListView mSlaveView;

	public LaunchStreamListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnScrollListener(this);
	}

	public void setSlaveView(LaunchStreamListView slave) {
		mSlaveView = slave;
	}

	public void selectMiddle() {
		CircularArrayAdapter<Object> adapter = (CircularArrayAdapter<Object>) getAdapter();
		View v = getChildAt(0);
		int offset = (v == null) ? 0 : v.getTop();
		setSelectionFromTop(adapter.getMiddle(), offset);
	}

	private int mSavedPosition;
	private int mSavedOffset;

	public void savePosition() {
		mSavedPosition = getFirstVisiblePosition();
		View v = getChildAt(0);
		mSavedOffset = (v == null) ? 0 : v.getTop();
	}

	public void restorePosition() {
		setSelectionFromTop(mSavedPosition, mSavedOffset);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// OnScrollListener
	//////////////////////////////////////////////////////////////////////////////////////////

	private boolean mDoNotPropogateNextScrollEvent = false;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mSlaveView == null) {
			return;
		}

		int deltaY = getDistanceScrolled();

		if (mDoNotPropogateNextScrollEvent) {
			mDoNotPropogateNextScrollEvent = false;
			return;
		}

		mSlaveView.scrollListBy(deltaY);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// getDistanceScrolled
	//////////////////////////////////////////////////////////////////////////////////////////

	private int mDistanceScrolledPosition;
	private int mDistanceScrolledOffset;

	// Returns the distance scrolled since the last call to this function.
	// *** This will only work if all cells in this ListView are the same height.
	private int getDistanceScrolled() {
		if (getChildAt(0) == null || getChildAt(1) == null) {
			return 0;
		}

		int position = getFirstVisiblePosition();
		int offset = getChildAt(0).getTop();
		int height = getChildAt(1).getTop() - offset;

		int deltaY = (mDistanceScrolledPosition - position) * height + (offset - mDistanceScrolledOffset);

		mDistanceScrolledPosition = position;
		mDistanceScrolledOffset = offset;

		return deltaY;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// scrollListBy()
	//////////////////////////////////////////////////////////////////////////////////////////

	private int mScrollByPosition = 0;
	private int mScrollByTop = 0;
	private int mScrollByDelta = 0;

	// If this is called twice quickly before a refresh, we need to just increase the distance
	// instead of replacing it.
	protected void scrollListBy(int deltaY) {
		mDoNotPropogateNextScrollEvent = true;

		int position = getFirstVisiblePosition();
		int top = (getChildAt(0) == null ? 0 : getChildAt(0).getTop());
		if (mScrollByPosition == position && mScrollByTop == top) {
			mScrollByDelta += deltaY;
		}
		else {
			mScrollByDelta = deltaY;
			mScrollByPosition = position;
			mScrollByTop = top;
		}

		if (mScrollByDelta != 0) {
			setSelectionFromTop(position, top + mScrollByDelta);
			invalidate();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Marquee to auto-scroll this view. Copied from TextView.java
	//////////////////////////////////////////////////////////////////////////////////////////

	private Marquee mMarquee;

	public void startMarquee() {
		if (mMarquee == null) {
			mMarquee = new Marquee(this);
		}
		mMarquee.start();
	}

	public void stopMarquee() {
		if (mMarquee != null) {
			mMarquee.stop();
		}
	}

	private static final class Marquee extends Handler {
		private static final int MARQUEE_DELAY = 1200;
		private static final int MARQUEE_RESOLUTION = 1000 / 30;
		private static final int MARQUEE_PIXELS_PER_SECOND = 30;

		private static final byte MARQUEE_STOPPED = 0x0;
		private static final byte MARQUEE_STARTING = 0x1;
		private static final byte MARQUEE_RUNNING = 0x2;

		private static final int MESSAGE_START = 0x1;
		private static final int MESSAGE_TICK = 0x2;
		private static final int MESSAGE_RESTART = 0x3;

		private final WeakReference<LaunchStreamListView> mView;

		private byte mStatus = MARQUEE_STOPPED;
		private final int mScrollUnit;

		Marquee(LaunchStreamListView v) {
			final float density = v.getContext().getResources().getDisplayMetrics().density;
			mScrollUnit = -(int) ((MARQUEE_PIXELS_PER_SECOND * density) / MARQUEE_RESOLUTION);
			mView = new WeakReference<LaunchStreamListView>(v);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_START:
				mStatus = MARQUEE_RUNNING;
				tick();
				break;
			case MESSAGE_TICK:
				tick();
				break;
			case MESSAGE_RESTART:
				if (mStatus == MARQUEE_RUNNING) {
					start();
				}
				break;
			}
		}

		void tick() {
			if (mStatus != MARQUEE_RUNNING) {
				return;
			}

			removeMessages(MESSAGE_TICK);

			final LaunchStreamListView listView = mView.get();
			if (listView != null) {
				listView.scrollListBy(mScrollUnit);
				//TODO: test for max scroll, and if so, reset
				//sendEmptyMessageDelayed(MESSAGE_RESTART, MARQUEE_RESTART_DELAY);
				sendEmptyMessageDelayed(MESSAGE_TICK, MARQUEE_RESOLUTION);
				listView.invalidate();
			}
		}

		void stop() {
			mStatus = MARQUEE_STOPPED;
			removeMessages(MESSAGE_START);
			removeMessages(MESSAGE_RESTART);
			removeMessages(MESSAGE_TICK);
		}

		void start() {
			final LaunchStreamListView listView = mView.get();
			if (listView != null) {
				mStatus = MARQUEE_STARTING;
				sendEmptyMessageDelayed(MESSAGE_START, MARQUEE_DELAY);
			}
		}
	}

}
