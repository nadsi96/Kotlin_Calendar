package com.example.calendar.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.example.calendar.*
import com.example.calendar.data.Schedule
import com.example.calendar.data.SelectedDate
import com.example.calendar.dialog.TimeSpinnerDialog
import com.example.calendar.workManager.NotificationAlarmWorker
import kotlinx.android.synthetic.main.activity_new_schedule.*
import kotlinx.coroutines.delay
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.TimeUnit

class Activity_New_Schedule : AppCompatActivity(), View.OnClickListener {

    // 새로운 일정/ 수정 구분하기 위한 flag'
    // 확인버튼 눌렀을때 구분
    // true - 수정
    // false - 추가
    var flag = false

    // 새로운 일정 - 빈 스케줄 데이터
    // 수정하는 일정 - 기존의 스케줄 데이터
    var schedule = Schedule()

    // 0 - 일정
    // 1 - 알람
    // 년/월/일 TextView
    lateinit var dateViews: List<List<TextView>>

    // 시/분 TextView
    lateinit var timeViews: List<List<TextView>>

    // 년/월/일
    var dates = listOf(
        mutableListOf(0, 0, 0),
        mutableListOf(0, 0, 0)
    )

    // 시/분 값
    var times = listOf(
        mutableMapOf("hour" to cal.get(Calendar.HOUR_OF_DAY), "min" to cal.get(Calendar.MINUTE)),
        mutableMapOf("hour" to cal.get(Calendar.HOUR_OF_DAY), "min" to cal.get(Calendar.MINUTE))
    )

    // 시간 선택 dialog
    lateinit var schedule_timeSpinnerDialog: TimeSpinnerDialog // 일정 시간
    lateinit var alarm_timeSpinnerDialog: TimeSpinnerDialog    // 알람 시간

    // 날짜 선택 dialog
    lateinit var datePickerDialog: DatePickerDialog
    lateinit var alarmDatePickerDialog: DatePickerDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_schedule)

        // 날짜 표시할 textView
        // [0] - 일정 날짜
        // [1] - 알람 날짜
        dateViews = listOf(
            listOf(tv_year, tv_month, tv_date, tv_day),
            listOf(tv_alarmYear, tv_alarmMonth, tv_alarmDate, tv_alarmDay)
        )
        // 시간 표시할 textView
        // [0] - 일정 시간
        // [1] - 알람 시간
        timeViews = listOf(
            listOf(tv_ampm, tv_selected_hour, tv_selected_min),
            listOf(tv_alarm_ampm, tv_alarm_hour, tv_alarm_min)
        )

        initData()

        // 일정/알람 시/분값 뷰에 표시
        for (i in 0 until 2) {
            setDateTextView(i)
            setTimeTextView(i, times[i]["hour"] ?: 0, times[i]["min"] ?: 0)
        }

        // 일정 시간 설정 체크박스
        // 체크하면 시간 설정 가능
        switch_enableTime.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mask_time.visibility = View.GONE
                Log.i("chkbox_enableTime", "${switch_enableTime.isChecked}")
            } else {
                mask_time.visibility = View.VISIBLE
                Log.i("chkbox_enableTime", "${switch_enableTime.isChecked}")
            }
        }

        // 알람 스위치
        // 켜면 알람 설정 가능
        switch_alarm.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mask_alarm.visibility = View.GONE
            } else {
                mask_alarm.visibility = View.VISIBLE
            }
        }

        setDatePickerDialog()
        setTimeSpinnerDialog()
        setButtons()
    }

    // 화면 초기화
    // 일정을 수정하는 경우, 받은 데이터를 화면에 표시
    // 새로 만드는 경우, 시간부분을 현재 시간으로 초기화
    fun initData() {
        val intent = intent
        val bundle = intent.getBundleExtra("bundle")

        if (bundle != null) {
            val selectedDate = bundle.get("selectedDate") as SelectedDate?
            if (selectedDate != null) {
                tv_pageTitle.text = "일정 생성"

                // 새로운 일정 생성
                dates[0][0] = selectedDate.year
                dates[0][1] = selectedDate.month
                dates[0][2] = selectedDate.date

                // 알람 시간도 현재 시간으로 초기화
                for (i in 0 until 3)
                    dates[1][i] = dates[0][i]
            } else {
                // 기존 일정 수정하는 경우
                flag = true

                tv_pageTitle.text = "일정 편집"

                schedule = bundle.getParcelable<Schedule>("schedule") ?: Schedule()

                with(schedule) {
                    if (id != null) {
                        dates[0][0] = year
                        dates[0][1] = month
                        dates[0][2] = date

                        cal.set(dates[0][0], dates[0][1], dates[0][2])

                        if (hour != null) {
                            switch_enableTime.isChecked = true
                            mask_time.visibility = View.GONE
                            times[0]["hour"] = hour!!
                            times[0]["min"] = min!!
                        }

                        et_title.setText(title)
                        et_content.setText(content ?: "")

                        if (alarmYear != null) {
                            switch_alarm.isChecked = true
                            mask_alarm.visibility = View.GONE

                            dates[1][0] = alarmYear ?: 0
                            dates[1][1] = alarmMonth ?: 0
                            dates[1][2] = alarmDate ?: 0

                            times[1]["hour"] = alarmHour!!
                            times[1]["min"] = alarmMin!!

                        } else {
                            for (i in 0 until 3)
                                dates[1][i] = dates[0][i]
                        }
                    }
                }//with

            }// if selectedDate != null
        }// if bundle != null
    }

    fun setButtons() {
        layout_selectedDate.setOnClickListener(this)
        layout_selectedTime.setOnClickListener(this)

        layout_alarmDate.setOnClickListener(this)
        layout_alarmTime.setOnClickListener(this)

        btn_cancel.setOnClickListener(this)
        btn_ok.setOnClickListener(this)
    }

    // 날짜선택화면 설정
    // datePickerDialog = 일정 날짜 선택
    // alarmDatePickerDialog = 알람 날짜 선택
    fun setDatePickerDialog() {
        datePickerDialog = DatePickerDialog(
            this,
            { view, year, month, dayOfMonth ->
                dates[0][0] = year
                dates[0][1] = month + 1
                dates[0][2] = dayOfMonth

                setDateTextView(0)
            },
            dates[0][0],
            dates[0][1] - 1,
            dates[0][2]
        )

        alarmDatePickerDialog = DatePickerDialog(
            this,
            { view, year, month, dayOfMonth ->

                dates[1][0] = year
                dates[1][1] = month + 1
                dates[1][2] = dayOfMonth

                setDateTextView(1)
            },
            dates[1][0],
            dates[1][1] - 1,
            dates[1][2]

        )
    }

    // 시간 설정 dialog
    // isAlarm - 0 -> 일정 시간 설정
    //         - 1 -> 알람 시간 설정
    fun setTimeSpinnerDialog() {
        val ok = { h: Int, m: Int, isAlarm: Int ->
            times[isAlarm]["hour"] = h
            times[isAlarm]["min"] = m

            setTimeTextView(isAlarm, h, m)
        }

        // 일정 시간 설정 dialog
        schedule_timeSpinnerDialog = TimeSpinnerDialog(this, times, 0).apply {
            setView(LayoutInflater.from(context).inflate(R.layout.dialog_time_spinner, null))
        }
        schedule_timeSpinnerDialog.create()
        schedule_timeSpinnerDialog.setButton(ok)

        // 알람 시간 설정 dialog
        alarm_timeSpinnerDialog = TimeSpinnerDialog(this, times, 1).apply {
            setView(LayoutInflater.from(context).inflate(R.layout.dialog_time_spinner, null))
        }
        alarm_timeSpinnerDialog.create()
        alarm_timeSpinnerDialog.setButton(ok)
    }

    // 입력된 내용을 Schedule 클래스로 반환
    // 시간과 알람은 체크한 경우에만 기록
    // content 입력된 내용 없으면 null
    fun setData(): Schedule {
        val newSchedule = Schedule(
            id = schedule.id,
            year = dates[0][0],
            month = dates[0][1],
            date = dates[0][2],
            title = et_title.text.toString()
        ).apply {
            val content = et_content.text.toString()
            if (content != "") {
                this.content = content
            }
            if (switch_enableTime.isChecked) {
                // 시간을 설정했다면 입력
                // else null
                hour = times[0]["hour"]
                min = times[0]["min"]
            }
            if (switch_alarm.isChecked) {
                // 알람을 체크한 경우 입력
                // else null
                alarmYear = dates[1][0]
                alarmMonth = dates[1][1]
                alarmDate = dates[1][2]
                alarmHour = times[1]["hour"]
                alarmMin = times[1]["min"]
            }
        }

        Log.i("Schedule Data", "${newSchedule.alarmYear}")
        return newSchedule
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_cancel -> {
//                Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show()
                finish()
            }
            R.id.btn_ok -> {
                if (et_title.text.toString() == "") {
                    Toast.makeText(this, "제목을 입력하세요", Toast.LENGTH_SHORT).show()
                    return
                }

                val schedule = setData()
                var _id: Long // db에 저장된 후, 해당 일정의 id값


                // flag == 수정하는 경우인지, 새로 추가하는 경우인지 판단
                // initData()에서 설정됨
                if (flag) {
                    // 수정하는 경우
                    _id = db_handler.modifySchdule(schedule)
                } else {
                    // 새로 추가하는 경우
                    _id = db_handler.saveSchedule(schedule)
                }

                // 기존에 알람이 설정되어있었는지 확인
                val alarmExisted = this.schedule.alarmYear != null
                if (switch_alarm.isChecked) {

                    // 기존에 알람이 설정되어있었다면
                        // 기존 알람 취소
                    if (alarmExisted) {
                        WorkManager.getInstance(this)
                            .cancelAllWorkByTag(_id.toString())
                    }

                    // 현재 입력/수정되는 일정의 알람으로 다시 설정
                    with(schedule) {

                        // 동작 지연시간
                        // 예약시간 - 현재시간
                        val delayTime = calcDelayTime(
                            alarmYear!!,
                            alarmMonth!!,
                            alarmDate!!,
                            alarmHour!!,
                            alarmMin!!
                        )

                        val notificationWorkRequest: WorkRequest =
                            OneTimeWorkRequestBuilder<NotificationAlarmWorker>()
                                .setInputData(workDataOf("schedule_id" to _id.toInt())) // 현재 일정의 id 넘겨줌 // 동작할 때, 넘겨준 id로 db검색
                                .setInitialDelay(delayTime, TimeUnit.MILLISECONDS) // 지연 시간
                                .addTag(_id.toString()) // 취소위한 tag. 일정의 id로 구분
                                .build()

                        WorkManager.getInstance(this@Activity_New_Schedule)
                            .enqueue(notificationWorkRequest)
                    }

                }
/*
                // 알람 설정한 경우
                if (switch_alarm.isChecked) {
                    if (alarmExisted) {
                        // 기존에 알람이 설정되어있던 경우
                        // 기존 알람 수정
//                        alarmHandler.modifyAlarm(_id, schedule)
                        workManager!!.motifyAlarm(this.schedule.id, schedule)
                    } else {
                        // 기존에 알람이 설정되어있지 않았던 경우
                        // 알람 새로 생성
//                        alarmHandler.setAlarm(_id, schedule)
                        workManager!!.setAlarm(_id.toInt(), schedule)
                    }
                } else {
                    // 알람을 설정하지 않은 경우
                    // 기존에 알람이 설정된 것이 있었다면 삭제
                    if (alarmExisted) {
//                        alarmHandler.removeAlarm(schedule)
                        workManager!!.cancelAlarm(schedule)
                    }
                }*/


                Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            R.id.layout_selectedDate -> {
                // 일정 날짜 선택
                datePickerDialog.show()
            }
            R.id.layout_selectedTime -> {
                // 일정 시간 선택
                if (switch_enableTime.isChecked) {
                    schedule_timeSpinnerDialog.show()
                }
            }
            R.id.layout_alarmDate -> {
                // 알람설정할 날짜 선택
                if (switch_alarm.isChecked)
                    alarmDatePickerDialog.show()
            }
            R.id.layout_alarmTime -> {
                // 알람설정할 시간 선택
                if (switch_alarm.isChecked) {
                    alarm_timeSpinnerDialog.show()
                }
            }
            else -> return
        }
    }

    /**
     * 날짜 표시하는 TextView 텍스트 설정
     * @param type - 0 - 일정
     *             - 1 - 알람
     */
    fun setDateTextView(type: Int) {

        // 년/월/일 설정
        for (i in 0 until 3) {
            dateViews[type][i].text = NUM_FORMAT(dates[type][i])
        }

        // 요일 설정
        dateViews[type][3].text =
            "${getDayOfWeekString(dates[type][0], dates[type][1], dates[type][2])}요일"
    }

    /**
     * 날짜 표시하는 TextView 텍스트 설정
     * @param type - 0 - 일정
     *             - 1 - 알람
     *
     */
    fun setTimeTextView(type: Int, hour: Int, min: Int) {
        timeViews[type][0].text = getAmPm(hour)
        timeViews[type][1].text = NUM_FORMAT(getHour(hour))
        timeViews[type][2].text = NUM_FORMAT(min)
    }

}