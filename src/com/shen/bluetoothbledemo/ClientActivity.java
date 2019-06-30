package com.shen.bluetoothbledemo;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ClientActivity extends Activity implements OnClickListener {

	private static final String TAG = "ClientActivity";
	private EditText edit_text;
	private Button scan_btn, stop_scan_btn, disconnect_btn, send_btn;
	private TextView tv_status;
	private ListView lv_list;

	private Context mContext;

	private BluetoothManager mManager;
	private BluetoothAdapter mAdapter;

	private MyDialogListener mListener = new MyDialogListener();

	private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	private ArrayList<String> names = new ArrayList<String>();
	private HelmetBluetoothScanAdapter mScanAdapter;
	private BluetoothDevice mBluetoothDevice = null;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case HelmetToolUtils.REFLASH_STATUS_SUCCESSFUL_NUM:
				disconnect_btn.setEnabled(true);
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
				BluetoothDevice mDevice = intent.getParcelableExtra("service_fond_device");
				if (mDevice.getName() != null && !(mDevice.getName()).equals("")) {
					if (!names.contains(mDevice.getName())) {

						devices.add(mDevice);
						stop_scan_btn.setEnabled(true);
						mScanAdapter = new HelmetBluetoothScanAdapter(context, devices);

						lv_list.setAdapter(mScanAdapter);
						mScanAdapter.notifyDataSetChanged();
					}
				}
				names.add(mDevice.getName());

				stop_scan_btn.setEnabled(true);
				lv_list.setAdapter(mScanAdapter);
			} else if (HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_ACTION.equals(intent.getAction())) {
				int newState = intent.getIntExtra(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_VALUES,
						HelmetToolUtils.HELMET_DEFAULT_NULL_NUM);
				tv_status.setText(HelmetToolUtils.getConnectionStatus(newState));
			} else if (HelmetToolUtils.BLE_CLIENT_SEND_CONTENTS_ACTION.equals(intent.getAction())) {
				String recevice_data = intent.getStringExtra(HelmetToolUtils.BLE_CLIENT_SEND_CONTENTS_DATA);
				Toast.makeText(ClientActivity.this, recevice_data, Toast.LENGTH_SHORT).show();
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
		names.clear();
		devices.clear();
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
				HelmetToolUtils.connectRemoteDeviceIntent(ClientActivity.this, devices.get(position));
			}
		});

		HelmetToolUtils.initBleService(this, HelmetClientBleConntectService.class);
	}

	public void buildAlertDialog() {
		AlertDialog.Builder mDialog = new AlertDialog.Builder(mContext);
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
			HelmetToolUtils.scanBleDeviceIntent(this);
			names.clear();
			devices.clear();
			lv_list.setAdapter(null);
			break;
		case R.id.stop_scan_btn:
			stop_scan();
			break;
		case R.id.send_btn:
			HelmetToolUtils.sendBleData(this, edit_text.getText().toString());
			break;
		case R.id.disconnect_btn:
			HelmetToolUtils.disconnectRemoteDeviceIntent(this);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (devices != null) {
			devices.clear();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
}
