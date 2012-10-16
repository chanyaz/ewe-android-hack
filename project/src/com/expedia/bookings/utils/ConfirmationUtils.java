package com.expedia.bookings.utils;

import android.content.Context;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Policy;
import com.expedia.bookings.data.Rate;
import com.mobiata.android.util.AndroidUtils;

public class ConfirmationUtils {

	//////////////////////////////////////////////////////////////////////////////////////////
	// Miscellaneous

	public static void determineCancellationPolicy(Rate rate, View view) {
		Policy cancellationPolicy = rate.getRateRules().getPolicy(Policy.TYPE_CANCEL);

		TextView cancellationPolicyView = (TextView) view.findViewById(R.id.cancellation_policy_text_view);
		if (cancellationPolicyView != null) {
			if (cancellationPolicy != null) {
				cancellationPolicyView.setText(Html.fromHtml(cancellationPolicy.getDescription()));
			}
			else {
				cancellationPolicyView.setVisibility(View.GONE);
			}
		}

		TextView rulesRestrictionsTitle = (TextView) view.findViewById(R.id.rules_and_restrictions_title_text_view);
		if (rulesRestrictionsTitle != null) {
			rulesRestrictionsTitle.setVisibility((cancellationPolicy != null) ? View.VISIBLE : View.GONE);
		}
	}

	public static String determineContactText(Context context) {
		return context.getString(R.string.contact_phone_template, SupportUtils.getInfoSupportNumber(context));
	}

	public static void configureContactView(Context context, TextView contactView, String contactText) {
		if (!AndroidUtils.hasTelephonyFeature(context)) {
			contactView.setAutoLinkMask(0);
		}
		else {
			contactView.setAutoLinkMask(Linkify.PHONE_NUMBERS);
		}

		contactView.setText(contactText);
	}
}
