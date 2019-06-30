package com.shen.bluetoothbledemo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.app.Service;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("UseSparseArrays")
public class HelmetClientBleConntectService extends Service {

	private static final String TAG = "HelmetClientBleConntectService";
	private BluetoothDevice connect_device;
	private BluetoothGatt mGatt;
	private BluetoothManager mManager;
	private BluetoothLeScanner mLeScanner;
	private byte[] ble_use_content_text = null;
	private byte[] ble_use_content_notice = null;
	private HashMap<Integer, Boolean> bool_result = new HashMap<Integer, Boolean>();

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			if (HelmetToolUtils.BLE_BROADCASE_BLUETOOTH_COMMAND_CLIENT_ACTION.equals(intent.getAction())
					&& isBluetoothEnable()) {
				int values = intent.getIntExtra(HelmetToolUtils.BLE_BROADCASE_BLUETOOTH_COMMAND_VALUES,
						HelmetToolUtils.HELMET_DEFAULT_NULL_NUM);
				if (HelmetToolUtils.BLE_COMMAND_CODE_START == values) {
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_SCAN == values) {
					start_scan();
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_CLOSE == values) {
					stop_scan();
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_DATA == values) {
					try {
						ble_use_content_text = intent
								.getStringExtra(HelmetToolUtils.BLE_BROADCASE_BLUETOOTH_COMMAND_CONTENT)
								.getBytes(HelmetToolUtils.HELMET_DEFAULT_CHARSET);
						send_data(ble_use_content_text, HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE);
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_CONNECT == values) {
					connect_device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (connect_device != null) {
						mGatt = connect_device.connectGatt(getApplicationContext(), false, mGattCallback);
					}
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_DISCONNECT == values) {
					disconnection();
				}
			} else if (HelmetToolUtils.BLE_BROADCASE_BLUETOOTH_COMMAND_CLIENT_ACTION.equals(intent.getAction())
					&& !isBluetoothEnable()) {
				Toast.makeText(getApplicationContext(), R.string.helmet_text_bluetooth_is_no_open, 0).show();
			} else if (HelmetToolUtils.NOTIFICATION_MESSAGE_ACTION.equals(intent.getAction())) {
				String data_package = intent.getStringExtra(HelmetToolUtils.NOTIFICATION_MESSAGE_PACKAGE_NUM);
				String data_title = intent.getStringExtra(HelmetToolUtils.NOTIFICATION_MESSAGE_TITLE_NUM);
				String data_content = intent.getStringExtra(HelmetToolUtils.NOTIFICATION_MESSAGE_CONTENTS_NUM);
				if (!data_package.equals("com.sohu.inputmethod.sogou")) {
					try {
						ble_use_content_notice = (data_package + ":" + data_title + ":" + data_content)
								.getBytes(HelmetToolUtils.HELMET_DEFAULT_CHARSET);
						send_data(ble_use_content_notice, HelmetToolUtils.HELMET_DEFAULT_NOTICE_TYPE);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("static-access")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(HelmetToolUtils.BLE_BROADCASE_BLUETOOTH_COMMAND_CLIENT_ACTION);
		mFilter.addAction(HelmetToolUtils.NOTIFICATION_MESSAGE_ACTION);
		registerReceiver(mReceiver, mFilter);
		return super.onStartCommand(intent, flags, startId);
	}

	private boolean isBluetoothEnable() {
		if (mManager == null) {
			mManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
		}
		return mManager.getAdapter().isEnabled();
	}

	private void start_scan() {
		if (mManager == null) {
			mManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
		}
		mLeScanner = mManager.getAdapter().getDefaultAdapter().getBluetoothLeScanner();
		mLeScanner.stopScan(mScanCallback);
		ScanFilter scanFilter = new ScanFilter.Builder()
				.setServiceUuid(new ParcelUuid(HelmetToolUtils.CONNECTION_SERVICE_UUID)).build();
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
	}

	private void disconnection() {
		if (mGatt != null) {
			mGatt.close();
		}
		stop_scan();
	}

	private ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			// TODO Auto-generated method stub
			super.onScanResult(callbackType, result);
			if (result.getDevice().getName()
					.equals(HelmetToolUtils.getHelmetDefaultStatusValues(getApplicationContext(),
							HelmetToolUtils.BLE_CONNECTED_DEVICES_TABLE, HelmetToolUtils.BLE_CONNECTED_DEVICES_NAME))) {
				connect_device = result.getDevice();
				mGatt = result.getDevice().connectGatt(getApplicationContext(), false, mGattCallback);
			} else {
				Intent fond_device_intent = new Intent();
				fond_device_intent.setAction(HelmetToolUtils.BLE_SCAN_FOND_ACTION);
				fond_device_intent.putExtra("service_fond_device", result.getDevice());
				sendBroadcast(fond_device_intent);
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			// TODO Auto-generated method stub
			super.onScanFailed(errorCode);
			Log.i(TAG, "scan failed info : " + HelmetToolUtils.getScanError(errorCode));
		}
	};

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Intent conntect_status_change = new Intent();
			conntect_status_change.setAction(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_ACTION);
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				gatt.discoverServices();
				conntect_status_change.putExtra(BluetoothDevice.EXTRA_DEVICE, connect_device);
				HelmetToolUtils.setHelmetDefaultStatusValues(getApplicationContext(),
						HelmetToolUtils.BLE_CONNECTED_DEVICES_TABLE, HelmetToolUtils.BLE_CONNECTED_DEVICES_NAME,
						connect_device.getName());
				HelmetToolUtils.setHelmetDefaultStatusValues(getApplicationContext(),
						HelmetToolUtils.BLE_CONNECTED_DEVICES_TABLE, HelmetToolUtils.BLE_CONNECTED_DEVICES_STATUS, "1");
				mLeScanner.stopScan(mScanCallback);
				Log.i(TAG, "auto connect ble = " + connect_device.getName());
			} else {
				start_scan();
				HelmetToolUtils.setHelmetDefaultStatusValues(getApplicationContext(),
						HelmetToolUtils.BLE_CONNECTED_DEVICES_TABLE, HelmetToolUtils.BLE_CONNECTED_DEVICES_STATUS,
						"-1");
				Log.i(TAG, "auto disconnect ble");
			}
			conntect_status_change.putExtra(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_VALUES, newState);
			sendBroadcast(conntect_status_change);
		};

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (HelmetToolUtils.CHARACTERISTIC_WRITE_UUID.equals(characteristic.getUuid())) {
				gatt.setCharacteristicNotification(characteristic, true);
			}
		};

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			for (BluetoothGattService service : gatt.getServices()) {
				if (HelmetToolUtils.CONNECTION_SERVICE_UUID.equals(service.getUuid())) {
					gatt.setCharacteristicNotification(
							service.getCharacteristic(HelmetToolUtils.CHARACTERISTIC_WRITE_UUID), true);
				}
			}
		};

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			super.onCharacteristicChanged(gatt, characteristic);
			final String charValue = characteristic.getStringValue(0);

			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Intent data_intent = new Intent();
					data_intent.setAction(HelmetToolUtils.BLE_CLIENT_SEND_CONTENTS_ACTION);
					data_intent.putExtra(HelmetToolUtils.BLE_CLIENT_SEND_CONTENTS_DATA, charValue);
					sendBroadcast(data_intent);
				}

			}).start();
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicWrite(gatt, characteristic, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.i(TAG, "write success");
			} else if (status == BluetoothGatt.GATT_FAILURE) {
				Log.i(TAG, "write failed");
			} else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
				Log.i(TAG, "No permission");
			}
		}
	};

	private boolean send_data(byte[] data, int type) {
		if (mGatt != null) {
			return writeCharacteristic(data, type);
		} else {
			Log.i(TAG, "mGatt == null");
			return false;
		}
	}

	/**
	 * Send Regularized data and send a broadcast of the transmission result
	 */
	private boolean writeCharacteristic(byte[] data_content, int type) {

		int index = HelmetToolUtils.HELMET_DEFAULT_NULL_NUM;

		if (mGatt == null) {
			Log.i(TAG, "mGatt not initialized");
			return sendBroadcase("mGatt not initialized", index, false);
		}

		BluetoothGattService service = mGatt.getService(HelmetToolUtils.CONNECTION_SERVICE_UUID);
		if (service == null) {
			Log.w(TAG, "service not initialized");
			return sendBroadcase("service not initialized", index, false);
		}

		BluetoothGattCharacteristic mGattCharacteristic = service
				.getCharacteristic(HelmetToolUtils.CHARACTERISTIC_WRITE_UUID);
		if (mGattCharacteristic == null) {
			Log.w(TAG, "characteristic not initialized");
			return sendBroadcase("characteristic not initialized", index, false);
		}

		mGatt.beginReliableWrite();

		bool_result.clear();

		byte[] buffer_byte = null;
		String head_content = "";

		if (type == HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE) {
			head_content = HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE + ":" + data_content.length + ":";
		} else if (type == HelmetToolUtils.HELMET_DEFAULT_NOTICE_TYPE) {
			head_content = HelmetToolUtils.HELMET_DEFAULT_NOTICE_TYPE + ":" + data_content.length + ":";
		}
		try {
			buffer_byte = head_content.getBytes(HelmetToolUtils.HELMET_DEFAULT_CHARSET);
			ByteArrayInputStream head_bais = new ByteArrayInputStream(buffer_byte);
			buffer_byte = new byte[buffer_byte.length];
			head_bais.read(buffer_byte);
			writeSendCharacteristic(mGatt, mGattCharacteristic, buffer_byte, 1);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(data_content);

		try {
			if (data_content.length < HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT && data_content.length != 0) {
				buffer_byte = new byte[data_content.length];
				bais.read(buffer_byte);
				writeSendCharacteristic(mGatt, mGattCharacteristic, buffer_byte, 2);
			} else if (data_content.length > HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT) {
				buffer_byte = new byte[HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT];
				for (int i = 0; i < data_content.length / HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT; i++) {
					bais.read(buffer_byte);
					writeSendCharacteristic(mGatt, mGattCharacteristic, buffer_byte, 3 + i);
				}
				buffer_byte = new byte[data_content.length % HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT];
				bais.read(buffer_byte);
				writeSendCharacteristic(mGatt, mGattCharacteristic, buffer_byte,
						4 + data_content.length / HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT);
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sendBroadcase("", index, (index == HelmetToolUtils.HELMET_DEFAULT_NULL_NUM) ? true : false);
	}

	private boolean writeSendCharacteristic(BluetoothGatt mGatt, BluetoothGattCharacteristic mGattCharacteristic,
			byte[] data_byte, int index) {
		boolean send_result = false;

		try {
			mGattCharacteristic.setValue(data_byte);
			send_result = mGatt.writeCharacteristic(mGattCharacteristic);
			bool_result.put(index, send_result);
			Thread.sleep(150);
			if (!send_result) {
				return sendBroadcase("", index, true);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sendBroadcase("", HelmetToolUtils.HELMET_DEFAULT_NULL_NUM, false);
	}

	private boolean sendBroadcase(String error_content, int error_type, boolean status) {
		Intent send_result_broad = new Intent();
		send_result_broad.setAction(HelmetToolUtils.BLE_SEND_DATA_RESULT_BROADCASE_ACTION);
		send_result_broad.putExtra(HelmetToolUtils.BLE_SEND_DATA_RESULT_BROADCASE_ERROR, error_content);
		send_result_broad.putExtra(HelmetToolUtils.BLE_SEND_DATA_RESULT_BROADCASE_TYPE, error_type);
		sendBroadcast(send_result_broad);
		return status;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		unregisterReceiver(mReceiver);
	}
}
