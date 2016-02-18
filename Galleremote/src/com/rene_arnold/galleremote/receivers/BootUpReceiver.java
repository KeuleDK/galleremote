package com.rene_arnold.galleremote.receivers;

import com.rene_arnold.galleremote.FullscreenActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, FullscreenActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}

}
