package com.expedia.bookings.test;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = "Features",
	glue = { "com.expedia.bookings.test" },
	monochrome = true,
	tags = { "@FlightSearch" },
	plugin = {
		"pretty", "html:/data/local/tmp/cucumber-htmlreport", "json:/data/local/tmp/cucumber-htmlreport/cucumber.json"
	}
)
public class CucumberRunner {
}
