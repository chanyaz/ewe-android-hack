package com.mobiata.android.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobiata.android.Log;
import com.mobiata.android.R;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

public class CopyrightFragment extends Fragment {

	public interface CopyrightFragmentListener {
		public boolean onLogoLongClick();

		public void onLogoClick();
	}

	private static final String ARG_NAME = "ARG_NAME";
	private static final String ARG_COPYRIGHT = "ARG_COPYRIGHT";
	private static final String ARG_COPYRIGHT_STRING = "ARG_COPYRIGHT_STRING";
	private static final String ARG_LOGO = "ARG_LOGO";
	private static final String ARG_SUPPRESS_VERSION_CODE = "SUPPRESS_VERSION_CODE";

	public static class Builder {
		private Bundle mArgs;

		public Builder() {
			mArgs = new Bundle();
		}

		public Builder setAppName(int id) {
			mArgs.putInt(ARG_NAME, id);
			return this;
		}

		public Builder setCopyright(int id) {
			mArgs.putInt(ARG_COPYRIGHT, id);
			return this;
		}

		public Builder setCopyright(String copyrightString) {
			mArgs.putString(ARG_COPYRIGHT_STRING, copyrightString);
			return this;
		}

		public Builder setLogo(int id) {
			mArgs.putInt(ARG_LOGO, id);
			return this;
		}

		public Builder suppressVersionCode(boolean suppress) {
			mArgs.putBoolean(ARG_SUPPRESS_VERSION_CODE, suppress);
			return this;
		}

		public CopyrightFragment build() {
			CopyrightFragment frag = CopyrightFragment.newInstance();
			frag.setArguments(mArgs);
			return frag;
		}
	}

	public static CopyrightFragment newInstance() {
		CopyrightFragment frag = new CopyrightFragment();
		return frag;
	}

	private ImageView mLogoView;
	private TextView mBuildInfoView;
	private TextView mCopyrightView;

	private String mCopyrightString;
	private String mAppName;
	private int mLogo;
	private boolean mSuppressVersionCode;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Context context = getActivity();
		Bundle args = getArguments();
		if (args.containsKey(ARG_COPYRIGHT_STRING)) {
			mCopyrightString = args.getString(ARG_COPYRIGHT_STRING);
		}
		else {
			mCopyrightString = context.getString(args.getInt(ARG_COPYRIGHT));
		}
		mAppName = context.getString(args.getInt(ARG_NAME));

		mLogo = args.getInt(ARG_LOGO);

		mSuppressVersionCode = args.getBoolean(ARG_SUPPRESS_VERSION_CODE, false);

		View root = inflater.inflate(R.layout.fragment_copyright, container, false);

		mLogoView = Ui.findView(root, R.id.logo);
		mLogoView.setImageResource(mLogo);
		if (getActivity() instanceof CopyrightFragmentListener) {
			mLogoView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((CopyrightFragmentListener) getActivity()).onLogoClick();
				}
			});

			mLogoView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					return ((CopyrightFragmentListener) getActivity()).onLogoLongClick();
				}
			});
		}

		mLogoView.setContentDescription(mAppName);

		mBuildInfoView = Ui.findView(root, R.id.build_info);
		mCopyrightView = Ui.findView(root, R.id.copyright_info);

		bind();
		return root;
	}

	private void bind() {
		if (getActivity() == null || mBuildInfoView == null || mCopyrightView == null) {
			return;
		}

		try {
			PackageManager pm = getActivity().getPackageManager();
			String pkgName = getActivity().getPackageName();
			PackageInfo pi = pm.getPackageInfo(pkgName, 0);

			ArrayList<String> infoStrings = new ArrayList<>();
			if (!TextUtils.isEmpty(mAppName)) {
				infoStrings.add(mAppName);
			}
			infoStrings.add(pi.versionName);

			if (!mSuppressVersionCode) {
				String versionCode;
				if (AndroidUtils.isRelease(getActivity())) {
					versionCode = Integer.toString(pi.versionCode);
				}
				else {
					versionCode = AndroidUtils.getAlphaBuildNumber(getActivity());
					// If buildNumber is not defined in the manifest, just use the versionCode
					if (TextUtils.isEmpty(versionCode) || versionCode.equals("0")) {
						versionCode = Integer.toString(pi.versionCode);
					}
				}
				infoStrings.add("(" + versionCode + ")");
			}

			String buildInfo = TextUtils.join(" ", infoStrings);
			mBuildInfoView.setText(buildInfo);
		}
		catch (Exception e) {
			// PackageManager is traditionally wonky, need to accept all exceptions here.
			Log.w("Couldn't get package info in order to show version code!", e);
			// We've just got to hide it :(
			mBuildInfoView.setVisibility(View.GONE);
		}

		mCopyrightView.setText(mCopyrightString);
	}
}

