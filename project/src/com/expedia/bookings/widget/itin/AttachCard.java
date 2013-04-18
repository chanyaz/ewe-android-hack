package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;

public class AttachCard<T extends ItinCardData> extends LinearLayout {

	private AttachCardContentGenerator<? extends ItinCardData> mAttachCardContentGenerator;

	private ImageView mActionImageView;
	private TextView mActionTextView;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public AttachCard(Context context) {
		super(context);
		init(context, null);
	}

	public AttachCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void bind(T itinCardData) {
		mAttachCardContentGenerator = HotelAttachCardContentGenerator.createGenerator(getContext(), itinCardData);

		mActionImageView.setImageResource(mAttachCardContentGenerator.getButtonImageResId());
		mActionTextView.setText(mAttachCardContentGenerator.getButtonText());
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void init(Context context, AttributeSet attrs) {
		inflate(context, R.layout.widget_attach_card, this);

		mActionImageView = Ui.findView(this, R.id.action_image_view);
		mActionTextView = Ui.findView(this, R.id.action_text_view);
	}
}