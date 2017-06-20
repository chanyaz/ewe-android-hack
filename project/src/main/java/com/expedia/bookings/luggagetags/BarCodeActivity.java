package com.expedia.bookings.luggagetags;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class BarCodeActivity extends AppCompatActivity {
	private static final String LOG_TAG = BarCodeActivity.class.getSimpleName();
	private static final int BARCODE_READER_REQUEST_CODE = 1;

	private TextView mResultTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.barcode_activity);

		mResultTextView = (TextView) findViewById(R.id.result_textview);

		Button scanBarcodeButton = (Button) findViewById(R.id.scan_barcode_button);
		scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), BarCodeCaptureActivity.class);
				startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BARCODE_READER_REQUEST_CODE) {
			if (resultCode == CommonStatusCodes.SUCCESS) {
				if (data != null) {
					Barcode barcode = data.getParcelableExtra(BarCodeCaptureActivity.BarcodeObject);
					Point[] p = barcode.cornerPoints;
					mResultTextView.setText(barcode.displayValue);
				} else mResultTextView.setText(R.string.no_barcode_captured);
			} else Log.e(LOG_TAG, "Error with barcode capture");
		} else super.onActivityResult(requestCode, resultCode, data);
	}
}
