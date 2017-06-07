package com.expedia.bookings.data;

/**
 * This enum standardizes the various LineOfBusiness-like enums that have popped up in various sections of the code
 * where we require different logic/params based on the LineOfBusiness.
 *
 * Important Note: This is not entirely analogous the the Expedia line of business. For instance, ITIN will never
 * be though of as a line of business to Expedia, although, the client teams like to refer to line of business for the
 * different "sub-projects" and code paths that we create in the app.
 */
public enum LineOfBusiness {
	FLIGHTS,
	FLIGHTS_V2,
	HOTELS,
	CARS,
	ITIN,
	LX,
	PACKAGES,
	TRANSPORT,
	RAILS,
	PROFILE,
	LAUNCH,
	NONE;
}
