package com.shen.bluetoothledemo;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class BluetoothScanAdapter extends BaseAdapter {

	private LayoutInflater minflater;
	private Context mcontext;
	private ArrayList<BluetoothDevice> mDates = new ArrayList<BluetoothDevice>();

	public BluetoothScanAdapter(Context mContext, ArrayList<BluetoothDevice> dates) {
		super();
		minflater = LayoutInflater.from(mContext);
		mDates = dates;
		mcontext = mContext;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDates.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mDates.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder mHolder = null;
		if (convertView == null) {
			convertView = minflater.inflate(R.layout.bluetooth_scan_devices_layout_items, null);
			mHolder = new ViewHolder();
			mHolder.name = (TextView) convertView.findViewById(R.id.devices_name);
			mHolder.type = (TextView) convertView.findViewById(R.id.devices_rssi);

			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		mHolder.name.setText(getShowAddressOrName(mDates.get(position).getName(), position));
		mHolder.type.setText(getBlueTypeName(mDates.get(position).getType()));

		return convertView;
	}

	class ViewHolder {
		private TextView name;
		private TextView type;
	}

	public String getShowAddressOrName(String input, int position) {
		if ("".equals(input) || input.equals(null)) {
			return mDates.get(position).getAddress();
		} else {
			return mDates.get(position).getName();
		}
	}

	public String getBlueTypeName(int type) {
		if (type == 1) {
			return String.valueOf(mcontext.getResources().getString(R.string.br_edr_blue_device));
		} else if (type == 2) {
			return String.valueOf(mcontext.getResources().getString(R.string.le_blue_device));
		} else if (type == 3) {
			return String.valueOf(mcontext.getResources().getString(R.string.double_blue_device));
		} else {
			return String.valueOf(mcontext.getResources().getString(R.string.unknown_blue_device));
		}
	}
}
