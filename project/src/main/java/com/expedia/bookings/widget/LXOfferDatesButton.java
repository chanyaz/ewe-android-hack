package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.content.Context;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.SpannableBuilder;

public class LXOfferDatesButton extends RadioButton implements OnClickListener, CompoundButton.OnCheckedChangeListener {

	private LocalDate offerDate;

	public LXOfferDatesButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setOnClickListener(this);
		this.setOnCheckedChangeListener(this);
	}

	public void bind(LocalDate offerDate, boolean isOfferAvailableOnDate) {
		this.offerDate = offerDate;

		updateText(false);

		if (!isOfferAvailableOnDate) {
			setEnabled(false);
			setBackgroundColor(getResources().getColor(R.color.lx_date_disabled_background_color));
			setTextColor(getResources().getColor(R.color.lx_dates_disabled_text_color));
		}
	}

	private void updateText(boolean isChecked) {
		TextAppearanceSpan daySpan = new TextAppearanceSpan(getContext(), R.style.LXOfferDayTextView);
		TextAppearanceSpan dateSpan = new TextAppearanceSpan(getContext(), R.style.LXOfferDateTextView);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(offerDate.dayOfWeek().getAsShortText(), daySpan);
		sb.append("\n");
		sb.append(offerDate.dayOfMonth().getAsText(), dateSpan);
		sb.append("\n");
		sb.append(isChecked ? offerDate.monthOfYear().getAsShortText() : "", daySpan);
		setText(sb.build());
	}

	@Override
	public void onClick(View v) {
		Events.post(new Events.LXDetailsDateChanged(offerDate));
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
		updateText(b);
	}
}
