package com.shen.bluetoothbledemo;

import java.util.Arrays;

public class HelmetSendDateInfo {

	private int index;
	private boolean result;
	private byte[] data;

	public HelmetSendDateInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HelmetSendDateInfo(int index, boolean result, byte[] data) {
		super();
		this.index = index;
		this.result = result;
		this.data = data;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "HelmetSendDateInfo [index=" + index + ", result=" + result + ", data=" + Arrays.toString(data) + "]";
	}

}
