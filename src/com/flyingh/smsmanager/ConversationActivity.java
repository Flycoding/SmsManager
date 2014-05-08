package com.flyingh.smsmanager;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ConversationActivity extends ActionBarActivity {

	private ListView listView;
	private TextView emptyConversationTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		listView = (ListView) findViewById(R.id.listView);
		emptyConversationTextView = (TextView) findViewById(R.id.emptyConversation);
		listView.setEmptyView(emptyConversationTextView);
	}

	public void newMessage(View view) {

	}

	public void selectAll(View view) {

	}

	public void unselectAll(View view) {

	}

	public void delete(View view) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conversation, menu);
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
