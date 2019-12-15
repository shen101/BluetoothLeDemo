package com.shen.bluetoothbledemo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

@SuppressLint("UseSparseArrays")
public class HelmetClientBleConntectService extends Service {

	private static final String TAG = "HelmetClientBleConntectService";
	private ScanResult connect_device;
	private BluetoothGatt mGatt;
	private WifiManager mWifiManager;
	private BluetoothManager mManager;
	private BluetoothLeScanner mLeScanner;
	private String ble_use_content_text = "";
	private String ble_use_content_notice = "";
	private String ble_use_content_bind = "";
	private String ble_use_failed_text = "";
	private static final int SEND_CLIENT_MAC_TO_SERVICE_NUM = 100;
	private static final int SEND_CLIENT_TEXT_DATE_TO_SERVICE_NUM = 101;
	private static final int SEND_CLIENT_NOTICE_DATE_TO_SERVICE_NUM = 102;
	private static final int SEND_CLIENT_ONLY_DATE_TO_SERVICE_NUM = 103;

	private StringBuffer mBuffer_head = new StringBuffer();
	private StringBuffer mBuffer_content = new StringBuffer();
	private boolean isHeadInfo = true;
	private int DateType, DateLength;

	private ArrayList<HelmetSendDateInfo> senddataLists = new ArrayList<HelmetSendDateInfo>();

	private Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SEND_CLIENT_MAC_TO_SERVICE_NUM:
				send_data(ble_use_content_bind, HelmetToolUtils.HELMET_DEFAULT_BIND_TYPE);
				break;
			case SEND_CLIENT_TEXT_DATE_TO_SERVICE_NUM:
				send_data(ble_use_content_text, HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE);
				break;
			case SEND_CLIENT_NOTICE_DATE_TO_SERVICE_NUM:
				send_data(ble_use_content_notice, HelmetToolUtils.HELMET_DEFAULT_NOTICE_TYPE);
				break;
			case SEND_CLIENT_ONLY_DATE_TO_SERVICE_NUM:
				send_data(ble_use_failed_text, HelmetToolUtils.HELMET_DEFAULT_ONLY_TYPE);
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

			if (HelmetToolUtils.BLE_BROADCASE_COMMAND_CLIENT_ACTION.equals(intent.getAction()) && isBluetoothEnable()) {
				int values = intent.getIntExtra(HelmetToolUtils.BLE_BROADCASE_COMMAND_CLIENT_VALUES,
						HelmetToolUtils.HELMET_DEFAULT_NULL_NUM);
				if (HelmetToolUtils.BLE_COMMAND_CODE_START == values) {
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_SCAN == values) {
					start_scan();
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_CLOSE == values) {
					stop_scan();
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_DATA == values) {
					ble_use_content_text = intent.getStringExtra(HelmetToolUtils.BLE_BROADCASE_COMMAND_CLIENT_CONTENT);
					Message.obtain(mhandler, SEND_CLIENT_TEXT_DATE_TO_SERVICE_NUM).sendToTarget();
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_CONNECT == values) {
					connect_device = intent.getParcelableExtra(HelmetToolUtils.BLE_BROADCASE_COMMAND_CLIENT_DEVICE);
					if (connect_device != null) {
						connect_device.getDevice().connectGatt(getApplicationContext(), false, mGattCallback);
					}
				} else if (HelmetToolUtils.BLE_COMMAND_CODE_DISCONNECT == values) {
					disconnection();
				}
			} else if (HelmetToolUtils.BLE_BROADCASE_COMMAND_CLIENT_ACTION.equals(intent.getAction())
					&& !isBluetoothEnable()) {
				HelmetToolUtils.showToast(context, context.getString(R.string.helmet_text_bluetooth_is_no_open));
			} else if (HelmetToolUtils.NOTIFICATION_MESSAGE_ACTION.equals(intent.getAction())) {
				String data_package = intent.getStringExtra(HelmetToolUtils.NOTIFICATION_MESSAGE_PACKAGE_NUM);
				String data_title = intent.getStringExtra(HelmetToolUtils.NOTIFICATION_MESSAGE_TITLE_NUM);
				String data_content = intent.getStringExtra(HelmetToolUtils.NOTIFICATION_MESSAGE_CONTENTS_NUM);
				if (!data_package.equals("com.sohu.inputmethod.sogou")) {
					ble_use_content_notice = (data_package + ":" + data_title + ":" + data_content);
					Message.obtain(mhandler, SEND_CLIENT_NOTICE_DATE_TO_SERVICE_NUM).sendToTarget();
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
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(HelmetToolUtils.BLE_BROADCASE_COMMAND_CLIENT_ACTION);
		mFilter.addAction(HelmetToolUtils.NOTIFICATION_MESSAGE_ACTION);
		registerReceiver(mReceiver, mFilter);

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		ble_use_content_bind = HelmetToolUtils.getMacAddress(mWifiManager);

		start_scan();
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
		mLeScanner = mManager.getAdapter().getBluetoothLeScanner();
		mLeScanner.stopScan(mScanCallback);
		ScanFilter scanFilter = new ScanFilter.Builder()
				.setServiceUuid(new ParcelUuid(HelmetToolUtils.CHARACTERISTIC_SERVICE_UUID)).build();
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
		stop_scan();
		if (mGatt != null) {
			mGatt.close();
			mGatt.disconnect();
		}
	}

	private void saveRemoteMacToDb(ScanResult remote_result) {
		String remote_mac = HelmetToolUtils.byteToMac(remote_result.getScanRecord().getManufacturerSpecificData(0XFF));
		HelmetToolUtils.updateSingleInfoToValues(HelmetClientBleConntectService.this,
				HelmetInfoContactProvider.SERVICE_HELMET_MAC, remote_mac);
	}

	private ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			// TODO Auto-generated method stub
			super.onScanResult(callbackType, result);

			String fond_helmet_mac = HelmetToolUtils
					.byteToMac(result.getScanRecord().getManufacturerSpecificData(0XFF));

			String localSavedAddress = HelmetToolUtils.getInfoFromSingleValueToString(getApplicationContext(),
					HelmetInfoContactProvider.SERVICE_HELMET_MAC);

			if (fond_helmet_mac.equals(localSavedAddress)) {
				Log.i(TAG, "auto connect to helmet");
				connect_device = result;
				result.getDevice().connectGatt(getApplicationContext(), false, mGattCallback);
				Intent fond_device_intent = new Intent();
				fond_device_intent.setAction(HelmetToolUtils.BLE_SCAN_FOND_ACTION);
				fond_device_intent.putExtra("service_fond_result", result);
				sendBroadcast(fond_device_intent);
				stop_scan();
			} else {
				if (result.getDevice().getName() == null) {
					Log.i(TAG, "fond helmet device : " + result.getDevice().getAddress());
				} else {
					Log.i(TAG, "fond helmet device : " + result.getDevice().getName());
				}
				Intent fond_device_intent = new Intent();
				fond_device_intent.setAction(HelmetToolUtils.BLE_SCAN_FOND_ACTION);
				fond_device_intent.putExtra("service_fond_result", result);
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
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorWrite(gatt, descriptor, status);
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorRead(gatt, descriptor, status);
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Log.i(TAG, "onConnectionStateChange newState = " + newState);
			Intent conntect_status_change = new Intent();
			conntect_status_change.setAction(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_ACTION);
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				mGatt = gatt;
				conntect_status_change.putExtra(BluetoothDevice.EXTRA_DEVICE, connect_device);
				saveRemoteMacToDb(connect_device);
				mLeScanner.stopScan(mScanCallback);
				gatt.discoverServices();
			} else {
				start_scan();
			}

			conntect_status_change.putExtra(HelmetToolUtils.BLE_SERVICE_CONNECTED_CHANGE_VALUES, newState);
			sendBroadcast(conntect_status_change);
		};

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

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (BluetoothGatt.GATT_SUCCESS == status) {
				Message.obtain(mhandler, SEND_CLIENT_MAC_TO_SERVICE_NUM).sendToTarget();
			}
		};

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			super.onCharacteristicChanged(gatt, characteristic);
			final String charContent = new String(characteristic.getValue());
			analyticalData(characteristic.getValue());
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Intent data_intent = new Intent();
					data_intent.setAction(HelmetToolUtils.BLE_CLIENT_SEND_CONTENTS_ACTION);
					data_intent.putExtra(HelmetToolUtils.BLE_CLIENT_SEND_CONTENTS_DATA, charContent);
					sendBroadcast(data_intent);
				}
			}).start();
		}
	};

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

	public void setCharacteristicNotification(UUID serviceUUID, UUID characteristicUUID, boolean enabled) {
		if (mGatt == null) {
			Log.i(TAG, "BluetoothAdapter not initialized");
			return;
		}
		try {
			BluetoothGattService service = mGatt.getService(serviceUUID);
			BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);

			mGatt.setCharacteristicNotification(characteristic, enabled);

			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(HelmetToolUtils.CHARACTERISTIC_DESCRIPTOR_UUID);
			if (descriptor == null) {
				Log.i(TAG, "descriptor is null");
				return;
			}
			descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
					: BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			mGatt.writeDescriptor(descriptor);
		} catch (Exception e) {
			e.printStackTrace();
			Log.i(TAG, "e:" + e);
		}
	}

	private boolean send_data(String data, int type) {

		setCharacteristicNotification(HelmetToolUtils.CHARACTERISTIC_SERVICE_UUID,
				HelmetToolUtils.CHARACTERISTIC_READ_WRITE_UUID, true);
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
	private boolean writeCharacteristic(String data_content, int type) {

		int index = HelmetToolUtils.HELMET_DEFAULT_NULL_NUM;

		if (mGatt == null) {
			Log.i(TAG, "mGatt not initialized");
			return sendBroadcase("mGatt not initialized", index, false);
		}

		BluetoothGattService service = mGatt.getService(HelmetToolUtils.CHARACTERISTIC_SERVICE_UUID);
		if (service == null) {
			Log.w(TAG, "service not initialized");
			return sendBroadcase("service not initialized", index, false);
		}

		BluetoothGattCharacteristic mGattCharacteristic = service
				.getCharacteristic(HelmetToolUtils.CHARACTERISTIC_READ_WRITE_UUID);
		if (mGattCharacteristic == null) {
			Log.w(TAG, "characteristic not initialized");
			return sendBroadcase("characteristic not initialized", index, false);
		}

		if (type != HelmetToolUtils.HELMET_DEFAULT_ONLY_TYPE) { // contains type
			String head_data = "";
			if (type == HelmetToolUtils.HELMET_DEFAULT_BIND_TYPE) {
				head_data = generateHeadInfo(HelmetToolUtils.HELMET_DEFAULT_BIND_TYPE, data_content);
			} else if (type == HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE) {
				head_data = generateHeadInfo(HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE, data_content);
			} else if (type == HelmetToolUtils.HELMET_DEFAULT_NOTICE_TYPE) {
				head_data = generateHeadInfo(HelmetToolUtils.HELMET_DEFAULT_NOTICE_TYPE, data_content);
			}

			// send head info
			sendContentDate(mGattCharacteristic, head_data);
		} else {
			// not contains type
		}

		// send main info
		sendContentDate(mGattCharacteristic, data_content);
		return sendBroadcase("", index, (index == HelmetToolUtils.HELMET_DEFAULT_NULL_NUM) ? true : false);
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

	private void sendContentDate(BluetoothGattCharacteristic mGattCharacteristic, String date_content) {

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
				writeSendCharacteristic(mGatt, mGattCharacteristic, temp_0, 0);
			} else if (date_length >= HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT) {
				for (int i = 0; i < cycles_num; i++) {

					buffer_byte = new byte[HelmetToolUtils.HELMET_DEFAULT_SEND_SPLIT_LIMIT];
					temp_tear_bais.read(buffer_byte);
					String temp_content = (new String(buffer_byte)) + calculateCyclesCount(i) + i;
					byte[] temp_buffer = temp_content.getBytes(HelmetToolUtils.HELMET_DEFAULT_CHARSET);

					writeSendCharacteristic(mGatt, mGattCharacteristic, temp_buffer, i);
				}
				if (remainder_num != 0) {
					buffer_byte = new byte[remainder_num];
					temp_tear_bais.read(buffer_byte);
					String temp_end = (new String(buffer_byte)) + calculateCyclesCount(cycles_num) + cycles_num;
					byte[] temp_buffer_end = temp_end.getBytes(HelmetToolUtils.HELMET_DEFAULT_CHARSET);

					writeSendCharacteristic(mGatt, mGattCharacteristic, temp_buffer_end, cycles_num);
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

	private int count_num = 0;

	private boolean writeSendCharacteristic(BluetoothGatt mGatt, BluetoothGattCharacteristic mGattCharacteristic,
			byte[] data_byte, int index) {
		boolean send_result = false;

		try {
			mGattCharacteristic.setValue(data_byte);
			send_result = mGatt.writeCharacteristic(mGattCharacteristic);
			HelmetSendDateInfo infos = new HelmetSendDateInfo();
			infos.setResult(send_result);
			infos.setIndex(index);
			infos.setData(data_byte);
			senddataLists.add(infos);
			Thread.sleep(150);
			if (!send_result) {
				if (count_num >= 10) {
					count_num = 0;
					return sendBroadcase("", index, true);
				} else {
					count_num++;
					Message.obtain(mhandler, SEND_CLIENT_ONLY_DATE_TO_SERVICE_NUM).sendToTarget();
					return sendBroadcase("", index, true);
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sendBroadcase("", HelmetToolUtils.HELMET_DEFAULT_NULL_NUM, false);
	}

	private boolean sendBroadcase(String error_content, int error_type, boolean status) {
		Intent send_result_broad = new Intent();
		send_result_broad.setAction(HelmetToolUtils.BLE_BROADCASE_SEND_DATA_RESULT_ACTION);
		send_result_broad.putExtra(HelmetToolUtils.BLE_BROADCASE_SEND_DATA_RESULT_ERROR, error_content);
		send_result_broad.putExtra(HelmetToolUtils.BLE_BROADCASE_SEND_DATA_RESULT_TYPE, error_type);
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
