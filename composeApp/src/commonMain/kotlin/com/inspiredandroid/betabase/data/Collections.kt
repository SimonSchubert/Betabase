package com.inspiredandroid.betabase.data

/** Add [value] if absent, remove it if present — used by filter toggle chips. */
fun <T> Set<T>.toggleMember(value: T): Set<T> = if (value in this) this - value else this + value
