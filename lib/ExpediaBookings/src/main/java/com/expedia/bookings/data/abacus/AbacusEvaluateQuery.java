package com.expedia.bookings.data.abacus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by malnguyen on 4/15/15.
 */
public class AbacusEvaluateQuery extends AbacusBaseQuery {
	public final List<Integer> evaluatedExperiments = new ArrayList<>();

	public AbacusEvaluateQuery(String guid, int tpid, int eapid) {
		super(guid, tpid, eapid);
	}

	public void addExperiment(int test) {
		evaluatedExperiments.add(test);
	}

	public void addExperiments(List<Integer> tests) {
		evaluatedExperiments.addAll(tests);
	}
}
