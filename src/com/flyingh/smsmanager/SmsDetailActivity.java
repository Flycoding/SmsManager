package com.flyingh.smsmanager;

import java.util.Date;
import java.util.Locale;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony.Sms;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SmsDetailActivity extends ActionBarActivity {

	private static final String COLUMN_DATE = "date";
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_BODY = "body";
	private static final String COLUMN_ADDRESS = "address";
	private static final String COLUMN__ID = "_id";
	private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	private ImageView imageView;
	private TextView nameTextView;
	private TextView numberTextView;
	private TextView receiveBodyTextView;
	private TextView receiveDateTextView;
	private TextView sendBodyTextView;
	private TextView sendDateTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_detail);
		findViews();
		asyncLoadData();
	}

	private void asyncLoadData() {
		new AsyncTask<Integer, Cursor, Cursor>() {

			@Override
			protected Cursor doInBackground(Integer... params) {
				Cursor cursor = getContentResolver().query(SMS_CONTENT_URI,
						new String[] { COLUMN__ID, COLUMN_ADDRESS, COLUMN_BODY, COLUMN_TYPE, COLUMN_DATE }, Sms._ID + "=?",
						new String[] { String.valueOf(params[0]) }, null);
				cursor.moveToFirst();
				String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));
				publishProgress(cursor);
				return getContentResolver().query(Phone.CONTENT_URI, new String[] { Contacts.DISPLAY_NAME }, Phone.NUMBER + "=?",
						new String[] { address }, null);
			}

			@Override
			protected void onProgressUpdate(Cursor... values) {
				Cursor cursor = values[0];
				cursor.moveToFirst();
				String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));
				String body = cursor.getString(cursor.getColumnIndex(COLUMN_BODY));
				long dateMillis = cursor.getLong(cursor.getColumnIndex(COLUMN_DATE));
				int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
				numberTextView.setText(address);
				String dateOrTime = String.format(Locale.getDefault(), DateUtils.isToday(dateMillis) ? "%1$tT" : "%1$tF %1$tT", new Date(dateMillis));
				if (type == 1) {
					receiveBodyTextView.setText(body);
					receiveBodyTextView.setBackgroundColor(Color.YELLOW);
					receiveDateTextView.setText(dateOrTime);
					sendBodyTextView.setVisibility(View.GONE);
					sendDateTextView.setVisibility(View.GONE);
				} else if (type == 2) {
					sendBodyTextView.setText(body);
					sendBodyTextView.setBackgroundColor(Color.GREEN);
					sendDateTextView.setText(dateOrTime);
					receiveBodyTextView.setVisibility(View.GONE);
					receiveDateTextView.setVisibility(View.GONE);
				}
				cursor.close();
			}

			@Override
			protected void onPostExecute(Cursor cursor) {
				if (cursor != null && cursor.moveToFirst()) {
					String name = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
					nameTextView.setText(name);
					imageView.setImageResource(TextUtils.isEmpty(name) ? R.drawable.ic_unknown_picture_normal : R.drawable.ic_contact_picture);
					cursor.close();
				}
			}

		}.execute(getIntent().getIntExtra(FolderDetailActivity.EXTRA_ID, 0));
	}

	private void findViews() {
		imageView = (ImageView) findViewById(R.id.imageView);
		nameTextView = (TextView) findViewById(R.id.nameTextView);
		numberTextView = (TextView) findViewById(R.id.numberTextView);
		receiveBodyTextView = (TextView) findViewById(R.id.receive_body);
		receiveDateTextView = (TextView) findViewById(R.id.receive_date);
		sendBodyTextView = (TextView) findViewById(R.id.send_body);
		sendDateTextView = (TextView) findViewById(R.id.send_date);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sms_detail, menu);
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
