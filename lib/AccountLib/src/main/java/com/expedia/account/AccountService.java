package com.expedia.account;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.expedia.account.data.AccountResponse;
import com.expedia.account.data.FacebookLinkResponse;
import com.expedia.account.data.PartialUser;
import com.expedia.account.server.ExpediaAccountApi;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AccountService {

	private ExpediaAccountApi api;
	private Integer siteId;
	private Integer langId;
	private String clientId;

	/**
	 * Creates an AccountService with a custom ExpediaAccountApi (used with a mock api for testing)
	 *
	 * @param api      An implementation of ExpediaAccountApi
	 * @param siteId   PointOfSale.getPointOfSale().getSiteId()
	 * @param langId   PointOfSale.getPointOfSale().getDualLanguageId()
	 * @param clientId The client id ("mobile.android.flighttrack" and stuff)
	 */
	public AccountService(@NonNull ExpediaAccountApi api, @Nullable Integer siteId,
		@Nullable Integer langId, @NonNull String clientId) {
		this.siteId = siteId;
		this.langId = langId;
		this.clientId = clientId;
		this.api = api;
	}

	/**
	 * Creates an AccountService that uses the specified client and server endpoint.
	 *
	 * @param client
	 * @param endpoint server endpoint (i.e. "http://www.expedia.com")
	 * @param siteId   PointOfSale.getPointOfSale().getSiteId()
	 * @param langId   PointOfSale.getPointOfSale().getDualLanguageId()
	 * @param clientId The client id ("mobile.android.flighttrack" and stuff)
	 */
	public AccountService(OkHttpClient client, String endpoint, int siteId, int langId, String clientId) {
		this(client, endpoint, siteId, langId, clientId, null, new ArrayList<Interceptor>());
	}

	/**
	 * Creates an AccountService that uses the specified client and server endpoint.
	 *
	 * @param client
	 * @param endpoint server endpoint (i.e. "http://www.expedia.com")
	 * @param siteId   PointOfSale.getPointOfSale().getSiteId()
	 * @param langId   PointOfSale.getPointOfSale().getDualLanguageId()
	 * @param clientId The client id ("mobile.android.flighttrack" and stuff)
	 * @param userAgent The String passed as User-Agent Header in all requests
	 */
	public AccountService(OkHttpClient client, String endpoint, int siteId, int langId, String clientId, final String userAgent,
		List<Interceptor> interceptorList) {
		this(null, siteId, langId, clientId);

		if (!endpoint.startsWith("https")) {
			throw new IllegalArgumentException("Must use an HTTPS endpoint");
		}

		OkHttpClient.Builder clientBuilder = client.newBuilder();
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(HttpLoggingInterceptor.Level.BODY);
		clientBuilder.addInterceptor(logger);

		for (Interceptor interceptor : interceptorList) {
			clientBuilder.addInterceptor(interceptor);
		}

		if (!TextUtils.isEmpty(userAgent)) {
			Interceptor requestInterceptor = new Interceptor() {
				@Override
				public Response intercept(Chain chain) throws IOException {
					Request.Builder request = chain.request().newBuilder();
					request.addHeader("User-Agent", userAgent);
					Response response = chain.proceed(request.build());
					return response;
				}

			};
			clientBuilder.addInterceptor(requestInterceptor);
		}

		Retrofit adapter = new Retrofit.Builder()
			.baseUrl(endpoint)
			.client(clientBuilder.build())
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.build();
		this.api = adapter.create(ExpediaAccountApi.class);
	}

	public Observable<AccountResponse> signIn(String email, String password, String recaptchaResponseToken) {
		return api.signIn(email, password, true /*staySignedIn*/, recaptchaResponseToken, getCommonParams());
	}

	public Observable<AccountResponse> signInProfileOnly() {
		return api.signInProfileOnly(true, getCommonParams());
	}

	/**
	 * Creates a new Expedia account with the provided information.
	 *
	 * @return the CreateUserResponse
	 */
	public Observable<AccountResponse> createUser(PartialUser user) {
		return api.createUser(user.email, user.password, user.firstName, user.lastName,
			user.expediaEmailOptin, true /*staySignedIn*/, user.enrollInLoyalty, user.recaptchaResponseToken, getCommonParams());
	}

	private Map<String, String> getCommonParams() {
		Map<String, String> params = new HashMap<>();

		params.put("langid", Integer.toString(langId));
		params.put("siteid", Integer.toString(siteId));
		params.put("clientid", clientId);
		params.put("sourceType", "mobileapp");

		return params;
	}

	//////////////////////////////////////////////////////////////////////////
	// Facebook Login API
	//
	// Note: This is the api for working with Expedia in regard to a facebook login.
	// The calls to facebook itself are handled by the FB sdk.
	//////////////////////////////////////////////////////////////////////////

	private static final String PROVIDER_FACEBOOK = "Facebook";

	/**
	 * Login to expedia using facebook credentials. This is the first step in the Facebook login process. If
	 * the response is "success" then the user has been logged in to Expedia and an appropriate cookie has
	 * been returned; you should then refresh the users profile in your app to get the full profile data. If
	 * the response is "existing" then an Expedia account exists with the same email as the primary email from
	 * Facebook, but it has not been linked to Facebook yet; you should ask the user to enter Expedia account
	 * credentials and then call {@link #facebookLinkExistingAccount(String, String, String, String)}. If the
	 * response is "notLinked" then the user has not linked Facebook with an Expedia account and there is not
	 * an existing Expedia account for the primary Facebook email address; you should ask the user to either
	 * provide credentials for an existing Expedia account (and then call
	 * {@link #facebookLinkExistingAccount(String, String, String, String)}), or to create a new Expedia account
	 * with the primary Facebook email by calling {@link #facebookLinkNewAccount(String, String, String)}.
	 *
	 * @param facebookUserId      the user's Facebook ID as returned from the Graph API
	 * @param facebookAccessToken the access token returned by Facebook's login process
	 * @return a FacebookLinkResponse
	 */
	public Observable<FacebookLinkResponse> facebookAutoLogin(
		String facebookUserId, String facebookAccessToken) {

		return api.facebookAutoLogin(PROVIDER_FACEBOOK, facebookUserId, facebookAccessToken);
	}

	/**
	 * Create a new Expedia user and associate the newly created user with the provided Facebook account and
	 * primary email address from Facebook.
	 *
	 * @param facebookUserId       the user's Facebook ID as returned from the Graph API
	 * @param facebookAccessToken  the access token returned by Facebook's login process
	 * @param facebookEmailAddress the user's primary email address as returned from the Facebook Graph API
	 * @return a FacebookLinkResponse
	 */
	public Observable<FacebookLinkResponse> facebookLinkNewAccount(
		String facebookUserId,
		String facebookAccessToken,
		String facebookEmailAddress) {

		return api.facebookLinkNewAccount(PROVIDER_FACEBOOK,
			facebookUserId, facebookAccessToken, facebookEmailAddress);
	}

	/**
	 * Link an existing Expedia user with a facebook account.
	 *
	 * @param facebookUserId      the user's Facebook ID as returned from the Graph API
	 * @param facebookAccessToken the access token returned by Facebook's login process
	 * @param expediaEmailAddress the email address for the user's Expedia account
	 * @param expediaPassword     the password for the user's Expedia account
	 * @return a FacebookLinkResponse
	 */
	public Observable<FacebookLinkResponse> facebookLinkExistingAccount(
		String facebookUserId,
		String facebookAccessToken,
		String expediaEmailAddress,
		String expediaPassword) {

		return api.facebookLinkExistingAccount(PROVIDER_FACEBOOK, facebookUserId,
			facebookAccessToken, expediaEmailAddress, expediaPassword);
	}

	/**
	 * @param context
	 * @return
	 */
	public Observable<FacebookLinkResponse> facebookReauth(Context context) {
		FacebookSdk.sdkInitialize(context);
		AccessToken token = AccessToken.getCurrentAccessToken();
		if (token == null) {
			return Observable.empty();
		}

		String fbUserId = token.getUserId();
		return facebookAutoLogin(fbUserId, token.getToken());
	}

	/**
	 * Performs a log out on any existing Facebook session. It's safe to call this even
	 * if the user isn't signed in using Facebook.
	 */
	public static void facebookLogOut() {
		LoginManager.getInstance().logOut();
	}

}
