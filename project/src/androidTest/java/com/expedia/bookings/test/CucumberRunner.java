package com.expedia.bookings.test;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;

@CucumberOptions(
		features = "Features",
		glue = { "com.expedia.bookings.test" },
		monochrome = true,
		tags = { "~@manual", "~@deprecated", "~@todo", "~@wip", "~@ignore" },
		plugin = {
			"pretty",
			"json:/data/local/tmp/cucumber-htmlreport/cucumber.json"
		},
		snippets = SnippetType.CAMELCASE
)

public class CucumberRunner {
}
