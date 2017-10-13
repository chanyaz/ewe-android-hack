package com.expedia.bookings.data.abacus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbacusEvaluateQuery extends AbacusBaseQuery {
	private Set<Integer> experiments = new HashSet<>();

	public AbacusEvaluateQuery(String guid, int tpid, int eapid) {
		super(guid, tpid, eapid);
	}

	public void addExperiment(int testId) {
		if (testId > 0) {
			experiments.add(testId);
		}
	}

	public void addExperiments(List<Integer> tests) {
		experiments.addAll(tests);
	}

	public List<Integer> getEvaluatedExperiments() {
		return new ArrayList<>(experiments);
	}
}
