package com.flyingh.smsmanager;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConversationDetailActivity extends ActionBarActivity {

	private static final Uri SENT_CONTENT_URI = Uri.parse("content://sms/sent");
	private static final String COLUMN_ADDRESS = "address";
	private static final int SEND_BACKGROUND_COLOR = Color.GREEN;
	private static final int RECEIVE_BACKGROUND_COLOR = Color.YELLOW;
	private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	private static final String DEFAULT_SORT_ORDER = "date ASC";
	private static final String COLUMN_ID = "_ID";
	private static final String COLUMN_THREAD_ID = "thread_id";
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_DATE = "date";
	private static final String COLUMN_BODY = "body";
	private ImageView imageView;
	private TextView nameTextView;
	private TextView numberTextView;
	private ListView listView;
	private CursorAdapter adapter;
	private EditText msgEditText;
	private LoaderCallbacks<Cursor> callback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation_detail);
		findViews();
		initHeader();
		initBody();
	}

	private void initBody() {
		initAdapter();
		asyncQuery();
		listView.setAdapter(adapter);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void asyncQuery() {
		callback = new LoaderCallbacks<Cursor>() {

			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				return new CursorLoader(ConversationDetailActivity.this, SMS_CONTENT_URI, new String[] { COLUMN_ID, COLUMN_BODY, COLUMN_DATE,
						COLUMN_TYPE }, COLUMN_THREAD_ID + "=?", new String[] { getIntent().getStringExtra(ConversationActivity.EXTRA_THREAD_ID) },
						DEFAULT_SORT_ORDER);
			}

			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				adapter.swapCursor(data);
			}

			@Override
			public void onLoaderReset(Loader<Cursor> loader) {
				adapter.swapCursor(null);
			}
		};
		getLoaderManager().initLoader(0, null, callback);
	}

	class DetailViewHolder {
		TextView receiveBodyTextView;
		TextView receiveDateTextView;
		TextView sendDateTextView;
		TextView sendBodyTextView;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initAdapter() {
		adapter = new CursorAdapter(this, null, 0) {

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				View view = LayoutInflater.from(context).inflate(R.layout.conversation_detail_item, null);
				DetailViewHolder detailViewHolder = new DetailViewHolder();
				detailViewHolder.receiveBodyTextView = (TextView) view.findViewById(R.id.receive_body);
				detailViewHolder.receiveDateTextView = (TextView) view.findViewById(R.id.receive_date);
				detailViewHolder.sendDateTextView = (TextView) view.findViewById(R.id.send_date);
				detailViewHolder.sendBodyTextView = (TextView) view.findViewById(R.id.send_body);
				view.setTag(detailViewHolder);
				return view;
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
				DetailViewHolder holder = (DetailViewHolder) view.getTag();
				TextView receiveBodyTextView = holder.receiveBodyTextView;
				TextView receiveDateTextView = holder.receiveDateTextView;
				TextView sendDateTextView = holder.sendDateTextView;
				TextView sendBodyTextView = holder.sendBodyTextView;
				if (type == 1) {
					receiveBodyTextView.setText(cursor.getString(cursor.getColumnIndex(COLUMN_BODY)));
					receiveBodyTextView.setBackgroundColor(RECEIVE_BACKGROUND_COLOR);
					long dateMillis = cursor.getLong(cursor.getColumnIndex(COLUMN_DATE));
					if (DateUtils.isToday(dateMillis)) {
						receiveDateTextView.setText(DateFormat.getTimeFormat(ConversationDetailActivity.this).format(new Date(dateMillis)));
					} else {
						receiveDateTextView.setText(DateFormat.getDateFormat(ConversationDetailActivity.this).format(new Date(dateMillis)));
					}
					sendDateTextView.setVisibility(View.GONE);
					sendBodyTextView.setVisibility(View.GONE);
				} else if (type == 2) {
					sendBodyTextView.setText(cursor.getString(cursor.getColumnIndex(COLUMN_BODY)));
					sendBodyTextView.setBackgroundColor(SEND_BACKGROUND_COLOR);
					long dateMillis = cursor.getLong(cursor.getColumnIndex(COLUMN_DATE));
					if (DateUtils.isToday(dateMillis)) {
						sendDateTextView.setText(DateFormat.getTimeFormat(ConversationDetailActivity.this).format(new Date(dateMillis)));
					} else {
						sendDateTextView.setText(DateFormat.getDateFormat(ConversationDetailActivity.this).format(new Date(dateMillis)));
					}
					receiveDateTextView.setVisibility(View.GONE);
					receiveBodyTextView.setVisibility(View.GONE);
				}
			}
		};
	}

	private void initHeader() {
		Intent intent = getIntent();
		imageView.setImageResource(intent.getIntExtra(ConversationActivity.EXTRA_IMAGE_RES_ID, 0));
		nameTextView.setText(intent.getStringExtra(ConversationActivity.EXTRA_NAME_WITH_MSG_COUNT));
		numberTextView.setText(intent.getStringExtra(ConversationActivity.EXTRA_ADDRESS));
	}

	private void findViews() {
		imageView = (ImageView) findViewById(R.id.imageView);
		nameTextView = (TextView) findViewById(R.id.nameTextView);
		numberTextView = (TextView) findViewById(R.id.numberTextView);
		listView = (ListView) findViewById(R.id.listView);
		msgEditText = (EditText) findViewById(R.id.msgEditText);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void send(View view) {
		String text = msgEditText.getText().toString().trim();
		if (TextUtils.isEmpty(text)) {
			Toast.makeText(this, "message should not be empty", Toast.LENGTH_SHORT).show();
			return;
		}
		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> msgs = smsManager.divideMessage(text);
		String address = getIntent().getStringExtra(ConversationActivity.EXTRA_ADDRESS);
		for (String msg : msgs) {
			smsManager.sendTextMessage(address, null, msg, null, null);
			ContentValues values = new ContentValues();
			values.put(COLUMN_BODY, msg);
			values.put(COLUMN_ADDRESS, address);
			Uri insert = getContentResolver().insert(SENT_CONTENT_URI, values);
			System.out.println(insert);
		}
		msgEditText.setText(null);
		Toast.makeText(this, "send success", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conversation_detail, menu);
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
