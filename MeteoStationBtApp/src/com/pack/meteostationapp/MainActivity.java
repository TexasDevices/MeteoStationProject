package com.pack.meteostationapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	// For Debugging
	private static final String TAG = "MAINCurrentMeter";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static Boolean isConnected = false;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	// private static final int REQUEST_EXIT_APLICATION = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	public static final String DATA_ACTION = "DATA_ACTION";

	// Name of the connected device
	public static String mConnectedDeviceName = null;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	public static BtService mMeteoAppBtService = null;
	private AlertDialogManager alertBuilder;
	private ReadResult receiver = new ReadResult();
	Intent iForBroadcastReceiverData; // intent where all received data are sent
										// to broadcast receivers in other
										// activities

	private Button mStartMeasureButton, mConnectButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // check if
																	// bluetooth
																	// is
																	// availabl
		if (mBluetoothAdapter == null) {
			alertBuilder = new AlertDialogManager(MainActivity.this);
			alertBuilder.showAlertDialog(getString(R.string.error),
					getString(R.string.no_bluetooth_device), 0);
		}

		iForBroadcastReceiverData = new Intent(DATA_ACTION);

	}

	@Override
	public void onStart() {
		super.onStart();
		// If BT is not on, request that it be enabled.

		if (!mBluetoothAdapter.isEnabled()) {

			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			if (mMeteoAppBtService == null) {
				// Intent searchIntent = null;
				// searchIntent = new Intent(this, BtDeviceListActivity.class);
				// startActivityForResult(searchIntent, REQUEST_CONNECT_DEVICE);
				mMeteoAppBtService = new BtService(MainActivity.this, mHandler);
			}
			// /Log.e(TAG, "----ON START----");

		}
		// setupMainMenu();
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
		setupMainMenu();
		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mMeteoAppBtService != null) { // /tttttttttttttttttttttttttttttttttttttttttt
			// TODO
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already

			if (mMeteoAppBtService.getState() == BtService.STATE_NONE) {
				// Start the Bluetooth chat services
				mMeteoAppBtService.start();
			}

		}
	}

	private void setupMainMenu() {

		mStartMeasureButton = (Button) findViewById(R.id.button1);
		mStartMeasureButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// Send to uc command to start measurement

				if (!MainActivity.isConnected) {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							MainActivity.this);
					alertDialogBuilder
							.setTitle("MAMY MA£Y PROBLEM")
							.setMessage(
									"Nie jesteœ po³¹czony ze stacj¹, czy mimo to kontynuowaæ? ")
							.setCancelable(true)
							.setInverseBackgroundForced(true);

					alertDialogBuilder
							.setIcon(R.drawable.ic_alert)
							.setPositiveButton("Tak",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											Intent measureResultsIntent = null;
											measureResultsIntent = new Intent(
													MainActivity.this,
													TempActivity.class);
											startActivity(measureResultsIntent);
											dialog.cancel();

										}
									})
							.setNegativeButton("Nie",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {

										}
									});
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
				} else {

					Intent measureResultsIntent = null;
					measureResultsIntent = new Intent(MainActivity.this,
							TempActivity.class);
					startActivity(measureResultsIntent);

				}
				// Results.class);
				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

			}
		});
		mConnectButton = (Button) findViewById(R.id.connect);
		mConnectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent serverIntent = null;
				serverIntent = new Intent(getApplicationContext(),
						BtDeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
		});

	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "-- ON PAUSE --");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mMeteoAppBtService != null)
			mMeteoAppBtService.stop();
		if (D)
			Log.e(TAG, "-- ON DESTROY --");
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendCommand(String message) {
		// Check that we're actually connected before trying anything
		if (mMeteoAppBtService.getState() != BtService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mMeteoAppBtService.write(send);
		}
	}

	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);

	}

	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	// The Handler that gets information back from the BluetoothChatService
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BtService.STATE_CONNECTED:
					setStatus(getString(R.string.title_connected_to,
							mConnectedDeviceName));
					isConnected = true;

					break;
				case BtService.STATE_CONNECTING:
					setStatus(R.string.title_connecting);
					break;
				case BtService.STATE_LISTEN:
				case BtService.STATE_NONE:
					isConnected = false;
					setStatus(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				break;
			case MESSAGE_READ: // odbieram dane tutaj

				receiver = parseMessageFromHandler((String) msg.obj, msg.arg1);
				if (D)
					Log.i(TAG, "READ_MESSAGE: " + receiver.valueFromData);

				iForBroadcastReceiverData.putExtra(
						receiver.actionStringForBroadcastReceiver,
						receiver.valueFromData);
				sendBroadcast(iForBroadcastReceiverData); // wysylam Broadcasta
				break;

			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////////

	// /public class Broadcast
	// //////////////////////////////////////////////////////////////////////////////////////////
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				// and then search new devices immediately
				Intent searchIntent = null;
				searchIntent = new Intent(this, BtDeviceListActivity.class);
				startActivityForResult(searchIntent, REQUEST_CONNECT_DEVICE);

			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		}
	}

	private void connectDevice(Intent data) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				BtDeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mMeteoAppBtService.connect(device);
	}

	class ReadResult {

		String valueFromData;
		String actionStringForBroadcastReceiver;
	};

	private ReadResult parseMessageFromHandler(String data, int dataLength) {

		ReadResult response = new ReadResult();
		response.valueFromData = "";
		for (int i = 1; i < dataLength - 1; i++) {
			response.valueFromData += data.charAt(i);
		}
		switch (data.charAt(0)) {
		case 'T':
			response.actionStringForBroadcastReceiver = "TEMPERATURE";
			break;
		case 'H':
			response.actionStringForBroadcastReceiver = "HUMIDITY";
			break;
		case 'P':
			response.actionStringForBroadcastReceiver = "PRESSURE";
			break;
		case 'L':
			response.actionStringForBroadcastReceiver = "LIGHT";
			break;
		case 'B':
			response.actionStringForBroadcastReceiver = "LIGHTNING";
			break;
		case 'C':
			response.actionStringForBroadcastReceiver = "CURRENT";
			break;
		case 'V':
			response.actionStringForBroadcastReceiver = "VOLTAGE";
			break;
		}
		return response;
	}

}
