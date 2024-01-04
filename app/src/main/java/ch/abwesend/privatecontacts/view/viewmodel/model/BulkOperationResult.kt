package ch.abwesend.privatecontacts.view.viewmodel.model

import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactIdBatchChangeResult
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult

data class BulkOperationResult<T>(val result: T, val numberOfSelectedContacts: Int)

typealias BulkContactExportResult = BulkOperationResult<BinaryResult<ContactExportData, VCardCreateError>>
typealias BulkContactDeleteResult = BulkOperationResult<ContactIdBatchChangeResult>
