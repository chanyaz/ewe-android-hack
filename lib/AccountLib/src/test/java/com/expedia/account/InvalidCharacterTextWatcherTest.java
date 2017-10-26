package com.expedia.account;

import org.junit.Before;
import org.junit.Test;

import android.text.Editable;

import com.expedia.account.util.InvalidCharacterTextWatcher;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public final class InvalidCharacterTextWatcherTest {
	@Test
	public void invalidCharacterPattern() {
		assertThat(invalid("a")).isFalse();
		assertThat(invalid("abcdefg?!")).isFalse();
		assertThat(invalid("abcd adefs")).isFalse();
		assertThat(invalid("ABDEFHTNTASD")).isFalse();
		assertThat(invalid("ABDdFeHgNsdTrwASD")).isFalse();
		assertThat(invalid("")).isFalse();

		assertThat(invalid("0")).isTrue();
		//We are totally cool with some other characters, too
		assertThat(invalid("\u00DC")).isFalse();
		assertThat(invalid("ñiøü")).isFalse();
	}

	private boolean invalid(String input) {
		return InvalidCharacterTextWatcher.INVALID_CHARACTER_PATTERN.matcher(input).matches();
	}

	private InvalidCharacterTextWatcher watcher;
	private Editable editable;
	private InvalidListener invalidListener;

	@Before
	public void reset() {
		editable = mock(Editable.class);
		watcher = new InvalidCharacterTextWatcher(null);
		invalidListener = new InvalidListener();
	}

	private void givenEditableWithInput(String input) {
		when(editable.length()).thenReturn(input.length());
		when(editable.toString()).thenReturn(input);
	}

	private void givenWatcherWithListener(InvalidCharacterTextWatcher.InvalidCharacterListener listener) {
		watcher = new InvalidCharacterTextWatcher(listener);
	}

	private void whenTextChanged() {
		watcher.afterTextChanged(editable);
	}

	private void thenEditableNotTouched() {
		verify(editable, never()).replace(anyInt(), anyInt(), anyString());
	}

	private void thenEditableIs(String expected) {
		verify(editable, times(1)).replace(eq(0), anyInt(), eq(expected));
	}

	private void thenListenerFired() {
		assertThat(invalidListener.fired).isTrue();
	}

	@Test
	public void afterTextChangedWithEmpty() {
		givenEditableWithInput("");

		whenTextChanged();

		thenEditableNotTouched();
	}

	@Test
	public void afterTextChangedDoNothing() {
		givenEditableWithInput("I am a string");

		whenTextChanged();

		thenEditableNotTouched();
	}

	@Test
	public void afterTextChangedFilterStart() {
		givenEditableWithInput("123456Bender Bending Rodriguez");

		whenTextChanged();

		thenEditableIs("Bender Bending Rodriguez");
	}

	@Test
	public void afterTextChangedFilterMiddle() {
		givenEditableWithInput("Bender Bending1234 Rodriguez");

		whenTextChanged();

		thenEditableIs("Bender Bending Rodriguez");
	}

	@Test
	public void afterTextChangedFilterEnd() {
		givenEditableWithInput("Bender Bending Rodriguez123");

		whenTextChanged();

		thenEditableIs("Bender Bending Rodriguez");
	}

	@Test
	public void afterTextChangedFireListener() {
		givenEditableWithInput("Bender Bending Rodriguez1");
		givenWatcherWithListener(invalidListener);

		whenTextChanged();

		thenEditableIs("Bender Bending Rodriguez");
		thenListenerFired();
	}

	private static class InvalidListener implements InvalidCharacterTextWatcher.InvalidCharacterListener {
		public boolean fired = false;

		@Override
		public void onInvalidCharacterEntered(String text) {
			fired = true;
		}
	}
}
