package ch.abwesend.privatecontacts.domain.logging
import ch.abwesend.privatecontacts.domain.util.getAnywhere

interface ILoggerFactory {
    /**
     * @return a logger instance of the default type
     */
    fun createDefault(callerClass: Class<*>): ILogger

    /**
     * @return a logger which only writes to logcat and not a file or anything else
     * e.g. for circumstances where the app might not have the necessary permissions for anything else
     */
    fun createLogcat(callerClass: Class<*>): ILogger
}

val Any.logger: ILogger
    get() {
        val factory: ILoggerFactory = getAnywhere()
        return factory.createDefault(javaClass)
    }
