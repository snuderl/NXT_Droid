package org.nxt.droid;

import static org.nxt.droid.NXT_Commands.MORSE;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class Tab extends TabActivity {

	private TabHost mTabHost;
	private BluetoothDevice nxtDevice;
	private ProgressDialog mProgressBar;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public void chooseSpeed() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose speed");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Toast.makeText(getApplicationContext(), items[item],
						Toast.LENGTH_SHORT).show();
				speed = SPEED.valueOf(items[item].toString());
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	final CharSequence[] items = { "SLOW", "NORMAL", "TURBO" };

	public enum SPEED {
		SLOW(0.25f), NORMAL(0.5f), TURBO(1);

		private float speed;

		private SPEED(float c) {
			speed = c;
		}

		public float getSpeed() {
			return speed;
		}
	}

	SPEED speed = SPEED.SLOW;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.choose_speed:
			chooseSpeed();
			return true;
		case R.id.morse:
			createMorseInput();
			return true;
		case R.id.custom:
			createCustomCommandInput();
			return true;
		case R.id.connect:
			if (!BT.getBT().isConnected()) {

				Log.d("Tag", "Connecting");
				mProgressBar = ProgressDialog.show(this, "","Connecting...",true);
				new AsyncConnect().execute(BT.getBT());

			} else {

				Log.d("Tag", "Disconecting");
				BT.getBT().disconect();
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void createMorseInput() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Title");
		alert.setMessage("Message");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				BT.getBT().send(Packet.make(MORSE, value));
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();

	}

	public void createCustomCommandInput() {

		LayoutInflater factory = LayoutInflater.from(this);

		final View textEntryView = factory.inflate(R.layout.twoinputs, null);

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Custom command");
		alert.setMessage("Enter command and data:");
		// Set an EditText view to get user input
		alert.setView(textEntryView);
		final EditText input1 = (EditText) textEntryView
				.findViewById(R.id.editText1);
		final EditText input2 = (EditText) textEntryView
				.findViewById(R.id.editText2);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String command = input1.getText().toString();
				String data = input2.getText().toString();
				BT.getBT().send(Packet.make(command, data));
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// construct the tabhost
		setContentView(R.layout.tablayout);

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.getRootView().setKeepScreenOn(true);
		mTabHost.setup();

		Intent intent = new Intent().setClass(this, GyroControlActivity.class);

		String deviceName = getIntent().getExtras().getString("device");
		intent.putExtra("device", deviceName);
		setupTab(new TextView(this), "Gyro", intent);
		intent = new Intent().setClass(this, JoystickControlActivity.class);
		setupTab(new TextView(this), "Jostick", intent);

		mTabHost.setCurrentTab(0);

		if (deviceName != null) {
			for (BluetoothDevice device : BluetoothAdapter.getDefaultAdapter()
					.getBondedDevices()) {
				if (device.getName().equals(deviceName)) {
					nxtDevice = device;
				}
			}

		}

		// No device, exit
		if (nxtDevice == null) {
			finish();
		}
	}

	private void setupTab(final View view, final String tag, Intent i) {
		View tabview = createTabView(mTabHost.getContext(), tag);

		TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview)
				.setContent(i);
		mTabHost.addTab(setContent);

	}

	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context)
				.inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}

	private class AsyncConnect extends AsyncTask<BT, Void, Boolean> {

		@Override
		protected Boolean doInBackground(BT... params) {
			BT control = params[0];
			boolean b = control.connect(nxtDevice);
			return b;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mProgressBar.cancel();
			if (result) {
				CharSequence text = "Connection success";
				Toast.makeText(getApplicationContext(), text,
						Toast.LENGTH_SHORT).show();
			} else {
				CharSequence text = "Connection failed";
				Toast.makeText(getApplicationContext(), text,
						Toast.LENGTH_SHORT).show();
			}

		}
	}

}