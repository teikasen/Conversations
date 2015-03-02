package eu.siacs.conversations.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import eu.siacs.conversations.Config;

public class UnreadCountBadgeHelper {

	private static int count = 0;

	public static void update(final Context context,
							  final int count) {
		if (UnreadCountBadgeHelper.count != count) {
			UnreadCountBadgeHelper.count = count;
			Log.d(Config.LOGTAG, "updating read count to: " + count);
			final String packageName = context.getPackageName();
			final String activity = context.getPackageManager().getLaunchIntentForPackage(packageName).getComponent().getClassName();
			updateTeslaUnreadApi(context, packageName, activity, count);
			updateApexNotificationApi(context, packageName, activity, count);
		}
	}

	/*
	TeslaUnread API primarily used by Nova Launcher
	documented here: http://novalauncher.com/teslaunread-api/
	 */
	private static void updateTeslaUnreadApi(final Context context,
											 final String packageName,
											 final String activity,
											 final int count) {
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put("tag",packageName+"/"+activity);
			contentValues.put("count",count);
			context.getContentResolver().insert(
					Uri.parse("content://com.teslacoilsw.notifier/unread_count"),
					contentValues
					);
		} catch (IllegalArgumentException e) {
			return;
		} catch (Exception e) {
			return;
		}
	}

	/*
	Apex Launcher Notifications API used by Apex Launcher Pro and others (Smart Launcher 2)
	 */
	private static void updateApexNotificationApi(final Context context,
												  final String packageName,
												  final String activity,
												  final int count) {
		Intent intent = new Intent("com.anddoes.launcher.COUNTER_CHANGED");
		intent.putExtra("package",packageName);
		//intent.putExtra("class",packageName+"."+activity);
		intent.putExtra("class",activity);
		intent.putExtra("count",count);
		context.sendBroadcast(intent);
	}
}
