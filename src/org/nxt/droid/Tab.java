package org.nxt.droid;


import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class Tab extends TabActivity {
	
	
	private TabHost mTabHost;



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// construct the tabhost
		setContentView(R.layout.tablayout);

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.getRootView().setKeepScreenOn(true);
		mTabHost.setup();

		Intent intent = new Intent().setClass(this, GyroControlActivity.class);

		String deviceName = getIntent().getExtras().getString("device");
		intent.putExtra("device", deviceName);
		setupTab(new TextView(this), "Gyro",intent);
		intent = new Intent().setClass(this, JoystickControlActivity.class);
		setupTab(new TextView(this), "Jostick",intent);
		
		mTabHost.setCurrentTab(0);
	}

	private void setupTab(final View view, final String tag, Intent i) {
		View tabview = createTabView(mTabHost.getContext(), tag);

		TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(i);
		mTabHost.addTab(setContent);

	}

	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}

	

}