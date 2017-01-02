package com.expedia.bookings.test;

import android.os.Bundle;
import android.support.test.runner.MonitoringInstrumentation;

import cucumber.api.android.CucumberInstrumentationCore;

public class CucumberInstrumentationRunner extends MonitoringInstrumentation {

	//keeping a separate runner for cucumber tests for now. Forking is not compatible with cucumber yet. WIP.
	private final CucumberInstrumentationCore mInstrumentationCore = new CucumberInstrumentationCore(this);

	@Override
	public void onCreate(Bundle arguments) {
		super.onCreate(arguments);

		mInstrumentationCore.create(arguments);
		start();
	}

	@Override
	public void onStart() {
		super.onStart();

		waitForIdleSync();
		mInstrumentationCore.start();
	}
}
