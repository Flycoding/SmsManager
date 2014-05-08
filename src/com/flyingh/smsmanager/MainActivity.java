package com.flyingh.smsmanager;

import android.annotation.TargetApi;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.flyingh.feature.Feature;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void init() {
		for (Feature feature : Feature.values()) {
			String desc = getString(feature.getDescId());
			Drawable drawable = getResources().getDrawable(feature.getDrawableId());
			TabSpec tabSpec = getTabHost().newTabSpec(feature.name()).setIndicator(desc, drawable)
					.setContent(new Intent(this, feature.getContentClass()));
			getTabHost().addTab(tabSpec);
			LinearLayout linearLayout = (LinearLayout) getTabHost().getTabWidget().getChildTabViewAt(feature.ordinal());
			TextView textView = (TextView) linearLayout.getChildAt(1);
			textView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
