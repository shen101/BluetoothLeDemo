package com.shen.bluetoothbledemo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.net.wifi.WifiManager;
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

	private WifiManager mWifiManager;

	private StringBuffer mBuffer_head = new StringBuffer();
	private StringBuffer mBuffer_content = new StringBuffer();
	private boolean isHeadInfo = true;
	private int DateType, DateLength;

	private String send_contents = "";
	private int send_contents_type = -1;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (HelmetToolUtils.BLE_BROADCASE_COMMAND_SERVICE_ACTION.equals(intent.getAction())) {
				int values = intent.getIntExtra(HelmetToolUtils.BLE_BROADCASE_COMMAND_SERVICE,
						HelmetToolUtils.HELMET_DEFAULT_NULL_NUM);
				if (HelmetToolUtils.BLE_COMMAND_CODE_START == values) {
					startAdvertising();
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_CLOSE == values) {
					stopAdvertising();
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_DATA == values) {
					send_contents = intent.getStringExtra(HelmetToolUtils.BLE_BROADCASE_COMMAND_SERVICE_CONTENTS);
					send_contents_type = intent.getIntExtra(HelmetToolUtils.BLE_BROADCASE_COMMAND_SERVICE_TYPE, -1);
					sendServiceDate(send_contents, send_contents_type);
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
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		mAdapter = mManager.getAdapter();

		mLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();
		mGattServer = mManager.openGattServer(getApplicationContext(), mGattServerCallback);
		initGattServer();

		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(HelmetToolUtils.BLE_BROADCASE_COMMAND_SERVICE_ACTION);
		mFilter.addAction(HelmetToolUtils.NOTIFICATION_MESSAGE_ACTION);
		registerReceiver(mReceiver, mFilter);
		return super.onStartCommand(intent, flags, startId);
	}

	private void initGattServer() {
		// TODO Auto-generated method stub
		BluetoothGattService service = new BluetoothGattService(HelmetToolUtils.CHARACTERISTIC_SERVICE_UUID,
				BluetoothGattService.SERVICE_TYPE_PRIMARY);

		int writeProperty = BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_INDICATE
				| BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE
				| BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
		int writePermission = BluetoothGattCharacteristic.PERMISSION_READ
				| BluetoothGattCharacteristic.PERMISSION_WRITE;
		BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
				HelmetToolUtils.CHARACTERISTIC_READ_WRITE_UUID, writeProperty, writePermission);

		service.addCharacteristic(writeCharacteristic);
		mGattServer.addService(service);
	}

	private void startAdvertising() {
		if (mLeAdvertiser == null) {
			return;
		}

		AdvertiseSettings.Builder set_builder = new AdvertiseSettings.Builder();
		set_builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
		set_builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
		set_builder.setConnectable(true);
		set_builder.setTimeout(0);

		AdvertiseData.Builder adv_builder = new AdvertiseData.Builder();
		byte[] mac = HelmetToolUtils.MacTobyte(HelmetToolUtils.getMacAddress(mWifiManager));
		adv_builder.setIncludeDeviceName(true);
		adv_builder.addServiceUuid(new ParcelUuid(HelmetToolUtils.CHARACTERISTIC_SERVICE_UUID));
		adv_builder.addManufacturerData(0XFF, mac);
		mLeAdvertiser.startAdvertising(set_builder.build(), adv_builder.build(), mAdvertiseCallback);
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
			Log.i(TAG, "onConnectionStateChange newState = " + newState);
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
			mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
					device.getName().getBytes());
		}

		@Override
		public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
				BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
				final byte[] value) {
			// TODO Auto-generated method stub
			super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset,
					value);
			mGattCharacteristic = characteristic;
			analyticalData(value);
			Intent data_intent = new Intent();
			data_intent.setAction(HelmetToolUtils.BLE_SERVICE_SEND_CONTENTS_ACTION);
			data_intent.putExtra(HelmetToolUtils.BLE_SERVICE_SEND_CONTENTS_DATA, value);
			sendBroadcast(data_intent);
			mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
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

	private String generateHeadInfo(int type, String date) {
		return "{dateType:" + type + ",dateLength:" + calculateTotalDateLength(date) + "}";
	}

	private int calculateTotalDateLength(String date) {
		if (date.length() == 0) {
			return 0;
		}
		int date_divisor_num = date.length() / HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT;
		int date_remainder_num = date.length() % HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT;

		if (date_divisor_num == 0) {
			return date.length();
		} else {
			if (date_remainder_num == 0) {
				return HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT * date_divisor_num;
			} else {
				return HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT * date_divisor_num + date_remainder_num;
			}
		}
	}

	private void sendServiceDate(String date_content, int type) {

		if (type != HelmetToolUtils.HELMET_DEFAULT_ONLY_TYPE) { // contains type
			String head_data = "";
			if (type == HelmetToolUtils.HELMET_DEFAULT_BIND_TYPE) {
				head_data = generateHeadInfo(HelmetToolUtils.HELMET_DEFAULT_BIND_TYPE, date_content);
			} else if (type == HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE) {
				head_data = generateHeadInfo(HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE, date_content);
			} else if (type == HelmetToolUtils.HELMET_DEFAULT_NOTICE_TYPE) {
				head_data = generateHeadInfo(HelmetToolUtils.HELMET_DEFAULT_NOTICE_TYPE, date_content);
			}

			// send head info
			dismantAndSendDate(head_data);
		} else {
			// not contains type
		}

		// send main info
		dismantAndSendDate(date_content);

	}

	private void dismantAndSendDate(String date_content) {
		byte[] buffer_byte = null;
		int date_length = date_content.length();
		if (date_length == 0) {
			return;
		}
		int cycles_num = date_length / HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT;
		int remainder_num = date_length % HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT;

		try {
			ByteArrayInputStream temp_tear_bais = new ByteArrayInputStream(
					date_content.getBytes(HelmetToolUtils.HELMET_DEFAULT_CHARSET));

			if (date_length < HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT) {
				byte[] temp_0 = (date_content + "00").getBytes(HelmetToolUtils.HELMET_DEFAULT_CHARSET);
				sendData(new String(temp_0));
			} else if (date_length >= HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT) {
				for (int i = 0; i < cycles_num; i++) {

					buffer_byte = new byte[HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT];
					temp_tear_bais.read(buffer_byte);
					String temp_content = (new String(buffer_byte)) + calculateCyclesCount(i) + i;
					byte[] temp_buffer = temp_content.getBytes(HelmetToolUtils.HELMET_DEFAULT_CHARSET);
					sendData(new String(temp_buffer));
				}
				if (remainder_num != 0) {
					buffer_byte = new byte[remainder_num];
					temp_tear_bais.read(buffer_byte);
					String temp_end = (new String(buffer_byte)) + calculateCyclesCount(cycles_num) + cycles_num;
					byte[] temp_buffer_end = temp_end.getBytes(HelmetToolUtils.HELMET_DEFAULT_CHARSET);
					sendData(new String(temp_buffer_end));
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Do not consider loops greater than 1000 (the client will prohibit
	 * exceeding this limit)
	 */
	private String calculateCyclesCount(int current_num) {
		if (current_num < 10) {
			return "0";
		} else {
			return "";
		}
	}

	private void analyticalData(byte[] date) {

		try {
			String temp_message = new String(date, HelmetToolUtils.HELMET_DEFAULT_CHARSET);
			if (isHeadInfo) {
				mBuffer_head.append(temp_message.substring(0, date.length - 2));

				JSONObject lenjsonObject = new JSONObject(mBuffer_head.toString());
				DateType = lenjsonObject.optInt("dateType");
				DateLength = lenjsonObject.optInt("dateLength");
				isHeadInfo = false;
			} else {
				mBuffer_content.append(temp_message.substring(0, date.length - 2));
				isHeadInfo = false;
				if (mBuffer_content.length() == DateLength) {
					Log.i(TAG, "mBuffer_head = " + mBuffer_head.toString() + ",  Date_content = "
							+ mBuffer_content.toString());
					mBuffer_head.setLength(0);
					mBuffer_content.setLength(0);
					isHeadInfo = true;
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isHeadInfo = true;
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
