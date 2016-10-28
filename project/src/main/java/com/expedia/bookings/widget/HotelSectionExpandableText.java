package com.expedia.bookings.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelTextSection;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.util.Ui;

/**
 * <p>
 * This class is a view for displaying hotel detail description section with collapsible/expandable text views.
 * If and when the section is collapsed then a "Read More" clickable text view is shown at it's bottom.
 * Upon clicking the "Read More" text view, user can expand to see the rest of it's content.
 * </p>
 *
 * @hide
 */
public class HotelSectionExpandableText extends RelativeLayout {

	private static final int DEFAULT_PARAGRAPH_CUTOFF = 360;

	private int mParagraphCutOff = DEFAULT_PARAGRAPH_CUTOFF;

	private android.widget.TextView mTitleText;
	private android.widget.TextView mBodyText;
	private View mReadMoreView;
	private View mFadeOverlay;
	private HotelTextSection mHotelSection;
	private boolean mShouldCut;
	private boolean mIsExpanded;
	private CharSequence mSectionBody;

	public HotelSectionExpandableText(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View container = inflater.inflate(R.layout.include_hotel_description_section, this);
		mTitleText = Ui.findView(container, R.id.title_text);
		mBodyText = Ui.findView(container, R.id.body_text);
		mReadMoreView = Ui.findView(container, R.id.read_more);
		mFadeOverlay = Ui.findView(container, R.id.body_text_fade_bottom);
	}

	public void setHotelSection(HotelTextSection hotelSection) {
		mHotelSection = hotelSection;
		bind();
	}

	/**
	 * Helper method to indicate to always cut the set {@link HotelTextSection} paragraph.
	 *
	 * @param alwaysCut If true, then cut off character limit is DEFAULT_PARAGRAPH_CUTOFF
	 */
	public void setAlwaysCut(boolean alwaysCut) {
		mShouldCut = alwaysCut;
		bind();
	}

	/**
	 * Helper method to calculate the paragraph cut off char numbers.
	 *
	 * @param minBulletPoints Minimum number of bullet points to be shown before the rest is cut off.
	 */
	public void showMinBulletPoints(int minBulletPoints) {
		if (mHotelSection != null) {
			String content = mHotelSection.getContent();
			mParagraphCutOff = Strings.characterCutOffWithMinBulletsShown(content, minBulletPoints);
			if (mParagraphCutOff > 0) {
				mShouldCut = true;
			}
			else {
				mShouldCut = false;
			}
		}
		bind();
	}

	private void bind() {
		mTitleText.setVisibility(View.VISIBLE);
		mTitleText.setText(mHotelSection.getNameWithoutHtml());

		mSectionBody = HtmlCompat.fromHtml(mHotelSection.getContentFormatted(getContext()));

		// Add "read more" button if the paragraph is longer than mParagraphCutOff
		if (mShouldCut) {
			final CharSequence untruncated = mSectionBody;
			mReadMoreView.setVisibility(View.VISIBLE);
			mFadeOverlay.setVisibility(View.VISIBLE);
			View.OnClickListener clickListener = new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (!mIsExpanded) {
						mBodyText.setText(untruncated);
						mReadMoreView.setVisibility(View.GONE);
						mFadeOverlay.setVisibility(View.GONE);
						mIsExpanded = true;
					}
					else {
						mBodyText.setText(mSectionBody);
						mReadMoreView.setVisibility(View.VISIBLE);
						mFadeOverlay.setVisibility(View.VISIBLE);
						mIsExpanded = false;
					}
				}
			};
			mBodyText.setOnClickListener(clickListener);
			mReadMoreView.setOnClickListener(clickListener);

			mSectionBody = String.format(getContext().getString(R.string.ellipsize_text_template),
				mSectionBody.subSequence(0, Strings.cutAtWordBarrier(mSectionBody, mParagraphCutOff)));
		}
		mBodyText.setText(mSectionBody);
	}
}
