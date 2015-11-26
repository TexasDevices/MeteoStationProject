package com.pack.meteostationapp;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
public abstract class DataReceiver extends BroadcastReceiver {

	@Override
	public abstract void onReceive(Context context , Intent i);
}
