package com.example.calendar.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.database.getIntOrNull


class ScheduleDB_Handler {

    private var db: SQLiteDatabase

    private val DB_SCHEDULE = "schedule_manage"
    private val TABLE_SCHEDULE = "schedules"
    private val ID = "id"
    private val YEAR = "year"
    private val MONTH = "month"
    private val DATE = "date"
    private val HOUR = "hour"
    private val MIN = "min"
    private val TITLE = "title"
    private val CONTENT = "content"
    private val ALARM_YEAR = "alarm_year"
    private val ALARM_MONTH = "alarm_month"
    private val ALARM_DATE = "alarm_date"
    private val ALARM_HOUR = "alarm_hour"
    private val ALARM_MIN = "alarm_min"

    constructor(context: Context) {
        db = context.openOrCreateDatabase(DB_SCHEDULE, Context.MODE_PRIVATE, null)

        // 테이블 없으면 만듦
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS ${TABLE_SCHEDULE} (" +
                    " ${ID} integer primary key autoincrement, " +
                    " ${YEAR} INTEGER not null, " +
                    " ${MONTH} INTEGER not null, " +
                    " ${DATE} INTEGER not null, " +
                    " ${HOUR} INTEGER, " +
                    " ${MIN} INTEGER, " +
                    " ${TITLE} TEXT not null, " +
                    " $CONTENT TEXT, " +
                    " ${ALARM_YEAR} INTEGER, " +
                    " ${ALARM_MONTH} INTEGER, " +
                    " ${ALARM_DATE} INTEGER, " +
                    " ${ALARM_HOUR} INTEGER, " +
                    " ${ALARM_MIN} INTEGER )"
        )
    }

    /**
     * 년, 월을 받아 db에서 해당하는 월의 일정들 map으로 반환
     * MutableMap(날짜, 해당 날짜에 속한 일정 리스트)
     */
    fun getMonthlySchedules(_year: Int, _month: Int): MutableMap<Int, MutableList<Schedule>> {

        var schedules = mutableMapOf<Int, MutableList<Schedule>>()

        var cursor = db.rawQuery(
            "select * from ${TABLE_SCHEDULE} " +
                    "where year=$_year and " +
                    "month=$_month " +
                    "order by $YEAR, $MONTH, $DATE",
            null
        )

        for (idx in 0 until cursor.count) {
            cursor.moveToNext()
            val id = cursor.getInt(0)
            val year = cursor.getInt(1)
            val month = cursor.getInt(2)
            val date = cursor.getInt(3)
            var hour: Int? = null
            if (!cursor.isNull(4)) {
                hour = cursor.getInt(4)
            }
//            val hour = cursor.getIntOrNull(4)
            var min: Int? = null
            if (!cursor.isNull(5))
                min = cursor.getInt(5)
//            val min = cursor.getIntOrNull(5)
            val title = cursor.getString(6)
            var content: String? = null
            if (!cursor.isNull(7))
                content = cursor.getString(7)
//            val content = cursor.getStringOrNull(7)
            val alarm_year = cursor.getIntOrNull(8)
            val alarm_month = cursor.getIntOrNull(9)
            val alarm_date = cursor.getIntOrNull(10)
            val alarm_hour = cursor.getIntOrNull(11)
            val alarm_min = cursor.getIntOrNull(12)


            // String? null -> "null"
            // Int? null -> 0
            // null값을 넣으려면 직접 isNull 비교 필요
//            Log.i("get content null?", "${content}    ${content?:""}")
//            Log.i("get alarm year", "${alarm_year}")

            if (date in schedules.keys) {
                schedules[date]?.add(
                    Schedule(
                        id, year, month, date, hour, min,
                        title, content,
                        alarm_year, alarm_month, alarm_date, alarm_hour, alarm_min
                    )
                )
            } else {
                schedules.put(
                    date, mutableListOf(
                        Schedule(
                            id, year, month, date, hour, min,
                            title, content,
                            alarm_year, alarm_month, alarm_date, alarm_hour, alarm_min
                        )
                    )
                )
            }
        }

        return schedules
    }

    /**
     * 년, 월, 일을 받아 db에서 해당하는 일자의 일정목록 반환
     */
    fun getDailySchedules(_year: Int, _month: Int, _date: Int): MutableList<Schedule> {

        var cursor = db.rawQuery(
            "select * from $TABLE_SCHEDULE " +
                    "where year=$_year and " +
                    "month=$_month and " +
                    "date=$_date " +
                    "order by $HOUR",
            null
        )

        return query(cursor)
    }

    /**
     * 지정한 날짜 이전/이후의 일정들 반환
     *
     * @param _year - 지정할 날짜 년/월/일
     * @param _month
     * @param _date
     * @param flag - 0 - 지정한 날짜 이후 일정 반환
     *             - 1 - 지정한 날짜 이전 일정 반환
     */
    fun getSchedules(_year: Int, _month: Int, _date: Int, flag: Int = 0): MutableList<Schedule> {
        var cursor: Cursor
        if (flag == 0) {
            cursor = db.rawQuery(
                "select * from $TABLE_SCHEDULE " +
                        "where ( $YEAR > $_year ) or " +
                        "( $YEAR = $_year and $MONTH > $_month ) or " +
                        "( $YEAR = $_year and $MONTH = $_month and $DATE >= $_date ) " +
                        "order by $YEAR ASC, $MONTH ASC, $DATE ASC, $HOUR ASC, $MIN ASC",
                null
            )
        } else {
            cursor = db.rawQuery(
                "select * from $TABLE_SCHEDULE " +
                        "where ( $YEAR < $_year ) or " +
                        "( $YEAR = $_year and $MONTH < $_month ) or " +
                        "( $YEAR = $_year and $MONTH = $_month and $DATE < $_date ) " +
                        "order by $YEAR DESC, $MONTH DESC, $DATE DESC, $HOUR DESC, $MIN DESC",
                null
            )
        }

        return query(cursor)
    }


    // 일정 기록
    fun saveSchedule(schedule: Schedule): Long {

        Log.i("schedule.alarm is null?", "${schedule.alarmYear}")

        /*db.execSQL(
            "INSERT INTO $TABLE_SCHEDULE ( " +
                    "$YEAR, $MONTH, $DATE, $HOUR, $MIN, " +
                    "$TITLE, $CONTENT, " +
                    "$ALARM_YEAR, $ALARM_MONTH, $ALARM_DATE, $ALARM_HOUR, $ALARM_MIN ) " +
                    "VALUES( '${schedule.year}', " +
                    "'${schedule.month}', " +
                    "'${schedule.date}', " +
                    "'${schedule.hour ?: -1}', " +
                    "'${schedule.min ?: -1}', " +
                    "'${schedule.title}', " +
                    "'${schedule.content}', " +
                    "'${schedule.alarmYear ?: -1}', " + // ??? null을 넣었는데 가져올 때는 0
                    "'${schedule.alarmMonth ?: -1}', " +
                    "'${schedule.alarmDate ?: -1}', " +
                    "'${schedule.alarmHour ?: -1}', " +
                    "'${schedule.alarmMin ?: -1}')"
        )*/
        var cv = ContentValues()
        cv.put(YEAR, schedule.year)
        cv.put(MONTH, schedule.month)
        cv.put(DATE, schedule.date)
        cv.put(HOUR, schedule.hour)
        cv.put(MIN, schedule.min)
        cv.put(TITLE, schedule.title)
        cv.put(CONTENT, schedule.content)
        cv.put(ALARM_YEAR, schedule.alarmYear)
        cv.put(ALARM_MONTH, schedule.alarmMonth)
        cv.put(ALARM_DATE, schedule.alarmDate)
        cv.put(ALARM_HOUR, schedule.alarmHour)
        cv.put(ALARM_MIN, schedule.alarmMin)

        val id = db.insert(TABLE_SCHEDULE, null, cv)
        return id

    }

    fun deleteSchedule(schedule: Schedule) {
        db.execSQL("DELETE FROM $TABLE_SCHEDULE WHERE id = ${schedule.id}")
        Log.i("일정이 지워져욧", "${schedule.id} ${schedule.title}")
    }

    /**
     * id값 유지
     * 기존 데이터 지우고 기존 데이터의 id값 부여해서 삽입
     */
    fun modifySchdule(schedule: Schedule): Long {
        Log.i("일정이 수정되욧", "${schedule.id} ${schedule.title}")

        db.execSQL("DELETE FROM $TABLE_SCHEDULE WHERE id = ${schedule.id}")
        /*db.execSQL(
            "INSERT INTO $TABLE_SCHEDULE ( $ID, " +
                    "$YEAR, $MONTH, $DATE, $HOUR, $MIN, " +
                    "$TITLE, $CONTENT, " +
                    "$ALARM_YEAR, $ALARM_MONTH, $ALARM_DATE, $ALARM_HOUR, $ALARM_MIN ) " +
                    "VALUES( '${schedule.id}', " +
                    "'${schedule.year}', " +
                    "'${schedule.month}', " +
                    "'${schedule.date}', " +
                    "'${schedule.hour ?: -1}', " +
                    "'${schedule.min ?: -1}', " +
                    "'${schedule.title}', " +
                    "'${schedule.content}', " +
                    "'${schedule.alarmYear ?: -1}', " + // ??? null을 넣었는데 가져올 때는 0
                    "'${schedule.alarmMonth ?: -1}', " +
                    "'${schedule.alarmDate ?: -1}', " +
                    "'${schedule.alarmHour ?: -1}', " +
                    "'${schedule.alarmMin ?: -1}')"
        )*/

        var cv = ContentValues()
        cv.put(ID, schedule.id)
        cv.put(YEAR, schedule.year)
        cv.put(MONTH, schedule.month)
        cv.put(DATE, schedule.date)
        cv.put(HOUR, schedule.hour)
        cv.put(MIN, schedule.min)
        cv.put(TITLE, schedule.title)
        cv.put(CONTENT, schedule.content)
        cv.put(ALARM_YEAR, schedule.alarmYear)
        cv.put(ALARM_MONTH, schedule.alarmMonth)
        cv.put(ALARM_DATE, schedule.alarmDate)
        cv.put(ALARM_HOUR, schedule.alarmHour)
        cv.put(ALARM_MIN, schedule.alarmMin)

        val id = db.insert(TABLE_SCHEDULE, null, cv)
        return id
    }

    /**
     * 주어진 일정의 id에 해당하는 일정 반환
     */
    fun getSchedule(id: Int): Schedule {
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SCHEDULE WHERE id = $id", null)
//        val schedules = query(cursor)

        return query(cursor)[0]
//        if (schedules.size > 0) {
//            return schedules[0]
//        } else {
//            return null
//        }
    }

    /**
     * 데이터 쿼리한 커서 받아서 일정목록리스트로 반환
     */
    fun query(cursor: Cursor): MutableList<Schedule> {
        var schedules = mutableListOf<Schedule>()

        for (idx in 0 until cursor.count) {
            cursor.moveToNext()
            val id = cursor.getInt(0)
            val year = cursor.getInt(1)
            val month = cursor.getInt(2)
            val date = cursor.getInt(3)
            var hour: Int? = null
            if (!cursor.isNull(4))
                hour = cursor.getInt(4)

            Log.i("asdfasf hour", "$hour, ${hour ?: 12345}")
//            val hour = cursor.getIntOrNull(4)
            var min: Int? = null
            if (!cursor.isNull(5))
                min = cursor.getInt(5)
//            val min = cursor.getIntOrNull(5)
            val title = cursor.getString(6)
            var content: String? = null
            if (!cursor.isNull(7))
                content = cursor.getString(7)
//            val content = cursor.getStringOrNull(7)
            val alarm_year = cursor.getIntOrNull(8)
            val alarm_month = cursor.getIntOrNull(9)
            val alarm_date = cursor.getIntOrNull(10)
            val alarm_hour = cursor.getIntOrNull(11)
            val alarm_min = cursor.getIntOrNull(12)

            schedules.add(
                Schedule(
                    id, year, month, date, hour, min,
                    title, content,
                    alarm_year, alarm_month, alarm_date, alarm_hour, alarm_min
                )
            )
        }

        return schedules
    }

}