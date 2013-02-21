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

	public static TabletPrivacyPolicyDialogFragment newInstance(String termsUrl, String privacyUrl) {
		TabletPrivacyPolicyDialogFragment fragment = new TabletPrivacyPolicyDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TERMS_URL, termsUrl);
		args.putString(ARG_PRIVACY_URL, privacyUrl);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();

		final int numUrls = 2;
		final String[] itemNames = new String[numUrls];
		final String[] itemUrls = new String[numUrls];

		itemNames[0] = getString(R.string.terms_and_conditions);
		itemUrls[0] = args.getString(ARG_TERMS_URL);

		itemNames[1] = getString(R.string.privacy_policy);
		itemUrls[1] = args.getString(ARG_PRIVACY_URL);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.legal_information));
		builder.setItems(itemNames, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(itemUrls[which]);
				builder.setTheme(R.style.Theme_Tablet);
				builder.setTitle(itemNames[which]);
				builder.setDisableSignIn(true);
				builder.setInjectExpediaCookies(true);

				getActivity().startActivity(builder.getIntent());
			}
		});
		return builder.create();
	}

}
