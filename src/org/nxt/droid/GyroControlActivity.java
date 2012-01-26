/**
 * 
 */
package org.nxt.droid;

import android.app.Activity;
import static java.lang.Math.abs;
import static org.nxt.droid.NXT_Commands.*;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Bla� �nuderl
 * 
 */

public class GyroControlActivity extends Activity implements
		SensorEventListener {
	boolean pauseSensor = true;
	NXTHandler handler = null;
	View layout;

	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control);
		findViewById(R.id.btnStop).getRootView();
		layout = findViewById(R.id.controllayout);
		if (BT.getBT().isConnected()) {
			layout.setBackgroundResource(R.drawable.ozadje2);
		}

		final Button stop = (Button) findViewById(R.id.btnStop);
		stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pauseSensor = pauseSensor ? false : true;
				if (!pauseSensor) {
					stop.setText("Stop");
					// imageSending.setImageResource(R.drawable.useronline);
				} else {
					// imageSending.setImageResource(R.drawable.useroffline);
					stop.setText("Resume");
				}
				control.send(Packet.make(STOP, "STOP"));

			}
		});

		Button claw = (Button) findViewById(R.id.btnClaws);
		claw.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				control.send(Packet.make("CLAWS", "CLAWS"));

			}
		});

		// statusImage = (ImageView) findViewById(R.id.imageView1);
		// statusImage.setImageResource(R.drawable.useroffline);

		steerView = (TextView) findViewById(R.id.steer1);

		// imageSending = (ImageView) findViewById(R.id.imageSending);
		// imageSending.setImageResource(R.drawable.useroffline);

		handler = new NXTHandler();
		BTManager.getManager().registerHandler(handler);
		sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		control = BT.getBT();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		sManager.unregisterListener(this);
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

	// When this Activity isn't visible anymore

	@Override
	public void onSensorChanged(SensorEvent event) {
		// if sensor is unreliable, return void
		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			Log.d("Sensor", "Unrealiable sensor");
			return;
		}

		orientation = event.values;

		calculateSteering(event.values[2], event.values[0], event.values[1]);
	}

	class NXTHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BTManager.messageRecived:
				String recived = (String) msg.getData().get("vsebina");
				Log.d("NXTHANDLER", "Recived: " + recived);
				break;
			case BTManager.disconnect:
				// statusImage.setImageResource(R.drawable.useroffline);
				layout.setBackgroundResource(R.drawable.ozadje);
				break;

			case BTManager.connect:
				int status = msg.arg1;
				if (status == 1) {

					layout.setBackgroundResource(R.drawable.ozadje2);

					// statusImage.setImageResource(R.drawable.useronline);
				} else {
					// statusImage.setImageResource(R.drawable.useroffline);
					layout.setBackgroundResource(R.drawable.ozadje);
				}
			}
		}
	}

	public void calculateSteering(float x, float z, float y) {
		float forward = 0;
		float maxSpeed = 45;
		// Minus je levo, pozitivno desno;
		float steer = 0;
		if (y > abs(100)) {
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
			float speed = ((Tab) getParent()).speed.getSpeed();
			String content = Packet.content(forward * speed, steer);

			control.send(Packet.make(STEER, content));
		}

	}

	public float getNumberInBounds(float number, float bound) {
		if (number < 0) {
			return Math.max(-bound, number);
		} else {
			return Math.min(bound, number);
		}
	}

	// when this Activity starts
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("Resume", "Resumed");
		/*
		 * register the sensor listener to listen to the gyroscope sensor, use
		 * the callbacks defined in this class, and gather the sensor
		 * information as quick as possible
		 */
		sManager.registerListener(this,
				sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_UI);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.calibrate:
			calibrate();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// the Sensor Manager
	private SensorManager sManager;

	BT control;
	TextView recieved;
	BluetoothDevice nxtDevice = null;
	BluetoothSocket bs = null;
	ImageView statusImage = null;
	ImageView imageSending = null;
	boolean sending = false;
	ProgressBar mprogress;
	TextView steerView;
	float[] orientation = null;
	float orientationSpeedBase = 45;
	float orientationSteerBase = 90;

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
}
