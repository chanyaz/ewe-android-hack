package com.expedia.bookings.data.abacus;

/**
 * Created by malnguyen on 4/2/15.
 */
public class AbacusBaseQuery {
	public String guid;
	public int tpid;
	public int eapid;

	public AbacusBaseQuery(String guid, int tpid, int eapid) {
		this.guid = guid;
		this.tpid = tpid;
		this.eapid = eapid;
	}

}
