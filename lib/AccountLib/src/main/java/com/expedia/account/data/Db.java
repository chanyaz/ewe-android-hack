package com.expedia.account.data;

/**
 * This represents an in-memory database of data for the app.
 * <p/>
 * Try to keep out information that is state data for a fragment.  For example,
 * keeping track of whether a field has been clicked is not for this.  This is
 * more for passing data between Activities.
 * <p/>
 * Also, be sure to NEVER add anything that could leak memory (such as a Context).
 */
public class Db {

	//////////////////////////////////////////////////////////////////////////
	// Singleton setup
	//
	// We configure this as a singleton in case we ever need to handle
	// multiple instances of Db in the future.  Doubtful, but no reason not
	// to set things up this way.

	private static final Db sDb = new Db();

	private Db() {
		// Cannot be instantiated
	}

	//////////////////////////////////////////////////////////////////////////
	// Stored data

	// Holds data about a new User that has not yet been created
	private PartialUser mNewUser = new PartialUser();

	//////////////////////////////////////////////////////////////////////////
	// Data access

	public static PartialUser getNewUser() {
		return sDb.mNewUser;
	}
}
