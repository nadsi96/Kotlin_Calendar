package com.example.calendar.workManager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.calendar.R
import com.example.calendar.activity.Activity_Check_Schedule
import com.example.calendar.data.Schedule
import com.example.calendar.db_handler
import com.example.calendar.setDB_Handler
import java.lang.Exception
import java.lang.Thread.sleep

class NotificationAlarmWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    var notificationID = 0
    val NOTIFICATION_CHANNEL = "Alert Schedule"

    /**
     * 동작할 작업 정의
     */
    override fun doWork(): Result {
        var schedule_id = inputData.getInt("schedule_id", -1)
        notificationID = schedule_id

        db_handler = setDB_Handler(applicationContext)

        Log.i("schedule_id", "$schedule_id")
        if (schedule_id != -1) {
            notificationID = schedule_id
        }else{
            return Result.failure()
        }

        with(db_handler.getSchedule(schedule_id)){
            if(this == null){
                return Result.failure()
            }
            try{
                // 알림 생성
                makeNotification(this)
                // 오류 없이 만들어졌다면 성공
                return Result.success()
            }catch (e: Exception){
                return Result.failure()
            }
        }
    }

    /**
     * 알림 생성
     * @param schedule - 알림을 띄울 일정
     *                 - 알림 띄운 후 알람시간 수정됨
     */
    private fun makeNotification(schedule: Schedule) {

        // 알람이 울리고 나면 일정 데이터에서 알람 설정되지 않은 모습으로 보여주기 위해 수정
        with(schedule) {
            id = notificationID
            alarmYear = null
            alarmMonth = null
            alarmDate = null
            alarmHour = null
            alarmMin = null

            db_handler.modifySchdule(this)
        }

        // 알림 클릭시 열릴 화면
        val intent = Intent(applicationContext, Activity_Check_Schedule::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("bundle", Bundle().apply {
                putParcelable("schedule", schedule)
            })
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*
            Builder class for Notification objects. Provides a convenient way to set the various
             fields of a Notification and generate content views using the platform's notification
             layout template. If your app supports versions of Android as old as API level 4, you
             can instead use NotificationCompat.Builder, available in the Android Support library.
Example:
   Notification noti = new Notification.Builder(mContext)
           .setContentTitle("New mail from " + sender.toString())
           .setContentText(subject)
           .setSmallIcon(R.drawable.new_mail)
           .setLargeIcon(aBitmap)
           .build();
             */

            // api 26 이상만 지원
            val builder = Notification.Builder(applicationContext, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.img_schedule)
                .setContentTitle(schedule.title)
                .setContentText(schedule.content ?: "")
                // .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 알림 우선순위 설정
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)// 알림 터치시 알림 사라짐
                .build()



            with(NotificationManagerCompat.from(applicationContext)) {
//                    startForeground(notificationID, (builder as Notification.Builder).build())
//                startForeground(notificationID, builder)
                notify(notificationID, builder)

            }
            /*
            The user is not allowed to swipe away a notification generated by an ongoing foreground service.
            Hence, stopForeground(false) first, which allows the notification to be swiped away by the user thereafter (at least on Lollipop+).
            For pre-Lollipop, you may have to stopForeground(true), which stops foreground and removes the notification,
            then re-issue the notification with notificationManager.
            notify(yourNotificationID, yourNotificationObject), so that your notification is visible but swipeable.
             */
//            stopForeground(false)

        } else {
            val builder = NotificationCompat.Builder(
                applicationContext,
                "$NOTIFICATION_CHANNEL $notificationID"
            )
                .setSmallIcon(R.drawable.img_schedule)
                .setContentTitle(schedule.title)
                .setContentText(schedule.content ?: "")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)


            with(NotificationManagerCompat.from(applicationContext)) {
                notify(notificationID, builder.build())
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alert Schedule"
            val descriptionText = "channel_description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}