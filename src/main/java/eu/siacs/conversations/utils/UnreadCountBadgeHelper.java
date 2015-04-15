package eu.siacs.conversations.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
			updateSamsungApi(context, packageName, activity, count);
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
			contentValues.put("tag", packageName + "/" + activity);
			contentValues.put("count", count);
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
		intent.putExtra("package", packageName);
		//intent.putExtra("class",packageName+"."+activity);
		intent.putExtra("class", activity);
		intent.putExtra("count", count);
		context.sendBroadcast(intent);
	}

	private static void updateSamsungApi(final Context context,
										 final String packageName,
										 final String activity,
										 final int count) {
		Uri mUri = Uri.parse("content://com.sec.badge/apps?notify=true");
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = null;
		ContentValues contentValues = new ContentValues();
		contentValues.put("badgecount", count);
		try {
			cursor = contentResolver.query(mUri,new String[]{"_id"}, "package=?", new String[]{packageName}, null);
			if (cursor != null) {
				boolean entryActivityExist = false;
				while (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					contentResolver.update(mUri, contentValues, "_id=?", new String[]{String.valueOf(id)});
					if (activity.equals(cursor.getString(cursor.getColumnIndex("class")))) {
						entryActivityExist = true;
					}
				}
				if (!entryActivityExist) {
					contentValues.put("package", packageName);
					contentValues.put("class", activity);
					contentResolver.insert(mUri, contentValues);
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}
