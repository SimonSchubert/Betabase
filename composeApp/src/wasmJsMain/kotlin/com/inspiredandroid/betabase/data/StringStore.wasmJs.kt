package com.inspiredandroid.betabase.data

import kotlinx.browser.localStorage

actual fun createStringStore(): StringStore = object : StringStore {
    override fun get(key: String): String? = localStorage.getItem(key)
    override fun put(key: String, value: String) {
        localStorage.setItem(key, value)
    }
}
