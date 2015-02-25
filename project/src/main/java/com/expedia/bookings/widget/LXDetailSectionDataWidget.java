package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.SpannableBuilder;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LXDetailSectionDataWidget extends TextView {

	public LXDetailSectionDataWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.section_content)
	TextView sectionContent;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void bindData(String title, String content) {
		SpannableBuilder sb = new SpannableBuilder();
		sb.append(title, new StyleSpan(Typeface.BOLD));
		sb.append(Html.fromHtml(content));
		sectionContent.setText(sb.build());
	}
}
