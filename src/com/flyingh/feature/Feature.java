package com.flyingh.feature;

import android.app.Activity;

import com.flyingh.smsmanager.ConversationActivity;
import com.flyingh.smsmanager.FolderActivity;
import com.flyingh.smsmanager.GroupActivity;
import com.flyingh.smsmanager.R;

public enum Feature {
	// @formatter:off
	CONVERSATION(R.string.conversation, R.drawable.tab_conversation, ConversationActivity.class), 
	FOLDER(R.string.folder, R.drawable.tab_folder, FolderActivity.class), 
	GROUP(R.string.group, R.drawable.tab_group,	GroupActivity.class);
	// @formatter:on
	private int descId;
	private int drawableId;
	private Class<? extends Activity> contentClass;

	private Feature(int descId, int drawableId, Class<? extends Activity> contentClass) {
		this.descId = descId;
		this.drawableId = drawableId;
		this.contentClass = contentClass;
	}

	public int getDescId() {
		return descId;
	}

	public int getDrawableId() {
		return drawableId;
	}

	public Class<? extends Activity> getContentClass() {
		return contentClass;
	}

}
