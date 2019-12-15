package com.shen.bluetoothbledemo;

import android.app.Activity;
import android.os.Bundle;

public class HelmetBaseActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initProviderDatabaseInfo();
	}

	private void initProviderDatabaseInfo() {
		// TODO Auto-generated method stub
		if (HelmetToolUtils
				.getInfoFromSingleValueToString(HelmetBaseActivity.this, HelmetInfoContactProvider.FIRST_INTO)
				.equals("")) {
			HelmetToolUtils.initDBValues(HelmetBaseActivity.this);
		}
	}
}
