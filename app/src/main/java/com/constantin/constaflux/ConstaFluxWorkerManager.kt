package com.constantin.constaflux

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.constantin.constaflux.ConstaFluxApplication.Companion.CHANNEL_NEW_ENTRY_ID
import com.constantin.constaflux.data.db.dao.*
import com.constantin.constaflux.data.db.entity.EntryIdTableEntity
import com.constantin.constaflux.data.db.entity.FeedEntity
import com.constantin.constaflux.data.network.MinifluxApiProvider
import com.constantin.constaflux.ui.activity.host.HostActivity
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import org.threeten.bp.OffsetDateTime
import retrofit2.HttpException
import java.io.IOException

class ConstaFluxWorkerManager(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KodeinAware {
    override val kodein: Kodein by closestKodein(context)
    private val minifluxApiService: MinifluxApiProvider by instance()

    private val categoryDao: CategoryDao by instance()
    private val feedsDao: FeedsDao by instance()
    private val entryDao: EntryDao by instance()
    private val entryIdTableDao: EntryIdTableDao by instance()
    private val meDao: MeDao by instance()

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(applicationContext)


    companion object {
        const val UNIQUE_WORKER_NAME_TRACKER = "workerTrackerPeriodic"
        const val NOTIFICATION_ID_ENTRY = 0
    }

    override suspend fun doWork(): Result {

        fetchAndPersist()

        return Result.success()
    }

    private suspend fun fetchAndPersist() {
        try {
            val categoriesResponse = minifluxApiService.getApi().getCategories()

            val feedsResponse = minifluxApiService
                .getApi().getFeeds()
            val feedEntity = feedsResponse.map { feed ->
                val feedIcon = try {
                    minifluxApiService.getApi().getFeedIcon(feed.id).data
                } catch (e: Exception) {
                    null
                }

                FeedEntity(
                    feed.id,
                    feed.title,
                    feed.siteUrl,
                    feed.feedUrl,
                    feed.checkedAt,
                    feed.category.id,
                    feedIcon,
                    feed.scraperRules,
                    feed.rewriteRules,
                    feed.crawler,
                    feed.username,
                    feed.password,
                    feed.userAgent
                )
            }

            val entriesUnreadResponse = minifluxApiService.getApi()
                .getEntries(status = "unread", starred = null, search = null, after = null)
            entriesUnreadResponse.category = EntryIdTableEntity.ALL

            val entriesReadResponse = minifluxApiService.getApi()
                .getEntries(status = "read", starred = null, search = null, after = null)
            entriesReadResponse.category = EntryIdTableEntity.ALL

            val meResponse = minifluxApiService.getApi().getMe()

            val latestUnreadEntry = entryDao.getLatestEntry()

            entryDao.insertAllForWorker(
                categoryDao,
                feedsDao,
                entryIdTableDao,
                meDao,
                categoriesResponse,
                feedEntity,
                entriesUnreadResponse,
                entriesReadResponse,
                meResponse
            )


            if (latestUnreadEntry != null && entriesUnreadResponse.entryEntities.isNotEmpty()) {
                val offsetDateTimeOldEntries =
                    OffsetDateTime.parse(latestUnreadEntry)
                val offsetDateTimeNewEntries =
                    OffsetDateTime.parse(entriesUnreadResponse.entryEntities[0].entryPublishedAt)

                if (offsetDateTimeOldEntries.isBefore(offsetDateTimeNewEntries)) {
                    sendOnNotificationRecordEntryUpdate()
                }
            } else if (entriesUnreadResponse.entryEntities.isNotEmpty()) {
                sendOnNotificationRecordEntryUpdate()
            }

        } catch (e: IOException) {
            Log.e("Connectivity", "No internet connection.", e)
        } catch (e: HttpException) {
            Log.e("Connectivity", "HttpException.", e)
        }

    }


    private fun sendOnNotificationRecordEntryUpdate() {

        val activityIntent = Intent(applicationContext, HostActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            activityIntent, PendingIntent.FLAG_CANCEL_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_NEW_ENTRY_ID)
            .setSmallIcon(R.drawable.ic_miniflux)
            .setContentTitle("You got new entries!")
            .setContentText("Open the app to see the updates.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID_ENTRY, notification)
    }
}
