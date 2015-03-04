package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.SpannableBuilder;


public class LXOfferDatesButton extends RadioButton implements OnClickListener {

	private LocalDate offerDate;

	public LXOfferDatesButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setOnClickListener(this);
	}

	public void bind(LocalDate offerDate) {
		this.offerDate = offerDate;
		SpannableBuilder sb = new SpannableBuilder();
		sb.append(offerDate.dayOfWeek().getAsShortText(), new StyleSpan(Typeface.BOLD));
		sb.append("\n");
		sb.append(offerDate.dayOfMonth().getAsText());
		setText(sb.build());
	}

	@Override
	public void onClick(View v) {
		Events.post(new Events.LXDetailsDateChanged(offerDate));
	}
}
