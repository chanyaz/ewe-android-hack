package com.expedia.bookings.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.AnimUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LXDetailSectionDataWidget extends LinearLayout implements View.OnClickListener {

	private static final int ANIMATION_DURATION = 100;

	public LXDetailSectionDataWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//@InjectView(R.id.section_content)
	TextView sectionContent;

	//@InjectView(R.id.section_title)
	TextView sectionTitle;

	//@InjectView(R.id.read_more)
	ImageButton readMoreView;

	private int maxLineCount;
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		setOnClickListener(this);
	}

	public void bindData(String title, CharSequence content, int maxLines) {
		this.maxLineCount = maxLines;
		readMoreView.clearAnimation();
		sectionTitle.setText(title);
		sectionContent.setText(content);
		setClickable(false);
		if (maxLines > 0) {
			sectionContent.setMaxLines(maxLines);
			sectionContent.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						sectionContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						Layout textLayout = sectionContent.getLayout();
						if (textLayout != null) {
							int lines = textLayout.getLineCount();
							boolean isReadMoreVisible = lines > maxLineCount ? true : false;
							readMoreView.setVisibility(isReadMoreVisible ? View.VISIBLE : View.GONE);
							setClickable(isReadMoreVisible);
						}
					}
				});
		}
	}

	@Override
	public void onClick(View v) {
		if (readMoreView.getVisibility() == View.VISIBLE) {
			int totalLineCount = sectionContent.getLineCount();
			int displayedLineCount = sectionContent.getMaxLines();
			if (displayedLineCount < totalLineCount) {
				AnimUtils.rotate(readMoreView);
				ObjectAnimator animation = ObjectAnimator.ofInt(sectionContent, "maxLines", totalLineCount);
				animation.setDuration(ANIMATION_DURATION).start();
				readMoreView.setContentDescription(getResources().getString(R.string.lx_read_less_cont_desc));
			}
			else {
				AnimUtils.reverseRotate(readMoreView);
				ObjectAnimator animation = ObjectAnimator.ofInt(sectionContent, "maxLines", maxLineCount);
				animation.setDuration(ANIMATION_DURATION).start();
				readMoreView.setContentDescription(getResources().getString(R.string.lx_read_more_cont_desc));
			}
		}
	}
}
