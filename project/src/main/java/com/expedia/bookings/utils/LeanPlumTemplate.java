package com.expedia.bookings.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.widget.TextView;
import com.leanplum.ActionArgs;
import com.leanplum.ActionContext;
import com.leanplum.Leanplum;
import com.leanplum.LeanplumActivityHelper;
import com.leanplum.callbacks.ActionCallback;
import com.leanplum.callbacks.VariablesChangedCallback;
import com.leanplum.messagetemplates.BaseMessageDialog;
import com.leanplum.messagetemplates.CenterPopupOptions;

/**
 * Created by t-junguyen on 6/11/15.
 */
public class LeanPlumTemplate extends BaseMessageDialog {

	public static final String BACKGROUND_IMAGE = "http://media.expedia.com/mobiata/mobile/apps/ExpediaBooking/ABDestinations/images/SYD.jpg";

	public LeanPlumTemplate(Activity activity, CenterPopupOptions options) {
		super(activity, false, options, null);
		this.options = options;
	}

	static String getApplicationName(Context context) {
		int stringId = context.getApplicationInfo().labelRes;
		if (stringId == 0) {
			return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
		}
		return context.getString(stringId);
	}

	public static void register(Context context) {
		Leanplum.defineAction(
			"My Template",
			Leanplum.ACTION_KIND_MESSAGE | Leanplum.ACTION_KIND_ACTION,
			new ActionArgs().with("My Template", getApplicationName(context))
				.with("Message", "Alert Message")
				.with("Dismiss Text", "Ok")
				.with("Background", "BACKGROUND_IMAGE")
				.withAction("Dismiss Action", null), new ActionCallback() {

				@Override
				public boolean onResponse(final ActionContext context) {
					LeanplumActivityHelper.queueActionUponActive(new VariablesChangedCallback() {
						@Override
						public void variablesChanged() {
							Activity activity = LeanplumActivityHelper.getCurrentActivity();
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
							LayoutInflater inflater = activity.getLayoutInflater();

							// Inflate and set the layout for the dialog
							// Pass null as the parent view because its going in the dialog layout
							View view = inflater.inflate(R.layout.leanplum_dialog, null);
							alertDialogBuilder.setView(view);
							TextView title = (TextView) view.findViewById(R.id.leanplumtemplate_title);
							title.setText(context.stringNamed("My Template"));
							TextView message = (TextView) view.findViewById(R.id.leanplumtemplate_message);
							message.setText(context.stringNamed("Message"));
							ImageView background = (ImageView) view.findViewById(R.id.leanplumtemplate_background);

							new PicassoHelper.Builder(background)
								.setError(R.drawable.cars_fallback)
								.fade()
								.build()
								.load(BACKGROUND_IMAGE);

							//set the image for background
							alertDialogBuilder
								.setCancelable(false)
								.setPositiveButton(context.stringNamed("Dismiss Text"),
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											context.runActionNamed("Dismiss Action");
										}
									});
							AlertDialog alertDialog = alertDialogBuilder.create();
							alertDialog.show();

							Button b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
							if (b != null) {
								b.setBackgroundColor(Color.parseColor("#FFFFFF"));
								b.setTextColor(Color.parseColor("#fc2176"));
							}
							alertDialog.getWindow()
								.setBackgroundDrawableResource(R.drawable.leanplum_dialog_background);
						}
					});
					return true;
				}
			});
	}
}
