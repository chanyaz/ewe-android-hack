package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

public abstract class ConfirmationFragment extends Fragment {

	protected abstract int getLayoutId();

	protected abstract int getActionsLayoutId();

	protected abstract String getItinNumber();

	private final static String PKG_VSC_VOYAGES = "com.vsct.vsc.mobile.horaireetresa.android";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(getLayoutId(), container, false);

		Ui.setText(v, R.id.itinerary_text_view,
				getString(R.string.itinerary_confirmation_TEMPLATE, getItinNumber()));

		Ui.setText(v, R.id.email_text_view, Db.getBillingInfo().getEmail());

		// Inflate the custom actions layout id
		ViewGroup actionContainer = Ui.findView(v, R.id.custom_actions_container);
		inflater.inflate(getActionsLayoutId(), actionContainer, true);

		// 1617. VSC Contact URL
		// 1619. VSC Add VSC train app cross sell
		if (ExpediaBookingApp.IS_VSC) {
			TextView actionTextView = Ui.findView(v, R.id.call_action_text_view);
			actionTextView.setText(R.string.vsc_customer_support);

			View vscAppDivider = Ui.findView(v, R.id.vsc_app_divider);
			vscAppDivider.setVisibility(View.VISIBLE);

			LinearLayout vscAppCrossSellLayout = Ui.findView(v, R.id.vscAppCrossSellLayout);
			vscAppCrossSellLayout.setVisibility(View.VISIBLE);
			vscAppCrossSellLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					SocialUtils.openSite(getActivity(), AndroidUtils.getGooglePlayAppLink(PKG_VSC_VOYAGES));
				}
			});

			TextView rowTitleView = Ui.findView(v, R.id.row_title);
			rowTitleView.setText(R.string.VSC_Voyages_SNF);
			TextView descriptionView = Ui.findView(v, R.id.row_description);
			descriptionView.setText(R.string.VSC_Voyages_SNF_description);
			ImageView imageView = Ui.findView(v, R.id.image);
			imageView.setImageResource(R.drawable.ic_vsc_train_app);
		}

		Ui.setOnClickListener(v, R.id.call_action_text_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 1617. VSC Contact URL
				if (ExpediaBookingApp.IS_VSC) {
					WebViewActivity.IntentBuilder webBuilder = new WebViewActivity.IntentBuilder(getActivity());
					webBuilder.setUrl("http://voyages-sncf.mobi/aide-appli-2/aide-appli-hotel/pagecontactandroid.html");
					webBuilder.setTheme(R.style.Theme_Phone);
					webBuilder.setTitle(R.string.vsc_customer_support);
					getActivity().startActivity(webBuilder.getIntent());
				}
				else {
					SocialUtils.call(getActivity(), PointOfSale.getPointOfSale().getSupportPhoneNumber());
				}

			}
		});

		return v;
	}
}
