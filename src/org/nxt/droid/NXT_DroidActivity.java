package org.nxt.droid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import lejos.pc.comm.NXTConnector;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.TextView;

public class NXT_DroidActivity extends Activity {
	/** Called when the activity is first created. */
	final int REQUEST_ENABLE_BT = 0;
	BluetoothDevice nxtDevice;
	TextView tStatus;
	NXTConnector conn;
	DataOutputStream nxtDos;
	DataInputStream nxtDis;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		tStatus = (TextView) findViewById(R.id.tStatus);

		conn = connect();
		nxtDos = conn.getDataOut();
		nxtDis = conn.getDataIn();
		try {
			nxtDos.writeInt(111);
			int recieve = nxtDis.readInt();
			tStatus.setText("Revieved: " + recieve);
		} catch (Exception e) {
			tStatus.setText("napaka pri sprejemu");
		}

	}

	public NXTConnector connect() {
		// info
		// https://github.com/Shawn-in-Tokyo/leJOS-Droid/tree/master/leJOS-Droid/src/lejos/android
		NXTConnector conn = new NXTConnector();
		conn.connectTo();
		return conn;
	}

}