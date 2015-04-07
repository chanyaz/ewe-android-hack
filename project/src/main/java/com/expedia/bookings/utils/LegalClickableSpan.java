package com.expedia.bookings.utils;

import android.content.Context;
import android.text.style.ClickableSpan;
import android.view.View;

import com.expedia.bookings.activity.WebViewActivity;

public class LegalClickableSpan extends ClickableSpan {

	private Context context;
	private String url;
	private String title;

	public LegalClickableSpan(Context c, String url, String title) {
		this.context = c;
		this.url = url;
		this.title = title;
	}

	@Override
	public void onClick(View widget) {
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
		builder.setUrl(url);
		builder.setTitle(title);
		builder.setAllowMobileRedirects(true);
		builder.setAttemptForceMobileSite(true);
		builder.setLoginEnabled(true);
		builder.setInjectExpediaCookies(true);
		context.startActivity(builder.getIntent());
	}

	public String getTitle() {
		return title;
	}

}
