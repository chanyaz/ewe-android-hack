package com.expedia.bookings.activity;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.expedia.bookings.R;
import com.opinionlab.oo.sdk.android.CommentCardActivity;

public class AppFeedbackActivity extends CommentCardActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/**
		 * Intercepting the changes to the rating bar to appropriate set the radiogroup 
		 * that the opinionlab code uses to pick up the rating. The inability to fundamentally
		 * change the layout of the CommentCardActivity causes us to have to put this workaround in place
		 * where we present a ratingbar to the user but internally conver the user's rating to
		 * the appropriate checked button in the radiogroup. 
		 */
		RatingBar appRating = (RatingBar) findViewById(R.id.feedback_rating);
		final RadioGroup appRatingForOpinionLab = (RadioGroup) findViewById(R.id.oosdk_rating_overall);
		appRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				int ratingInt = (int) rating;
				switch (ratingInt) {
				case 1:
					appRatingForOpinionLab.check(R.id.oosdk_rating_overall_1);
					break;
				case 2:
					appRatingForOpinionLab.check(R.id.oosdk_rating_overall_2);
					break;
				case 3:
					appRatingForOpinionLab.check(R.id.oosdk_rating_overall_3);
					break;
				case 4:
					appRatingForOpinionLab.check(R.id.oosdk_rating_overall_4);
					break;
				case 5:
					appRatingForOpinionLab.check(R.id.oosdk_rating_overall_5);
					break;
				default:
					appRatingForOpinionLab.clearCheck();
					break;
				}

			}
		});
	}
}
