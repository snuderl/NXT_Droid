package org.nxt.droid;

import static org.nxt.droid.NXT_Commands.*;

import android.app.Activity;
import android.os.Bundle;

public class JoystickControlActivity extends Activity {

	JostickView joystick;
	BT controls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joysticklayout);

		joystick = (JostickView) findViewById(R.id.jostickView1);
		controls = BT.getBT();

		joystick.setOnJostickMovedListener(_listener);
	}

	private JostickMovedListener _listener = new JostickMovedListener() {

		@Override
		public void OnMoved(int pan, int tilt) {
			float speed = ((Tab) getParent()).speed.getSpeed();
			String content = Packet.content(pan*speed, tilt);
			controls.send(Packet.make(STEER, content));
		}

		@Override
		public void OnReleased() {
			String content = Packet.content("STOP");
			controls.send(Packet.make(STOP, content));
		}
	};
}
