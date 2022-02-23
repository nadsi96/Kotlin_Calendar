package com.example.calendar.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.calendar.NUM_FORMAT
import com.example.calendar.R
import com.example.calendar.activity.Activity_Check_Schedule
import com.example.calendar.activity.Activity_New_Schedule
import com.example.calendar.adapter.DailyScheduleAdapter
import com.example.calendar.data.Schedule
import com.example.calendar.data.SelectedDate
import kotlinx.android.synthetic.main.dialog_daily_schedule_list.*

class DailyScheduleListDialog(context: Context?, var selectedDate: SelectedDate) :
    AlertDialog(context), View.OnClickListener {
    lateinit var dailyScheduleAdapter: DailyScheduleAdapter
    var schedules = mutableListOf<Schedule>()

    /**
     * 선택한 날짜, 요일 표시
     */
    fun setDate() {
        tv_dailyScheduleListdate.text =
            "${selectedDate.year}.${NUM_FORMAT(selectedDate.month)}.${NUM_FORMAT(selectedDate.date)}"
        tv_dailyScheduleListday.text = "${selectedDate.getDayOfWeek()}"
    }

    fun updateSchedules(schedules: MutableList<Schedule>){
        this.schedules = schedules
        dailyScheduleAdapter.schedules = schedules
        dailyScheduleAdapter.notifyDataSetChanged()


        // 기록된 일정이 없다면
        // 기록된 일정이 없습니다. 문구 띄워줌
        if(schedules.size == 0){
            tv_textNoSchedule.visibility = View.VISIBLE
            lv_scheduleList.visibility = View.GONE
        }else{
            tv_textNoSchedule.visibility = View.GONE
            lv_scheduleList.visibility = View.VISIBLE
        }
    }
    /**
     * 다이얼로그 화면을 띄울 때
     * 선택한 날짜 설정
     * 해당 날짜의 일정을 받아서 리스트뷰에 표시
     * @param schedules listView에 보여줄 일정 목록
     */
    fun show(schedules: MutableList<Schedule> = mutableListOf()) {
        setDate()
        updateSchedules(schedules)
        super.show()
    }

    override fun create() {
        super.create()

        dailyScheduleAdapter = DailyScheduleAdapter()
        lv_scheduleList.adapter = dailyScheduleAdapter

        // 리스트뷰에서 항목 선택시
        // 해당하는 일정의 상세정보 화면으로 이동
        lv_scheduleList.setOnItemClickListener { parent, view, position, id ->
            var schedule = schedules[position]
            val bundle = Bundle().apply {
                putParcelable("schedule", schedule)
            }
            context.startActivity(Intent(context, Activity_Check_Schedule::class.java).apply {
                putExtra("bundle", bundle)
            })
        }

        setButton()

    }

    fun setButton(){
        // 닫기
        btn_close.setOnClickListener(this)
        // 새로운 일정 추가
        btn_newSchedule.setOnClickListener(this)

        // 다이얼로그 영역 밖 클릭하면 종료
        layout_top.setOnClickListener(this)
        layout_dialog.setOnClickListener(this)
        tv_textNoSchedule.setOnClickListener(this)
    }
    override fun onClick(v: View) {
        when(v.id){
            R.id.layout_top, R.id.tv_dailyScheduleListdate, R.id.tv_dailyScheduleListday, R.id.tv_textNoSchedule -> {

            }
            R.id.btn_close -> {
                this.dismiss()
            }
            R.id.btn_newSchedule -> {
                // 새로운 일정 추가 화면으로 이동
                val bundle = Bundle().apply {
                    putParcelable("selectedDate", selectedDate)
                }
                val intent = Intent(context, Activity_New_Schedule::class.java)
                intent.putExtra("bundle", bundle)
                context.startActivity(intent)
            }
            R.id.lv_scheduleList -> {
                lv_scheduleList.onItemClickListener
            }

            else -> {
                this.dismiss()
            }
        }
    }

}