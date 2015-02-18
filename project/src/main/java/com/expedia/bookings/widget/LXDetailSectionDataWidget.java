package com.expedia.bookings.widget;

import android.content.Context;
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

	@InjectView(R.id.title)
	TextView title;

	@InjectView(R.id.content)
	TextView content;

	@InjectView(R.id.read_more)
	TextView readMore;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void bindData(String titleText, String sectionContent) {
		title.setText(titleText);
		content.setText(sectionContent);
	}
}
