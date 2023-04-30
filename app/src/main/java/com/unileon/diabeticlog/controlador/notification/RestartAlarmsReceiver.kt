package com.unileon.diabeticlog.controlador.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RestartAlarmsReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if(intent.action.equals("android.intent.action.BOOT_COMPLETED")) {
            val lista = intent.getSerializableExtra("listaDatos") as Array<String>
            val notificationUtils = NotificationUtils(context)
            val notification = notificationUtils.getNotificationBuilder(lista).build()
            notificationUtils.getManager().notify(150, notification)
        }


    }

}