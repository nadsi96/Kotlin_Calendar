package com.example.calendar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.calendar.NUM_FORMAT
import com.example.calendar.R
import com.example.calendar.data.Schedule
import com.example.calendar.getAmPm
import com.example.calendar.getHour
import kotlinx.android.synthetic.main.item_daily_schedule.view.img_alarm
import kotlinx.android.synthetic.main.item_daily_schedule.view.tv_content
import kotlinx.android.synthetic.main.item_daily_schedule.view.tv_title
import kotlinx.android.synthetic.main.item_list_daily_schedule.view.*

class DailyScheduleAdapter(var schedules: MutableList<Schedule> = mutableListOf<Schedule>()) :
    BaseAdapter() {
    override fun getCount(): Int {
        return schedules.size
    }

    override fun getItem(position: Int): Schedule {
        return schedules[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var itemView = convertView
        var vh: DailyScheduleViewHolder

        if (itemView == null) {
//            itemView = LayoutInflater.from(parent?.context).inflate(R.layout.item_daily_schedule, parent, false)
            itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_list_daily_schedule, parent, false)
            vh = DailyScheduleViewHolder(itemView)
            itemView.tag = vh
        } else {
            vh = itemView.tag as DailyScheduleViewHolder
        }

        val item = getItem(position)

        // 일정에 시간, 알람시간 둘 다 설정되지 않았는지 확인
        var flag = false
        // 일정, 알람 시간 사이 구분선
        // 둘 다 설정되지 않았다면 INVISIBLE
        vh.img_timeSeparator.visibility = View.VISIBLE

        // 일정 시간 설정
        if (item.hour== null || item.min == null) {
            vh.tv_time.visibility = View.INVISIBLE
            flag = true
        } else {
            // 시간이 설정된 경우, 오전/오후 00시 00분
            vh.tv_time.visibility = View.VISIBLE
            vh.tv_time.text =
                "${getAmPm(item.hour!!)} ${NUM_FORMAT(getHour(item.hour!!))}시 ${NUM_FORMAT(item.min!!)}분"
        }

        if (item.alarmYear == null) {
            vh.img_alarm.visibility = View.INVISIBLE
            vh.tv_alarmTime.visibility = View.INVISIBLE
            if (flag) {
                // 일정 시간도 설정되지 않은 경우
                // 구분선도 안 보이게
                vh.img_timeSeparator.visibility = View.INVISIBLE
            }
        } else {
            // 알람이 설정된 경우,
            // 알람 이미지 출력
            // 오전/오후 00시 00분
            vh.img_alarm.visibility = View.VISIBLE
            vh.tv_alarmTime.visibility = View.VISIBLE
            vh.tv_alarmTime.text =
                "${getAmPm(item.alarmHour!!)} ${NUM_FORMAT(getHour(item.alarmHour!!))}시 ${
                    NUM_FORMAT(
                        item.alarmMin!!
                    )
                }분"
        }
        vh.tv_title.text = item.title

        if (item.content== null) {
            vh.tv_content.visibility = View.INVISIBLE
        } else {
            vh.tv_content.text = item.content
            vh.tv_content.visibility = View.VISIBLE
        }


        return itemView
    }

}

class DailyScheduleViewHolder(v: View) {
    val img_timeSeparator = v.img_timeSeparator
    val img_alarm = v.img_alarm
    val tv_time = v.tv_time
    val tv_alarmTime = v.tv_alarmTime
    val tv_title = v.tv_title
    val tv_content = v.tv_content
}