package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.content.Context;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.OffersDetail;
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

	public void bind(OffersDetail offersDetail, LocalDate offerDate) {
		this.offerDate = offerDate;
		TextAppearanceSpan daySpan = new TextAppearanceSpan(getContext(), R.style.LXOfferDayTextView);
		TextAppearanceSpan dateSpan = new TextAppearanceSpan(getContext(), R.style.LXOfferDateTextView);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(offerDate.dayOfWeek().getAsShortText(), daySpan);
		sb.append("\n");
		sb.append(offerDate.dayOfMonth().getAsText(), dateSpan);
		setText(sb.build());
		if (!offersDetail.isAvailableOnDate(offerDate)) {
			setEnabled(false);
			setBackgroundColor(getResources().getColor(R.color.lx_date_disabled_background_color));
		}
	}

	@Override
	public void onClick(View v) {
		Events.post(new Events.LXDetailsDateChanged(offerDate));
	}
}
