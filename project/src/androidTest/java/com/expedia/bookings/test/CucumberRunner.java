package com.expedia.bookings.test;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = "Features",
	glue = { "com.expedia.bookings.test" },
	monochrome = true,
	tags = {"@Flights"},
	plugin = { "pretty", "html:/data/local/tmp/cucumber-htmlreport" }
)
public class CucumberRunner {
}

