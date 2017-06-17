package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class DynamicFeedbackWidget extends CardView {

	public DynamicFeedbackWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.dynamic_feedback_widget, this);
	}

	//@InjectView(R.id.dynamic_feedback_counter)
	android.widget.TextView dynamicFeedbackCounter;

	//@InjectView(R.id.dynamic_feedback_clear_button)
	android.widget.TextView dynamicFeedbackClearButton;

	//@OnClick(R.id.dynamic_feedback_clear_button)
	public void onClearFiltersClick() {
		Events.post(new Events.DynamicFeedbackClearButtonClicked());
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void animateDynamicFeedbackWidget() {
		AnimUtils.doTheHarlemShake(this);
	}

	public void hideDynamicFeedback() {
		setTranslationY(0.0f);
		animate().alpha(0.0f);
		setVisibility(GONE);
	}

	public void showDynamicFeedback() {
		setVisibility(VISIBLE);
		setTranslationY(0.0f);
		animate().alpha(1.0f);
	}

	public void setDynamicCounterText(int count) {
		CharSequence text = Phrase
			.from(getContext().getResources().getQuantityString(R.plurals.number_results_template, count))
			.put("number", count)
			.format();

		if (count == 0) {
			SpannableBuilder sb = new SpannableBuilder();
			sb.append(text, new ForegroundColorSpan(0xFFE6492C));
			text = sb.build();
		}

		dynamicFeedbackCounter.setText(text);

		String announcementString = Phrase
			.from(getContext().getResources().getQuantityString(R.plurals.number_results_announcement_text_TEMPLATE, count))
				.put("number", count)
				.format().toString();
		announceForAccessibility(announcementString);
	}
}
