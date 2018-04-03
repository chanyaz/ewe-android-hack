package com.expedia.account.recaptcha;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.mobiata.android.Log;

/**
 * Created by Jeff on 11/3/2017.
 */

public class Recaptcha {

	private static Exception lastRecaptchaException = null;

	public static void recaptchaCheck(Activity context, String recaptchaAPIKey, final RecaptchaHandler handler) {
		try {
			lastRecaptchaException = null;
			SafetyNet.getClient(context).verifyWithRecaptcha(recaptchaAPIKey)
				.addOnSuccessListener(context,
					new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
						@Override
						public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
							String userResponseToken = response.getTokenResult();
							handler.onRecaptchaSuccess(userResponseToken);
							Log.i("RECAPTCHA", "Successful Token Acquired: " + userResponseToken);
						}
					})
				.addOnFailureListener(context, new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						if (e instanceof ApiException) {
							// An error occurred when communicating with the reCAPTCHA service.
							ApiException apiException = (ApiException) e;
							int statusCode = apiException.getStatusCode();
							Log.e("RECAPTCHA: API EXCEPTION: " + statusCode, e);
						}
						else {
							Log.e("RECAPTCHA: FAILURE", e);
						}
						lastRecaptchaException = e;
						handler.onRecaptchaFailure();
					}
				});
		}
		catch (Exception e) {
			Log.e("RECAPTCHA: SDK EXCEPTION", e);
			lastRecaptchaException = e;
			handler.onRecaptchaFailure();
		}
	}

	public static Exception getLastRecaptchaException() {
		return lastRecaptchaException;
	}
}
