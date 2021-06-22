package com.cobresun.brun.pantsorshorts.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.cobresun.brun.pantsorshorts.MainActivity
import com.cobresun.brun.pantsorshorts.R

class MyAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            // Launch app when widget is clicked
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java).let { intent ->
                PendingIntent.getActivity(context, 0, intent, 0)
            }

            val views: RemoteViews = RemoteViews(context.packageName, R.layout.appwidget).apply {
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            context.startService(Intent(context, UpdateWidgetService::class.java))
        }
    }
}

