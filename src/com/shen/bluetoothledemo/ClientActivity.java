package com.shen.bluetoothledemo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
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

public class ClientActivity extends Activity implements OnClickListener {

	private static final String TAG = "ClientActivity";
	private EditText edit_text;
	private Button scan_btn, stop_scan_btn, disconnect_btn, send_btn;
	private TextView tv_status;
	private ListView lv_list;

	private Context mContext;

	private BluetoothManager mManager;
	private BluetoothAdapter mAdapter;
	private BluetoothLeScanner mLeScanner;
	private BluetoothGatt mGatt;

	private MyDialogListener mListener = new MyDialogListener();

	private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	private ArrayList<String> names = new ArrayList<String>();
	private BluetoothScanAdapter mScanAdapter;
	private BluetoothDevice mBluetoothDevice = null;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case Utils.REFLASH_STATUS_SUCCESSFUL_NUM:
				disconnect_btn.setEnabled(true);
				break;

			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_main);
		mContext = ClientActivity.this;
		initViews();
		mManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mAdapter = mManager.getAdapter();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (mAdapter == null) {
			Toast.makeText(mContext, R.string.system_text_blue_no_support, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mAdapter.isEnabled()) {
			buildAlertDialog();
		}

		mLeScanner = mAdapter.getBluetoothLeScanner();

	}

	private void start_scan() {

		mLeScanner.stopScan(mScanCallback);
		names.clear();
		devices.clear();
		lv_list.setAdapter(null);

		ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(Utils.CONNECTION_SERVICE_UUID))
				.build();
		ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
		filters.add(scanFilter);
		ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
		mLeScanner.startScan(filters, settings, mScanCallback);
	}

	private void stop_scan() {
		if (mLeScanner == null) {
			return;
		}
		mLeScanner.stopScan(mScanCallback);
		names.clear();
		devices.clear();
		lv_list.setAdapter(null);
		stop_scan_btn.setEnabled(false);
	}

	private ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			// TODO Auto-generated method stub
			super.onScanResult(callbackType, result);
			processResult(result);
		}

		@Override
		public void onScanFailed(int errorCode) {
			// TODO Auto-generated method stub
			super.onScanFailed(errorCode);
			tv_status.setText(Utils.getScanError(errorCode));
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			// TODO Auto-generated method stub
			super.onBatchScanResults(results);
		}

		private void processResult(ScanResult result) {

			mBluetoothDevice = result.getDevice();
			if (result.getScanRecord().getDeviceName() != null
					&& !(result.getScanRecord().getDeviceName()).equals("")) {
				if (!names.contains(result.getScanRecord().getDeviceName())) {

					devices.add(mBluetoothDevice);
					stop_scan_btn.setEnabled(true);
					mScanAdapter = new BluetoothScanAdapter(mContext, devices);

					mScanAdapter.notifyDataSetChanged();
					lv_list.setAdapter(mScanAdapter);
				}
			}
			names.add(result.getScanRecord().getDeviceName());
		}
	};

	public void buildAlertDialog() {
		AlertDialog.Builder mDialog = new AlertDialog.Builder(mContext);
		mDialog.setMessage(R.string.system_text_openblue_dialog_message);
		mDialog.setNegativeButton(android.R.string.cancel, mListener);
		mDialog.setPositiveButton(android.R.string.ok, mListener);
		mDialog.show();
	}

	private class MyDialogListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			if (dialog.BUTTON_NEGATIVE == which) {

			} else if (dialog.BUTTON_POSITIVE == which) {
				mAdapter.enable();
			}
		}
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
				mGatt = devices.get(position).connectGatt(mContext, false, mGattCallback);
				for (BluetoothGattService mGattService : mGatt.getServices()) {
					Log.i(TAG, "mGattService.getUuid() 1 : " + mGattService.getUuid());
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "requestCode = " + requestCode + ", resultCode = " + resultCode);
		if (requestCode == Utils.OPEN_BLUETOOTH_NUM && resultCode == Activity.RESULT_OK) {

		} else if (requestCode == Utils.OPEN_BLUETOOTH_NUM && resultCode == Activity.RESULT_CANCELED) {
			finish();
		}
	}

	private void disconnection() {
		if (mGatt != null) {
			mGatt.close();
		}
		stop_scan();
		disconnect_btn.setEnabled(false);
		tv_status.setText(R.string.system_text_disconnection);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.scan_btn:
			start_scan();
			break;
		case R.id.stop_scan_btn:
			stop_scan();
			break;
		case R.id.send_btn:
			send_data();
			break;
		case R.id.disconnect_btn:
			disconnection();
			break;
		default:
			break;
		}
	}

	private void send_data() {
		if (mGatt != null) {
			Log.i(TAG, "EditText = " + edit_text.getText().toString());
			writeCharacteristic(Utils.CONNECTION_SERVICE_UUID, Utils.CHARACTERISTIC_WRITE_UUID,
					edit_text.getText().toString());
		} else {
			Log.i(TAG, "mGatt == null");
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mGatt != null) {
			mGatt.disconnect();
			mGatt = null;
		}
		if (devices != null) {
			devices.clear();
		}
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				gatt.discoverServices();
				mHandler.sendEmptyMessage(Utils.REFLASH_STATUS_SUCCESSFUL_NUM);
			}
			tv_status.setText(Utils.getConnectionStatus(status, newState));
		};

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				android.bluetooth.BluetoothGattCharacteristic characteristic, int status) {
			Log.i(TAG, "onCharacteristicRead");
		};

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.i(TAG, "onServicesDiscovered");
			for (BluetoothGattService service : gatt.getServices()) {
				Log.i(TAG, "Service: " + service.getUuid());
				if (Utils.CONNECTION_SERVICE_UUID.equals(service.getUuid())) {
					// 读取当前 characteristic 的值
					gatt.readCharacteristic(service.getCharacteristic(Utils.CHARACTERISTIC_WRITE_UUID));
				}
			}
		};

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			super.onCharacteristicChanged(gatt, characteristic);
			Log.i(TAG, "onCharacteristicChanged");
		}
	};

	private void writeCharacteristic(UUID server_uuid, UUID read_write_uuid, String data) {
		if (mGatt == null) {
			Log.i(TAG, "mGatt not initialized");
			return;
		}

		BluetoothGattService service = mGatt.getService(server_uuid);
		if (service == null) {
			Log.w(TAG, "service not initialized");
			return;
		}

		BluetoothGattCharacteristic mGattCharacteristic = service.getCharacteristic(read_write_uuid);
		if (mGattCharacteristic == null) {
			Log.w(TAG, "characteristic not initialized");
			return;
		}

		mGattCharacteristic.setValue(data);
		mGatt.writeCharacteristic(mGattCharacteristic);
	}
}
