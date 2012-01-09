/**
 * 
 */
package org.nxt.droid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author Blaž Šnuderl
 * 
 */
public class Controls extends Thread implements ISend {
	Handler mUIMessageHandler;
	BluetoothSocket bs = null;
	BluetoothDevice nxtDevice = null;
	private String TAG = "Control";
	boolean sending = false;
	boolean connected = false;

	ConcurrentLinkedQueue<StringPacket> queue = new ConcurrentLinkedQueue<StringPacket>();

	/**
	 * constructor establishes call back path of the RCNavigationControl
	 * 
	 * @param mUIMessageHandler
	 * @param control
	 */
	public Controls(Handler mUIMessageHandler, BluetoothDevice btBluetoothDevice) {
		Log.d(TAG, " Controls start");
		this.mUIMessageHandler = mUIMessageHandler;
		nxtDevice = btBluetoothDevice;
	}

	/**
	 * connects to NXT using Bluetooth
	 * 
	 * @param name
	 *            of NXT
	 * @param address
	 *            bluetooth address
	 */
	public boolean setUp() {

		boolean connected = false;
		queue = new ConcurrentLinkedQueue<Controls.StringPacket>();
		try {
			bs = nxtDevice.createRfcommSocketToServiceRecord(UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));

			bs.connect();
			dataIn = new DataInputStream(bs.getInputStream());
			dataOut = new DataOutputStream(bs.getOutputStream());
			connected = true;
			if (reader.isAlive() == false) {
				reader = new Worker();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (dataIn == null || dataOut == null) {
			connected = false;
			return connected;
		}
		if (!reader.isAlive()) {
			reader.start();
		}
		updateIcon(connected);
		this.connected = connected;
		return connected;
	}

	/**
	 * inner class to monitor for an incoming message after a command has been
	 * sent <br>
	 * calls showRobotPosition() on the controller
	 */
	class Packet {
		public int command;
		public boolean response;
		public float[] data = null;
	}

	public class StringPacket extends Packet {
		public String strData;
	}

	class Worker extends Thread {

		public boolean reading = false;
		int count = 0;
		boolean isRunning = false;

		public void run() {
			setName("RCNavComms read thread");
			isRunning = true;
			while (isRunning) {
				if (reading) // reads one message at a time
				{
					Log.d(TAG, "reading ");
					String x = "";
					boolean ok = false;
					try {
						x = dataIn.readUTF();
						ok = true;
						Log.d(TAG, "data  " + x);
					} catch (IOException e) {
						Log.d(TAG, "connection lost");
						ok = false;
					}
					if (ok) {
						sendPosToUIThread(x);
						reading = false;
						updateIcon(false);
					}
				} else {
					if (!queue.isEmpty()) {
						try {
							StringPacket m = queue.poll();
							dataOut.writeInt(m.command); // convert the enum to
															// an
															// integer
							if (m.data != null) {
								for (float d : m.data) // iterate over the data
														// array
								{
									dataOut.writeFloat(d);
								}
							} else {
								dataOut.writeUTF(m.strData);
							}
							dataOut.flush();
							reading = m.response;
						} catch (IOException e) {
							Log.e(TAG, " send throws exception  ", e);
							updateIcon(false);
						}
					}
				}
				if (queue.isEmpty() && reading == false) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}// if reading
			try {
				bs.close();
				bs = null;
				dataIn = null;
				dataOut = null;
				isRunning = false;
				connected = false;
				updateIcon(false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}// while is running

	}

	void end() {
		reader.isRunning = false;
		connected = false;
		updateIcon(false);
	}

	/**
	 * sends a command with a variable number of float parameters. see
	 * http://java.sun.com/docs/books/tutorial/java/javaOO/arguments.html the
	 * section on Arbitrary Number of Arguments
	 * 
	 * @param c
	 *            a Command enum
	 * @param data
	 *            an array of floats built from the collection list parameters.
	 */
	public void send(int command, boolean response, float... data) {
		if (sending) {
			StringPacket m = new StringPacket();
			m.command = command;
			m.response = response;
			m.data = data;
			queue.add(m);
		}
	}

	public void send(int command, boolean response, String data) {
		if (sending) {
			StringPacket m = new StringPacket();
			m.command = command;
			m.response = response;
			m.strData = data;
			queue.add(m);
		}
	}

	/**
	 * used by reader
	 */
	private DataInputStream dataIn;
	/**
	 * used by send()
	 */
	private DataOutputStream dataOut;
	private Worker reader = new Worker();

	public void updateIcon(boolean connected) {
		Bundle b = new Bundle();
		b.putBoolean("online", connected);
		Message message_holder = new Message();
		message_holder.setData(b);
		message_holder.what = 2;
		mUIMessageHandler.sendMessage(message_holder);
	}

	public void sendPosToUIThread(String x) {
		Bundle b = new Bundle();
		b.putString("vsebina", x);
		Message message_holder = new Message();
		message_holder.setData(b);
		message_holder.what = 1;
		mUIMessageHandler.sendMessage(message_holder);
	}

}
