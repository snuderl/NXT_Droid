/**
 * 
 */
package org.nxt.droid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * @author BlaZ Snuderl
 * 
 */
public class BT extends Thread {
	IBTUser callback;
	BluetoothSocket bs = null;
	BluetoothDevice nxtDevice = null;
	private String TAG = "BTClass";
	Timer keepAlive;
	private static BT singleton = null;

	public static BT getBT() {
		if (singleton == null) {
			singleton = new BT(BTManager.getManager());
			singleton.run();
		}
		return singleton;
	}

	public boolean isConnected() {
		if (bs != null && dataIn != null && dataOut != null)
			return true;
		return false;
	}

	public void setCallback(IBTUser callback) {
		this.callback = callback;
		if (callback == null) {
			this.callback = new IBTUser() {
				@Override
				public void recived(String message) {
				}

				@Override
				public void onDisconnect() {
				}

				@Override
				public void onConnect(boolean b) {
					// TODO Auto-generated method stub
					
				}
			};
		}
	}

	ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();

	private BT(IBTUser callback) {
		this.callback = callback;
	}

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
		keepAlive = new Timer();
		keepAlive.schedule(new KeepAlive(), 100, 1000);
		callback.onConnect(connected);
		return connected;
	}

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
							isRunning = false;
						}
					} else {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			end();
			try {
				dataIn.close();
				dataOut.close();
				bs.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dataIn = null;
			dataOut = null;
			bs = null;
			keepAlive.cancel();
		}

	}

	void disconect() {
		reader.isRunning = false;
		reader.reading = false;
	}

	void end() {
		reader.isRunning = false;
		reader.reading = false;
		callback.onDisconnect();
	}

	public void send(String message) {
		queue.add(message);
	}

	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	private Worker reader = new Worker();

	class KeepAlive extends TimerTask {

		@Override
		public void run() {
			send(Packet.make("KeepAlive", "KeepAlive"));
		}

	}
}
