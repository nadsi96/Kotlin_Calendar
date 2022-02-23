package com.example.calendar

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.example.calendar.data.ScheduleDB_Handler
import com.example.calendar.workManager.NotificationWorkManager
import java.text.SimpleDateFormat
import java.util.*


// 정수 공백은 0으로 채운 두 자리로 반환
val NUM_FORMAT = { num: Int -> if(num < 100) String.format("%02d", num) else num.toString() }

val DATE_FORMATTER = SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.KOREA)
val cal = Calendar.getInstance()

val currentYear = cal.get(Calendar.YEAR)
val currentMonth = cal.get(Calendar.MONTH) + 1
val currentDate = cal.get(Calendar.DATE)

val raw_datetime = cal.time
val CURRENTDATE = DATE_FORMATTER.format(raw_datetime)

// db 접근
// 앱 시작시 setDB_Handler 호출
lateinit var db_handler: ScheduleDB_Handler
fun setDB_Handler(applicationContext: Context): ScheduleDB_Handler {
    db_handler = ScheduleDB_Handler(applicationContext)
    return db_handler
}

// 알림 위한 WorkManager
// 앱 시작시 setWorkManager 호출
//var workManager: NotificationWorkManager? = null
//fun setWorkManager(applicationContext: Context){
//    workManager = NotificationWorkManager(applicationContext)
//}

/**
 * Calendar.DAY_OF_WEEK 값을 문자열로 반환
 * @param dayOfWeek - 1 ~ 7
 */
fun getDayOfWeekString(dayOfWeek: Int): String? {
    return when (dayOfWeek) {
        1 -> "일"
        2 -> "월"
        3 -> "화"
        4 -> "수"
        5 -> "목"
        6 -> "금"
        7 -> "토"
        else -> null
    }
}

/**
 * 년/월/일 입력받아서 무슨 요일 문자열로 반환
 */
fun getDayOfWeekString(year: Int, month: Int, date: Int): String? {
    var tempY = cal.get(Calendar.YEAR)
    var tempM = cal.get(Calendar.MONTH)
    var tempD = cal.get(Calendar.DATE)

    cal.set(year, (month - 1), date)
    val day = getDayOfWeekString(cal.get(Calendar.DAY_OF_WEEK))
    cal.set(tempY, tempM, tempD)

    return day
}

/**
 * 입력받은 년/월의 첫 날 요일 인덱스 반환
 */
fun getFirstDayOfWeek(year: Int, month: Int): Int {
    var tempY = cal.get(Calendar.YEAR)
    var tempM = cal.get(Calendar.MONTH)
    var tempD = cal.get(Calendar.DATE)

    cal.set(year, month - 1, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    cal.set(tempY, tempM, tempD)

    return firstDayOfWeek
}

/**
 * 입력된 시간이 오전인지 오후인지 문자열로 반환
 */
fun getAmPm(h: Int): String {
    return if (h >= 12)
        "오후"
    else {
        "오전"
    }
}

// 12시가 넘어가는 시간에 -12
fun getHour(h: Int): Int {
    return if (h > 12)
        h - 12
    else
        h
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

    Log.i("delayTime", dis.toString())
    return if (dis >= 0) {
        dis
    } else {
        0
    }
}