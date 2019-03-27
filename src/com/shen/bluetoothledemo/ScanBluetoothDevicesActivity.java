package com.shen.bluetoothledemo;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ScanBluetoothDevicesActivity extends Activity implements OnClickListener {

	private static final String TAG = "ScanBluetoothDevicesActivity";
	private Button start_scan_btn, stop_scan_btn;
	private ListView lv_view;

	private Context mContext;
	private BluetoothManager mManager;
	private BluetoothAdapter mAdapter;
	private BluetoothScanAdapter mScanAdapter;
	private ArrayList<String> temp_name = new ArrayList<String>();
	private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
				BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (!temp_name.contains(mDevice.getName())) {
					Log.i(TAG, "name == " + mDevice.getName());
					devices.add(mDevice);
					stop_scan_btn.setEnabled(true);
					mScanAdapter = new BluetoothScanAdapter(mContext, devices);
					mScanAdapter.notifyDataSetChanged();
					lv_view.setAdapter(mScanAdapter);
				}
				temp_name.add(mDevice.getName());
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_devices_layout);
		mContext = ScanBluetoothDevicesActivity.this;
		mManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mAdapter = mManager.getAdapter();
		initViews();
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

		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(BluetoothDevice.ACTION_FOUND);
		mFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		mFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
		mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		registerReceiver(mReceiver, mFilter);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void initViews() {
		// TODO Auto-generated method stub
		start_scan_btn = (Button) findViewById(R.id.scan_devies_btn);
		start_scan_btn.setOnClickListener(this);
		stop_scan_btn = (Button) findViewById(R.id.stop_devies_btn);
		stop_scan_btn.setOnClickListener(this);
		stop_scan_btn.setEnabled(false);
		lv_view = (ListView) findViewById(R.id.devices_list);
		lv_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				try {
					Utils.createBond(devices.get(position).getClass(), devices.get(position));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public void buildAlertDialog() {
		AlertDialog.Builder mDialog = new AlertDialog.Builder(mContext);
		mDialog.setMessage(R.string.system_text_openblue_dialog_message);
		mDialog.setNegativeButton(android.R.string.cancel, mListener);
		mDialog.setPositiveButton(android.R.string.ok, mListener);
		mDialog.show();
	}

	private DialogInterface.OnClickListener mListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			Log.i(TAG, "which = " + which);
			if (dialog.BUTTON_NEGATIVE == which) {
				finish();
			} else if (dialog.BUTTON_POSITIVE == which) {
				mAdapter.enable();
			}
		}
	};

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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.scan_devies_btn:
			if (!mAdapter.isDiscovering()) {
				mAdapter.startDiscovery();
			}
			break;
		case R.id.stop_devies_btn:
			if (mAdapter.isDiscovering()) {
				mAdapter.cancelDiscovery();
			}
			devices.clear();
			lv_view.setAdapter(null);
			stop_scan_btn.setEnabled(false);
			break;

		default:
			break;
		}
	}
}
