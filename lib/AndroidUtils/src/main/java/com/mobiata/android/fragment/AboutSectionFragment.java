package com.mobiata.android.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobiata.android.Log;
import com.mobiata.android.R;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

public class AboutSectionFragment extends Fragment {
	public interface AboutSectionFragmentListener {
		boolean onAboutRowClicked(int tag);
		void onAboutRowRebind(int tag, TextView titleTextView, TextView descriptionTextView);
	}

	private static final String ARG_TITLE = "ARG_TITLE";
	private static final String ARG_ROW_DESCRIPTORS = "ARG_ROW_DESCRIPTORS";

	public static class Builder {
		private Context mContext;
		private Bundle mArgs;
		private ArrayList<RowDescriptor> mRowDescriptors;

		public Builder(Context context) {
			mContext = context;
			mArgs = new Bundle();
			mRowDescriptors = new ArrayList<>();
		}

		public Builder setTitle(@StringRes int resId) {
			String title = mContext.getString(resId);
			setTitle(title);
			return this;
		}

		public Builder setTitle(String title) {
			mArgs.putString(ARG_TITLE, title);
			return this;
		}

		public Builder addRow(@StringRes int stringId, int rowTag) {
			addRow(mContext.getString(stringId), rowTag);
			return this;
		}

		public Builder addRow(String rowTitle, int rowTag) {
			RowDescriptor descriptor = new RowDescriptor();
			descriptor.title = rowTitle;
			descriptor.clickTag = rowTag;
			addRow(descriptor);
			return this;
		}

		public Builder addRow(int rowTag, @StringRes int stringId, String contentDescription) {
			RowDescriptor descriptor = new RowDescriptor();
			descriptor.title = mContext.getString(stringId);
			descriptor.clickTag = rowTag;
			descriptor.contentDescription = contentDescription;
			addRow(descriptor);
			return this;
		}

		public Builder addRow(int rowTag, String title, String contentDescription) {
			RowDescriptor descriptor = new RowDescriptor();
			descriptor.title = title;
			descriptor.clickTag = rowTag;
			descriptor.contentDescription = contentDescription;
			addRow(descriptor);
			return this;
		}

		public Builder addRow(RowDescriptor desc) {
			mRowDescriptors.add(desc);
			return this;
		}

		public AboutSectionFragment build() {
			JSONUtils.putJSONableList(mArgs, ARG_ROW_DESCRIPTORS, mRowDescriptors);

			AboutSectionFragment frag = AboutSectionFragment.newInstance();
			frag.setArguments(mArgs);
			return frag;
		}
	}

	public static class RowDescriptor implements JSONable {
		public static final int UNKNOWN_ID = 0;

		public String title;
		public String description;
		public String contentDescription;
		public int clickTag = UNKNOWN_ID;
		public @DrawableRes int drawableId = UNKNOWN_ID;

		@Override
		public JSONObject toJson() {
			JSONObject obj = new JSONObject();
			try {
				if (!TextUtils.isEmpty(title)) {
					obj.put("title", title);
				}
				if (!TextUtils.isEmpty(description)) {
					obj.put("description", description);
				}
				if (!TextUtils.isEmpty(contentDescription)) {
					obj.put("contentDescription", contentDescription);
				}
				obj.put("clickTag", clickTag);
				obj.put("drawableId", drawableId);
			}
			catch (JSONException e) {
				Log.w("Could not write RowDescriptor JSON", e);
			}
			return obj;
		}

		@Override
		public boolean fromJson(JSONObject obj) {
			title = obj.optString("title", null);
			description = obj.optString("description", null);
			contentDescription = obj.optString("contentDescription", null);
			clickTag = obj.optInt("clickTag", UNKNOWN_ID);
			drawableId = obj.optInt("drawableId", UNKNOWN_ID);
			return true;
		}

		public boolean isComplex() {
			boolean ret = false;
			ret |= !TextUtils.isEmpty(description);
			ret |= drawableId != UNKNOWN_ID;
			return ret;
		}
	}

	public static AboutSectionFragment newInstance() {
		return new AboutSectionFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof AboutSectionFragmentListener)) {
			throw new RuntimeException("Activity must implement AboutSectionFragmentListener to use this fragment");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup aboutSection = (ViewGroup) inflater.inflate(R.layout.fragment_about_section, container, false);

		Bundle args = getArguments();
		TextView sectionTitle = Ui.findView(aboutSection, R.id.section_title);
		ViewGroup sectionLayout = Ui.findView(aboutSection, R.id.section_layout);
		if (args.containsKey(ARG_TITLE)) {
			sectionTitle.setText(args.getString(ARG_TITLE));
		}
		else {
			sectionTitle.setVisibility(View.GONE);
		}

		List<RowDescriptor> rowDescriptors = JSONUtils.getJSONableList(args, ARG_ROW_DESCRIPTORS, RowDescriptor.class);

		for (int i = 0; i < rowDescriptors.size(); i++) {
			RowDescriptor desc = rowDescriptors.get(i);
			View row;
			if (desc.isComplex()) {
				row = inflater.inflate(R.layout.snippet_about_row_complex, sectionLayout, false);
			}
			else {
				row = inflater.inflate(R.layout.snippet_about_row_simple, sectionLayout, false);
			}

			if (desc.contentDescription != null) {
				row.setContentDescription(desc.contentDescription);
			}

			row.setTag(desc.clickTag);
			row.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v.getTag() != null) {
						final int tag = (Integer) v.getTag();

						if (getActivity() != null) {
							((AboutSectionFragmentListener) getActivity()).onAboutRowClicked(tag);
						}
					}
					else {
						Log.w("No id set for AboutSectionFragment row");
					}
				}
			});

			TextView rowTitleView = Ui.findView(row, R.id.row_title);
			if (!TextUtils.isEmpty(desc.title)) {
				rowTitleView.setText(desc.title);
			}
			else {
				rowTitleView.setVisibility(View.GONE);
			}

			if (desc.isComplex()) {
				ImageView imageView = Ui.findView(row, R.id.image);
				if (desc.drawableId != RowDescriptor.UNKNOWN_ID) {
					imageView.setImageResource(desc.drawableId);
				}
				else {
					imageView.setVisibility(View.GONE);
				}

				TextView description_view = Ui.findView(row, R.id.row_description);
				if (!TextUtils.isEmpty(desc.description)) {
					description_view.setText(desc.description);
				}
				else {
					description_view.setVisibility(View.GONE);
				}
			}

			if (i > 0) {
				View divider = inflater.inflate(R.layout.snippet_about_divider, sectionLayout, false);
				sectionLayout.addView(divider);
			}
			sectionLayout.addView(row);
		}

		return aboutSection;
	}

	public void setRowVisibility(int rowTag, int visibility) {
		View root = getView();
		if (root != null) {
			View row = root.findViewWithTag(rowTag);
			if (row != null) {
				row.setVisibility(visibility);
				ViewGroup parent = (ViewGroup) row.getParent();
				int count = parent.getChildCount();
				for (int i = 0; i < count; i++) {
					View v = parent.getChildAt(i);
					if (v == row) {
						if (i > 0) {
							View divider = parent.getChildAt(i - 1);
							divider.setVisibility(visibility);
						}
						break;
					}
				}
			}
		}
	}

	public void notifyOnRowDataChanged(int rowTag) {
		View root = getView();
		if (root != null) {
			View row = root.findViewWithTag(rowTag);
			if (row != null) {
				TextView titleView = Ui.findView(row, R.id.row_title);
				TextView descriptionView = Ui.findView(row, R.id.row_description);
				((AboutSectionFragmentListener) getActivity()).onAboutRowRebind(rowTag, titleView, descriptionView);
			}
		}
	}
}

