package android.security;

/**
 * Hack to get Roboelectric working with OkHTTP-3.3.1
 * https://github.com/square/okhttp/issues/2533
 * https://github.com/ExpediaInc/ewe-android-eb/pull/5276
 */
public class NetworkSecurityPolicy {
	private static final NetworkSecurityPolicy INSTANCE = new NetworkSecurityPolicy();

	public static NetworkSecurityPolicy getInstance() {
		return INSTANCE;
	}

	public boolean isCleartextTrafficPermitted() {
		return true;
	}
}
