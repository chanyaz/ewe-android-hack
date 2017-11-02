package com.expedia.account.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class CombiningFakeObservable {

	private List<Observer<Boolean>> mSubscribers = new LinkedList<>();
	private HashMap<Observer<Boolean>, Boolean> mObservations = new HashMap<>();

	private boolean last;

	public void subscribe(Observer<Boolean> subscriber) {
		mSubscribers.add(subscriber);
		subscriber.onNext(last);
	}

	public void unsubscribe(Observer<Boolean> subscriber) {
		mSubscribers.remove(subscriber);
	}

	public void emit() {
		for (Observer<Boolean> subscriber : mSubscribers) {
			subscriber.onNext(last);
		}
	}

	public void addSource(StatusObservableWrapper source) {
		final Observer<Boolean> sub = new Observer<Boolean>() {
			@Override
			public void onComplete() {
				mObservations.remove(this);
			}

			@Override
			public void onError(Throwable e) {
			}

			@Override
			public void onSubscribe(Disposable d) {
			}

			@Override
			public void onNext(Boolean aBoolean) {
				recompute(this, aBoolean);
			}
		};
		//The false should be immediately overwritten when the subscribe happens
		mObservations.put(sub, false);
		source.subscribe(sub);
	}

	public void recompute(Observer<Boolean> subscriber, Boolean value) {
		if (mObservations.get(subscriber) != value) {
			mObservations.put(subscriber, value);
		}
		else {
			return;
		}
		boolean isAllTrue = true;
		for (boolean item : mObservations.values()) {
			if (!item) {
				isAllTrue = false;
				break;
			}
		}
		//Only bother changing and emitting it if it's different
		if (last != isAllTrue) {
			last = isAllTrue;
			emit();
		}
	}
}
