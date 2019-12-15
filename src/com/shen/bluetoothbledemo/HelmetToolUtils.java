package com.shen.bluetoothbledemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class HelmetToolUtils {

	private static final String TAG = "HelmetToolUtils";

	// Service UUID
	public static UUID CHARACTERISTIC_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
	// Notice
	public static UUID CHARACTERISTIC_NOTICE_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
	// Descriptor
	public static UUID CHARACTERISTIC_DESCRIPTOR_UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
	// Read-write characteristic
	public static UUID CHARACTERISTIC_READ_WRITE_UUID = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb");

	// BLE BlueTooth client action
	public static final String BLE_BROADCASE_COMMAND_CLIENT_ACTION = "com.yiyang.helmet.BLE_BROADCASE_COMMAND_CLIENT_ACTION";
	public static final String BLE_BROADCASE_COMMAND_CLIENT_VALUES = "ble_broadcase_command_client_values";
	public static final String BLE_BROADCASE_COMMAND_CLIENT_CONTENT = "ble_broadcase_command_client_content";
	public static final String BLE_BROADCASE_COMMAND_CLIENT_TYPE = "ble_broadcase_command_client_type";
	public static final String BLE_BROADCASE_COMMAND_CLIENT_DEVICE = "ble_broadcase_command_client_device";

	// BLE BlueTooth service action
	public static final String BLE_BROADCASE_COMMAND_SERVICE_ACTION = "com.yiyang.helmet.BLE_BROADCASE_COMMAND_SERVICE_ACTION";
	public static final String BLE_BROADCASE_COMMAND_SERVICE_STATUS = "ble_broadcase_command_service_status";
	public static final String BLE_BROADCASE_COMMAND_SERVICE_VALUES = "ble_broadcase_command_service_values";
	public static final String BLE_BROADCASE_COMMAND_SERVICE_CONTENTS = "ble_broadcase_command_service_contents";
	public static final String BLE_BROADCASE_COMMAND_SERVICE_TYPE = "ble_broadcase_command_service_type";
	public static final String BLE_BROADCASE_COMMAND_SERVICE = "ble_broadcase_command_service";

	// BLE send data state
	public static final String BLE_BROADCASE_SEND_DATA_RESULT_ACTION = "ble_send_data_result_action";
	public static final String BLE_BROADCASE_SEND_DATA_RESULT_ERROR = "ble_send_data_result_error";
	public static final String BLE_BROADCASE_SEND_DATA_RESULT_TYPE = "ble_send_data_result_type";

	// BLE service connect state
	public static final String BLE_SERVICE_CONNECTED_CHANGE_ACTION = "com.yiyang.helmet.BLE_SERVICE_CHANGE";
	public static final String BLE_SERVICE_CONNECTED_CHANGE_VALUES = "ble_broadcase_service_change";
	public static final String BLE_SERVICE_CONNECTED_CHANGE_STATUS = "ble_broadcase_service_status";

	public static final String BLE_SERVICE_ADVERTISE_CHANGE_ACTION = "com.yiyang.helmet.BLE_ADV_SERVICE_CHANGE";
	public static final String BLE_SERVICE_ADVERTISE_CHANGE_STATUS = "ble_broadcase_adv_service_status";

	public static final String BLE_SERVICE_SEND_CONTENTS_ACTION = "com.yiyang.helmet.BLE_SEND_SERVICE";
	public static final String BLE_SERVICE_SEND_CONTENTS_DATA = "ble_broadcase_send_service_data";

	public static final String BLE_CLIENT_SEND_CONTENTS_ACTION = "com.yiyang.helmet.BLE_SEND_CLIENT";
	public static final String BLE_CLIENT_SEND_CONTENTS_DATA = "ble_broadcase_send_client_data";

	// BLE connected devices
	public static final String BLE_CONNECTED_DEVICES_TABLE = "ble_connected_service_table";
	public static final String BLE_CONNECTED_DEVICES_NAME = "ble_connected_service_name";
	public static final String BLE_CONNECTED_DEVICES_STATUS = "ble_connected_service_status";

	// BLE Scan state
	public static final String BLE_SCAN_FOND_ACTION = "ble_scan_fond_action";

	// Notice push apps
	public static final String SAVE_PUSH_NOTIFICATION_APPS_TABLE = "notice_app_table";
	public static final String SAVE_PUSH_NOTIFICATION_APPS_VALUES = "notice_app_values";

	// Notice
	public static final String NOTIFICATION_MESSAGE_ACTION = "com.yiyang.notification.NOTIFICATION_MESSAGE";
	public static final String NOTIFICATION_MESSAGE_PACKAGE_NUM = "yiyang_notification_package";
	public static final String NOTIFICATION_MESSAGE_TITLE_NUM = "yiyang_notification_title";
	public static final String NOTIFICATION_MESSAGE_CONTENTS_NUM = "yiyang_notification_contents";

	public static final String HELMET_DEFAULT_CHARSET = "utf-8";
	public static int HELMET_DEFAULT_SEND_SPLIT_LIMIT = 16;
	public static final int HELMET_DEFAULT_NULL_NUM = -1;

	public static final boolean HELMET_DEFAULT_BOOLEAN_NUM = false;

	public static final int HELMET_DEFAULT_ONLY_TYPE = -1;
	public static final int HELMET_DEFAULT_BIND_TYPE = 0;
	public static final int HELMET_DEFAULT_TEXT_TYPE = 1;
	public static final int HELMET_DEFAULT_NOTICE_TYPE = 2;

	public static final int OPEN_BLUETOOTH_NUM = 10;
	public static final int REFLASH_STATUS_SUCCESSFUL_NUM = 11;
	public static final int REFLASH_STATUS_FAILED_NUM = 12;

	/**
	 * START BLE SERVICE
	 */
	public static final int BLE_COMMAND_CODE_START = 1;
	/**
	 * SCAN BLE DEVICES
	 */
	public static final int BLE_COMMAND_CODE_SCAN = 2;
	/**
	 * SEND BLE DATA
	 */
	public static final int BLE_COMMAND_CODE_DATA = 3;
	/**
	 * CONNECT BLE DEVICE
	 */
	public static final int BLE_COMMAND_CODE_CONNECT = 4;
	/**
	 * DISCONNECT BLE DEVICE
	 */
	public static final int BLE_COMMAND_CODE_DISCONNECT = 5;
	/**
	 * CLOSE BLE SERVICE
	 */
	public static final int BLE_COMMAND_CODE_CLOSE = 6;

	private static Toast toast;

	public static void showToast(Context context, String content) {
		if (toast == null) {
			toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
		} else {
			toast.setText(content);
		}
		toast.show();
	}

	public static int getAdvError(int error) {

		if (error == AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE) {
			return R.string.system_text_adv_error_one;
		} else if (error == AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
			return R.string.system_text_adv_error_two;
		} else if (error == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED) {
			return R.string.system_text_adv_error_three;
		} else if (error == AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
			return R.string.system_text_adv_error_five;
		}
		return R.string.system_text_adv_error_four;
	}

	public static int getScanError(int error) {
		if (error == ScanCallback.SCAN_FAILED_ALREADY_STARTED) {
			return R.string.system_text_scan_error_one;
		} else if (error == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
			return R.string.system_text_scan_error_two;
		} else if (error == ScanCallback.SCAN_FAILED_INTERNAL_ERROR) {
			return R.string.system_text_scan_error_three;
		} else if (error == ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED) {
			return R.string.system_text_scan_error_four;
		}
		return R.string.system_text_scan_error_five;
	}

	public static int getConnectionStatus(int newsattus) {
		return newsattus == 2 ? R.string.system_text_connect_success : R.string.system_text_connect_failed;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	/**
	 * Determine if a service is already running
	 */
	public static boolean isServiceExisted(Context context, String className) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);

		if (!(serviceList.size() > 0)) {
			return false;
		}

		for (int i = 0; i < serviceList.size(); i++) {
			ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
			ComponentName serviceName = serviceInfo.service;
			if (serviceName.getClassName().equals(className)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	public static void initBleService(Activity mActivity, Class activityclass) {
		// TODO Auto-generated method stub
		Intent start_ble_intent = new Intent();
		start_ble_intent.putExtra(BLE_BROADCASE_COMMAND_CLIENT_VALUES, BLE_COMMAND_CODE_START);
		if (activityclass.getName().equals(HelmetClientBleConntectService.class.getName())) {
			start_ble_intent.setAction(BLE_BROADCASE_COMMAND_CLIENT_ACTION);
			start_ble_intent.setClass(mActivity, HelmetClientBleConntectService.class);
		} else {
			start_ble_intent.setAction(BLE_BROADCASE_COMMAND_SERVICE_ACTION);
			start_ble_intent.setClass(mActivity, HelmetServicetBleConntectService.class);
		}
		if (!HelmetToolUtils.isServiceExisted(mActivity, activityclass.getName())) {
			mActivity.startService(start_ble_intent);
		}
	}

	public static void scanBleDeviceIntent(Context mContext) {
		Intent scan_intent = new Intent();
		scan_intent.setAction(BLE_BROADCASE_COMMAND_CLIENT_ACTION);
		scan_intent.putExtra(BLE_BROADCASE_COMMAND_CLIENT_VALUES, BLE_COMMAND_CODE_SCAN);
		mContext.sendBroadcast(scan_intent);
	}

	public static void stopScanBleDeviceIntent(Context mContext) {
		Intent stop_intent = new Intent();
		stop_intent.setAction(BLE_BROADCASE_COMMAND_CLIENT_ACTION);
		stop_intent.putExtra(BLE_BROADCASE_COMMAND_CLIENT_VALUES, BLE_COMMAND_CODE_CLOSE);
		mContext.sendBroadcast(stop_intent);
	}

	public static void sendBleData(Context mContext, String contents) {
		Intent send_data_intent = new Intent();
		send_data_intent.setAction(BLE_BROADCASE_COMMAND_CLIENT_ACTION);
		send_data_intent.putExtra(BLE_BROADCASE_COMMAND_CLIENT_VALUES, BLE_COMMAND_CODE_DATA);
		send_data_intent.putExtra(BLE_BROADCASE_COMMAND_CLIENT_TYPE, HELMET_DEFAULT_TEXT_TYPE);
		send_data_intent.putExtra(BLE_BROADCASE_COMMAND_CLIENT_CONTENT, contents);
		mContext.sendBroadcast(send_data_intent);
	}

	public static void connectRemoteDeviceIntent(Context mContext, ScanResult remoteResult) {
		Intent connect_intent = new Intent();
		connect_intent.setAction(BLE_BROADCASE_COMMAND_CLIENT_ACTION);
		connect_intent.putExtra(BLE_BROADCASE_COMMAND_CLIENT_VALUES, BLE_COMMAND_CODE_CONNECT);
		connect_intent.putExtra(BLE_BROADCASE_COMMAND_CLIENT_DEVICE, remoteResult);
		mContext.sendBroadcast(connect_intent);
	}

	public static void disconnectRemoteDeviceIntent(Context mContext) {
		Intent disconnect_intent = new Intent();
		disconnect_intent.setAction(BLE_BROADCASE_COMMAND_CLIENT_ACTION);
		disconnect_intent.putExtra(BLE_BROADCASE_COMMAND_CLIENT_VALUES, BLE_COMMAND_CODE_DISCONNECT);
		mContext.sendBroadcast(disconnect_intent);
	}

	public static void startBleServiceAdvertising(Context mContext) {
		Intent start_intent = new Intent();
		start_intent.setAction(BLE_BROADCASE_COMMAND_SERVICE_ACTION);
		start_intent.putExtra(BLE_BROADCASE_COMMAND_SERVICE, BLE_COMMAND_CODE_START);
		mContext.sendBroadcast(start_intent);
	}

	public static void stopBleServiceAdvertising(Context mContext) {
		Intent stop_intent = new Intent();
		stop_intent.setAction(BLE_BROADCASE_COMMAND_SERVICE_ACTION);
		stop_intent.putExtra(BLE_BROADCASE_COMMAND_SERVICE, BLE_COMMAND_CODE_CLOSE);
		mContext.sendBroadcast(stop_intent);
	}

	public static void sendBleServiceData(Context mContext, String contents,int type) {
		Intent send_data_intent = new Intent();
		send_data_intent.setAction(BLE_BROADCASE_COMMAND_SERVICE_ACTION);
		send_data_intent.putExtra(BLE_BROADCASE_COMMAND_SERVICE, BLE_COMMAND_CODE_DATA);
		if(HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE == type){
			send_data_intent.putExtra(BLE_BROADCASE_COMMAND_SERVICE_TYPE, HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE);			
		}
		send_data_intent.putExtra(BLE_BROADCASE_COMMAND_SERVICE_CONTENTS, contents);
		mContext.sendBroadcast(send_data_intent);
	}

	public static String getMacAddress(WifiManager mManager) {
		String mac = "02:00:00:00:00:00";
		if (Build.VERSION.SDK_INT < 23) {
			mac = getMacDefault(mManager);
		} else if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 27) {
			mac = getMacFromFile();
		} else if (Build.VERSION.SDK_INT >= 27) {
			mac = getMacFromHardware();
		}
		Log.i("BTTransmissionService", "The MAC address of this machine is : " + mac);
		return mac;
	}

	/*
	 * > Android 5.1
	 */
	private static String getMacFromHardware() {
		try {
			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
				if (!nif.getName().equalsIgnoreCase("wlan0"))
					continue;

				byte[] macBytes = nif.getHardwareAddress();
				if (macBytes == null) {
					return "";
				}

				StringBuilder res1 = new StringBuilder();
				for (byte b : macBytes) {
					res1.append(String.format("%02X:", b));
				}

				if (res1.length() > 0) {
					res1.deleteCharAt(res1.length() - 1);
				}
				return res1.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "02:00:00:00:00:00";
	}

	/**
	 * < Android 6.0
	 */
	private static String getMacDefault(WifiManager mManager) {
		String default_mac = "02:00:00:00:00:00";

		if (mManager == null) {
			return default_mac;
		} else if (mManager.getConnectionInfo() == null) {
			return default_mac;
		} else if (mManager.getConnectionInfo().getMacAddress() == null) {
			return default_mac;
		} else if (!TextUtils.isEmpty(mManager.getConnectionInfo().getMacAddress())) {
			return mManager.getConnectionInfo().getMacAddress().toLowerCase(Locale.ENGLISH);
		} else {
			return default_mac;
		}
	}

	/**
	 * == Android 6.0
	 */
	@SuppressWarnings("resource")
	private static String getMacFromFile() {
		String WifiAddress = "02:00:00:00:00:00";
		try {
			WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return WifiAddress;
	}

	public static byte[] MacTobyte(String mac) {
		char[] macs = mac.replaceAll(":", "").toCharArray();
		int j = 0;
		byte[] bmacs = new byte[6];
		for (int i = 0; i < bmacs.length; i++) {
			bmacs[i] = (byte) ((charToHex(macs[j]) << 4) | (charToHex(macs[j + 1])));
			j += 2;
		}
		return bmacs;
	}

	public static String byteToMac(byte[] bytes) {
		String mac = "";
		for (int i = 0; i < bytes.length; i++) {
			mac = mac + intTochar(((bytes[i] & 0xf0) >> 4)) + intTochar((bytes[i] & 0xf));
			if (i < (bytes.length - 1)) {
				mac = mac + ":";
			}
		}
		return mac;
	}

	/*
	 * int to char
	 */
	private static char intTochar(int a) {
		char c = '0';

		switch (a) {
		case 0:
			c = '0';
			break;
		case 1:
			c = '1';
			break;
		case 2:
			c = '2';
			break;
		case 3:
			c = '3';
			break;
		case 4:
			c = '4';
			break;
		case 5:
			c = '5';
			break;
		case 6:
			c = '6';
			break;
		case 7:
			c = '7';
			break;
		case 8:
			c = '8';
			break;
		case 9:
			c = '9';
			break;
		case 10:
			c = 'A';
			break;
		case 11:
			c = 'B';
			break;
		case 12:
			c = 'C';
			break;
		case 13:
			c = 'D';
			break;
		case 14:
			c = 'E';
			break;
		case 15:
			c = 'F';
			break;
		}
		return c;
	}

	/**
	 * char to hex
	 */
	private static int charToHex(char c) {
		int res = -1;
		switch (c) {
		case '0':
			res = 0x0;
			break;
		case '1':
			res = 0x1;
			break;
		case '2':
			res = 0x2;
			break;
		case '3':
			res = 0x3;
			break;
		case '4':
			res = 0x4;
			break;
		case '5':
			res = 0x5;
			break;
		case '6':
			res = 0x6;
			break;
		case '7':
			res = 0x7;
			break;
		case '8':
			res = 0x8;
			break;
		case '9':
			res = 0x9;
			break;
		case 'a':
		case 'A':
			res = 0xa;
			break;
		case 'b':
		case 'B':
			res = 0xb;
			break;
		case 'c':
		case 'C':
			res = 0xc;
			break;
		case 'd':
		case 'D':
			res = 0xd;
			break;
		case 'e':
		case 'E':
			res = 0xe;
			break;
		case 'f':
		case 'F':
			res = 0xf;
			break;
		}
		return res;
	}

	public static void initDBValues(Context context) {
		ContentValues base_values = new ContentValues();
		base_values.put(HelmetInfoContactProvider.FIRST_INTO, "1");
		base_values.put(HelmetInfoContactProvider.SOS_CONTACT_NAME, "");
		base_values.put(HelmetInfoContactProvider.SOS_CONTACT_NUMBER, "");
		base_values.put(HelmetInfoContactProvider.SOS_CONTACT_EMAIL, "");
		base_values.put(HelmetInfoContactProvider.IS_UBIND, "");
		base_values.put(HelmetInfoContactProvider.PLAYING_MUSIC_POSITION, "-1");
		base_values.put(HelmetInfoContactProvider.PLAYING_MUSIC_PATH, "");
		base_values.put(HelmetInfoContactProvider.FRONT_PHOTO_CAMERA, "1300");
		base_values.put(HelmetInfoContactProvider.REAR_PHOTO_CAMERA, "500");
		base_values.put(HelmetInfoContactProvider.FRONT_VIDEO_CAMERA, "1080");
		base_values.put(HelmetInfoContactProvider.REAR_VIDEO_CAMERA, "720");
		base_values.put(HelmetInfoContactProvider.SERVICE_HELMET_MAC, "02:00:00:00:00:00");
		base_values.put(HelmetInfoContactProvider.CLIENT_CONTROL_MAC, "02:00:00:00:00:00");
		base_values.put(HelmetInfoContactProvider.CREATE_TIME, new Date().getTime());
		context.getContentResolver().insert(HelmetInfoContactProvider.CONTENT_URL, base_values);
	}

	/**
	 * system values (int)
	 */
	public static void updateSingleInfoToValues(Context context, String db_value, String input_value) {
		ContentValues values = new ContentValues();
		values.put(db_value, input_value);
		values.put(HelmetInfoContactProvider.CREATE_TIME, new Date().getTime());
		context.getContentResolver().update(HelmetInfoContactProvider.CONTENT_URL, values, "_id=?",
				new String[] { "1" });
	}

	public static String getInfoFromSingleValueToString(Context context, String db_values) {
		Cursor cursor = context.getContentResolver().query(HelmetInfoContactProvider.CONTENT_URL, null, null, null,
				null);
		String values = "";
		try {
			if (cursor != null && cursor.moveToFirst()) {
				do {
					values = cursor.getString(cursor.getColumnIndex(db_values));
				} while (cursor.moveToNext());
			}
			return values;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return values;
	}

	public static int getInfoFromSingleValueToInt(Context context, String db_values) {
		Cursor cursor = context.getContentResolver().query(HelmetInfoContactProvider.CONTENT_URL, null, null, null,
				null);
		int values = -1;
		try {
			if (cursor != null && cursor.moveToFirst()) {
				do {
					values = cursor.getInt(cursor.getColumnIndex(db_values));
				} while (cursor.moveToNext());
			}
			return values;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return values;
	}

	public static Queue<byte[]> splitPacketFor18Byte(byte[] data) {
		Queue<byte[]> dataInfoQueue = new LinkedList<>();
		if (data != null) {
			int index = 0;
			do {
				byte[] surplusData = new byte[data.length - index];
				byte[] currentData;
				System.arraycopy(data, index, surplusData, 0, data.length - index);
				if (surplusData.length <= 18) {
					currentData = new byte[surplusData.length];
					System.arraycopy(surplusData, 0, currentData, 0, surplusData.length);
					index += surplusData.length;
				} else {
					currentData = new byte[18];
					System.arraycopy(data, index, currentData, 0, 18);
					index += 18;
				}
				Log.e("mcy_�ְ�����", "" + Arrays.toString(currentData));
				dataInfoQueue.offer(currentData);
			} while (index < data.length);
		}
		return dataInfoQueue;
	}
}
