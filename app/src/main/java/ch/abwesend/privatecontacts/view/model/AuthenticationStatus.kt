package ch.abwesend.privatecontacts.view.model

enum class AuthenticationStatus(val allowAccess: Boolean) {
    SUCCESS(allowAccess = true),

    /** authentication has not happened yet */
    NOT_AUTHENTICATED(allowAccess = false),

    /** authentication was processed properly but the user should not have access */
    FAILURE(allowAccess = false),

    /** authentication could not be processed properly */
    ERROR(allowAccess = false),
}
