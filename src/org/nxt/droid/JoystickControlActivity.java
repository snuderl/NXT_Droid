package org.nxt.droid;

import static org.nxt.droid.NXT_Commands.*;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class JoystickControlActivity extends Activity {

	JostickView joystick;
	BT controls;
	Button claw;
	JoystickHandler handler;
	TextView text;
	View layout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joysticklayout);

		handler = new JoystickHandler();
		BTManager.getManager().registerHandler(handler);
		joystick = (JostickView) findViewById(R.id.joystickView1);
		controls = BT.getBT();
		layout = findViewById(R.id.jostickLayout);

		joystick.setOnJostickMovedListener(_listener);
		text=(TextView)findViewById(R.id.textViewJostick);
		
		
		claw = (Button) findViewById(R.id.btnKlesce);
		claw.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				BT.getBT().send(Packet.make("CLAWS", "CLAWS"));

			}
		});
		
	}
	


	private JostickMovedListener _listener = new JostickMovedListener() {

		@Override
		public void OnMoved(int pan, int tilt) {
			float speed = ((Tab) getParent()).speed.getSpeed();
			String content = Packet.content(pan*speed, tilt);
			text.setText("Hitrost: "+pan*speed+"\nSteer: "+tilt);
			controls.send(Packet.make(STEER, content));
		}

		@Override
		public void OnReleased() {
			String content = Packet.content("STOP");
			text.setText("Stoped");
			controls.send(Packet.make(STOP, content));
		}
	};
	
	
	class JoystickHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BTManager.disconnect:
				layout.setBackgroundResource(
						R.drawable.ozadje);
				break;

			case BTManager.connect:
				int status = msg.arg1;
				if (status == 1) {

					layout.setBackgroundResource(
							R.drawable.ozadje2);

					// statusImage.setImageResource(R.drawable.useronline);
				} else {
					// statusImage.setImageResource(R.drawable.useroffline);
					layout.setBackgroundResource(
							R.drawable.ozadje);
				}
			}
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		BTManager.getManager().unregisterHandler(handler);
	}
}
