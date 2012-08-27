package com.expedia.bookings.section;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.StoredCreditCard;

public class SectionStoredCreditCard extends LinearLayout implements ISection<StoredCreditCard> {

	ArrayList<SectionField<?, StoredCreditCard>> mFields = new ArrayList<SectionField<?, StoredCreditCard>>();

	Context mContext;
	StoredCreditCard mStoredCard;

	public SectionStoredCreditCard(Context context) {
		super(context);
		init(context);
	}

	public SectionStoredCreditCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionStoredCreditCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		mFields.add(mDisplayCardDesc);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		for (SectionField<?, StoredCreditCard> field : mFields) {
			field.bindField(this);
		}
	}

	@Override
	public void bind(StoredCreditCard data) {
		mStoredCard = data;
		if (mStoredCard != null) {
			for (SectionField<?, StoredCreditCard> field : mFields) {
				field.bindData(mStoredCard);
			}
		}
	}

	SectionField<TextView, StoredCreditCard> mDisplayCardDesc = new SectionField<TextView, StoredCreditCard>(
			R.id.display_stored_card_desc) {
		@Override
		public void onHasFieldAndData(TextView field, StoredCreditCard data) {
			field.setText(data.getDescription() == null ? "" : data.getDescription());
		}
	};

}
