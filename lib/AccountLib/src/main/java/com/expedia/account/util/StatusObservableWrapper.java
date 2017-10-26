package com.expedia.account.util;

import android.support.annotation.NonNull;

import rx.Subscriber;

public class StatusObservableWrapper {

	public interface StatusEmitter {
		boolean isGood();
	}

	public void clearSubscriber() {
		mSubscriber = mDummySubscriber;
	}

	private StatusEmitter mEmitter;

	public void emit(boolean newValue) {
		mSubscriber.onNext(newValue);
	}

	public StatusObservableWrapper(@NonNull StatusEmitter emitter) {
		if (emitter == null) {
			throw new IllegalArgumentException("Emitter passed to StatusEmitter can not be null, but it is");
		}
		mEmitter = emitter;
	}

	private Subscriber<? super Boolean> mDummySubscriber = new Subscriber<Boolean>() {
		@Override
		public void onCompleted() {

		}

		@Override
		public void onError(Throwable e) {

		}

		@Override
		public void onNext(Boolean aBoolean) {

		}
	};

	private Subscriber<? super Boolean> mSubscriber = mDummySubscriber;

	public void subscribe(Subscriber<Boolean> subscriber) {
		mSubscriber = subscriber;
		mSubscriber.onNext(mEmitter.isGood());
	}
}
