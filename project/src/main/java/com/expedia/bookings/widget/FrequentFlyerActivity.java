package com.expedia.bookings.widget;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.expedia.bookings.R;

public class FrequentFlyerActivity extends Activity implements AdapterView.OnItemSelectedListener {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Spinner spinner = (Spinner) findViewById(R.id.spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ffn_programs, R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(adapter);
	}


//	Spinner spinner = (Spinner) findViewById(R.id.spinner);
//
//	//get the spinner from the xml.
//	Spinner spinner = (Spinner)findViewById(R.id.spinner);
//	//create a list of items for the spinner.
//	String[] items = new String[]{"1", "2", "three"};
//	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
//	dropdown.setAdapter(adapter);
//	FrequentFlyerProgramSpinner spinner = (FrequentFlyerProgramSpinner) View.findViewById(R.id.edit_frequent_flyer_program_spinner);
//
//
//	public FrequentFlyerActivity(Context context) {
//		FrequentFlyerProgramSpinner spinner: (FrequentFlyerProgramSpinner) bindView(R.id.edit_frequent_flyer_program_spinner);
//		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
//			R.array.ffn_programs, R.layout.simple_spinner_item);
//		adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
//		spinner.setAdapter(adapter);
//	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}