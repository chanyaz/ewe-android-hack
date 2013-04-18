package com.expedia.bookings.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.mobiata.android.util.AndroidUtils;

public class TerminalMapLegendDialogFragment extends DialogFragment {

	public static final String TAG = TerminalMapLegendDialogFragment.class.getName();

	public static TerminalMapLegendDialogFragment newInstance() {
		TerminalMapLegendDialogFragment fragment = new TerminalMapLegendDialogFragment();
		return fragment;
	}

	@SuppressLint("NewApi")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ImageView legend = new ImageView(getActivity());
		Point size = AndroidUtils.getScreenSize(getActivity());
		Resources res = getResources();
		int screenWidth = size.x;

		int legendDrawableId = R.drawable.terminal_map_legend_2cols;
		if (screenWidth >= res.getDimensionPixelSize(R.dimen.terminal_map_legend_4cols_width)) {
			legendDrawableId = R.drawable.terminal_map_legend_4cols;
		}
		else if (screenWidth >= res.getDimensionPixelSize(R.dimen.terminal_map_legend_3cols_width)) {
			legendDrawableId = R.drawable.terminal_map_legend_3cols;
		}
		legend.setImageResource(legendDrawableId);
		legend.setBackgroundColor(Color.WHITE);

		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setView(legend)
				.setNegativeButton(R.string.button_done, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						TerminalMapLegendDialogFragment.this.dismissAllowingStateLoss();
					}
				});

		return builder.create();
	}
}
