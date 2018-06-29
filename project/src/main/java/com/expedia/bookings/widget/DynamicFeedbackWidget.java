package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.R2;

import com.expedia.bookings.R2;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.BindView;

public class DynamicFeedbackWidget extends CardView {

	public DynamicFeedbackWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.dynamic_feedback_widget, this);
	}

	@BindView(R2.id.dynamic_feedback_counter)
	android.widget.TextView dynamicFeedbackCounter;

	@BindView(R2.id.dynamic_feedback_clear_button)
	android.widget.TextView dynamicFeedbackClearButton;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.bind(this);
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
		announceForAccessibility(announcementString + " " + getContext().getString(R.string.search_filter_clear_button_alert_cont_desc));
	}

	public void setDynamicCounterText(int count, Money minPrice) {
		CharSequence text;
		if (count == 1) {
			text = Phrase.from(getContext(), R.string.count_with_price__for_one_result_TEMPLATE)
				.put("price", Money
					.getFormattedMoneyFromAmountAndCurrencyCode(minPrice.roundedAmount, minPrice.currencyCode,
						Money.F_NO_DECIMAL).toString())
				.format();
		}
		else {
			text = Phrase
				.from(getContext(), R.string.count_with_price_results_TEMPLATE)
				.put("number", count).put("price", Money
					.getFormattedMoneyFromAmountAndCurrencyCode(minPrice.roundedAmount, minPrice.currencyCode,
						Money.F_NO_DECIMAL).toString())
				.format();
		}
		dynamicFeedbackCounter.setText(text);
	}
}
