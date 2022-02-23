package com.example.calendar.workManager

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.calendar.cal
import com.example.calendar.data.Schedule
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationWorkManager(applicationContext: Context) {
    val workManager = WorkManager.getInstance(applicationContext)

    fun setAlarm(id: Int?, schedule: Schedule) {
        if (id != null && id != -1) {
            // WorkRequest에 지정할 지연시간 계산
            val delayTime = calcDelayTime(
                schedule.alarmYear ?: schedule.year,
                schedule.alarmMonth ?: schedule.month,
                schedule.alarmDate ?: schedule.date,
                schedule.alarmHour ?: schedule.hour ?: 0,
                schedule.alarmMin ?: schedule.min ?: 0
            )

            // WorkRequest 정의
            // db에 저장된 id를 tag로 부여해서 작업 삭제시 비교
            // workDataOf로 Parcelable class 넘기지 못한다고..
            //          지정된 data type만 넘길 수 있다고 함
            // id를 넘겨서 다시 찾는 걸로
            val notificationWorkRequest =
                OneTimeWorkRequestBuilder<NotificationAlarmWorker>()
                    .setInputData(
                        workDataOf("schedule_id" to id)
                    )
                    .setInitialDelay(delayTime, TimeUnit.MILLISECONDS)
                    .addTag(id.toString())
                    .build()

            workManager.enqueue(notificationWorkRequest)
        }
    }

    fun cancelAlarm(schedule: Schedule) {
        if (schedule.id != null) {
            workManager.cancelAllWorkByTag(schedule.id.toString())
        } else {
        }
    }

    fun motifyAlarm(id: Int?, schedule: Schedule) {
        cancelAlarm(schedule)
        setAlarm(id, schedule)
    }

    // 현재 시간과 지정한 시간의 차이 반환
    fun calcDelayTime(year: Int, month: Int, date: Int, hour: Int, min: Int): Long {
        var tempY = cal.get(Calendar.YEAR)
        var tempM = cal.get(Calendar.MONTH)
        var tempD = cal.get(Calendar.DATE)

        cal.set(year, month - 1, date)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, min)
        cal.set(Calendar.SECOND, 0)

        val dis = cal.timeInMillis - System.currentTimeMillis()
        cal.set(tempY, tempM, tempD)

        return if (dis >= 0) {
            dis
        } else {
            0
        }
    }

}