package org.nxt.droid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
	BluetoothAdapter btAdapter;
	ArrayAdapter<String> listAdapter;
	ArrayList<BluetoothDevice> bondedDevices;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		tStatus = (TextView) findViewById(R.id.tStatus);
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new ArrayList<String>());
		final ListView lw = (ListView) findViewById(R.id.listView12);
		lw.setAdapter(listAdapter);
		lw.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				nxtDevice = bondedDevices.get(arg2);

				// connect
				try {
					BluetoothSocket bs = nxtDevice.createRfcommSocketToServiceRecord(UUID
							.fromString("00001101-0000-1000-8000-00805F9B34FB"));
					bs.connect();

					CharSequence text = "Connection success";
					Toast.makeText(getApplicationContext(), text,
							Toast.LENGTH_SHORT).show();

					((Application) getApplicationContext()).bs = bs;
					Intent i = new Intent(arg1.getContext(),
							ControlActivity.class);
					startActivity(i);
				} catch (Exception e) {
					e.printStackTrace();
					CharSequence text = "Connection failed";
					Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
				}

			}
		});

		Button button_connect = (Button) findViewById(R.id.button1);
		button_connect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				bondedDevices = new ArrayList<BluetoothDevice>();
				Set<BluetoothDevice> bondedDevices1 = btAdapter
						.getBondedDevices();
				for (BluetoothDevice bluetoothDevice1 : bondedDevices1) {
					bondedDevices.add(bluetoothDevice1);
				}
				for (int i = 0; i < bondedDevices.size(); i++) {
					listAdapter.add(bondedDevices.get(i).getName() + "\n"
							+ bondedDevices.get(i).getAddress());
				}
			}
		});

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			showDialog(NoBT_Dialog);
		} else {
			if (!btAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

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