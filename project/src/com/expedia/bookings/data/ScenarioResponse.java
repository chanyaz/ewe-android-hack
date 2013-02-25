package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

public class ScenarioResponse extends Response {

	private List<Scenario> mScenarios = new ArrayList<Scenario>();

	public void addScenario(String name, String url) {
		mScenarios.add(new Scenario(name, url));
	}

	public List<Scenario> getScenarios() {
		return mScenarios;
	}
}
