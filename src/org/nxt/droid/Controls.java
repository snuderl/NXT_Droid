/**
 * 
 */
package org.nxt.droid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author Bla� �nuderl
 * 
 */
public class Controls extends Thread {
	Handler mUIMessageHandler;
	BluetoothSocket bs = null;
	private String TAG = "Control";

	ConcurrentLinkedQueue<Packet> queue = new ConcurrentLinkedQueue<Packet>();

	/**
	 * constructor establishes call back path of the RCNavigationControl
	 * 
	 * @param mUIMessageHandler
	 * @param control
	 */
	public Controls(Handler mUIMessageHandler, BluetoothSocket bs) {
		Log.d(TAG, " Controls start");
		this.mUIMessageHandler = mUIMessageHandler;
		this.bs = bs;
		setUp();
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
		try {
			dataIn = new DataInputStream(bs.getInputStream());
			dataOut = new DataOutputStream(bs.getOutputStream());
			connected = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (dataIn == null) {
			connected = false;
			return connected;
		}
		if (!reader.isRunning) {
			reader.start();
		}
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
		public float[] data;
	}

	class Worker extends Thread {

		public boolean reading = false;
		int count = 0;
		boolean isRunning = false;

		public void run() {
			setName("RCNavComms read thread");
			isRunning = true;
			while (true) {
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
						count++;
						reading = count < 20;// give up
						ok = false;
					}
					if (ok) {
						sendPosToUIThread(x);
						reading = false;
					}
				} else {
					if (!queue.isEmpty()) {
						try {
							Packet m = queue.poll();
							dataOut.writeInt(m.command); // convert the enum to
															// an
															// integer

							for (float d : m.data) // iterate over the data
													// array
							{
								dataOut.writeFloat(d);
							}
							dataOut.flush();
							reading = m.response;
						} catch (IOException e) {
							Log.e(TAG, " send throws exception  ", e);
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
		}// while is running

	}

	void end() {
		reader.isRunning = false;
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
		Packet m = new Packet();
		m.command = command;
		m.response = response;
		m.data = data;
		queue.add(m);
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

	public void sendPosToUIThread(String x) {
		Bundle b = new Bundle();
		b.putString("vsebina", x);
		Message message_holder = new Message();
		message_holder.setData(b);
		mUIMessageHandler.sendMessage(message_holder);
	}

}
