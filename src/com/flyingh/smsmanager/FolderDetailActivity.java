package com.flyingh.smsmanager;

import java.util.Date;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FolderDetailActivity extends ActionBarActivity {

	public static final String EXTRA_ID = "extra_id";
	private static final String COLUMN_ADDRESS = "address";
	private static final String COLUMN_BODY = "body";
	private static final String COLUMN_DATE = "date";
	private static final String DEFAULT_SORT_ORDER = "date ASC";
	private ListView listView;
	private CursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder_detail);
		setTitle(getIntent().getIntExtra(FolderActivity.EXTRA_TITLE, R.string.inbox));
		listView = (ListView) findViewById(R.id.listView);
		initAdapter();
		listView.setAdapter(adapter);
		asyncQuery();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(FolderDetailActivity.this, SmsDetailActivity.class);
				Cursor cursor = (Cursor) adapter.getItem(position);
				intent.putExtra(EXTRA_ID, cursor.getInt(cursor.getColumnIndex(Sms._ID)));
				startActivity(intent);
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void asyncQuery() {
		getLoaderManager().initLoader(0, null, new LoaderCallbacks<Cursor>() {

			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				return new CursorLoader(FolderDetailActivity.this, (Uri) getIntent().getParcelableExtra(FolderActivity.EXTRA_URI), new String[] {
						Sms._ID, COLUMN_ADDRESS, COLUMN_BODY, COLUMN_DATE }, null, null, DEFAULT_SORT_ORDER);
			}

			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				adapter.swapCursor(data);
			}

			@Override
			public void onLoaderReset(Loader<Cursor> loader) {
				adapter.swapCursor(null);
			}
		});
	}

	class FolderItemViewHolder {
		TextView dateGroupTextView;
		ImageView imageView;
		TextView nameTextView;
		TextView bodyTextView;
		TextView dateTextView;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initAdapter() {
		adapter = new CursorAdapter(this, null, 0) {

			private ImageView imageView;
			private TextView nameTextView;
			private TextView dateTextView;
			private TextView bodyTextView;
			private TextView dateGroupTextView;

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				View view = LayoutInflater.from(context).inflate(R.layout.folder_detail_item, null);
				FolderItemViewHolder folderItemViewHolder = new FolderItemViewHolder();
				folderItemViewHolder.dateGroupTextView = (TextView) view.findViewById(R.id.dateGroup);
				folderItemViewHolder.imageView = (ImageView) view.findViewById(R.id.imageView);
				folderItemViewHolder.nameTextView = (TextView) view.findViewById(R.id.nameTextView);
				folderItemViewHolder.bodyTextView = (TextView) view.findViewById(R.id.bodyTextView);
				folderItemViewHolder.dateTextView = (TextView) view.findViewById(R.id.dateTextView);
				view.setTag(folderItemViewHolder);
				return view;
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				FolderItemViewHolder folderItemViewHolder = (FolderItemViewHolder) view.getTag();
				dateGroupTextView = folderItemViewHolder.dateGroupTextView;
				imageView = folderItemViewHolder.imageView;
				nameTextView = folderItemViewHolder.nameTextView;
				bodyTextView = folderItemViewHolder.bodyTextView;
				dateTextView = folderItemViewHolder.dateTextView;
				String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));
				String body = cursor.getString(cursor.getColumnIndex(COLUMN_BODY));
				long dateMillis = cursor.getLong(cursor.getColumnIndex(COLUMN_DATE));
				Cursor nameCursor = getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address)),
						new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
				boolean hasName = nameCursor != null && nameCursor.moveToFirst();
				imageView.setImageResource(hasName ? R.drawable.ic_contact_picture : R.drawable.ic_unknown_picture_normal);
				nameTextView.setText(hasName ? nameCursor.getString(nameCursor.getColumnIndex(PhoneLookup.DISPLAY_NAME)) : address);
				nameCursor.close();
				bodyTextView.setText(body);
				bodyTextView.setBackgroundColor(getBodyColor());
				dateTextView.setText(String.format(DateUtils.isToday(dateMillis) ? "%1$tT" : "%1$tF %1$tT", new Date(dateMillis)));
				String date = String.format("%tF", new Date(dateMillis));
				if (cursor.moveToPrevious()) {
					long previousDateMillis = cursor.getLong(cursor.getColumnIndex(COLUMN_DATE));
					String previousDate = String.format("%tF", new Date(previousDateMillis));
					if (date.equals(previousDate)) {
						dateGroupTextView.setVisibility(View.GONE);
					} else {
						dateGroupTextView.setVisibility(View.VISIBLE);
						dateGroupTextView.setText(date);
					}
					cursor.moveToNext();
				} else {
					dateGroupTextView.setVisibility(View.VISIBLE);
					dateGroupTextView.setText(date);
				}
			}

			private int getBodyColor() {
				int titleId = getIntent().getIntExtra(FolderActivity.EXTRA_TITLE, 0);
				switch (titleId) {
				case R.string.inbox:
					return Color.YELLOW;
				case R.string.outbox:
					return Color.GREEN;
				case R.string.sent:
					return Color.GREEN;
				case R.string.draft:
					return Color.RED;
				default:
					break;
				}
				return 0;
			}

		};
	}

	public void newMsg(View view) {
		// TODO
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.folder_detail, menu);
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
