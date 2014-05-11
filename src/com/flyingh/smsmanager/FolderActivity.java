package com.flyingh.smsmanager;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FolderActivity extends ListActivity {
	private static final Uri WATCH_FOR_CHANGES_URI = Uri.parse("content://mms-sms/conversations/");
	private static final int[] iconIds = { R.drawable.inbox, R.drawable.outbox, R.drawable.sent, R.drawable.draft };
	private static final int[] boxNameIds = { R.string.inbox, R.string.outbox, R.string.sent, R.string.draft };
	private static final Uri[] uris = { Uri.parse("content://sms/inbox"), Uri.parse("content://sms/outbox"), Uri.parse("content://sms/sent"),
			Uri.parse("content://sms/drafts") };
	// private static final Uri[] uris = { Inbox.CONTENT_URI, Outbox.CONTENT_URI, Sent.CONTENT_URI, Draft.CONTENT_URI };
	private BaseAdapter adapter;
	private ContentObserver observer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder);
		initAdapter();
		initObserver();
		setListAdapter(adapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		getContentResolver().registerContentObserver(WATCH_FOR_CHANGES_URI, true, observer);
	}

	@Override
	protected void onStop() {
		super.onStop();
		getContentResolver().unregisterContentObserver(observer);
	}

	private void initObserver() {
		observer = new ContentObserver(new Handler()) {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				adapter.notifyDataSetChanged();
			}
		};
	}

	public void newMsg(View view) {
		// TODO
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initAdapter() {
		adapter = new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = convertView != null ? convertView : LayoutInflater.from(FolderActivity.this).inflate(R.layout.folder_item, null);
				ImageView iconImageView = (ImageView) view.findViewById(R.id.icon);
				TextView boxTextView = (TextView) view.findViewById(R.id.box);
				final TextView countTextView = (TextView) view.findViewById(R.id.count);
				iconImageView.setImageResource(iconIds[position]);
				boxTextView.setText(boxNameIds[position]);
				new AsyncTask<Uri, Void, Integer>() {

					@Override
					protected Integer doInBackground(Uri... params) {
						Cursor cursor = getContentResolver().query(params[0], new String[] { "count(*)" }, null, null, null);
						if (cursor != null && cursor.moveToFirst()) {
							return cursor.getInt(0);
						}
						return 0;
					}

					@Override
					protected void onPostExecute(Integer result) {
						countTextView.setText(result > 0 ? String.valueOf(result) : null);
					}
				}.execute(uris[position]);
				return view;
			}

			@Override
			public int getCount() {
				return iconIds.length;
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.folder, menu);
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
