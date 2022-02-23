package com.example.calendar.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.calendar.*
import com.example.calendar.adapter.WholeScheduleAdapter
import kotlinx.android.synthetic.main.activity_whole_schedule.*
import kotlinx.android.synthetic.main.btn_layout_schedulelist.view.*
import kotlinx.android.synthetic.main.item_list_whole_schedule.view.*

/**
 * 전체 일정 조회
 * 오늘 날짜보다 이전/이후로 나눠서
 */
class Activity_Whole_Schedule : AppCompatActivity() {

    var flag = 0
    lateinit var btnFrames: MutableList<View>

    lateinit var adapter: WholeScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whole_schedule)

        // 상단 버튼 (이후 일정/지난 일정) 전환
        btnFrames = mutableListOf(btn_period.btnFrame_post, btn_period.btnFrame_pre)

        adapter = WholeScheduleAdapter()
        lv_scheduleList.adapter = adapter


        // 일정 선택
        lv_scheduleList.setOnItemClickListener { parent, view, position, id ->
            val bundle = Bundle().apply {
                putParcelable("schedule", adapter.getItem(position))
            }
            startActivity(Intent(this, Activity_Check_Schedule::class.java).apply {
                putExtra("bundle", bundle)
            })
        }
        setButton()
    }

    fun setButton() {

        // 지난 일정/이후 일정 버튼 클릭 시
        btn_period.setOnClickListener {
            // 상단 텍스트 모양 변경
            btnFrames[flag].visibility = View.GONE
            flag = ++flag % 2
            btnFrames[flag].visibility = View.VISIBLE

            val schedules = db_handler.getSchedules(currentYear, currentMonth, currentDate, flag)
            if(schedules.size == 0){
                tv_textNoSchedule.visibility = View.VISIBLE
                lv_scheduleList.visibility = View.GONE
            }else{
                tv_textNoSchedule.visibility = View.GONE
                lv_scheduleList.visibility = View.VISIBLE

                // 데이터 변경
                adapter.schedules = schedules
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        // 화면 시작될 때, 다시 돌아왔을 때
        // 일정 목록 업데이트
        adapter.schedules = db_handler.getSchedules(currentYear, currentMonth, currentDate, flag)
        adapter.notifyDataSetChanged()
        super.onStart()
    }
}