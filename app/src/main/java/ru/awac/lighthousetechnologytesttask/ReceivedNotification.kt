package ru.awac.lighthousetechnologytesttask

import java.text.DateFormat


data class ReceivedNotification(
    var tableId: Long?,
    val id: Int,
    val packageName: String,
    val millis: Long,
    val title: String,
    val text: String,
    val read: Boolean
) {
    val time: String get() = DateFormat.getTimeInstance().format(millis).toString()
    val date: String get() = DateFormat.getDateInstance().format(millis).toString()
}
