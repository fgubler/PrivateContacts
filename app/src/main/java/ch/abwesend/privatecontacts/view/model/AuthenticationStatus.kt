package ch.abwesend.privatecontacts.view.model

enum class AuthenticationStatus(val allowAccess: Boolean) {
    SUCCESS(allowAccess = true),

    /** the device has no registered authentication-measures (no lock-screen) */
    NO_DEVICE_AUTHENTICATION_REGISTERED(allowAccess = true),

    /** authentication has not happened yet */
    NOT_AUTHENTICATED(allowAccess = false),

    /** the user has cancelled the authentication-request */
    CANCELLED(allowAccess = false),

    /** authentication was processed properly but the user should not have access */
    DENIED(allowAccess = false),

    /** authentication could not be processed properly */
    ERROR(allowAccess = false),
}
