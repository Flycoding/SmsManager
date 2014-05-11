package com.flyingh.smsmanager;

import java.util.ArrayList;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class NewMessageActivity extends ActionBarActivity {

	private static final String[] PROJECTION = new String[] { Contacts._ID, Contacts.DISPLAY_NAME, Phone.NUMBER };
	private static final Uri SENT_CONTENT_URI = Uri.parse("content://mms/sent");
	private static final String COLUMN_BODY = "body";
	private static final String COLUMN_ADDRESS = "address";
	private AutoCompleteTextView addressAutoCompleteTextView;
	private EditText bodyEditText;
	private SimpleCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_message);
		addressAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.address);
		bodyEditText = (EditText) findViewById(R.id.body);
		initAdapter();
		addressAutoCompleteTextView.setAdapter(adapter);
		addressAutoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor) adapter.getItem(position);
				String address = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
				addressAutoCompleteTextView.setText(address);
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initAdapter() {
		adapter = new SimpleCursorAdapter(this, R.layout.address_item, null, new String[] { Contacts.DISPLAY_NAME, Phone.NUMBER }, new int[] {
				R.id.contactName, R.id.contactNumber }, 0) {
			@Override
			public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
				Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, PROJECTION, Phone.NUMBER + " like ?",
						new String[] { String.valueOf("%" + constraint + "%") }, null);
				return cursor;
			}
		};
	}

	public void sendMsg(View view) {
		String address = addressAutoCompleteTextView.getText().toString().trim();
		String body = bodyEditText.getText().toString().trim();
		if (TextUtils.isEmpty(address) || TextUtils.isEmpty(body)) {
			Toast.makeText(this, R.string.phone_number_and_message_should_not_be_empty, Toast.LENGTH_SHORT).show();
			return;
		}
		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> msgs = smsManager.divideMessage(body);
		for (String msg : msgs) {
			smsManager.sendTextMessage(address, null, msg, null, null);
			ContentValues values = new ContentValues();
			values.put(COLUMN_ADDRESS, address);
			values.put(COLUMN_BODY, body);
			getContentResolver().insert(SENT_CONTENT_URI, values);
		}
		Toast.makeText(this, R.string.send_success, Toast.LENGTH_SHORT).show();
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_message, menu);
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
