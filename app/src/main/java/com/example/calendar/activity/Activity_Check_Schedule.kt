package com.example.calendar.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import com.example.calendar.*
import com.example.calendar.data.Schedule
import kotlinx.android.synthetic.main.activity_check_schedule2.*

/**
 * 일정 목록에서 선택하면 보여줄 상세화면
 */
class Activity_Check_Schedule : AppCompatActivity(), View.OnClickListener {

    // 삭제시 확인창
    lateinit var builder: AlertDialog.Builder

    // 화면에 보여줄 일정
    lateinit var schedule: Schedule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_schedule2)

        val intent = intent
        val bundle = intent.getBundleExtra("bundle")

        if (bundle != null) {
            schedule = bundle.getParcelable<Schedule>("schedule") ?: Schedule(
                year = 0,
                month = 0,
                date = 0,
                title = ""
            )
            if (schedule.id != null) {
                setData()
            }
        }

        btn_delete.setOnClickListener(this)
        btn_modify.setOnClickListener(this)
        buildDialog()
    }

    // 삭제시 확인창
    // 취소 선택시, 확인창만 닫고,
    // 확인 선택시, 알림, 일정 삭제 후 화면 종료
    fun buildDialog() {
        builder = AlertDialog.Builder(this).apply {
            setTitle("일정 삭제")
            setMessage("정말 삭제하시겠습니까")
            setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                // 알람이 설정되어있는 경우
                // 알림 삭제
                if (schedule.alarmYear != null) {
//                    if (workManager == null) {
//                        setWorkManager(applicationContext)
//                    }
//                    workManager!!.cancelAlarm(schedule)
                    WorkManager.getInstance(applicationContext).cancelAllWorkByTag(schedule.id.toString())
                }

                // db에서 해당 일정 삭제
                db_handler.deleteSchedule(schedule)

                Toast.makeText(this.context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()

                dialog.dismiss()
                finish()
            })
        }
        builder.create()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_delete -> {
                builder.show()
            }
            R.id.btn_modify -> {
                if (schedule.id != null) {
                    val bundle = Bundle().apply {
                        putParcelable("schedule", schedule)
                    }
                    startActivity(Intent(this, Activity_New_Schedule::class.java).apply {
                        putExtra("bundle", bundle)
                    })
                }
            }
            else -> return
        }
    }

    // 받은 schedule 내용으로 화면 초기화
    fun setData() {

        with(schedule) {// 년/월/일 설정
            tv_year.text = year.toString()
            tv_month.text = NUM_FORMAT(month)
            tv_date.text = NUM_FORMAT(date)

            // 요일 설정
            tv_day.text = "${getDayOfWeekString(year, month, date)}요일"

            // 일정 시간을 지정했다면
            // 시간 설정
            // 아닌경우 invisible
            if (hour == null) {
                layout_scheduleTime.visibility = View.GONE
            } else {
                layout_scheduleTime.visibility = View.VISIBLE
                tv_scheduleAmPm.text = getAmPm(hour!!)
                tv_scheduleHour.text = NUM_FORMAT(getHour(hour!!))
                tv_scheduleMin.text = NUM_FORMAT(min!!)
            }

            // 일정 타이틀, 내용 설정
            tv_scheduleTitle.text = title
            tv_scheduleContent.text = content ?: ""

            // 알람 시간 설정
            // 알람을 설정하지 않은 경우, Gone
            if (alarmYear == null) {
                layout_alarm.visibility = View.GONE
            } else {
                layout_alarm.visibility = View.VISIBLE
                tv_alarmYear.text = alarmYear.toString()
                tv_alarmMonth.text = NUM_FORMAT(alarmMonth!!)
                tv_alarmDate.text = NUM_FORMAT(alarmDate!!)

                tv_alarm_ampm.text = getAmPm(alarmHour!!)
                tv_alarmHour.text = NUM_FORMAT(getHour(alarmHour!!))
                tv_alarmMin.text = NUM_FORMAT(alarmMin!!)
                tv_alarmDay.text = "${
                    getDayOfWeekString(
                        alarmYear!!,
                        alarmMonth!!,
                        alarmDate!!
                    )
                }요일"

            }
        }
    }

    override fun onRestart() {
        // 수정 후 돌아왔을 때, 내용 수정된 것으로 업데이트
        if (schedule.id != null) {
            schedule = db_handler.getSchedule(schedule.id!!) ?: Schedule()
            setData()
        }
        super.onRestart()
    }

    override fun onBackPressed() {
        // 알림으로 들어온 경우
        // 해당 화면만 acticity stack에 있음
        // isTaskRoot는 현재 activity가 작업에서 첫번째 Activity인지 boolean으로 반환
        if (isTaskRoot) {
            startActivity(Intent(this, ActivityCalendar::class.java).apply {
                putExtra("isFromNotification", true)
            })
            finish()
        }
        super.onBackPressed()
    }

}