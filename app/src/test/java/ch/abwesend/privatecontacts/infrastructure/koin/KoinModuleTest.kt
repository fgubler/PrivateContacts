package ch.abwesend.privatecontacts.infrastructure.koin

import android.content.Context
import androidx.navigation.NavHostController
import ch.abwesend.privatecontacts.application.koinModule
import ch.abwesend.privatecontacts.domain.lib.coroutine.ApplicationScope
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseHolder
import ch.abwesend.privatecontacts.view.routing.GenericRouter
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify
import kotlin.reflect.KClass

@KoinExperimentalAPI
class KoinModuleTest : KoinTest {
    private val whiteList: List<KClass<*>> = listOf(
        /** for the manually used constructor of [ApplicationScope] */
        CoroutineScope::class,
        /** for the manually used constructor of [GenericRouter] */
        NavHostController::class,
        /** for the manually used constructor of [DatabaseHolder] */
        Context::class,
    )

    @Test
    fun checkAllModules() {
        koinModule.verify(extraTypes = whiteList)
    }
}
