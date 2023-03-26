/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model

import ch.abwesend.privatecontacts.domain.lib.logging.logger

sealed class ModelStatus {
    fun tryChangeTo(targetStatus: ModelStatus): ModelStatus =
        when {
            targetStatus == this -> this
            isTransitionAllowed(targetStatus) -> targetStatus
            else -> {
                logger.debug("Blocked state-transition from $this to $targetStatus.")
                this
            }
        }

    protected open fun isTransitionAllowed(targetStatus: ModelStatus): Boolean = true

    object NEW : ModelStatus() {
        override fun isTransitionAllowed(targetStatus: ModelStatus): Boolean =
            when (targetStatus) {
                NEW, DELETED -> true
                UNCHANGED, CHANGED -> false
            }
    }

    object UNCHANGED : ModelStatus() {
        override fun isTransitionAllowed(targetStatus: ModelStatus): Boolean =
            when (targetStatus) {
                NEW -> false
                UNCHANGED, CHANGED, DELETED -> true
            }
    }

    object CHANGED : ModelStatus() {
        override fun isTransitionAllowed(targetStatus: ModelStatus): Boolean =
            when (targetStatus) {
                NEW, UNCHANGED -> false
                CHANGED, DELETED -> true
            }
    }

    object DELETED : ModelStatus() {
        override fun isTransitionAllowed(targetStatus: ModelStatus): Boolean = false
    }
}

interface WithModelStatus {
    val modelStatus: ModelStatus
}

/** only considers positive changes (i.e. which should be stored in the database) */
fun <T : WithModelStatus> Collection<T>.filterForChanged(): List<T> {
    return filter { it.modelStatus.isChanged }
}

/** only considers positive changes (i.e. which should be stored in the database) */
val ModelStatus.isChanged: Boolean
    get() = this == ModelStatus.NEW || this == ModelStatus.CHANGED
