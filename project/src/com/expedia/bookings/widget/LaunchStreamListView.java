package com.expedia.bookings.widget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class LaunchStreamListView extends MeasureListView implements OnTouchListener, OnGestureListener {

	private LaunchStreamListView mSlaveView;
	private GestureDetector mGestureDetector;

	public LaunchStreamListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//setOnScrollListener(this);
		setOnTouchListener(this);
		mGestureDetector = new GestureDetector(context, this);
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
	// OnTouchListener / GestureDetector
	//////////////////////////////////////////////////////////////////////////////////////////
	// All this stuff is to keep the two ListView's in sync. Send flick and scroll 
	// touch gestures to the slave view, but intercept click gestures, since we don't want
	// to click on both ListView's.

	// This list contains a list of events that are intended for dispatch to the slave. events are queued up rather than
	// sent directly so as not to erroneously send click events to the slave. we only want to share the move/fling
	// events
	private List<MotionEvent> eventsForSlave = new ArrayList<MotionEvent>();

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// Set the other ListView onTouchListener to null
		// to ensure there does not exist resonance in touch dispatch
		mSlaveView.setOnTouchListener(null);

		int action = event.getAction();

		// Our OnGestureListener implementation returns true if it is click event. clear out the slave event queue to
		// prevent click events from being sent to the slave view
		if (mGestureDetector.onTouchEvent(event)) {
			eventsForSlave.clear();
		}

		// If it is not a tap click, queue up the events to be sent over to slave view once down and up have occurred
		else {

			// add this event to the list
			eventsForSlave.add(MotionEvent.obtain(event));

			// gesture is complete, send the dispatch stream of MotionEvents and clear list
			if (action == MotionEvent.ACTION_UP) {
				eventsForSlave.add(MotionEvent.obtain(event));

				for (MotionEvent ev : eventsForSlave) {
					mSlaveView.dispatchTouchEvent(ev);
				}

				eventsForSlave.clear();
			}

		}

		// Post the resetting of the onTouchListener on ACTION_UP
		// as that is when the ListView is no longer touched.
		if (action == MotionEvent.ACTION_UP) {
			mSlaveView.post(new Runnable() {
				@Override
				public void run() {
					mSlaveView.setOnTouchListener(mSlaveView);
				}
			});
		}

		// return false to pass the MotionEvent down to child
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return true;
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

	protected void scrollSlaveBy(int deltaY) {
		// TODO: this "*2" formula is pretty simple. We may want to make it more complex. 
		mSlaveView.scrollListBy(deltaY * 2);
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
