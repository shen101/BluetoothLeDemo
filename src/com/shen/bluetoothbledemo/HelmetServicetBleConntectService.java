package com.shen.bluetoothbledemo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

@SuppressLint("UseSparseArrays")
public class HelmetServicetBleConntectService extends Service {

	private static final String TAG = "HelmetServicetBleConntectService";

	private BluetoothManager mManager;
	private BluetoothAdapter mAdapter;
	private BluetoothLeAdvertiser mLeAdvertiser;
	private BluetoothGattServer mGattServer;
	private BluetoothGattCharacteristic mGattCharacteristic;
	private BluetoothDevice sendDevice;

	private String send_contents = "";
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (HelmetToolUtils.BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_ACTION.equals(intent.getAction())) {
				int values = intent.getIntExtra(HelmetToolUtils.BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE,
						HelmetToolUtils.HELMET_DEFAULT_NULL_NUM);
				if (HelmetToolUtils.BLE_COMMAND_CODE_START == values) {
					startAdvertising();
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_CLOSE == values) {
					stopAdvertising();
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_DATA == values) {
					send_contents = intent
							.getStringExtra(HelmetToolUtils.BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_CONTENTS);
					sendData(send_contents);
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		mManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

		mAdapter = mManager.getAdapter();

		mLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();
		mGattServer = mManager.openGattServer(getApplicationContext(), mGattServerCallback);
		initGattServer();

		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(HelmetToolUtils.BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_ACTION);
		mFilter.addAction(HelmetToolUtils.NOTIFICATION_MESSAGE_ACTION);
		registerReceiver(mReceiver, mFilter);
		return super.onStartCommand(intent, flags, startId);
	}

	private void initGattServer() {
		// TODO Auto-generated method stub
		BluetoothGattService service = new BluetoothGattService(HelmetToolUtils.CONNECTION_SERVICE_UUID,
				BluetoothGattService.SERVICE_TYPE_PRIMARY);

		BluetoothGattCharacteristic readCharacteristic = new BluetoothGattCharacteristic(
				HelmetToolUtils.CHARACTERISTIC_READ_UUID,
				// Read-only characteristic, supports notifications
				BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
				BluetoothGattCharacteristic.PERMISSION_READ);
		BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
				HelmetToolUtils.CHARACTERISTIC_WRITE_UUID,
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
				.addServiceUuid(new ParcelUuid(HelmetToolUtils.CONNECTION_SERVICE_UUID)).build();

		mLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
	}

	private void stopAdvertising() {
		if (mLeAdvertiser == null) {
			return;
		}
		mLeAdvertiser.stopAdvertising(mAdvertiseCallback);
		Intent status_text = new Intent();
		status_text.setAction(HelmetToolUtils.BLE_SERVICE_ADVERTISE_CHANGE_ACTION);
		status_text.putExtra(HelmetToolUtils.BLE_SERVICE_ADVERTISE_CHANGE_STATUS, 11);
		sendBroadcast(status_text);
	}

	private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
		@Override
		public void onStartSuccess(AdvertiseSettings settingsInEffect) {
			// TODO Auto-generated method stub
			super.onStartSuccess(settingsInEffect);
			Intent status_text = new Intent();
			status_text.setAction(HelmetToolUtils.BLE_SERVICE_ADVERTISE_CHANGE_ACTION);
			status_text.putExtra(HelmetToolUtils.BLE_SERVICE_ADVERTISE_CHANGE_STATUS, 10);
			sendBroadcast(status_text);
		}

		@Override
		public void onStartFailure(int errorCode) {
			// TODO Auto-generated method stub
			super.onStartFailure(errorCode);
			Intent status_text = new Intent();
			status_text.setAction(HelmetToolUtils.BLE_SERVICE_ADVERTISE_CHANGE_ACTION);
			status_text.putExtra(HelmetToolUtils.BLE_SERVICE_ADVERTISE_CHANGE_STATUS, errorCode);
			sendBroadcast(status_text);
		}
	};

	private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

		@Override
		public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
			// TODO Auto-generated method stub
			super.onConnectionStateChange(device, status, newState);
			Intent status_text = new Intent();
			status_text.setAction(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_ACTION);
			status_text.putExtra(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_STATUS, newState);
			sendBroadcast(status_text);
			if (newState == 2) {
				// stopAdvertising();
			} else {

			}

			sendDevice = mAdapter.getRemoteDevice(device.getAddress());
		}

		@Override
		public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
			mGattCharacteristic = characteristic;
			mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
		}

		@Override
		public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
				BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
				final byte[] value) {
			// TODO Auto-generated method stub
			super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset,
					value);
			mGattCharacteristic = characteristic;
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Intent data_intent = new Intent();
					data_intent.setAction(HelmetToolUtils.BLE_SERVICE_SEND_CONTENTS_ACTION);
					data_intent.putExtra(HelmetToolUtils.BLE_SERVICE_SEND_CONTENTS_DATA, value);
					sendBroadcast(data_intent);
				}
			}).start();
			if (HelmetToolUtils.CHARACTERISTIC_WRITE_UUID.equals(characteristic.getUuid())) {
				if (responseNeeded) {
					mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value);
				}
			}
		}

		@Override
		public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
				BluetoothGattDescriptor descriptor) {
			// TODO Auto-generated method stub
			super.onDescriptorReadRequest(device, requestId, offset, descriptor);
			mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
		}

		@Override
		public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor,
				boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
			// TODO Auto-generated method stub
			super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
			mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
		}
	};

	private void sendData(String contents) {
		// TODO Auto-generated method stub
		if (mGattServer != null) {
			if (sendDevice != null && !"".equals(contents) && mGattCharacteristic != null) {
				mGattCharacteristic.setValue(contents.getBytes());
				mGattServer.notifyCharacteristicChanged(sendDevice, mGattCharacteristic, true);
			} else {
				Log.i(TAG, "sendDevice == null" + ", Edit = " + contents);
			}
		} else {
			Log.i(TAG, "mGattServer == null");
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		if (mLeAdvertiser != null) {
			stopAdvertising();
			mLeAdvertiser = null;
		} else if (mGattServer != null) {
			mGattServer.close();
			mGattServer = null;
		}
		unregisterReceiver(mReceiver);
	}

}
