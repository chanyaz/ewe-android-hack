package com.expedia.account.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import rx.Observer;

import static org.assertj.core.api.Assertions.assertThat;

public class CombiningFakeObservableTest {

	List<StatusObservableWrapper> sources;
	List<ControlEmitter> emitters;
	CombiningFakeObservable observable;
	Observer<Boolean> listener = new Observer<Boolean>() {
		@Override
		public void onCompleted() {

		}

		@Override
		public void onError(Throwable e) {

		}

		@Override
		public void onNext(Boolean aBoolean) {
			result = aBoolean;
		}
	};
	boolean result;

	class ControlEmitter implements StatusObservableWrapper.StatusEmitter {
		public boolean value = false;

		@Override
		public boolean isGood() {
			return value;
		}
	}

	@Before
	public void setUp() throws Exception {
		emitters = new ArrayList<>();
		sources = new ArrayList<>();
		observable = new CombiningFakeObservable();

	}

	private void prep(int size) {
		for (int i = 0; i < size; i++) {
			emitters.add(new ControlEmitter());
			sources.add(new StatusObservableWrapper(emitters.get(i)));
			observable.addSource(sources.get(i));
		}
	}

	@Test
	public void testStartAllFalse() {
		prep(3);
		observable.subscribe(listener);
		assertThat(result).isFalse();
	}

	@Test
	public void testMix() {
		prep(5);
		sources.get(0).emit(true);
		sources.get(4).emit(true);
		observable.subscribe(listener);
		assertThat(result).isFalse();
	}

	@Test
	public void testAllTrue() {
		prep(3);
		for (StatusObservableWrapper wrapper : sources) {
			wrapper.emit(true);
		}
		observable.subscribe(listener);
		assertThat(result).isTrue();
	}

	@Test
	public void testFalseThenTrueThenFalse() {
		prep(3);
		observable.subscribe(listener);
		sources.get(0).emit(true);
		assertThat(result).isFalse();
		sources.get(1).emit(true);
		assertThat(result).isFalse();
		sources.get(2).emit(true);
		assertThat(result).isTrue();
		sources.get(1).emit(false);
		assertThat(result).isFalse();
	}
}