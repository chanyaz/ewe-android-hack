package com.expedia.bookings.test.rules;

import java.net.URL;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import android.support.test.InstrumentationRegistry;

import com.expedia.bookings.test.ui.tablet.pagemodels.Settings;
import com.expedia.bookings.utils.AndroidFileOpener;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileOpener;
import com.squareup.okhttp.mockwebserver.MockWebServer;

public class ExpediaMockWebServerRule implements TestRule {
	@Override
	public final Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				MockWebServer mockServer = new MockWebServer();
				FileOpener fileOpener = new AndroidFileOpener(InstrumentationRegistry.getInstrumentation().getTargetContext());
				ExpediaDispatcher dispatcher = new ExpediaDispatcher(fileOpener);
				mockServer.setDispatcher(dispatcher);

				mockServer.start();
				URL mockUrl = mockServer.getUrl("");
				String server = mockUrl.getHost() + ":" + mockUrl.getPort();
				Settings.setCustomServer(InstrumentationRegistry.getInstrumentation(), server);
				Settings.clearPrivateData(InstrumentationRegistry.getInstrumentation());

				base.evaluate();

				mockServer.shutdown();
			}
		};
	}
}
