package com.shen.bluetoothbledemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ServerActivity extends HelmetBaseActivity implements OnClickListener {

	private static final String TAG = "ServerActivity";

	private BluetoothManager mManager;
	private BluetoothAdapter mAdapter;

	private TextView tv_status;
	private EditText et_text;
	private Button btn_send, btn_stop, send_server_data_btn;

	private MyDialogListener mListener = new MyDialogListener();

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_ACTION.equals(intent.getAction())) {
				int connectStatus = intent.getIntExtra(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_STATUS,
						HelmetToolUtils.HELMET_DEFAULT_NULL_NUM);
				tv_status.setText(HelmetToolUtils.getConnectionStatus(connectStatus));
			} else if (HelmetToolUtils.BLE_SERVICE_ADVERTISE_CHANGE_ACTION.equals(intent.getAction())) {
				int errorCode = intent.getIntExtra(HelmetToolUtils.BLE_SERVICE_ADVERTISE_CHANGE_STATUS,
						HelmetToolUtils.HELMET_DEFAULT_NULL_NUM);
				if (errorCode == 10) {
					tv_status.setText(R.string.system_text_send_success);
				} else if (errorCode == 11) {
					tv_status.setText(R.string.system_text_stop_send);
				} else {
					tv_status.setText(HelmetToolUtils.getAdvError(errorCode));
				}
			} else if (HelmetToolUtils.BLE_SERVICE_SEND_CONTENTS_ACTION.equals(intent.getAction())) {
				byte[] recevice_data = intent.getByteArrayExtra(HelmetToolUtils.BLE_SERVICE_SEND_CONTENTS_DATA);
				Log.i(TAG, "service receive data = "+new String(recevice_data));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_main);

		tv_status = (TextView) findViewById(R.id.tv_status);
		btn_send = (Button) findViewById(R.id.btn_send);
		btn_send.setOnClickListener(this);
		btn_stop = (Button) findViewById(R.id.btn_stop);
		btn_stop.setOnClickListener(this);

		et_text = (EditText) findViewById(R.id.send_server_edit);
		send_server_data_btn = (Button) findViewById(R.id.send_server_btn);
		send_server_data_btn.setOnClickListener(this);

		mManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		HelmetToolUtils.initBleService(this, HelmetServicetBleConntectService.class);
		
		Log.i(TAG, "MAC = "+HelmetToolUtils.getMacAddress(wifimanager));
	}
	
	WifiManager wifimanager;

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mAdapter = mManager.getAdapter();
		if (mAdapter == null) {
			Toast.makeText(this, R.string.system_text_blue_no_support, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
				|| !mAdapter.isMultipleAdvertisementSupported()) {
			Toast.makeText(this, R.string.system_text_blue_le_no_support, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mAdapter.isEnabled()) {
			buildAlertDialog();
		}

		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(HelmetToolUtils.BLE_SERVICE_ADVERTISE_CHANGE_ACTION);
		mFilter.addAction(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_ACTION);
		mFilter.addAction(HelmetToolUtils.BLE_SERVICE_SEND_CONTENTS_ACTION);
		registerReceiver(mReceiver, mFilter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == HelmetToolUtils.OPEN_BLUETOOTH_NUM && resultCode == Activity.RESULT_OK) {
			mAdapter.enable();
		} else if (requestCode == HelmetToolUtils.OPEN_BLUETOOTH_NUM && resultCode == Activity.RESULT_CANCELED) {
			finish();
		}
	}

	public void buildAlertDialog() {
		AlertDialog.Builder mDialog = new AlertDialog.Builder(ServerActivity.this);
		mDialog.setMessage(R.string.system_text_openblue_dialog_message);
		mDialog.setNegativeButton(android.R.string.cancel, mListener);
		mDialog.setPositiveButton(android.R.string.ok, mListener);
		mDialog.show();
	}

	private class MyDialogListener implements DialogInterface.OnClickListener {

		@SuppressWarnings("static-access")
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			if (dialog.BUTTON_NEGATIVE == which) {

			} else if (dialog.BUTTON_POSITIVE == which) {
				mAdapter.enable();
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_send:
			HelmetToolUtils.startBleServiceAdvertising(this);
			break;
		case R.id.btn_stop:
			HelmetToolUtils.stopBleServiceAdvertising(this);
			break;
		case R.id.send_server_btn:
			HelmetToolUtils.sendBleServiceData(this, et_text.getText().toString(),HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
}
