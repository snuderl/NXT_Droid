package org.nxt.droid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.widget.TextView;

public class NXT_DroidActivity extends Activity {
	/** Called when the activity is first created. */
	final int REQUEST_ENABLE_BT = 0;
	BluetoothDevice nxtDevice;
	TextView tStatus;
	DataOutputStream nxtDos;
	DataInputStream nxtDis;
	BluetoothSocket bs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		tStatus = (TextView) findViewById(R.id.tStatus);

		// conn = connect();
		// nxtDos = conn.getDataOut();
		// nxtDis = conn.getDataIn();
		// try {
		// nxtDos.writeInt(111);
		// int recieve = nxtDis.readInt();
		// tStatus.setText("Revieved: " + recieve);
		// } catch (Exception e) {
		// tStatus.setText("napaka pri sprejemu");
		// }

		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	
			Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
			for (BluetoothDevice bluetoothDevice : bondedDevices) {
				if (bluetoothDevice.getName().equals("NXT")) {
					nxtDevice = bluetoothDevice;
				}
			}

			if (nxtDevice == null) {
				tStatus.setText("No paired NXT device found");
				try {
					bs = nxtDevice
							.createRfcommSocketToServiceRecord(UUID
									.fromString("00001101-0000-1000-8000-00805F9B34FB"));
					bs.connect();

					nxtDos = new DataOutputStream(bs.getOutputStream());
					nxtDos.writeInt(111);
					nxtDis = new DataInputStream(bs.getInputStream());

					tStatus.setText("Successfully connected to NXT"
							+ nxtDis.readInt());
				} catch (IOException e) {
					tStatus.setText("Connection to NXT failed");

				}

		}

	}

}