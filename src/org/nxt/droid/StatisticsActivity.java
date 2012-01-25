package org.nxt.droid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StatisticsActivity extends Activity {
	TextView text;
	Button getStats;
	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);

		text = (TextView) findViewById(R.id.textStats);
		getStats = (Button) findViewById(R.id.btnStats);
		getStats.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String packet = Packet.make(NXT_Commands.STATS,
						NXT_Commands.STATS, true);
				BT.getBT().send(packet);
			}
		});

		handler = new StatsHandler();
		BTManager.getManager().registerHandler(handler);

	}

	@Override
	protected void onStop() {
		super.onStop();
		BTManager.getManager().unregisterHandler(handler);
	}

	class StatsHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BTManager.messageRecived:
				String recived = (String) msg.getData().get("vsebina");
				Packet p = new Packet(recived);
				if (p.command.equals(NXT_Commands.STATS)) {
					text.setText(p.packet);
				}
			}
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}
	}
}
