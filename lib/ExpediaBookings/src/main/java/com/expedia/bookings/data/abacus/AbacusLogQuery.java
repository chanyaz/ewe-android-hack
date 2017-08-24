package com.expedia.bookings.data.abacus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by malnguyen on 4/15/15.
 */
public class AbacusLogQuery extends AbacusBaseQuery {
	public final List<AbacusTest> evaluatedExperiments = new ArrayList<>();

	public AbacusLogQuery(String guid, int tpid, int eapid) {
		super(guid, tpid, eapid);
	}

	public void addExperiment(AbacusTest test) {
		if (test != null) {
			evaluatedExperiments.add(test);
		}
	}

}
