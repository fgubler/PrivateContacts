package ch.abwesend.privatecontacts.testutil

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

// More information about testing live data: https://jeroenmols.com/blog/2019/01/17/livedatajunit5/
@ExperimentalCoroutinesApi
class AsyncTestExtension : BeforeEachCallback, AfterEachCallback {

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)

    @SuppressLint("RestrictedApi")
    override fun beforeEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance()
            .setDelegate(object : TaskExecutor() {
                override fun executeOnDiskIO(runnable: Runnable) = runBlocking { runnable.run() }
                override fun postToMainThread(runnable: Runnable) = runBlocking { runnable.run() }
                override fun isMainThread(): Boolean = true
            })

        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @SuppressLint("RestrictedApi")
    override fun afterEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(null)
        Dispatchers.resetMain()
        testCoroutineScope.cleanupTestCoroutines()
    }
}
