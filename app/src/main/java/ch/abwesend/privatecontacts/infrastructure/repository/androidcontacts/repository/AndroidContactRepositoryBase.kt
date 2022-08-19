package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.permission.MissingPermissionException
import ch.abwesend.privatecontacts.domain.service.ContactValidationService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.alexstyl.contactstore.ContactStore
import kotlinx.coroutines.withContext

abstract class AndroidContactRepositoryBase {
    protected val permissionService: PermissionService by injectAnywhere()
    protected val validationService: ContactValidationService by injectAnywhere()
    protected val dispatchers: IDispatchers by injectAnywhere()

    private val contactStore: ContactStore by injectAnywhere()

    protected suspend fun <T> withContactStore(block: suspend (contactStore: ContactStore) -> T): T {
        return withContext(dispatchers.io) {
            block(contactStore)
        }
    }

    protected inline fun <T> checkContactReadPermission(permissionDeniedHandler: (MissingPermissionException) -> T) {
        if (!permissionService.hasContactReadPermission()) {
            val errorMessage = "Trying to read android contacts without read-permission.".also { logger.warning(it) }
            permissionDeniedHandler(MissingPermissionException(errorMessage))
        }
    }

    protected inline fun <T> checkContactWritePermission(permissionDeniedHandler: (MissingPermissionException) -> T) {
        if (!permissionService.hasContactWritePermission()) {
            val errorMessage = "Trying to write android contacts without write-permission.".also { logger.warning(it) }
            permissionDeniedHandler(MissingPermissionException(errorMessage))
        }
    }
}
