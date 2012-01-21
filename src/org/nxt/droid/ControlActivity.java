/**
 * 
 */
package org.nxt.droid;

import android.app.Activity;
import static java.lang.Math.abs;
import static org.nxt.droid.NXT_Commands.*;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
	boolean pauseSensor = true;

	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.control);
		findViewById(R.id.stopButton).getRootView().setKeepScreenOn(true);

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

		Button stop = (Button) findViewById(R.id.stopButton);
		stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				control.send(Packet.make(STOP, "STOP"));

			}
		});

		Button claw = (Button) findViewById(R.id.claws);
		claw.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				control.send(Packet.make("CLAWS", "CLAWS"));

			}
		});

		statusImage = (ImageView) findViewById(R.id.imageView1);
		statusImage.setImageResource(R.drawable.useroffline);

		tv = (TextView) findViewById(R.id.textView1);
		steerView = (TextView) findViewById(R.id.textView4);

		imageSending = (ImageView) findViewById(R.id.imageSending);
		imageSending.setImageResource(R.drawable.useroffline);

		NXTHandler handler = new NXTHandler();
		sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		tv = (TextView) findViewById(R.id.textView1);

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
		case R.id.toggle_control:
			pauseSensor = pauseSensor ? false : true;
			if (!pauseSensor) {
				imageSending.setImageResource(R.drawable.useronline);
			} else {
				imageSending.setImageResource(R.drawable.useroffline);
			}
			return true;
		case R.id.connect:
			if (!control.isConnected()) {
				mprogress.setVisibility(View.VISIBLE);
				new AsyncConnect().execute(control);

			} else {
				control.disconect();
			}
			return true;
		case R.id.morse:
			createMorseInput();
			return true;
		case R.id.custom:
			createCustomCommandInput();
			return true;
		case R.id.calibrate:
			calibrate();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void calibrate() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Put your device into neutral position.");
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String text = "Calibration failed.";
				if (orientation != null) {
					float z = orientation[0];
					orientationSpeedBase = orientation[2];
					orientationSteerBase = z;
					text = "Calibrated successfully.\nX: "
							+ orientationSpeedBase + "\nZ: "
							+ orientationSteerBase;
				}
				Toast.makeText(getApplicationContext(), text,
						Toast.LENGTH_SHORT).show();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

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
			Log.d("Sensor", "Unrealiable sensor");
			return;
		}

		orientation = event.values;
		tv.setText("Os X :" + Float.toString(event.values[2]) + "\n" + "Os Y :"
				+ Float.toString(event.values[1]) + "\n" + "Os Z :"
				+ Float.toString(event.values[0]));
		
		calculateSteering(event.values[2], event.values[0], event.values[1]);
	}
	
	public void calculateSteering(float x, float z,float y) {
		float forward = 0;
		float maxSpeed = 45;
		// Minus je levo, pozitivno desno;
		float steer = 0;
		if(y>abs(100)) {
			x = 90 + (90 - x);
		}
		if (x < orientationSpeedBase) {
			forward = (orientationSpeedBase - x);
		} else {
			forward = orientationSpeedBase - x;
		}
		forward = getNumberInBounds(forward, maxSpeed);

		// Max steer
		float maxSteer = 90;

		float dist1 = (z - orientationSteerBase);
		float dist2 = (360f - z + orientationSteerBase);
		if (abs(dist1) < abs(dist2)) {
			z = dist1;
		} else {
			z = -dist2;
		}
		steer = getNumberInBounds(z, maxSteer);
		steerView.setText("Steer: " + steer + ".\n Speed: " + forward);

		if (!pauseSensor) {

			String content = Packet.content(forward * speed.speed, steer);

			control.send(Packet.make(STEER, content));
		}

	}
	
	public float getNumberInBounds(float number, float bound) {
		if (number < 0) {
			return  Math.max(-bound, number);
		} else {
			return  Math.min(bound, number);
		}
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
				control.send(Packet.make(MORSE, value));
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
				control.send(Packet.make(command, data));
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
	TextView steerView;
	float[] orientation = null;
	float orientationSpeedBase = 45;
	float orientationSteerBase = 90;

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
