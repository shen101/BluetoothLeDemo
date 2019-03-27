package com.shen.bluetoothledemo;

import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.ScanCallback;

public class Utils {
	// Service UUID
	public static UUID CONNECTION_SERVICE_UUID = UUID.fromString("00007777-0000-1000-8000-00805f9b34fb");
	// Read-only
	public static UUID CHARACTERISTIC_READ_UUID = UUID.fromString("275348FB-C14D-4FD5-B434-7C3F351DEA5F");
	// Read-write
	public static UUID CHARACTERISTIC_WRITE_UUID = UUID.fromString("BD28E457-4026-4270-A99F-F9BC20182E15");
	// Read-write characteristic
	public static UUID CHARACTERISTIC_DEMO_UUID = UUID.fromString("BD28E457-4026-4270-A99F-F9BC20182E15");

	public static final int OPEN_BLUETOOTH_NUM = 10;
	public static final int REFLASH_STATUS_SUCCESSFUL_NUM = 11;
	public static final int REFLASH_STATUS_FAILED_NUM = 12;

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

	public static int getConnectionStatus(int status, int newsattus) {
		if (status == 0 && newsattus == 2) {
			return R.string.system_text_connect_success;
		} else {
			return R.string.system_text_connect_failed;
		}
	}

	public static boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}
}
