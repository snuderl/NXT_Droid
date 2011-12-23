/**
 * 
 */
package org.nxt.droid;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author Blaž Šnuderl
 *
 */
public class ControlActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control);
		
		BluetoothSocket bs = ((Application)getApplicationContext()).bs;
		UiMessage messageHandler = new UiMessage();
		
		final Controls control = new Controls(messageHandler,bs);
		control.run();

		Button sendButton = (Button)findViewById(R.id.send_button);
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				control.send(1, 1);
				
			}
		});

		
	}
	class UiMessage extends Handler{
		float[] pos;

		@Override
		public void handleMessage(Message msg) {
			Log.d("Message recieved", msg.arg1 + ","+msg.arg2 + ".");
		}
	}
}
