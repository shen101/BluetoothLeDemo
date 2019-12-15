package com.shen.bluetoothbledemo;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ClientActivity extends HelmetBaseActivity implements OnClickListener {

	private static final String TAG = "ClientActivity";
	private EditText edit_text;
	private Button scan_btn, stop_scan_btn, disconnect_btn, send_btn;
	private TextView tv_status;
	private ListView lv_list;

	private BluetoothManager mManager;
	private BluetoothAdapter mAdapter;

	private MyDialogListener mListener = new MyDialogListener();

	private ArrayList<ScanResult> Results = new ArrayList<ScanResult>();
	private ArrayList<String> devices_names = new ArrayList<String>();
	private HelmetBluetoothScanAdapter mScanAdapter;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case HelmetToolUtils.REFLASH_STATUS_SUCCESSFUL_NUM:
				tv_status.setText(HelmetToolUtils.getConnectionStatus(2));
				stop_scan_btn.setEnabled(false);
				disconnect_btn.setEnabled(true);
				break;
			case HelmetToolUtils.REFLASH_STATUS_FAILED_NUM:
				tv_status.setText(HelmetToolUtils.getConnectionStatus(0));
//				stop_scan_btn.setEnabled(true);
				disconnect_btn.setEnabled(false);
				break;

			default:
				break;
			}
		}
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (HelmetToolUtils.BLE_SCAN_FOND_ACTION.equals(intent.getAction())) {
				ScanResult mDevice = intent.getParcelableExtra("service_fond_result");
				if (mDevice.getDevice().getName() != null && !(mDevice.getDevice().getName()).equals("")) {
					if (!devices_names.contains(mDevice.getDevice().getName())) {

						Results.add(mDevice);
						stop_scan_btn.setEnabled(true);
						mScanAdapter = new HelmetBluetoothScanAdapter(context, Results);

						lv_list.setAdapter(mScanAdapter);
						mScanAdapter.notifyDataSetChanged();
					}
				}
				devices_names.add(mDevice.getDevice().getName());

				stop_scan_btn.setEnabled(true);
				lv_list.setAdapter(mScanAdapter);
			} else if (HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_ACTION.equals(intent.getAction())) {
				int newState = intent.getIntExtra(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_VALUES,
						HelmetToolUtils.HELMET_DEFAULT_NULL_NUM);
				if(newState == 2){
					mHandler.sendEmptyMessage(HelmetToolUtils.REFLASH_STATUS_SUCCESSFUL_NUM);
				}else{
					mHandler.sendEmptyMessage(HelmetToolUtils.REFLASH_STATUS_FAILED_NUM);
				}
			} else if (HelmetToolUtils.BLE_CLIENT_SEND_CONTENTS_ACTION.equals(intent.getAction())) {
				String recevice_data = intent.getStringExtra(HelmetToolUtils.BLE_CLIENT_SEND_CONTENTS_DATA);
				Log.i(TAG, "client recevice data = "+recevice_data);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_main);
		initViews();
		mManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mAdapter = mManager.getAdapter();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (mAdapter == null) {
			Toast.makeText(this, R.string.system_text_blue_no_support, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mAdapter.isEnabled()) {
			buildAlertDialog();
		}

		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(HelmetToolUtils.BLE_SCAN_FOND_ACTION);
		mFilter.addAction(HelmetToolUtils.BLE_CLIENT_SEND_CONTENTS_ACTION);
		mFilter.addAction(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_ACTION);
		registerReceiver(mReceiver, mFilter);
	}

	private void stop_scan() {
		devices_names.clear();
		Results.clear();
		lv_list.setAdapter(null);
		stop_scan_btn.setEnabled(false);
		HelmetToolUtils.stopScanBleDeviceIntent(this);
	}

	private void initViews() {
		// TODO Auto-generated method stub
		edit_text = (EditText) findViewById(R.id.edit_text);
		scan_btn = (Button) findViewById(R.id.scan_btn);
		scan_btn.setOnClickListener(this);
		stop_scan_btn = (Button) findViewById(R.id.stop_scan_btn);
		stop_scan_btn.setOnClickListener(this);
		stop_scan_btn.setEnabled(false);
		disconnect_btn = (Button) findViewById(R.id.disconnect_btn);
		disconnect_btn.setOnClickListener(this);
		disconnect_btn.setEnabled(false);
		send_btn = (Button) findViewById(R.id.send_btn);
		send_btn.setOnClickListener(this);
		tv_status = (TextView) findViewById(R.id.client_status);
		lv_list = (ListView) findViewById(R.id.list_devices);
		lv_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				HelmetToolUtils.connectRemoteDeviceIntent(ClientActivity.this, Results.get(position));
			}
		});

		HelmetToolUtils.initBleService(this, HelmetClientBleConntectService.class);
	}

	public void buildAlertDialog() {
		AlertDialog.Builder mDialog = new AlertDialog.Builder(ClientActivity.this);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == HelmetToolUtils.OPEN_BLUETOOTH_NUM && resultCode == Activity.RESULT_OK) {
			buildAlertDialog();
		} else if (requestCode == HelmetToolUtils.OPEN_BLUETOOTH_NUM && resultCode == Activity.RESULT_CANCELED) {
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.scan_btn:
			HelmetToolUtils.scanBleDeviceIntent(ClientActivity.this);
			devices_names.clear();
			Results.clear();
			lv_list.setAdapter(null);
			break;
		case R.id.stop_scan_btn:
			stop_scan();
			break;
		case R.id.send_btn:
			HelmetToolUtils.sendBleData(this, edit_text.getText().toString());
			break;
		case R.id.disconnect_btn:
			HelmetToolUtils.disconnectRemoteDeviceIntent(ClientActivity.this);
			mHandler.sendEmptyMessage(HelmetToolUtils.REFLASH_STATUS_FAILED_NUM);
			stop_scan();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (Results != null) {
			Results.clear();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
}
