package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Strings;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class LXDetailSectionDataWidget extends LinearLayout {

	private static final int SHOW_MORE_CUTOFF = 120;
	private boolean isSectionExpanded;
	private String content;
	private CharSequence untruncated;

	public LXDetailSectionDataWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.section_content)
	TextView sectionContent;

	@InjectView(R.id.section_title)
	TextView sectionTitle;

	@InjectView(R.id.read_more)
	TextView readMoreView;

	@OnClick(R.id.read_more)
	public void readMore() {
		if (!isSectionExpanded) {
			sectionContent.setText(untruncated);
			readMoreView.setVisibility(View.GONE);
			isSectionExpanded = true;
		}
		else {
			sectionContent.setText(content);
			readMoreView.setVisibility(View.VISIBLE);
			isSectionExpanded = false;
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void bindData(String title, String content) {

		isSectionExpanded = false;
		this.content = Html.fromHtml(content).toString();
		// Add "read more" button if the content is too long.
		if (content.length() > SHOW_MORE_CUTOFF) {
			untruncated = Html.fromHtml(content);
			readMoreView.setVisibility(View.VISIBLE);
			content = String.format(getContext().getString(R.string.ellipsize_text_template),
				content.subSequence(0, Strings.cutAtWordBarrier(Html.fromHtml(content), SHOW_MORE_CUTOFF)));
		}
		sectionTitle.setText(title);
		sectionContent.setText(Html.fromHtml(content));

	}
}
