package com.pack.meteostationapp;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

public class BtDeviceListActivity extends Activity {
	// Debugging
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;

	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// Member fields
	private ProgressBar seekDevicesProgress;
	private int seekDeviceProgressStatus = 0;
	private BluetoothAdapter mBtAdapter;
	private Map<String, String> btPairedDevices;
	private Handler deviceHandler = new Handler();

	AlertDialog alertDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		// doDiscovery();
		// mProgress = (ProgressBar) findViewById(R.id.progress_bar_searching);
		btPairedDevices = new HashMap<String, String>();
		seekDevicesProgress = (ProgressBar) findViewById(R.id.progress_bar_searching);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter); // registration of Broadcast
													// Receiver / na dole
													// obsluzone zdarzenie

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// Get a set of currently paired devices
		/*
		 * Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		 * 
		 * // If there are paired devices, add each one to the ArrayAdapter
		 * 
		 * if (pairedDevices.size() > 0) {
		 * 
		 * for (BluetoothDevice device : pairedDevices) {
		 * 
		 * btPairedDevices.put(device.getName(), device.getAddress());//
		 * ///////////////////////////////////////// } }
		 */
		new Thread(new Runnable() { // moze sie przydac
					@Override
					public void run() {
						while (seekDeviceProgressStatus < 100) {
							try {
								Thread.sleep(5);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							seekDeviceProgressStatus++;
							deviceHandler.post(new Runnable() {

								@Override
								public void run() {
									seekDevicesProgress
											.setProgress(seekDeviceProgressStatus);
								}
							});

						}

					}
				}).start();

		doDiscovery();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
		if (alertDialog != null) {
			alertDialog.dismiss();
		}
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		seekDevicesProgress.setVisibility(View.VISIBLE);
		if (D)
			Log.d(TAG, "doDiscovery()");
		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();

	}

	private class WaitDialogThread implements Runnable {
		private int counter = 0;

		@Override
		public void run() {
			while (counter < 100) {
				if (D)
					Log.d("THREADD", "waitThread");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				counter++;
			}
			finish(); // after 10 sec , close application
		}
	}

	private void searchForAvailableMeter() {
		mBtAdapter.cancelDiscovery(); // Cancel discovery because it's costly
										// and we're about to connect
		String[] availableDevices = getResources().getStringArray(
				R.array.AVAILABLE_DEVICES);
		for (Map.Entry<String, String> entry : btPairedDevices.entrySet()) {
			if (D)
				Log.d(TAG, entry.getKey() + "\n");
			for (String avDevice : availableDevices) {
				// if (D)
				// Log.d(TAG, "---->" + avDevice + "\n");
				if ((entry.getKey()).equals(avDevice)) { // TODO wydupczyl sie
															// tu pies wie czemu
					if (D)
						Log.d(TAG, "DEVICE was fOUND!!!!!!!!");
					// Create the result Intent and include the MAC address
					Intent intent = new Intent();
					intent.putExtra(EXTRA_DEVICE_ADDRESS, entry.getValue());

					// Set result and finish this Activity
					setResult(Activity.RESULT_OK, intent);
					if (D)
						Log.d(TAG, "BFR FINISH");
					finish();
					if (D)
						Log.d(TAG, "AFTER FINISH");
					return;
				}
			}
		}
		if (D)
			Log.d(TAG, "NO DEVICE FOUND!!! ! !!!!!!!!!");
		noDeviceAlertDialog();

	}

	private void noDeviceAlertDialog() {
		// requestWindowFeature(Window.); //TODO wylaczyc scanning devices after
		// all
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setTitle("Nie znaleziono ¿adnej stacji w zasiêgu")
				.setMessage(
						"Chcesz spróbowaæ wyszukaæ ponownie ?")
				.setCancelable(true).setInverseBackgroundForced(true);
		alertDialogBuilder
				.setIcon(R.drawable.ic_not_ok)
				.setPositiveButton("Tak",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								mBtAdapter.cancelDiscovery();// zakoncz trwajace
								doDiscovery();
								dialog.cancel();
							}
						})
				.setNegativeButton("Nie",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								android.os.Process
										.killProcess(android.os.Process.myPid());

							}
						});
		alertDialog = alertDialogBuilder.create();
		alertDialog.show();
		Thread waiter = new Thread(new WaitDialogThread()); // gdyby ktos nic
															// nie wisnal to po
															// 10 sek ,
															// wylaczenie
		waiter.start();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (D)
				Log.d(TAG, "onRECEIVE!!!!!!!!! ! !!!!!!!!!");
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				btPairedDevices.put(device.getName(), device.getAddress());
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				seekDevicesProgress.setVisibility(View.GONE);
				setTitle(R.string.scanning);
				if (btPairedDevices.isEmpty()) {
					if (D)
						Log.d(TAG, "HASH MAP IS EMPTY"); // TODO
					noDeviceAlertDialog();
				} else
					searchForAvailableMeter();
			}
		}
	};

}