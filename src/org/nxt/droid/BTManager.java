package org.nxt.droid;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

class BTManager implements IBTUser {
	final int message = 1, disconnect = 2;
	float[] pos;
	ArrayList<Handler> registered = new ArrayList<Handler>();
	static private BTManager singleton = null;

	public static BTManager getManager() {
		if (singleton == null) {
			singleton = new BTManager();
		}
		return singleton;
	}

	private BTManager() {
	}

	public void registerHandler(Handler a) {
		registered.add(a);
	}

	public void unregisterHandler(Handler a) {
		registered.remove(a);
	}

	@Override
	public void recived(String message) {
		Bundle b = new Bundle();
		b.putString("content", message);

		Message m = new Message();
		m.what = this.message;
		m.setData(b);

		for (Handler h : registered) {
			h.sendMessage(m);
		}
	}

	@Override
	public void onDisconnect() {
		Message m = new Message();
		m.what = disconnect;
		for (Handler h : registered) {
			h.sendMessage(m);
		}
	}

	@Override
	public void onConnect(boolean connected) {
		Message m = new Message();
		m.what = 3;
		m.arg1 = connected ? 1 : 0;
		for (Handler h : registered) {
			h.sendMessage(m);
		}

	}
}