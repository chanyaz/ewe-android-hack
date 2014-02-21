package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.Ui;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletPrivacyPolicyDialogFragment extends DialogFragment {

	public static TabletPrivacyPolicyDialogFragment newInstance() {
		return new TabletPrivacyPolicyDialogFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		// http://stackoverflow.com/questions/8045556/cant-make-the-custom-dialogfragment-transparent-over-the-fragment
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final List<String> itemNames = new ArrayList<String>();
		final List<String> itemUrls = new ArrayList<String>();

		itemNames.add(getString(R.string.terms_and_conditions));
		itemUrls.add(PointOfSale.getPointOfSale().getTermsAndConditionsUrl());

		itemNames.add(getString(R.string.privacy_policy));
		itemUrls.add(PointOfSale.getPointOfSale().getPrivacyPolicyUrl());

		if (PointOfSale.getPointOfSale().displayBestPriceGuarantee()) {
			itemNames.add(getString(Ui.obtainThemeResID(getActivity(), R.attr.bestPriceGuaranteeString)));
			itemUrls.add(PointOfSale.getPointOfSale().getBestPriceGuaranteeUrl());
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_Light_Dialog_NoAb);
		builder.setTitle(getString(R.string.legal_information));
		builder.setItems(itemNames.toArray(new String[itemNames.size()]), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(itemUrls.get(which));
				builder.setTheme(R.style.Theme_Tablet);
				builder.setTitle(itemNames.get(which));
				builder.setInjectExpediaCookies(true);

				getActivity().startActivity(builder.getIntent());
			}
		});
		return builder.create();
	}
}
