package com.example.androidwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.provider.Settings.Global
import android.widget.RemoteViews
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

@OptIn(DelicateCoroutinesApi::class)
internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    GlobalScope.launch {
        val fetcher = ArticleFetcher()
        val articles = fetcher.fetchArticles()
        articles.forEach { println(it) }
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.new_app_widget)

        val title = articles[0].title
        val imageUrl = articles[0].imageUrl!!
        views.setTextViewText(R.id.news_title, title)
        //WidgetImageLoader(context, views, appWidgetId, R.id.news_image).loadImage(imageUrl)


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}