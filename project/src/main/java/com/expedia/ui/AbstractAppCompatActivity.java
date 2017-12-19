package com.expedia.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.data.Db;
import com.expedia.util.LanguageHelper;
import com.mobiata.android.Log;

public abstract class AbstractAppCompatActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) {
			LanguageHelper.initLangSetup(this);
		}
	}

	protected void clearStoredCard() {
		Db.getBillingInfo().setStoredCard(null);
		Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(null);
	}

	protected void clearCCNumber() {
		try {
			Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setNumber(null);
			Db.getBillingInfo().setNumber(null);
		}
		catch (Exception ex) {
			Log.e("Error clearing billingInfo card number", ex);
		}
	}
}
