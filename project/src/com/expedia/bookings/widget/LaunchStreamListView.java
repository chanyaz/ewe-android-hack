package com.expedia.bookings.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class LaunchStreamListView extends MeasureListView implements OnScrollListener {

	private LaunchStreamListView mSlaveView;

	private double mScrollMultiplier = 1.0;

	public LaunchStreamListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnScrollListener(this);
	}

	public void setSlaveView(LaunchStreamListView slave) {
		mSlaveView = slave;
	}

	public void setScrollMultiplier(double multiplier) {
		mScrollMultiplier = multiplier;
	}

	public void selectMiddle() {
		CircularArrayAdapter<Object> adapter = (CircularArrayAdapter<Object>) getAdapter();
		View v = getChildAt(0);
		int offset = (v == null) ? 0 : v.getTop();
		mSetExplicitPosition = true;
		setSelectionFromTop(adapter.getMiddle(), offset);
	}

	private boolean mHasSavedPosition;
	private int mSavedPosition;
	private int mSavedOffset;

	public void savePosition() {
		mSavedPosition = getFirstVisiblePosition();
		View v = getChildAt(0);
		mSavedOffset = (v == null) ? 0 : v.getTop();
		mHasSavedPosition = true;
	}

	public boolean restorePosition() {
		if (!mHasSavedPosition) {
			return false;
		}

		mSetExplicitPosition = true;
		setSelectionFromTop(mSavedPosition, mSavedOffset);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mBeingSlaveDriven) {
			// If we're being slave driven, pass the touch event to the other guy.  Let him handle it, bro.
			return mSlaveView.onTouchEvent(ev);
		}
		else {
			// Stop marquee when we get any touch events, and only start again if we are not flinging.
			// (The scroll state change will indicate when to restart marquee in that circumstnace.)
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				stopMarquee();
				mSlaveView.stopMarquee();
				break;
			case MotionEvent.ACTION_UP:
				startMarquee();
				mSlaveView.startMarquee();
				break;
			}

			return super.onTouchEvent(ev);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// OnScrollListener
	//////////////////////////////////////////////////////////////////////////////////////////

	private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	// Represents if an explicit position has been set for this ListView.  This can happen
	// when setting itself to the middle, or restoring its position.  In those cases, we
	// don't want any of the normal master/slave relationship to take place
	private boolean mSetExplicitPosition;

	// Represents of state of being explicitly moved by another ListView.  This ListView should not
	// respond to any touches and should not move itself in any fashion.
	private boolean mBeingSlaveDriven = false;

	public void setBeingSlaveDriven(boolean beingSlaveDriven) {
		mBeingSlaveDriven = beingSlaveDriven;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// If an explicit position was set for this ListView, assume it was meant just for
		// this and don't pass it along to the slave.
		if (mSetExplicitPosition) {
			mSetExplicitPosition = false;
			return;
		}

		// Always call this, otherwise we get out of sync for future calls of getDistanceScrolled()
		int deltaY = getDistanceScrolled();

		// Don't move our slave if we're being slaved over, or if it's idle (in which case, why would we pass
		// the move on?  This is a weird thing, but needs to be checked for some versions of Android).
		if (mSlaveView == null || mBeingSlaveDriven || mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			return;
		}

		// If we've gotten this far, move the slave a certain distance down
		scrollSlaveBy(deltaY);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;

		if (mSlaveView == null || mBeingSlaveDriven) {
			return;
		}

		if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
			mSlaveView.setBeingSlaveDriven(true);
		}
		else if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			mSlaveView.setBeingSlaveDriven(false);
		}

		// Change marquee state based on state
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			resumeMarquee();
			mSlaveView.resumeMarquee();
		}
		else {
			stopMarquee();
			mSlaveView.stopMarquee();
		}
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
	private double mScrollByDelta = 0;

	// If this is called twice quickly before a refresh, we need to just increase the distance
	// instead of replacing it.
	protected void scrollListBy(double deltaY) {
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
			setSelectionFromTop(position, (int) (top + mScrollByDelta));
			invalidate();
		}
	}

	protected void scrollSlaveBy(int deltaY) {
		mSlaveView.scrollListBy(deltaY * (mSlaveView.mScrollMultiplier / mScrollMultiplier));
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

	public void resumeMarquee() {
		if (mMarquee != null) {
			mMarquee.start();
		}
	}

	public void stopMarquee() {
		if (mMarquee != null) {
			mMarquee.stop();
		}
	}

	private static final class Marquee extends Handler {
		private static final int MARQUEE_DELAY = 0;
		private static final int MARQUEE_RESOLUTION = 1000 / 30;
		private static final int MARQUEE_PIXELS_PER_SECOND = 20;

		private static final byte MARQUEE_STOPPED = 0x0;
		private static final byte MARQUEE_STARTING = 0x1;
		private static final byte MARQUEE_RUNNING = 0x2;

		private static final int MESSAGE_START = 0x1;
		private static final int MESSAGE_TICK = 0x2;
		private static final int MESSAGE_RESTART = 0x3;

		private final WeakReference<LaunchStreamListView> mView;

		private byte mStatus = MARQUEE_STOPPED;
		private final float mScrollUnit;

		private float mRemainder = 0f;

		Marquee(LaunchStreamListView v) {
			final float density = v.getContext().getResources().getDisplayMetrics().density;
			mScrollUnit = (MARQUEE_PIXELS_PER_SECOND * density) / MARQUEE_RESOLUTION;
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
				float scrollf = mScrollUnit + mRemainder;
				int scroll = (int) scrollf;
				mRemainder = scrollf - scroll;
				listView.scrollListBy(-scroll);
				listView.scrollSlaveBy(-scroll);
				sendEmptyMessageDelayed(MESSAGE_TICK, MARQUEE_RESOLUTION);
				listView.invalidate();
			}
		}

		void stop() {
			if (mStatus != MARQUEE_STOPPED) {
				mStatus = MARQUEE_STOPPED;
				removeMessages(MESSAGE_START);
				removeMessages(MESSAGE_RESTART);
				removeMessages(MESSAGE_TICK);
			}
		}

		void start() {
			if (mStatus == MARQUEE_STOPPED && mView.get() != null) {
				mStatus = MARQUEE_STARTING;
				sendEmptyMessageDelayed(MESSAGE_START, MARQUEE_DELAY);
			}
		}
	}
}
