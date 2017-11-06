package com.expedia.account.recaptcha;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import timber.log.Timber;

/**
 * Created by Jeff on 11/3/2017.
 */

public class Recaptcha {

	static public void recaptchaCheck(Activity context, String recaptchaAPIKey, final RecaptchaHandler handler) {
		try {
			SafetyNet.getClient(context).verifyWithRecaptcha(recaptchaAPIKey)
				.addOnSuccessListener(context,
					new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
						@Override
						public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
							String userResponseToken = response.getTokenResult();
							handler.onSuccess(userResponseToken);
						}
					})
				.addOnFailureListener(context, new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						if (e instanceof ApiException) {
							// An error occurred when communicating with the reCAPTCHA service.
							ApiException apiException = (ApiException) e;
							int statusCode = apiException.getStatusCode();
							Timber.e(e, "RECAPTCHA: API EXCEPTION: " + statusCode);
						}
						else {
							Timber.e(e, "RECAPTCHA: FAILURE");
						}
						handler.onFailure();
					}
				});
		}
		catch (Exception e) {
			Timber.e(e, "RECAPTCHA: SDK EXCEPTION");
			handler.onFailure();
		}
	}
}
