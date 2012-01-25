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
	View layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);

		layout = findViewById(R.id.statsid);
		if (BT.getBT().isConnected()) {
			layout.setBackgroundResource(R.drawable.ozadje2);
		} else {
			layout.setBackgroundResource(R.drawable.ozadje);
		}

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
				text.setText(recived);
				//				Packet p = new Packet(recived);
//				if (p.command.equals(NXT_Commands.STATS)) {
//					StringBuilder b = new StringBuilder();
//					for (Object o : p.values) {
//						b.append(o.toString() + "/n");
//					}
//					text.setText(b.toString());
//				}

			case BTManager.disconnect:
				// statusImage.setImageResource(R.drawable.useroffline);
				layout.setBackgroundResource(R.drawable.ozadje);
				break;

			case BTManager.connect:
				int status = msg.arg1;
				if (status == 1) {

					layout.setBackgroundResource(R.drawable.ozadje2);

					// statusImage.setImageResource(R.drawable.useronline);
				} else {
					// statusImage.setImageResource(R.drawable.useroffline);
					layout.setBackgroundResource(R.drawable.ozadje);
				}
				// TODO Auto-generated method stub
				super.handleMessage(msg);
			}
		}
	}
}
