package com.example.calendar.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.calendar.R
import com.example.calendar.adapter.DailyScheduleAdapter
import com.example.calendar.data.Schedule
import com.example.calendar.data.SelectedDate
import com.example.calendar.db_handler
import com.example.calendar.getDayOfWeekString
import kotlinx.android.synthetic.main.activity_daily_schedule.*

class ActivityDailySchedule : AppCompatActivity() {

    // 선택된 년/월/일/요일
    lateinit var selectedDate: SelectedDate

    lateinit var adapter: DailyScheduleAdapter
    lateinit var schedules: MutableList<Schedule>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_schedule)

        val intent = intent

        val bundle = intent.getBundleExtra("bundle")
        if(bundle != null){
            selectedDate = bundle.get("selectedDate") as SelectedDate
            schedules = bundle.get("schedules") as MutableList<Schedule>
            setTitle(selectedDate.year, selectedDate.month, selectedDate.date, selectedDate.getDayOfWeek()?: "_")
        }

        adapter = DailyScheduleAdapter(schedules)
        lv_scheduleList.adapter = adapter
        adapter.notifyDataSetChanged()

        btn_newSchedule.setOnClickListener {
            val bundle = Bundle().apply{
                putParcelable("selectedDate", selectedDate)
            }
            val intent = Intent(this, Activity_New_Schedule::class.java)
            intent.putExtra("bundle", bundle)
            startActivity(intent)
        }

        // 일정 목록에서 선택
        // 해당 일정의 상세정보 화면으로 이동
        lv_scheduleList.setOnItemClickListener { parent, view, position, id ->
            var schedule = schedules[position]
            val bundle = Bundle().apply{
                putParcelable("schedule", schedule)
            }
            startActivity(Intent(this, Activity_Check_Schedule::class.java).apply{
                putExtra("bundle", bundle)
            })
        }
    }

    /**
     * 상단바에 날짜 입력
     */
    fun setTitle(year: Int, month: Int, date: Int, dayOfWeek: String){
        tv_date.text = "${year}년 ${month}월 ${date}일 ${dayOfWeek}요일"
    }

    /**
     * 일정 추가/수정 후, 돌아왔을 때, 일정 목록 업데이트
     */
    override fun onRestart() {
        schedules = db_handler.getDailySchedules(selectedDate.year, selectedDate.month, selectedDate.date)
        adapter.schedules = schedules
        adapter.notifyDataSetChanged()
        super.onRestart()
    }
}