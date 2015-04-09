package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class LXDetailSectionDataWidget extends LinearLayout {

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
		sectionContent.setMaxLines(sectionContent.getLineCount());
		readMoreView.setVisibility(View.GONE);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void bindData(String title, CharSequence content) {
		sectionContent.setMaxLines(3);
		sectionTitle.setText(title);
		sectionContent.setText(content);
		sectionContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Ui.removeOnGlobalLayoutListener(sectionContent, this);
				Layout textLayout = sectionContent.getLayout();
				if (textLayout != null) {
					int lines = textLayout.getLineCount();
					readMoreView.setVisibility(lines > 3 ? View.VISIBLE : View.GONE);
				}
			}
		});

	}
}
