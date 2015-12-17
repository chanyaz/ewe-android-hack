package com.expedia.bookings.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.DeepLinkRouterActivity;
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
 * Created by t-junguyen on 6/24/15.
 */
public class GTLeanPlumTemplate extends BaseMessageDialog {

	public static final String MESSAGE1 = "Message 1";
	public static final String LINK1 = "Link 1";
	public static final String MESSAGE2 = "Message 2";
	public static final String LINK2 = "Link 2";
	public static final String CAMPAIGN_TEXT = "Campaign Text";
	public static final String DISMISS_ACTION = "Dismiss Action";
	public static final String TITLE = "Title";

	public GTLeanPlumTemplate(Activity activity) {
		super(activity, false, null, null);
	}

	public static void register(Context context) {
		Leanplum.defineAction(
			"LeanPlum GT Message",
			Leanplum.ACTION_KIND_MESSAGE | Leanplum.ACTION_KIND_ACTION,
			new ActionArgs()
				.with(TITLE, "Need a Ride in [...]?")
				.with(MESSAGE1, "Book a Rental Car")
				.with (LINK1, "")
				.with(LINK2, "")
				.with(MESSAGE2, "Retrieve a Shuttle")
				.with(CAMPAIGN_TEXT, "default")
				.withAction(DISMISS_ACTION, null), new ActionCallback() {

				@Override
				public boolean onResponse(final ActionContext actionContext) {
					LeanplumActivityHelper.queueActionUponActive(new VariablesChangedCallback() {
						@Override
						public void variablesChanged() {
							if (Strings.isEmpty(actionContext.stringNamed(MESSAGE1)) || Strings.isEmpty(actionContext.stringNamed(MESSAGE2))) {
								Log.d("Cannot show leanplum dialog with empty message");
								return;
							}

							Activity activity = LeanplumActivityHelper.getCurrentActivity();
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.MyThemeLeamPLum));
							LayoutInflater inflater = activity.getLayoutInflater();

							OmnitureTracking.trackLeanPlumInAppMessage(actionContext.stringNamed(CAMPAIGN_TEXT));
							// Inflate and set the layout for the dialog
							// Pass null as the parent view because its going in the dialog layout
							View view = inflater.inflate(R.layout.gt_leanplum_dialog, null);
							alertDialogBuilder.setView(view);
							TextView title = (TextView) view.findViewById(R.id.leanplumtemplate_title);
							title.setText(actionContext.stringNamed(TITLE));
							Button message1 = (Button) view.findViewById(R.id.leanplumtemplate_message1);
							message1.setText(actionContext.stringNamed(MESSAGE1));
							message1.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));
							Button message2 = (Button) view.findViewById(R.id.leanplumtemplate_message2);
							message2.setText(actionContext.stringNamed(MESSAGE2));
							message2.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));

							//set the image for background
							alertDialogBuilder.setCancelable(false);
							AlertDialog alertDialog = alertDialogBuilder.create();
							alertDialog.setCanceledOnTouchOutside(true);
							alertDialog.show();

							message1.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									if (actionContext.stringNamed(LINK1).length() > 0) {
										Uri deepLink = Uri.parse(actionContext.stringNamed(LINK1));
										Intent intent = new Intent();
										intent.setClass(view.getContext(), DeepLinkRouterActivity.class);
										intent.setData(deepLink);
										view.getContext().startActivity(intent);
									}
								}
							});

							message2.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									if (actionContext.stringNamed(LINK2).length() > 0) {
										Uri deepLink = Uri.parse(actionContext.stringNamed(LINK2));
										Intent intent = new Intent();
										intent.setClass(view.getContext(), DeepLinkRouterActivity.class);
										intent.setData(deepLink);
										view.getContext().startActivity(intent);
									}
								}
							});
						}
					});
					return true;
				}
			});
	}
}
