package com.expedia.bookings.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.TextView;

public class CheckoutSummaryWidgetUtils {

	public static View addRow(Context context, String text) {
		View row = LayoutInflater.from(context).inflate(R.layout.cost_summary_row, null);
		TextView priceDescription = Ui.findView(row, R.id.price_type_text_view);
		TextView priceValue = Ui.findView(row, R.id.price_text_view);
		priceValue.setVisibility(View.GONE);
		priceDescription.setText(text);
		return row;
	}

	public static View addRow(Context context, String leftSideText, String rightSideText) {
		View row = LayoutInflater.from(context).inflate(R.layout.cost_summary_row, null);
		TextView priceDescription = Ui.findView(row, R.id.price_type_text_view);
		TextView priceValue = Ui.findView(row, R.id.price_text_view);
		priceDescription.setText(leftSideText);
		priceValue.setText(rightSideText);
		return row;
	}
}
