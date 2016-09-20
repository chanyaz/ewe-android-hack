package com.expedia.bookings.utils;

import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import com.expedia.bookings.activity.WebViewActivity;

public class LegalClickableSpan extends URLSpan {

	private String url;
	private String title;
	private boolean hasUnderline;

	public LegalClickableSpan(String url, String title, boolean hasUnderline) {
		super(url);
		this.url = url;
		this.title = title;
		this.hasUnderline = hasUnderline;
	}

	@Override
	public void onClick(View widget) {
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(widget.getContext());
		builder.setUrl(url);
		builder.setTitle(title);
		builder.setAllowMobileRedirects(true);
		builder.setAttemptForceMobileSite(true);
		builder.setLoginEnabled(true);
		builder.setInjectExpediaCookies(true);
		widget.getContext().startActivity(builder.getIntent());
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		ds.setUnderlineText(hasUnderline);
	}

	public String getTitle() {
		return title;
	}
	public String getUrl() {
		return url;
	}

}
