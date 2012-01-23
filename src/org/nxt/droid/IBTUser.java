package org.nxt.droid;

public interface IBTUser {
	void recived(String message);
	void onDisconnect();
	void onConnect(boolean connected);
}
