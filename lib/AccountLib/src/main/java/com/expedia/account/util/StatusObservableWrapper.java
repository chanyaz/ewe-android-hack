package com.expedia.account.util;

import android.support.annotation.NonNull;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

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

	private Observer<? super Boolean> mDummySubscriber = new Observer<Boolean>() {
		@Override
		public void onComplete() {

		}

		@Override
		public void onError(Throwable e) {

		}

		@Override
		public void onSubscribe(Disposable d) {
		}

		@Override
		public void onNext(Boolean aBoolean) {

		}
	};

	private Observer<? super Boolean> mSubscriber = mDummySubscriber;

	public void subscribe(Observer<Boolean> subscriber) {
		mSubscriber = subscriber;
		mSubscriber.onNext(mEmitter.isGood());
	}
}
