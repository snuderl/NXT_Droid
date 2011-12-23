package org.nxt.droid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NXT_DroidActivity extends Activity {
	/** Called when the activity is first created. */
	final int REQUEST_ENABLE_BT = 0;
	final int NoBT_Dialog = 1, ConnectionFailed_Dialog = 2;
	BluetoothDevice nxtDevice;
	TextView tStatus;
	DataOutputStream nxtDos;
	DataInputStream nxtDis;
	public static final String tag = "NXT_DroidActivity";

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

		Button button_connect = (Button) findViewById(R.id.button1);
		button_connect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				BluetoothSocket bs = connect();
				if (bs == null) {
					CharSequence text = "Connection failed";
					Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
				}
				else{
					CharSequence text = "Connection success";
					Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
					
					((Application)getApplicationContext()).bs = bs;
					Intent i = new Intent(v.getContext(), ControlActivity.class);
					startActivity(i);
					
				}
			}
		});

		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			showDialog(NoBT_Dialog);
		} else {
			Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
			for (BluetoothDevice bluetoothDevice : bondedDevices) {
				if (bluetoothDevice.getName().equals("SNUDERL-LAPTOP")) {
					nxtDevice = bluetoothDevice;
				}
			}
			if (nxtDevice == null) {
				tStatus.setText("No paired NXT device found");

				CharSequence text = "Connection failed";
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
				Log.v(tag, "No NXT paired");
			}
		}

	}

	public BluetoothSocket connect() {
		BluetoothSocket bs = null;
		try {
			bs = nxtDevice.createRfcommSocketToServiceRecord(UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//			Method m = nxtDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
//	         bs = (BluetoothSocket) m.invoke(nxtDevice, 1);
	         
	         bs.connect();

		} catch (IOException e) {
			Log.v(tag, "Connectin failed.");
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		}
		return bs;
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {

		case NoBT_Dialog:
			builder = new AlertDialog.Builder(this);
			builder.setMessage("Bluetooth is not supported")
					.setCancelable(false)
					.setPositiveButton(
							"Exit",
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									NXT_DroidActivity.this.finish();

								}
							});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
}