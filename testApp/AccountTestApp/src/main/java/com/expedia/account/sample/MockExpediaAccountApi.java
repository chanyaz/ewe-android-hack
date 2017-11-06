package com.expedia.account.sample;

import java.util.ArrayList;
import java.util.Map;

import com.expedia.account.data.AccountResponse;
import com.expedia.account.data.FacebookLinkResponse;
import com.expedia.account.server.ExpediaAccountApi;
import com.expedia.account.util.MockFacebookViewHelper;

import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import rx.Observable;
import timber.log.Timber;

public class MockExpediaAccountApi implements ExpediaAccountApi {
	public static final String NEW_USER = "newuser@test.com";
	public static final String SUCCESS = "success@test.com";
	public static final String BAD_EMAIL = "bademail@test.com";
	public static final String BAD_FIRST_NAME = "badfname@test.com";
	public static final String BAD_LAST_NAME = "badlname@test.com";
	public static final String BAD_PASSWORD = "badpass@test.com";
	public static final String DUPLICATE_EMAIL = "dupemail@test.com";

	public MockExpediaAccountApi() {
	}

	@Override
	public Observable<AccountResponse> createUser(String email, String password, String firstName, String lastName,
		boolean expediaEmailOptin, boolean staySignedIn, boolean enrollInLoyalty, String recaptchaResponseToken, Map<String, String> extraParams) {

		AccountResponse response = new AccountResponse();

		switch (email) {
		case NEW_USER:
			response.success = true;
			break;
		case SUCCESS:
			response.success = true;
			break;
		case BAD_EMAIL:
			response.success = false;
			response.errors = createError(AccountResponse.ErrorCode.INVALID_INPUT, AccountResponse.ErrorField.email);
			break;
		case BAD_FIRST_NAME:
			response.success = false;
			response.errors = createError(AccountResponse.ErrorCode.INVALID_INPUT, AccountResponse.ErrorField.firstName);
			break;
		case BAD_LAST_NAME:
			response.success = false;
			response.errors = createError(AccountResponse.ErrorCode.INVALID_INPUT, AccountResponse.ErrorField.lastName);
			break;
		case BAD_PASSWORD:
			response.success = false;
			response.errors = createError(AccountResponse.ErrorCode.INVALID_INPUT, AccountResponse.ErrorField.password);
			break;
		case DUPLICATE_EMAIL:
			response.success = false;
			response.errors = createError(AccountResponse.ErrorCode.USER_SERVICE_DUPLICATE_EMAIL,
				AccountResponse.ErrorField.email);
			break;
		default:
			response.success = false;
			response.errors = createError(AccountResponse.ErrorCode.INVALID_INPUT, AccountResponse.ErrorField.email);
			break;
		}
		return Observable.just(response);
	}

	@Override
	public Observable<AccountResponse> signIn(String email, String password, boolean staySignedIn,
		String recaptchaResponseToken, Map<String, String> extraParams) {

		AccountResponse response = new AccountResponse();

		switch (email) {
		case SUCCESS:
			response.success = true;
			break;
		case BAD_PASSWORD:
			response.success = false;
			response.errors = createError(AccountResponse.ErrorCode.INVALID_INPUT, AccountResponse.ErrorField.password);
			break;
		}
		return Observable.just(response);
	}

	@Override
	public Observable<AccountResponse> signInProfileOnly(@Field("profileOnly") boolean profileOnly,
		@FieldMap Map<String, String> extraParams) {
		AccountResponse response = new AccountResponse();
		response.success = true;
		return Observable.just(response);
	}

	@Override
	public Observable<FacebookLinkResponse> facebookAutoLogin(String provider, String userId, String accessToken) {
		Timber.e("FACEBOOK: facebookAutoLogin: " + provider + ", " + userId + ", " + accessToken);
		FacebookLinkResponse response = new FacebookLinkResponse();
		switch (userId) {
		case MockFacebookViewHelper.NOT_LINKED_ADDRESS:
			response.status = FacebookLinkResponse.FacebookLinkResponseCode.notLinked;
			break;
		case MockFacebookViewHelper.EXISTING_ADDRESS:
			response.status = FacebookLinkResponse.FacebookLinkResponseCode.existing;
			break;
		case MockFacebookViewHelper.SUCCESS_ADDRESS:
			response.status = FacebookLinkResponse.FacebookLinkResponseCode.success;
			break;
		}
		return Observable.just(response);
	}

	@Override
	public Observable<FacebookLinkResponse> facebookLinkNewAccount(String provider,
		String facebookUserId, String facebookAccessToken, String facebookEmailAddress) {

		FacebookLinkResponse response = new FacebookLinkResponse();
		response.status = FacebookLinkResponse.FacebookLinkResponseCode.success;
		return Observable.just(response);
	}

	@Override
	public Observable<FacebookLinkResponse> facebookLinkExistingAccount(String provider, String facebookUserId,
		String facebookAccessToken, String expediaEmailAddress, String expediaPassword) {

		FacebookLinkResponse response = new FacebookLinkResponse();

		switch (expediaEmailAddress) {
		case MockFacebookViewHelper.EXISTING_ADDRESS:
		case SUCCESS:
			response.status = FacebookLinkResponse.FacebookLinkResponseCode.success;
			break;
		case BAD_EMAIL:
			response.status = FacebookLinkResponse.FacebookLinkResponseCode.loginFailed;
			//TODO: some sort of indication of what the error is?
			break;
		default:
			response.status = FacebookLinkResponse.FacebookLinkResponseCode.loginFailed;
		}

		return Observable.just(response);
	}

	private ArrayList<AccountResponse.AccountError> createError(AccountResponse.ErrorCode errorCode,
		AccountResponse.ErrorField errorField) {

		AccountResponse.AccountError error = new AccountResponse.AccountError();
		error.errorCode = errorCode;
		error.errorInfo = new AccountResponse.ErrorInfo();
		error.errorInfo.summary = "summary of " + errorField.name() + " error";
		error.errorInfo.field = errorField;
		error.errorInfo.cause = "because we mocked it";
		error.diagnosticId = "1234";
		error.getDiagnosticFullText = "5678";
		error.activityId = "9012";

		ArrayList<AccountResponse.AccountError> errorList = new ArrayList<>();
		errorList.add(error);

		return errorList;
	}
}
