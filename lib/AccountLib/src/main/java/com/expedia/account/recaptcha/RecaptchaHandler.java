package com.expedia.account.recaptcha;

/**
 * Created by Jeff on 11/3/2017.
 */

public interface RecaptchaHandler {
	void onSuccess(String recaptchaResponseToken);
	void onFailure();
}

