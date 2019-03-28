package com.shen.bluetoothledemo;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ServerActivity extends Activity implements OnClickListener {

	private static final String TAG = "ServerActivity";
	private BluetoothManager mManager;
	private BluetoothAdapter mAdapter;
	private BluetoothLeAdvertiser mLeAdvertiser;
	private BluetoothGattServer mGattServer;
	private BluetoothGattCharacteristic mGattCharacteristic;

	private TextView tv_status;
	private Button btn_send, btn_stop;

	private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

	private Context mContext;
	private MyDialogListener mListener = new MyDialogListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_main);
		mContext = ServerActivity.this;

		tv_status = (TextView) findViewById(R.id.tv_status);
		btn_send = (Button) findViewById(R.id.btn_send);
		btn_send.setOnClickListener(this);
		btn_stop = (Button) findViewById(R.id.btn_stop);
		btn_stop.setOnClickListener(this);
		mManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mAdapter = mManager.getAdapter();
		if (mAdapter == null) {
			Toast.makeText(mContext, R.string.system_text_blue_no_support, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mAdapter.isEnabled()) {
			buildAlertDialog();
		}

		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
				|| !mAdapter.isMultipleAdvertisementSupported()) {
			Toast.makeText(mContext, R.string.system_text_blue_le_no_support, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		mLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();
		mGattServer = mManager.openGattServer(mContext, mGattServerCallback);
		initGattServer();
	}

	private void initGattServer() {
		// TODO Auto-generated method stub
		BluetoothGattService service = new BluetoothGattService(Utils.CONNECTION_SERVICE_UUID,
				BluetoothGattService.SERVICE_TYPE_PRIMARY);

		BluetoothGattCharacteristic readCharacteristic = new BluetoothGattCharacteristic(Utils.CHARACTERISTIC_READ_UUID,
				// Read-only characteristic, supports notifications
				BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
				BluetoothGattCharacteristic.PERMISSION_READ);
		BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
				Utils.CHARACTERISTIC_WRITE_UUID,
				// Read+write characteristic permissions
				BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
				BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

		service.addCharacteristic(readCharacteristic);
		service.addCharacteristic(writeCharacteristic);
		mGattServer.addService(service);
	}

	private void startAdvertising() {
		if (mLeAdvertiser == null) {
			return;
		}
		AdvertiseSettings settings = new AdvertiseSettings.Builder()
				.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED).setConnectable(true).setTimeout(0)
				.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM).build();

		AdvertiseData data = new AdvertiseData.Builder().setIncludeDeviceName(true)
				.addServiceUuid(new ParcelUuid(Utils.CONNECTION_SERVICE_UUID)).build();

		mLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
	}

	private void stopAdvertising() {
		if (mLeAdvertiser == null) {
			return;
		}
		mLeAdvertiser.stopAdvertising(mAdvertiseCallback);
		tv_status.setText(R.string.system_text_stop_send);
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

	private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
		@Override
		public void onStartSuccess(AdvertiseSettings settingsInEffect) {
			// TODO Auto-generated method stub
			super.onStartSuccess(settingsInEffect);
			tv_status.setText(R.string.system_text_send_success);
		}

		@Override
		public void onStartFailure(final int errorCode) {
			// TODO Auto-generated method stub
			super.onStartFailure(errorCode);
			tv_status.setText(Utils.getAdvError(errorCode));
		}
	};

	private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

		@Override
		public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
			// TODO Auto-generated method stub
			super.onConnectionStateChange(device, status, newState);
			Log.i(TAG, "device.getName() = " + device.getName() + ", status = " + status + ",  newState = " + newState);
			tv_status.setText(Utils.getConnectionStatus(status, newState));
			if (status == newState) {
				stopAdvertising();
			}
		}

		@Override
		public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
			Log.i(TAG, "requestId = " + requestId);
			mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
		}

		@Override
		public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
				BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
				final byte[] value) {
			// TODO Auto-generated method stub
			super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset,
					value);

			runOnUiThread(new Runnable() {
				public void run() {
					Log.i(TAG, "values === " + new String(value));
					Toast.makeText(mContext, "value = " + new String(value), 1).show();
				}
			});
			
			if (Utils.CHARACTERISTIC_WRITE_UUID.equals(characteristic.getUuid())) {
				if (responseNeeded) {
					mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value);
				}
			}
		}
	};

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mLeAdvertiser != null) {
			stopAdvertising();
			mLeAdvertiser = null;
		} else if (mGattServer != null) {
			mGattServer.close();
			mGattServer = null;
		}

	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_send:
			startAdvertising();
			break;
		case R.id.btn_stop:
			stopAdvertising();
			break;
		default:
			break;
		}
	}
}
