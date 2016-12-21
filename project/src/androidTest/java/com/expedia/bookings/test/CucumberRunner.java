package com.expedia.bookings.test;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = "MultibrandFeatures",
	glue = { "com.expedia.bookings.test" },
	monochrome = true,
	plugin = { "pretty" }
)
public class CucumberRunner {
}

