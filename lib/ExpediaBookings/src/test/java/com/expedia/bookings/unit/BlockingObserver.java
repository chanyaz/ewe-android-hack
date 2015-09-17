package com.expedia.bookings.unit;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Observer;

public class BlockingObserver<T> implements Observer<T> {
	private final CountDownLatch mLatch;
	private final ArrayList<T> mItems = new ArrayList<>();
	private final ArrayList<Throwable> mErrors = new ArrayList<>();
	private boolean mCompleted = false;

	public BlockingObserver(int count) {
		mLatch = new CountDownLatch(count);
	}

	@Override
	public void onCompleted() {
		mCompleted = true;
		mLatch.countDown();
	}

	@Override
	public void onError(Throwable e) {
		mErrors.add(e);
		mLatch.countDown();
	}

	@Override
	public void onNext(T object) {
		mItems.add(object);
		mLatch.countDown();
	}

	public void await() throws Throwable {
		mLatch.await(5, TimeUnit.SECONDS);
	}

	public ArrayList<T> getItems() {
		return mItems;
	}

	public ArrayList<Throwable> getErrors() {
		return mErrors;
	}

	public boolean completed() {
		return mCompleted;
	}
}
