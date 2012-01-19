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
 * @author Bla� �nuderl
 * 
 */
public class BT extends Thread {
	IBTUser callback;
	BluetoothSocket bs = null;
	BluetoothDevice nxtDevice = null;
	private String TAG = "BTClass";

	public boolean isConnected() {
		if (bs != null && dataIn != null && dataOut != null)
			return true;
		return false;
	}

	ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();

	/**
	 * constructor establishes call back path of the RCNavigationControl
	 * 
	 * @param mUIMessageHandler
	 * @param control
	 */
	public BT(IBTUser callback) {
		this.callback = callback;
	}

	/**
	 * connects to NXT using Bluetooth
	 * 
	 * @param name
	 *            of NXT
	 * @param address
	 *            bluetooth address
	 */
	public boolean connect(BluetoothDevice device) {
		nxtDevice = device;

		boolean connected = false;
		queue = new ConcurrentLinkedQueue<String>();
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
		return connected;
	}

	/**
	 * inner class to monitor for an incoming message after a command has been
	 * sent <br>
	 * calls showRobotPosition() on the controller
	 */
	class Worker extends Thread {

		public boolean reading = false;
		int count = 0;
		boolean isRunning = false;

		public void run() {
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
					} catch (IOException e) {
						Log.d(TAG, "connection lost");
						ok = false;
					}
					if (ok) {
						reading = false;
						callback.recived(x);
					}
				} else {
					if (!queue.isEmpty()) {
						try {
							String m = queue.poll();
							dataOut.writeUTF(m);
							dataOut.flush();
						} catch (IOException e) {
							Log.e(TAG, "Execption while sending", e);
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
				callback.onDisconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}// while is running

	}

	void end() {
		reader.isRunning = false;
		reader.reading = false;
		callback.onDisconnect();
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
	public void send(String message) {
		queue.add(message);
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
}
