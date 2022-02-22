/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn

class EventFlow<T>private constructor(
    private val internalChannel: SendChannel<T>,
    private val internalFlow: Flow<T>,
) : FlowCollector<T>, Flow<T> by internalFlow {
    companion object {
        fun <T> create(): EventFlow<T> {
            val channel = Channel<T>(Channel.CONFLATED)
            val flow = channel.consumeAsFlow()
            return EventFlow(channel, flow)
        }

        fun <T> createShared(scope: CoroutineScope = CoroutineScope(Dispatchers.Default)): EventFlow<T> {
            val channel = Channel<T>(Channel.CONFLATED)
            val flow = channel.receiveAsFlow().shareIn(scope, SharingStarted.WhileSubscribed())
            return EventFlow(channel, flow)
        }
    }

    val isShared: Boolean
        get() = internalFlow is SharedFlow

    /**
     * emit is suspending but should never actually suspend/block the execution due to buffering
     */
    override suspend fun emit(value: T) {
        internalChannel.send(value)
    }
}
