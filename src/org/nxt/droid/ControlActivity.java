/**
 * 
 */
package org.nxt.droid;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Bla� �nuderl
 * 
 */

public class ControlActivity extends Activity implements SensorEventListener {

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control);

		String deviceName = getIntent().getExtras().getString("device");

		if (deviceName != null) {
			for (BluetoothDevice device : BluetoothAdapter.getDefaultAdapter()
					.getBondedDevices()) {
				if (device.getName().equals(deviceName)) {
					nxtDevice = device;
				}
			}
			TextView twNaprava = (TextView) findViewById(R.id.deviceName);
			twNaprava.setText("Naprava: " + deviceName);

		}

		// No device, exit
		if (nxtDevice == null) {
			finish();
		}

		statusImage = (ImageView) findViewById(R.id.imageView1);
		statusImage.setImageResource(R.drawable.useroffline);

		imageSending = (ImageView) findViewById(R.id.imageSending);
		imageSending.setImageResource(R.drawable.useroffline);

		NXTHandler handler = new NXTHandler();
		sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		tv = (TextView) findViewById(R.id.textView1);
		recieved = (TextView) findViewById(R.id.recieved);

		control = new BT(handler);
		control.run();

		mprogress = (ProgressBar) findViewById(R.id.progressBar1);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.choose_speed:
			chooseSpeed();
			return true;
			// case R.id.toggle_control:
			// control.sending = control.sending ? false : true;
			// if (control.sending) {
			// imageSending.setImageResource(R.drawable.useronline);
			// } else {
			// imageSending.setImageResource(R.drawable.useroffline);
			// }
			// return true;
		case R.id.connect:
			if (!control.isConnected()) {
				mprogress.setVisibility(View.VISIBLE);
				new AsyncConnect().execute(control);

			} else {
				control.end();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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

	class NXTHandler extends Handler implements IBTUser {
		final int message = 1, disconnect = 2;
		float[] pos;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				String recived = (String) msg.getData().get("vsebina");
				Log.d("NXTHANDLER", "Recived: " + recived);
				break;
			case 2:
				statusImage.setImageResource(R.drawable.useroffline);

				CharSequence text = "Connection failed, try reconnectiong...";
				Toast.makeText(getApplicationContext(), text,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

		@Override
		public void recived(String message) {
			Bundle b = new Bundle();
			b.putString("content", message);

			Message m = new Message();
			m.what = this.message;
			m.setData(b);

		}

		@Override
		public void onDisconnect() {
			Message m = new Message();
			m.what = disconnect;
			this.sendMessage(m);

		}
	}

	// When this Activity isn't visible anymore
	@Override
	protected void onStop() {
		// unregister the sensor listener
		sManager.unregisterListener(this);
		super.onStop();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Do nothing
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// if sensor is unreliable, return void
		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			return;
		}

		tv.setText("Os X :" + Float.toString(event.values[2]) + "\n" + "Os Y :"
				+ Float.toString(event.values[1]) + "\n" + "Os Z :"
				+ Float.toString(event.values[0]));

		// control.send(1, false, event.values[0], event.values[1],
		// event.values[2]);
		String content = Packet.content(event.values[0],event.values[1],event.values[2]);
		
		control.send(Packet.make("Sensor", content));

	}

	// when this Activity starts
	@Override
	protected void onResume() {
		super.onResume();
		/*
		 * register the sensor listener to listen to the gyroscope sensor, use
		 * the callbacks defined in this class, and gather the sensor
		 * information as quick as possible
		 */
		sManager.registerListener(this,
				sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
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
				control.send("Morse: " + value);
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

	// a TextView
	private TextView tv;
	// the Sensor Manager
	private SensorManager sManager;

	BT control;

	TextView recieved;
	BluetoothDevice nxtDevice = null;
	BluetoothSocket bs = null;
	ImageView statusImage = null;
	ImageView imageSending = null;
	boolean sending = false;
	SPEED speed = SPEED.SLOW;
	ProgressBar mprogress;

	final CharSequence[] items = { "SLOW", "NORMAL", "TURBO" };

	public enum SPEED {
		SLOW(1), NORMAL(3), TURBO(5);

		private int speed;

		private SPEED(int c) {
			speed = c;
		}

		public int getSpeed() {
			return speed;
		}
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
			mprogress.setVisibility(View.GONE);
			if (result) {
				CharSequence text = "Connection success";
				Toast.makeText(getApplicationContext(), text,
						Toast.LENGTH_SHORT).show();
				statusImage.setImageResource(R.drawable.useronline);
			} else {
				CharSequence text = "Connection failed";
				Toast.makeText(getApplicationContext(), text,
						Toast.LENGTH_SHORT).show();
				statusImage.setImageResource(R.drawable.useroffline);
			}

		}
	}
}
