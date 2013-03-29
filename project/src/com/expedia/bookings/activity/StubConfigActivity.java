package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Scenario;
import com.expedia.bookings.data.ScenarioResponse;
import com.expedia.bookings.data.ScenarioSetResponse;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.app.AsyncLoadListActivity;
import com.mobiata.android.util.Ui;

public class StubConfigActivity extends AsyncLoadListActivity {

	private static final int DIALOG_SCENARIO = 1;

	private static final int DIALOG_SCENARIO_FAILED = 2;

	private static final String KEY_SET_SCENARIO = "KEY_SET_SCENARIO";

	private ProgressBar mProgressBar;
	private TextView mTextView;

	private List<Scenario> mScenarios;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stub_config);

		mProgressBar = Ui.findView(this, R.id.progress_bar);
		mTextView = Ui.findView(this, R.id.message_text_view);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		showDialog(DIALOG_SCENARIO);

		final Scenario scenario = mScenarios.get(position);

		Download<ScenarioSetResponse> setScenarioDownload = new Download<ScenarioSetResponse>() {
			@Override
			public ScenarioSetResponse doDownload() {
				// Won't work unless you clear all private data first
				ClearPrivateDataUtil.clear(StubConfigActivity.this);

				ExpediaServices services = new ExpediaServices(StubConfigActivity.this);
				return services.setScenario(scenario);
			}
		};

		BackgroundDownloader.getInstance().startDownload(KEY_SET_SCENARIO, setScenarioDownload, mSetScenarioCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();

		BackgroundDownloader.getInstance().registerDownloadCallback(KEY_SET_SCENARIO, mSetScenarioCallback);
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_SET_SCENARIO);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DIALOG_SCENARIO:
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("Setting scenario...");
			return dialog;
		case DIALOG_SCENARIO_FAILED:
			Builder builder = new Builder(this);
			builder.setMessage("Error while setting scenario");
			builder.setNeutralButton(R.string.ok, null);
			return builder.create();
		}

		return super.onCreateDialog(id, args);
	}

	//////////////////////////////////////////////////////////////////////////
	// AsyncLoadListActivity

	@Override
	public String getUniqueKey() {
		return "com.mobiata.stubconfig";
	}

	@Override
	public void showProgress() {
		mProgressBar.setVisibility(View.VISIBLE);
		mTextView.setText("Loading scenarios...");
	}

	@Override
	public Object downloadImpl() {
		ExpediaServices services = new ExpediaServices(this);
		return services.getScenarios();
	}

	@Override
	public void onResults(Object results) {
		if (results == null) {
			mTextView.setText("Got nothing, boss.  Are you connected to trunk-stubbed or a proxy to trunk-stubbed?");
			mProgressBar.setVisibility(View.GONE);
		}
		else {
			ScenarioResponse response = (ScenarioResponse) results;
			mScenarios = response.getScenarios();
			List<String> names = new ArrayList<String>();
			for (Scenario scenario : mScenarios) {
				names.add(scenario.getName());
			}

			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Scenario setting

	private OnDownloadComplete<ScenarioSetResponse> mSetScenarioCallback = new OnDownloadComplete<ScenarioSetResponse>() {
		@Override
		public void onDownload(ScenarioSetResponse results) {
			if (results.isSuccess()) {
				finish();
			}
			else {
				removeDialog(DIALOG_SCENARIO);
				showDialog(DIALOG_SCENARIO_FAILED);
			}
		}
	};
}
