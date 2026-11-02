package com.example.rshotsbilling.worker

import android.content.Context
import androidx.work.*
import com.example.rshotsbilling.repository.MpesaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MpesaSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val repository = MpesaRepository()

        val allTransactions = repository.getAllTransactionsFlow().first()
        val pendingTransactions = allTransactions.filter { it.status == "PENDING" }
        
        if (pendingTransactions.isEmpty()) return@withContext Result.success()

        var hasFailure = false
        pendingTransactions.forEach { transaction ->
            try {
                transaction.transactionId?.let { repository.checkStatus(it) }
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

            val syncRequest = PeriodicWorkRequestBuilder<MpesaSyncWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "MpesaSync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
