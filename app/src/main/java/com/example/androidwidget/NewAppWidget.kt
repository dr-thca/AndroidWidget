package com.example.androidwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class NewAppWidget : AppWidgetProvider(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        job.cancel() // Cancel the job when the last widget is disabled
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        launch {
            try {
                val fetcher = ArticleFetcher()
                val articles = withContext(Dispatchers.IO) {
                    fetcher.fetchArticles()
                }

                if (articles.isNotEmpty()) {
                    val views = RemoteViews(context.packageName, R.layout.new_app_widget)
                    val title = articles[0].title
                    val imageUrl = articles[0].imageUrl

                    views.setTextViewText(R.id.news_title, title)

                    if (imageUrl != null) {
                        // Uncomment and implement WidgetImageLoader
                        println("Loading image")
                        WidgetImageLoader(context, views, appWidgetId, R.id.news_image).loadImage(imageUrl)
                    }

                    // Instruct the widget manager to update the widget
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } else {
                    Log.e("NewAppWidget", "No articles fetched")
                }
            } catch (e: Exception) {
                Log.e("NewAppWidget", "Error updating widget: ${e.message}", e)
            }
        }
    }
}