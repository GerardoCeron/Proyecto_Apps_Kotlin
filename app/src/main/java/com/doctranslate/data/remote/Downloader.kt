package com.doctranslate.data.remote

interface Downloader {
    fun downloadFile(url: String, fileName: String): Long
}
