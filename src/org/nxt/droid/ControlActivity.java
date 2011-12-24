/**
 * 
 */
package org.nxt.droid;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Blaž Šnuderl
 * 
 */
public class ControlActivity extends Activity implements SensorEventListener {
TextView recieved;
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control);

		BluetoothSocket bs = ((Application) getApplicationContext()).bs;
		UiMessage messageHandler = new UiMessage();
		sManager = (SensorManager) getSystemService(SENSOR_SERVICE); 

		tv = (TextView) findViewById(R.id.textView1);
		recieved = (TextView) findViewById(R.id.recieved);
		control = new Controls(messageHandler, bs);
		
		control.run();

		Button sendButton = (Button) findViewById(R.id.send_button);
		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				control.send(1, 1);

			}
		});

	}

	class UiMessage extends Handler {
		float[] pos;

		@Override
		public void handleMessage(Message msg) {
			Log.d("Message recieved", msg.arg1 + "," + msg.arg2 + ".");
			recieved.setText((String)msg.getData().get("vsebina"));
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
		
		tv.setText("Os X :"+ Float.toString(event.values[2]) +"\n"+
				"Os Y :"+ Float.toString(event.values[1]) +"\n"+
				"Os Z :"+ Float.toString(event.values[0]));
		
		control.send(4, event.values[0]);
		
		
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

	// a TextView
	private TextView tv;
	// the Sensor Manager
	private SensorManager sManager;
	
	Controls control;
}
