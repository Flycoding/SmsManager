package com.flyingh.smsmanager;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.Sms;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ConversationActivity extends Activity {

	private static final String EXTRA_THREAD_ID = "thread_id";
	private static final int SNIPPET_MAX_LENGTH = 10;
	private static final String[] PROJECTION = new String[] { "sms.thread_id as _id,sms.address as address,groups.msg_count as msg_count,sms.body as snippet,sms.date as date" };
	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_ADDRESS = "address";
	private static final String COLUMN_MSG_COUNT = "msg_count";
	private static final String COLUMN_SNIPPET = "snippet";
	private static final String COLUMN_DATE = "date";
	private ListView listView;
	private TextView emptyConversationTextView;
	private CursorAdapter adapter;
	private MenuItem searchMenuItem;
	private MenuItem deleteMenuItem;
	private MenuItem backMenuItem;
	private DisplayMode mode = DisplayMode.NOT_EDIT;// call setMode method to modify
	private Button newMsgButton;
	private Button deleteButton;
	private LinearLayout selectOptionsLinearLayout;
	private final Set<String> selectedThreadIds = new HashSet<>();
	private Button selectAllButton;
	private Button unselectButton;

	enum DisplayMode {
		NOT_EDIT, EDIT;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		init();
		asyncQuery();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void init() {
		findViews();
		listView.setEmptyView(emptyConversationTextView);
		initAdapter();
		listView.setAdapter(adapter);
		setItemClickListeners();
	}

	private void setItemClickListeners() {
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				setMode(DisplayMode.EDIT);
				Cursor cursor = (Cursor) listView.getItemAtPosition(position);
				selectOrNot(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
				return true;
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor) adapter.getItem(position);
				String threadId = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
				if (isEdit(mode)) {
					CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
					checkBox.setChecked(!selectedThreadIds.contains(threadId));
					selectOrNot(threadId);
				} else {
					Intent intent = new Intent(ConversationActivity.this, ConversationDetailActivity.class);
					intent.putExtra(EXTRA_THREAD_ID, threadId);
					startActivity(intent);
				}
			}
		});
	}

	class ViewHolder {
		CheckBox checkBox;
		ImageView imageView;
		TextView nameTextView;
		TextView snippetTextView;
		TextView dateTextView;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private CursorAdapter initAdapter() {
		return adapter = new CursorAdapter(this, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				View view = LayoutInflater.from(context).inflate(R.layout.conversation_item, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkbox);
				viewHolder.imageView = (ImageView) view.findViewById(R.id.imageView);
				viewHolder.nameTextView = (TextView) view.findViewById(R.id.nameTextView);
				viewHolder.snippetTextView = (TextView) view.findViewById(R.id.snippetTextView);
				viewHolder.dateTextView = (TextView) view.findViewById(R.id.dateTextView);
				view.setTag(viewHolder);
				return view;
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				if (cursor == null) {
					return;
				}

				ViewHolder viewHolder = (ViewHolder) view.getTag();
				CheckBox checkBox = viewHolder.checkBox;
				ImageView imageView = viewHolder.imageView;
				TextView nameTextView = viewHolder.nameTextView;
				TextView snippetTextView = viewHolder.snippetTextView;
				TextView dateTextView = viewHolder.dateTextView;

				checkBox.setVisibility(isNotEdit(mode) ? View.GONE : View.VISIBLE);
				String threadId = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
				checkBox.setChecked(selectedThreadIds.contains(threadId));

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
	}

	private void findViews() {
		listView = (ListView) findViewById(R.id.listView);
		emptyConversationTextView = (TextView) findViewById(R.id.emptyConversation);
		newMsgButton = (Button) findViewById(R.id.newMsgButton);
		selectAllButton = (Button) findViewById(R.id.selectAllButton);
		unselectButton = (Button) findViewById(R.id.unselectButton);
		deleteButton = (Button) findViewById(R.id.deleteButton);
		selectOptionsLinearLayout = (LinearLayout) findViewById(R.id.layout_select_options);
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

	public void newMsg(View view) {
		startActivity(new Intent(this, NewMessageActivity.class));
	}

	public void selectAll(View view) {
		addAll(adapter.getCursor());
	}

	private void addAll(Cursor cursor) {
		cursor.moveToPosition(-1);
		while (cursor.moveToNext()) {
			selectedThreadIds.add(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
		}
		onSelectedChanged();
		adapter.notifyDataSetChanged();
	}

	public void unselect(View view) {
		selectedThreadIds.clear();
		onSelectedChanged();
		adapter.notifyDataSetChanged();
	}

	public void delete(View view) {
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.confirm_to_delete_)
				.setMessage(R.string.are_you_sure_to_delele_the_selected_conversations_)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO

					}
				}).setNegativeButton(android.R.string.cancel, null).show();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (isEdit(mode)) {
			if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
				onBack();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.conversation, menu);
		searchMenuItem = menu.findItem(R.id.search);
		deleteMenuItem = menu.findItem(R.id.delete);
		backMenuItem = menu.findItem(R.id.back);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean notEdit = isNotEdit(mode);
		searchMenuItem.setVisible(notEdit);
		deleteMenuItem.setVisible(notEdit);
		backMenuItem.setVisible(!notEdit);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.search:
			// TODO
			break;
		case R.id.delete:
			onDelete();
			break;
		case R.id.back:
			onBack();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onBack() {
		setMode(DisplayMode.NOT_EDIT);
		selectedThreadIds.clear();
	}

	private void onDelete() {
		setMode(DisplayMode.EDIT);
		onSelectedChanged();
	}

	private void setMode(DisplayMode mode) {
		this.mode = mode;
		onModeChanged(mode);
	}

	private void onModeChanged(DisplayMode mode) {
		boolean notEdit = isNotEdit(mode);
		newMsgButton.setVisibility(notEdit ? View.VISIBLE : View.GONE);
		selectOptionsLinearLayout.setVisibility(notEdit ? View.GONE : View.VISIBLE);
		deleteButton.setVisibility(notEdit ? View.GONE : View.VISIBLE);
	}

	private boolean isNotEdit(DisplayMode mode) {
		return mode == DisplayMode.NOT_EDIT;
	}

	private boolean isEdit(DisplayMode mode) {
		return mode == DisplayMode.EDIT;
	}

	private void selectOrNot(String threadId) {
		if (selectedThreadIds.contains(threadId)) {
			selectedThreadIds.remove(threadId);
		} else {
			selectedThreadIds.add(threadId);
		}
		onSelectedChanged();
	}

	private void onSelectedChanged() {
		selectAllButton.setEnabled(selectedThreadIds.size() != adapter.getCount());
		unselectButton.setEnabled(!selectedThreadIds.isEmpty());
		deleteButton.setEnabled(!selectedThreadIds.isEmpty());
	}
}
