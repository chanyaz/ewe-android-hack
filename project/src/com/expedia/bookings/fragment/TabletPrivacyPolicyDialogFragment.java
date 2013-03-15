package com.expedia.bookings.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;

public class TabletPrivacyPolicyDialogFragment extends DialogFragment {

	private static final String ARG_TERMS_URL = "ARG_TERMS_URL";
	private static final String ARG_PRIVACY_URL = "ARG_PRIVACY_URL";
	private static final String ARG_BEST_PRICE_GUARANTEE_URL = "ARG_BEST_PRICE_GUARANTEE_URL";

	// TODO feels weird sending urls from Db as args to a fragment, but maybe the DialogFragment can be
	// refactored to be more generalized, so I keep it like this for now.

	public static TabletPrivacyPolicyDialogFragment newInstance(String termsUrl, String privacyUrl, String bestPriceUrl) {
		TabletPrivacyPolicyDialogFragment fragment = new TabletPrivacyPolicyDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TERMS_URL, termsUrl);
		args.putString(ARG_PRIVACY_URL, privacyUrl);
		args.putString(ARG_BEST_PRICE_GUARANTEE_URL, bestPriceUrl);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Tablet);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();

		final int numUrls = 3;
		final String[] itemNames = new String[numUrls];
		final String[] itemUrls = new String[numUrls];

		itemNames[0] = getString(R.string.terms_and_conditions);
		itemUrls[0] = args.getString(ARG_TERMS_URL);

		itemNames[1] = getString(R.string.privacy_policy);
		itemUrls[1] = args.getString(ARG_PRIVACY_URL);

		itemNames[2] = getString(R.string.best_price_guarantee);
		itemUrls[2] = args.getString(ARG_BEST_PRICE_GUARANTEE_URL);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.legal_information));
		builder.setItems(itemNames, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(itemUrls[which]);
				builder.setTheme(R.style.Theme_Tablet);
				builder.setTitle(itemNames[which]);
				builder.setInjectExpediaCookies(true);

				getActivity().startActivity(builder.getIntent());
			}
		});
		return builder.create();
	}
}
