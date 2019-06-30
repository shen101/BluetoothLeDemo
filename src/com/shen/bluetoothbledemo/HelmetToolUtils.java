package com.shen.bluetoothbledemo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.ScanCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class HelmetToolUtils {
	// Service UUID
	public static UUID CONNECTION_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
	// Read-only
	public static UUID CHARACTERISTIC_READ_UUID = UUID.fromString("275348FB-C14D-4FD5-B434-7C3F351DEA5F");
	// Read-write
	public static UUID CHARACTERISTIC_WRITE_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
	// Read-write characteristic
	public static UUID CHARACTERISTIC_DEMO_UUID = UUID.fromString("BD28E457-4026-4270-A99F-F9BC20182E15");

	// BLE BlueTooth client service num
	public static final String BLE_BROADCASE_BLUETOOTH_COMMAND_CLIENT_ACTION = "com.yiyang.helmet.BLE_BROADCASE_CLIENT_COMMAND";
	public static final String BLE_BROADCASE_BLUETOOTH_COMMAND_VALUES = "ble_broadcase_command_values";
	public static final String BLE_BROADCASE_BLUETOOTH_COMMAND_CONTENT = "ble_broadcase_command_content";
	public static final String BLE_BROADCASE_BLUETOOTH_COMMAND_TYPE = "ble_broadcase_command_type";

	// BLE BlueTooth service num
	public static final String BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_ACTION = "com.yiyang.helmet.BLE_BROADCASE_SERVICE_COMMAND";
	public static final String BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_STATUS = "ble_broadcase_command_service_status";
	public static final String BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_VALUES = "ble_broadcase_command_service_values";
	public static final String BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_CONTENTS = "ble_broadcase_command_service_contents";
	public static final String BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_TYPE = "ble_broadcase_command_service_type";
	public static final String BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE = "ble_broadcase_command_service";

	// BLE send data state
	public static final String BLE_SEND_DATA_RESULT_BROADCASE_ACTION = "ble_send_data_result_action";
	public static final String BLE_SEND_DATA_RESULT_BROADCASE_ERROR = "ble_send_data_result_error";
	public static final String BLE_SEND_DATA_RESULT_BROADCASE_TYPE = "ble_send_data_result_type";

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
	public static int HELMET_DEFAULT_SEND_SPLIT_LIMIT = 18;
	public static final int HELMET_DEFAULT_NULL_NUM = -1;

	public static final boolean HELMET_DEFAULT_BOOLEAN_NUM = false;

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

	public static boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	public static boolean reflashRemoteCache(Class gattClass, BluetoothGatt mGatt) throws Exception {
		Method reflash_cache = gattClass.getMethod("refresh");
		Boolean returnValue = (Boolean) reflash_cache.invoke(mGatt);
		return returnValue.booleanValue();
	}

	/**
	 * Set helmet status (int)
	 */
	public static void setHelmetIconStatusValues(Context mContext, String table_name, String values_name, int values) {
		SharedPreferences share_data = mContext.getSharedPreferences(table_name, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = share_data.edit();
		editor.putInt(values_name, values);
		editor.commit();
	}

	/**
	 * Set helmet status (String)
	 */
	public static void setHelmetDefaultStatusValues(Context mContext, String table_name, String values_name,
			String values) {
		SharedPreferences share_data = mContext.getSharedPreferences(table_name, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = share_data.edit();
		editor.putString(values_name, values);
		editor.commit();
	}

	/**
	 * Get helmet status (boolean)
	 */
	public static boolean getHelmetIconStatusValues(Context mContext, String table_name, String values_name) {
		SharedPreferences share_data = mContext.getSharedPreferences(table_name, Activity.MODE_PRIVATE);
		return share_data.getInt(values_name, 0) == 0 ? false : true;
	}

	/**
	 * Get helmet status (String)
	 */
	public static String getHelmetDefaultStatusValues(Context mContext, String table_name, String values_name) {
		SharedPreferences share_data = mContext.getSharedPreferences(table_name, Activity.MODE_PRIVATE);
		return share_data.getString(values_name, "");
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

	public static void initBleService(Activity mActivity, Class activityclass) {
		// TODO Auto-generated method stub
		Intent start_ble_intent = new Intent();
		start_ble_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_VALUES, BLE_COMMAND_CODE_START);
		if (activityclass.getName().equals(HelmetClientBleConntectService.class.getName())) {
			start_ble_intent.setAction(BLE_BROADCASE_BLUETOOTH_COMMAND_CLIENT_ACTION);
			start_ble_intent.setClass(mActivity, HelmetClientBleConntectService.class);
		} else {
			start_ble_intent.setAction(BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_ACTION);
			start_ble_intent.setClass(mActivity, HelmetServicetBleConntectService.class);
		}
		if (!HelmetToolUtils.isServiceExisted(mActivity, activityclass.getName())) {
			mActivity.startService(start_ble_intent);
		}
	}

	public static void scanBleDeviceIntent(Context mContext) {
		Intent scan_intent = new Intent();
		scan_intent.setAction(BLE_BROADCASE_BLUETOOTH_COMMAND_CLIENT_ACTION);
		scan_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_VALUES, BLE_COMMAND_CODE_SCAN);
		mContext.sendBroadcast(scan_intent);
	}

	public static void stopScanBleDeviceIntent(Context mContext) {
		Intent stop_intent = new Intent();
		stop_intent.setAction(BLE_BROADCASE_BLUETOOTH_COMMAND_CLIENT_ACTION);
		stop_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_VALUES, BLE_COMMAND_CODE_CLOSE);
		mContext.sendBroadcast(stop_intent);
	}

	public static void sendBleData(Context mContext, String contents) {
		Intent send_data_intent = new Intent();
		send_data_intent.setAction(BLE_BROADCASE_BLUETOOTH_COMMAND_CLIENT_ACTION);
		send_data_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_VALUES, BLE_COMMAND_CODE_DATA);
		send_data_intent.putExtra(BLE_SEND_DATA_RESULT_BROADCASE_TYPE, HELMET_DEFAULT_TEXT_TYPE);
		send_data_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_CONTENT, contents);
		mContext.sendBroadcast(send_data_intent);
	}

	public static void connectRemoteDeviceIntent(Context mContext, BluetoothDevice remoteDevice) {
		Intent connect_intent = new Intent();
		connect_intent.setAction(BLE_BROADCASE_BLUETOOTH_COMMAND_CLIENT_ACTION);
		connect_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_VALUES, BLE_COMMAND_CODE_CONNECT);
		connect_intent.putExtra(BluetoothDevice.EXTRA_DEVICE, remoteDevice);
		mContext.sendBroadcast(connect_intent);
	}

	public static void disconnectRemoteDeviceIntent(Context mContext) {
		Intent disconnect_intent = new Intent();
		disconnect_intent.setAction(BLE_BROADCASE_BLUETOOTH_COMMAND_CLIENT_ACTION);
		disconnect_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_VALUES, BLE_COMMAND_CODE_DISCONNECT);
		mContext.sendBroadcast(disconnect_intent);
	}

	public static void startBleServiceAdvertising(Context mContext) {
		Intent start_intent = new Intent();
		start_intent.setAction(BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_ACTION);
		start_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE, BLE_COMMAND_CODE_START);
		mContext.sendBroadcast(start_intent);
	}

	public static void stopBleServiceAdvertising(Context mContext) {
		Intent stop_intent = new Intent();
		stop_intent.setAction(BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_ACTION);
		stop_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE, BLE_COMMAND_CODE_CLOSE);
		mContext.sendBroadcast(stop_intent);
	}

	public static void sendBleServiceData(Context mContext, String contents) {
		Intent send_data_intent = new Intent();
		send_data_intent.setAction(BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_ACTION);
		send_data_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE, BLE_COMMAND_CODE_DATA);
		send_data_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_TYPE,
				HelmetToolUtils.HELMET_DEFAULT_TEXT_TYPE);
		send_data_intent.putExtra(BLE_BROADCASE_BLUETOOTH_COMMAND_SERVICE_CONTENTS, contents);
		mContext.sendBroadcast(send_data_intent);
	}
}
