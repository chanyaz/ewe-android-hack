package com.expedia.bookings.test.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.expedia.bookings.test.Settings;
import com.expedia.bookings.utils.ExpediaMockWebServer;

public class ExpediaMockWebServerRule implements TestRule {

	@Override
	public final Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
				Context context = instrumentation.getTargetContext();

				ExpediaMockWebServer mockServer = new ExpediaMockWebServer(context);

				Settings.setCustomServer(mockServer.getHostWithPort());
				Settings.clearPrivateData();

				base.evaluate();

				mockServer.shutdown();
			}
		};
	}
}
