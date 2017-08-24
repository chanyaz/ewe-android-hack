package com.expedia.bookings.utils;
/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.text.Editable;
import android.text.TextWatcher;

public class NumberMaskTextWatcher implements TextWatcher {
	private static final String TAG = NumberMaskTextWatcher.class.getSimpleName();

	boolean mLoopDetect = false;
	private final NumberMaskFormatter mMaskFormatter;

	public NumberMaskTextWatcher(String mask) {
		this(new NumberMaskFormatter(mask));
	}

	public NumberMaskTextWatcher(NumberMaskFormatter formatter) {
		mMaskFormatter = formatter;
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (mLoopDetect) {
			// Don't do an infinite loop please.
			return;
		}

		mLoopDetect = true;
		mMaskFormatter.applyTo(s);
		mLoopDetect = false;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

}
