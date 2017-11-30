package com.expedia.bookings.widget;

import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.PriceBreakdownItemType;
import com.expedia.bookings.data.lx.LXBookableItem;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.CheckoutSummaryWidgetUtils;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXCheckoutSummaryWidget extends LinearLayout {

	private LXBookableItem lxBookableItem;

	public LXCheckoutSummaryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.lx_activity_title_text)
	TextView lxActivityTitleText;

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

	@InjectView(R.id.price_change_container)
	ViewGroup priceChangeContainer;

	@InjectView(R.id.price_change_text)
	TextView priceChangeText;

	@Inject
	LXState lxState;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Ui.getApplication(getContext()).lxComponent().inject(this);
	}

	/**
	 * Detecting whether we are in a Price Change Flow
	 * Required to show hint message in "Cost Breakdown Dialog" that "Breakdown is not updated".
	 */
	boolean isInPriceChangeFlow = false;

	/**
	 *
	 * @param originalPrice - In case there was a Price Change [during CreateTrip/Checkout], this is non-null
	 *                        and contains the original price. Otherwise it is null.
	 * @param latestPrice - Always non-null. Contains the up-to-date price of the selected offer(s) to be displayed to the user
	 *                 and deducted during Payment.
	 */
	public void bind(Money originalPrice, Money latestPrice, LXBookableItem lxBookableItem) {
		this.lxBookableItem = lxBookableItem;
		lxActivityTitleText.setText(lxState.activity.title);
		lxOfferTitleText.setText(lxState.offer.title);
		lxGroupText.setText(lxState.selectedTicketsCountSummary(getContext()));
		LocalDate offerSelectedDate = DateUtils.yyyyMMddHHmmssToLocalDate(
			lxState.offer.availabilityInfoOfSelectedDate.availabilities.valueDate);
		lxOfferDate.setText(LocaleBasedDateFormatUtils.localDateToEEEMMMd(offerSelectedDate));
		lxOfferLocation.setText(lxState.activity.location);

		freeCancellationText.setVisibility(lxState.offer.freeCancellation ? VISIBLE : GONE);

		String tripTotal = Money.getFormattedMoneyFromAmountAndCurrencyCode(latestPrice.getAmount(), latestPrice.getCurrency());
		tripTotalText.setText(tripTotal);
		tripTotalText.setContentDescription(Phrase.from(getContext(), R.string.lx_selection_cost_summary_cont_desc_TEMPLATE)
				.put("trip_total", tripTotal).format().toString());

		// Price change
		isInPriceChangeFlow = (originalPrice != null);
		if (isInPriceChangeFlow) {
			priceChangeContainer.setVisibility(View.VISIBLE);
			priceChangeText.setText(getResources().getString(R.string.price_changed_from_TEMPLATE,
				originalPrice.getFormattedMoney()));
		}
		else {
			priceChangeContainer.setVisibility(View.GONE);
		}
	}

	@OnClick(R.id.free_cancellation_text)
	public void showLxRules() {
		Events.post(new Events.LXShowRulesOnCheckout());
	}

	@OnClick(R.id.price_text)
	public void showCostBreakdown() {
		buildCostBreakdownDialog(getContext(), lxState.selectedTickets());
	}

	private void buildCostBreakdownDialog(Context context, List<Ticket> tickets) {
		View view = LayoutInflater.from(context).inflate(R.layout.cost_summary_alert, null);
		LinearLayout ll = Ui.findView(view, R.id.cost_summary_container);

		ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
			context.getString(R.string.lx_cost_breakdown_due_today),
			lxState.latestTotalPrice()
				.getFormattedMoneyFromAmountAndCurrencyCode(lxState.latestTotalPrice().getAmount(),
					lxState.latestTotalPrice().getCurrency())));

		String currencyCode = tickets.get(0).money.getCurrency();
		for (Ticket ticket : lxBookableItem.tickets) {
			//Presently API is not sending currency code within the Ticket Json Object, so we are resorting to
			//creating the formatted string by picking the amount from `moneyWithoutCurrencyCode` and `currencyCode`.
			Money moneyWithoutCurrencyCode = ticket.getBreakdownForType(PriceBreakdownItemType.PER_CATEGORY_TOTAL).price;

			ll.addView(
				CheckoutSummaryWidgetUtils.addRow(context,
					LXDataUtils.ticketCountSummary(getContext(), ticket.code, ticket.count),
					Money
						.getFormattedMoneyFromAmountAndCurrencyCode(moneyWithoutCurrencyCode.getAmount(), currencyCode)));
		}

		ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
			context.getString(R.string.lx_cost_breakdown_taxes_included)));
		ll.addView(addDisclaimerRow(context, currencyCode));
		ll.addView(CheckoutSummaryWidgetUtils.addRow(context,
			context.getString(R.string.checkout_breakdown_total_price),
			lxState.latestTotalPrice()
				.getFormattedMoneyFromAmountAndCurrencyCode(lxState.latestTotalPrice().getAmount(),
					lxState.latestTotalPrice().getCurrency())));

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
