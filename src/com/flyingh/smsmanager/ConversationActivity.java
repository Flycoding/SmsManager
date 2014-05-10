package com.flyingh.smsmanager;

import java.util.Date;

import android.annotation.TargetApi;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.Sms;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ConversationActivity extends ActionBarActivity {

	private static final int SNIPPET_MAX_LENGTH = 10;
	private static final String[] PROJECTION = new String[] { "sms.thread_id as _id,sms.address as address,groups.msg_count as msg_count,sms.body as snippet,sms.date as date" };
	// private static final String COLUMN_ID = "_id";
	private static final String COLUMN_ADDRESS = "address";
	private static final String COLUMN_MSG_COUNT = "msg_count";
	private static final String COLUMN_SNIPPET = "snippet";
	private static final String COLUMN_DATE = "date";
	private ListView listView;
	private TextView emptyConversationTextView;
	private CursorAdapter adapter;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		listView = (ListView) findViewById(R.id.listView);
		emptyConversationTextView = (TextView) findViewById(R.id.emptyConversation);
		listView.setEmptyView(emptyConversationTextView);
		adapter = new CursorAdapter(this, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {

			private CheckBox checkBox;
			private ImageView imageView;
			private TextView nameTextView;
			private TextView snippetTextView;
			private TextView dateTextView;

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				View view = LayoutInflater.from(context).inflate(R.layout.conversation_item, null);
				checkBox = (CheckBox) view.findViewById(R.id.checkbox);
				imageView = (ImageView) view.findViewById(R.id.imageView);
				nameTextView = (TextView) view.findViewById(R.id.nameTextView);
				snippetTextView = (TextView) view.findViewById(R.id.snippetTextView);
				dateTextView = (TextView) view.findViewById(R.id.dateTextView);
				return view;
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				if (cursor == null) {
					return;
				}
				checkBox.setSelected(false);
				String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));
				Cursor nameCursor = getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address),
						new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
				boolean flag = nameCursor != null && nameCursor.moveToFirst();
				imageView.setImageResource(flag ? R.drawable.ic_contact_picture : R.drawable.ic_unknown_picture_normal);
				nameTextView.setText(flag ? nameCursor.getString(nameCursor.getColumnIndex(PhoneLookup.DISPLAY_NAME)) : address);
				int msgCount = cursor.getInt(cursor.getColumnIndex(COLUMN_MSG_COUNT));
				if (msgCount > 1) {
					nameTextView.append("(" + msgCount + ")");
				}
				String snippet = cursor.getString(cursor.getColumnIndex(COLUMN_SNIPPET));
				snippetTextView.setText(snippet.length() > SNIPPET_MAX_LENGTH ? snippet.substring(0, SNIPPET_MAX_LENGTH) : snippet);
				if (snippet.length() > SNIPPET_MAX_LENGTH) {
					snippetTextView.append("...");
				}
				long longMillis = cursor.getLong(cursor.getColumnIndex(COLUMN_DATE));
				dateTextView.setText(String.format(DateUtils.isToday(longMillis) ? "%1$tT" : "%1$tF %1$tT", new Date(longMillis)));
				nameCursor.close();
			}

		};
		listView.setAdapter(adapter);
		asyncQuery();
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void asyncQuery() {
		new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, android.database.Cursor cursor) {
				adapter.changeCursor(cursor);
			}
		}.startQuery(0, null, Sms.Conversations.CONTENT_URI, PROJECTION, null, null, " date DESC");
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
