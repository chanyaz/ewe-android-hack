package com.expedia.bookings.test;

import android.os.Bundle;
import android.support.test.runner.MonitoringInstrumentation;

import cucumber.api.android.CucumberInstrumentationCore;

public class CucumberInstrumentationRunner extends MonitoringInstrumentation {

	private final CucumberInstrumentationCore mInstrumentationCore = new CucumberInstrumentationCore(this);

	@Override
	public void onCreate(Bundle arguments) {
		mInstrumentationCore.create(arguments);
		super.onCreate(arguments);
		start();
	}

	@Override
	public void onStart() {
		super.onStart();
		waitForIdleSync();
		mInstrumentationCore.start();
	}
}
