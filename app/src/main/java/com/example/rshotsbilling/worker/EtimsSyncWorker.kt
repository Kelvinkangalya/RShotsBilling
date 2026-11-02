package com.example.rshotsbilling.worker

import android.content.Context
import androidx.work.*
import com.example.rshotsbilling.repository.EtimsRepository
import com.example.rshotsbilling.repository.SalesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.rshotsbilling.utils.NotificationHelper

class EtimsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val etimsRepository = EtimsRepository()
        val salesRepository = SalesRepository()

        val pendingSales = etimsRepository.getPendingSales()

        if (pendingSales.isEmpty()) return@withContext Result.success()

        var hasFailure = false

        pendingSales.forEach { sale ->
            try {
                val updatedSale = etimsRepository.submitSaleToEtims(sale)
                
                if (updatedSale.etimsStatus == "SUCCESS") {
                    NotificationHelper.showNotification(
                        applicationContext,
                        "eTIMS Submission Success",
                        "Invoice ${sale.invoiceNumber} has been successfully submitted to KRA."
                    )
                } else {
                    hasFailure = true
                }
            } catch (e: Exception) {
                hasFailure = true
            }
        }

        if (hasFailure) Result.retry() else Result.success()
    }

    companion object {
        fun scheduleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<EtimsSyncWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "EtimsSync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
