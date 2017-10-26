package com.expedia.account.server;

import java.util.Map;

import com.expedia.account.data.AccountResponse;
import com.expedia.account.data.FacebookLinkResponse;

import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface ExpediaAccountApi {

	@FormUrlEncoded
	@POST("/api/user/sign-in")
	Observable<AccountResponse> signIn(
		@Field("email") String email,
		@Field("password") String password,
		@Field("staySignedIn") boolean staySignedIn,
		@FieldMap Map<String, String> extraParams
	);

	@FormUrlEncoded
	@POST("/api/user/sign-in")
	Observable<AccountResponse> signInProfileOnly(
		@Field("profileOnly") boolean profileOnly,
		@FieldMap Map<String, String> extraParams
	);

	@FormUrlEncoded
	@POST("/api/user/create")
	Observable<AccountResponse> createUser(
		@Field("email") String email,
		@Field("password") String password,
		@Field("firstName") String firstName,
		@Field("lastName") String lastName,
		@Field("expediaEmailOptin") boolean expediaEmailOptin,
		@Field("staySignedIn") boolean staySignedIn,
		@Field("enrollInLoyalty") boolean enrollInLoyalty,
		@FieldMap Map<String, String> extraParams
	);

	///////////////////////////////////////////////////////////////////////////
	// Facebook
	///////////////////////////////////////////////////////////////////////////

	@FormUrlEncoded
	@POST("/api/auth/autologin")
	Observable<FacebookLinkResponse> facebookAutoLogin(
		@Field("provider") String provider,
		@Field("userId") String userId,
		@Field("accessToken") String accessToken
	);

	@FormUrlEncoded
	@POST("/api/auth/linkNewAccount")
	Observable<FacebookLinkResponse> facebookLinkNewAccount(
		@Field("provider") String provider,
		@Field("userId") String facebookUserId,
		@Field("accessToken") String facebookAccessToken,
		@Field("fbemail") String facebookEmailAddress
	);

	@FormUrlEncoded
	@POST("/api/auth/linkExistingAccount")
	Observable<FacebookLinkResponse> facebookLinkExistingAccount(
		@Field("provider") String provider,
		@Field("userId") String facebookUserId,
		@Field("accessToken") String facebookAccessToken,
		@Field("email") String expediaEmailAddress,
		@Field("password") String expediaPassword
	);
}
