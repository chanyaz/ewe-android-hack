package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LXDetailSectionDataWidget extends LinearLayout {

	public LXDetailSectionDataWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.section_content)
	TextView sectionContent;

	@InjectView(R.id.section_title)
	TextView sectionTitle;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void bindData(String title, String content) {
		sectionTitle.setText(title);
		sectionContent.setText(Html.fromHtml(content));
	}
}
