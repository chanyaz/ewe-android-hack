package com.expedia.bookings.test;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = "Features",
	glue = { "com.expedia.bookings.test" },
	monochrome = true,
	tags = {"@HotelSearch"},
	plugin = { "pretty" }
)
public class CucumberRunner {
}

