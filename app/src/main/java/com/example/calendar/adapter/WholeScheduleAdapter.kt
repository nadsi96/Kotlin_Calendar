package com.example.calendar.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import com.example.calendar.NUM_FORMAT
import com.example.calendar.R
import com.example.calendar.activity.Activity_Check_Schedule
import com.example.calendar.data.Schedule
import com.example.calendar.getDayOfWeekString
import kotlinx.android.synthetic.main.item_list_whole_schedule.view.*

class WholeScheduleAdapter(var schedules: MutableList<Schedule> = mutableListOf<Schedule>()) :
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
        var vh: WholeScheduleViewHolder
        if (itemView == null) {
//            itemView = LayoutInflater.from(parent?.context).inflate(R.layout.item_list_schedule, parent, false)
            itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_list_whole_schedule, parent, false)
            vh = WholeScheduleViewHolder(itemView)
            itemView.tag = vh
        } else {
            vh = itemView.tag as WholeScheduleViewHolder
        }


        with(getItem(position)) {

            if (position > 0) {
                // 날짜 구분선 설정
                val preItem = getItem(position - 1)
                if (preItem.year == year && preItem.month == month && preItem.date == date) {
                    // 이전 항목과 날짜가 같다면 날짜 구분선 없앰
                    vh.line_date_divide.visibility = View.GONE
                } else {
                    // 다르다면 현재 항목 날짜를 구분선으로 추가
                    vh.line_date_divide.visibility = View.VISIBLE
                    vh.tv_date.text = "${year}.${NUM_FORMAT(month)}.${NUM_FORMAT(date)} ${
                        getDayOfWeekString(
                            year,
                            month,
                            date
                        )
                    }요일"
                }

                vh.tv_title.text = title
                if (content != null) {
                    vh.tv_content.text = content
                } else {
                    vh.tv_content.text = ""
                }
            } else {
                vh.line_date_divide.visibility = View.VISIBLE
                vh.tv_date.text = "${year}.${NUM_FORMAT(month)}.${NUM_FORMAT(date)} ${
                    getDayOfWeekString(
                        year,
                        month,
                        date
                    )
                }요일"
                vh.tv_title.text = title
                if (content != null && content != "null") {
                    vh.tv_content.text = content
                } else {
                    vh.tv_content.text = ""
                }
            }
        }
        return itemView
    }
}

class WholeScheduleViewHolder(v: View) {
    val tv_title = v.tv_title
    val tv_content = v.tv_content

    val line_date_divide = v.line_date_divide
    val tv_date = v.tv_date
}
