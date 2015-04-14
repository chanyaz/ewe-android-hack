package com.expedia.bookings.widget;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LXFormatter;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.CheckoutSummaryWidgetUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXCheckoutSummaryWidget extends LinearLayout {

	public LXCheckoutSummaryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.lx_offer_title_text)
	TextView lxOfferTitleText;

	@InjectView(R.id.lx_group_text)
	TextView lxGroupText;

	@InjectView(R.id.lx_offer_date)
	TextView lxOfferDate;

	@InjectView(R.id.lx_offer_location)
	TextView lxOfferLocation;

	@InjectView(R.id.free_cancellation_text)
	TextView freeCancellationText;

	@InjectView(R.id.price_text)
	TextView tripTotalText;

	@Inject
	LXState lxState;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);
	}

	public void bind() {
		lxOfferTitleText.setText(lxState.offer.title);
		lxGroupText.setText(LXFormatter.selectedTicketsSummaryText(getContext(), lxState.selectedTickets));
		lxOfferDate.setText(lxState.offer.availabilityInfoOfSelectedDate.availabilities.displayDate);
		lxOfferLocation.setText(lxState.activity.location);

		String totalMoney = LXUtils.getTotalAmount(lxState.selectedTickets).getFormattedMoney();
		tripTotalText.setText(totalMoney);

		freeCancellationText.setVisibility(lxState.offer.freeCancellation ? VISIBLE : GONE);
	}

	@OnClick(R.id.price_text)
	public void showCostBreakdown() {
		buildCostBreakdownDialog(getContext(), lxState.selectedTickets);
	}

	private void buildCostBreakdownDialog(Context context, List<Ticket> tickets) {
		View view = LayoutInflater.from(context).inflate(R.layout.cost_summary_alert, null);
		LinearLayout ll = Ui.findView(view, R.id.parent);

		ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
			context.getString(R.string.lx_cost_breakdown_due_today),
			LXUtils.getTotalAmount(lxState.selectedTickets).getFormattedMoney()));

		for (Ticket ticketSelected : tickets) {
			Money totalMoneyForTicketType = new Money(ticketSelected.money);
			totalMoneyForTicketType.setAmount(totalMoneyForTicketType.getAmount().multiply(BigDecimal.valueOf(ticketSelected.count)));

			ll.addView(
				CheckoutSummaryWidgetUtils.addRow(context,
					LXDataUtils.ticketCountSummary(getContext(), ticketSelected.code, ticketSelected.count),
					totalMoneyForTicketType.getFormattedMoney()));
		}

		ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
			context.getString(R.string.lx_cost_breakdown_taxes_included)));
		ll.addView(addDisclaimerRow(context, tickets.get(0).money.getCurrency()));
		ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
			context.getString(R.string.checkout_breakdown_total_price),
			LXUtils.getTotalAmount(lxState.selectedTickets).getFormattedMoney()));

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(view);
		builder.setPositiveButton(context.getString(R.string.lx_cost_breakdown_button_text),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		builder.create().show();
	}

	private View addDisclaimerRow(Context context, String currency) {
		View row = LayoutInflater.from(context).inflate(R.layout.checkout_breakdown_price_disclaimer, null);
		TextView disclaimer = Ui.findView(row, R.id.price_disclaimer);
		disclaimer.setText(context.getResources()
			.getString(R.string.lx_checkout_breakdown_price_disclaimer_text, currency));
		return row;
	}
}
