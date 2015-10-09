package com.expedia.bookings.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.TextView;
import com.leanplum.ActionArgs;
import com.leanplum.ActionContext;
import com.leanplum.Leanplum;
import com.leanplum.LeanplumActivityHelper;
import com.leanplum.callbacks.ActionCallback;
import com.leanplum.callbacks.VariablesChangedCallback;
import com.leanplum.messagetemplates.BaseMessageDialog;
import com.mobiata.android.Log;

/**
 * Created by t-junguyen on 6/11/15.
 */
public class LeanPlumTemplate extends BaseMessageDialog {

	public static final String BACKGROUND_IMAGE = "http://media.expedia.com/mobiata/mobile/apps/ExpediaBooking/ABDestinations/images/SYD.jpg";
	public static final String MESSAGE = "Message";
	public static final String DISMISS_TEXT = "Dismiss Text";
	public static final String BACKGROUND = "Background";
	public static final String CAMPAIGN_TEXT = "Campaign Text";
	public static final String DISMISS_ACTION = "Dismiss Action";
	public static final String TITLE = "Title";

	public LeanPlumTemplate(Activity activity) {
		super(activity, false, null, null);
	}

	public static void register(Context context) {
		Leanplum.defineAction(
			"LeanPlum Simple Message",
			Leanplum.ACTION_KIND_MESSAGE | Leanplum.ACTION_KIND_ACTION,
			new ActionArgs()
				.with(TITLE, context.getString(R.string.app_name))
				.with(MESSAGE, "")
				.with(DISMISS_TEXT, context.getString(R.string.ok))
				.with(BACKGROUND, BACKGROUND_IMAGE)
				.with(CAMPAIGN_TEXT, "default")
				.withAction(DISMISS_ACTION, null), new ActionCallback() {

				@Override
				public boolean onResponse(final ActionContext actionContext) {
					LeanplumActivityHelper.queueActionUponActive(new VariablesChangedCallback() {
						@Override
						public void variablesChanged() {
							if (Strings.isEmpty(actionContext.stringNamed(MESSAGE))) {
								Log.d("Cannot show leanplum dialog with empty message");
								return;
							}

							Activity activity = LeanplumActivityHelper.getCurrentActivity();
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
							LayoutInflater inflater = activity.getLayoutInflater();

							OmnitureTracking.trackLeanPlumInAppMessage(actionContext.stringNamed(CAMPAIGN_TEXT));
							// Inflate and set the layout for the dialog
							// Pass null as the parent view because its going in the dialog layout
							View view = inflater.inflate(R.layout.leanplum_dialog, null);
							alertDialogBuilder.setView(view);
							TextView title = (TextView) view.findViewById(R.id.leanplumtemplate_title);
							title.setText(actionContext.stringNamed(TITLE));
							TextView message = (TextView) view.findViewById(R.id.leanplumtemplate_message);
							message.setText(actionContext.stringNamed(MESSAGE));
							ImageView background = (ImageView) view.findViewById(R.id.leanplumtemplate_background);
							Button b = (Button) view.findViewById(R.id.positive_button);

							String originalImageUrl = actionContext.stringNamed(BACKGROUND);
							int width = activity.getResources().getDimensionPixelSize(
								R.dimen.leanplum_dialog_image_width);
							int height = activity.getResources().getDimensionPixelSize(
								R.dimen.leanplum_dialog_image_height);
							String imageUrl = new Akeakamai(originalImageUrl)
								.resizeExactly(width, height)
								.build();

							new PicassoHelper.Builder(background)
								.setError(R.drawable.cars_fallback)
								.fade()
								.build()
								.load(imageUrl);

							//set the image for background
							alertDialogBuilder
								.setCancelable(false);
							final AlertDialog alertDialog = alertDialogBuilder.create();
							alertDialog.show();

							b.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									actionContext.runActionNamed(DISMISS_ACTION);
									alertDialog.dismiss();
								}
							});
						}
					});
					return true;
				}
			});
	}
}
