/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

fun <TKey, TValue> Map<TKey?, TValue>.filterKeysNotNull(): Map<TKey, TValue> =
    filterKeys { it != null }.mapKeys { it.key!! }

fun <TKey, TValue> Map<TKey, TValue?>.filterValuesNotNull(): Map<TKey, TValue> =
    filterValues { it != null }.mapValues { it.value!! }
