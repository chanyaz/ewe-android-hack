package com.expedia.account;

import java.lang.ref.WeakReference;

import android.support.v4.app.FragmentActivity;

import okhttp3.OkHttpClient;

/**
 * Holds the configuration for AccountView. Config uses a builder pattern:
 * <p/>
 * <pre class="prettyprint">
 * vAccountView.configure(Config.build()
 * .setService(api, 0, 0, "accountstest.phone.android")
 * .setBackgroundImageView(vBackground)
 * .setPOSEnableSpamByDefault(true)
 * .setPOSShowSpamOptIn(true)
 * .setAutoEnrollUserInRewards(false)
 * .setUserRewardsEnrollmentCheck(true)
 * .setEnableFacebookButton(true)
 * .setMarketingString(getString(R.string.marketing_string))
 * .setListener(mAccountViewListener)
 * .setAnalyticsListener(mAnalyticsListener)
 * .setInitialState(InitialState.CreateAccount)
 * .setRewardsText(getString(R.string.loyalty_terms_of_service))
 * );
 * </pre>
 */
public class Config {

	public enum InitialState {
		SignIn(AccountView.STATE_SIGN_IN),
		FacebookSignIn(AccountView.STATE_FACEBOOK_API_HOST),
		CreateAccount(AccountView.STATE_EMAIL_NAME),
		SinglePageCreateAccount(AccountView.STATE_SINGLE_PAGE_SIGN_UP);

		private String state;

		InitialState(String state) {
			this.state = state;
		}
	}

	WeakReference<PanningImageView> background;

	boolean showSpamOptIn = false;
	boolean enableSpamByDefault = false;
	boolean enableFacebookButton = true;
	boolean enableSignInMessaging = false;
	boolean enableSinglePageSignUp = false;
	boolean shouldAutoEnrollUserInRewards = false;
	boolean hasUserRewardsEnrollmentCheck = false;
	CharSequence tosText;
	CharSequence marketingText;
	CharSequence signInMessagingText;
	CharSequence rewardsText;
	String initialState = AccountView.STATE_SIGN_IN;
	String signupString;

	String facebookAppId;

	FragmentActivity parentActivity;

	private AnalyticsListener analyticsListener;
	private AccountView.Listener accountViewListener;

	private AccountService service;

	public static Config build() {
		return new Config();
	}

	private Config() {
		// use build() instead
	}

	/**
	 * Sets the API endpoint that AccountLib should connect to.
	 * For example:
	 * <p/>
	 * config.setService(new OkHttpClient(), "https://www.expedia.com/", 0, 0, "com.expedia.myapp");
	 */
	public Config setService(OkHttpClient client, String endpoint, int siteId, int langId, String clientId) {
		if (!endpoint.startsWith("https")) {
			throw new IllegalArgumentException("Must use an HTTPS endpoint");
		}
		this.service = new AccountService(client, endpoint, siteId, langId, clientId);
		return this;
	}

	/**
	 * Sets a custom AccountService that AccountLib should connect to, for testing purposes.
	 * For example:
	 * <p/>
	 * config.setService(new AccountService(MockExpediaAccountApi.build(), 0, 0, "com.expedia.myapp"));
	 *
	 * @param service An instance of an already created AccountService
	 */
	public Config setService(AccountService service) {
		this.service = service;
		return this;
	}

	public Config setBackgroundImageView(PanningImageView background) {
		this.background = background == null ? null : new WeakReference<>(background);
		return this;
	}

	public Config setPOSShowSpamOptIn(boolean showSpamOptIn) {
		this.showSpamOptIn = showSpamOptIn;
		return this;
	}

	public Config setUserRewardsEnrollmentCheck(boolean hasUserRewardsEnrollmentCheck) {
		this.hasUserRewardsEnrollmentCheck = hasUserRewardsEnrollmentCheck;
		return this;
	}

	public Config setAutoEnrollUserInRewards(boolean shouldAutoEnrollUserInRewards) {
		this.shouldAutoEnrollUserInRewards = shouldAutoEnrollUserInRewards;
		return this;
	}

	public Config setPOSEnableSpamByDefault(boolean enableSpamByDefault) {
		this.enableSpamByDefault = enableSpamByDefault;
		return this;
	}

	public Config setEnableFacebookButton(boolean enableFacebookButton) {
		this.enableFacebookButton = enableFacebookButton;
		return this;
	}

	public Config setEnableSignInMessaging(boolean enableSignInMessaging) {
		this.enableSignInMessaging = enableSignInMessaging;
		return this;
	}

	public Config setEnableSinglePageSignUp(boolean enableSinglePageSignUp) {
		this.enableSinglePageSignUp = enableSinglePageSignUp;
		return this;
	}

	/**
	 * Sets the text for the Terms of Service. This text can have clickable
	 * links (ClickableSpan) in it; and if so, it will automatically have
	 * its MovementMethod set to LinkMovementMethod. It can also have
	 * other types of spans.
	 * <p/>
	 * For example:
	 * <p/>
	 * config.setTOSText(Html.fromHtml(getString(R.string.terms_of_service)));
	 */
	public Config setTOSText(CharSequence tosText) {
		this.tosText = tosText;
		return this;
	}

	/**
	 * Sets the text for the Marketing Opt In checkbox. This text can have clickable
	 * links (ClickableSpan) in it; and if so, it will automatically have
	 * its MovementMethod set to LinkMovementMethod. It can also have
	 * other types of spans.
	 * <p/>
	 * For example:
	 * <p/>
	 * config.setMarketingText(getString(R.string.agree_to_spam));
	 */
	public Config setMarketingText(CharSequence marketingText) {
		this.marketingText = marketingText;
		return this;
	}

	public Config setSignInMessagingText(CharSequence signInMessagingText) {
		this.signInMessagingText = signInMessagingText;
		return this;
	}

	public Config setRewardsText(CharSequence rewardsText) {
		this.rewardsText = rewardsText;
		return this;
	}

	public Config setFacebookAppId(String appId) {
		this.facebookAppId = appId;
		return this;
	}

	public Config setAnalyticsListener(AnalyticsListener listener) {
		this.analyticsListener = listener;
		return this;
	}

	public Config setListener(AccountView.Listener listener) {
		this.accountViewListener = listener;
		return this;
	}

	public Config setInitialState(InitialState initialState) {
		this.initialState = initialState.state;
		return this;
	}

	public Config setParentActivity(FragmentActivity activity) {
		this.parentActivity = activity;
		return this;
	}

	public Config setSignupString(String signupText) {
		this.signupString = signupText;
		return this;
	}

	public AnalyticsListener getAnalyticsListener() {
		return analyticsListener;
	}

	public AccountView.Listener getListener() {
		return accountViewListener;
	}

	public void clearPointers() {
		setAnalyticsListener(null);
		setListener(null);
	}

	public AccountService getService() {
		return this.service;
	}
}