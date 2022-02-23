package com.example.calendar.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.calendar.R
import com.example.calendar.data.CalendarItem
import kotlinx.android.synthetic.main.layout_day.view.*

class MainCalendarAdapter(var calendarItems: List<CalendarItem> = listOf()) : BaseAdapter() {

    val GRAY = Color.rgb(0xd1, 0xd1, 0xd1)
    val BLUE = Color.rgb(0x55, 0x55, 0xff)
    val RED = Color.RED
    val BLACK = Color.BLACK

    private var itemHeight = 0

    fun setItemHeight(h: Int){
        itemHeight = h-6
    }

    override fun getCount(): Int {
        return calendarItems.size
    }

    override fun getItem(position: Int): CalendarItem {
        return calendarItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var itemView = convertView
        var vh: CalendarItemViewHolder
        if (itemView == null) {
            itemView =
                LayoutInflater.from(parent?.context).inflate(R.layout.layout_day, parent, false)

            vh = CalendarItemViewHolder(itemView)

            itemView.tag = vh
        } else {
            vh = itemView.tag as CalendarItemViewHolder
        }

        // GridView Item 높이 설정
        // 화면에 맞춤
        if(itemHeight != 0){
            val params: ViewGroup.LayoutParams = vh.lyt.layoutParams
            params.height = itemHeight
            vh.lyt.layoutParams = params
        }else{
            val params: ViewGroup.LayoutParams = vh.lyt.layoutParams
            params.height = 294
            vh.lyt.layoutParams = params
        }


        with(getItem(position)) {

            // 해당 위치의 줄이 빈 줄인지 확인
            // 빈 줄이면 GONE
            // 넷째주까지는 항상 있으니까 idx가 28 이상인것만 확인
            // 해당 주의 첫 날의 데이터 확인해서 빈칸인지 확인
            // 빈 칸이면 GONE하고 리턴
            if (position >= 28 && getItem(position - position % 7).date == "") {
                vh.v.visibility = View.GONE
                return itemView
            }

            vh.v.visibility = View.VISIBLE
            vh.tv_date.text = date

            // 선택된 칸인지 확인
            // 선택된 칸이면 해당 칸의 테두리, 숫자 색 변경
            if (isSelected) {
                vh.v.setBackgroundResource(R.drawable.bg_line_light_blue)
                vh.tv_date.setBackgroundResource(R.drawable.bg_oval_light_blue)
                vh.tv_date.setTextColor(BLACK)
            } else {
                // 선택된 칸이 아니면 요일에 따라 색 지정
                vh.v.setBackgroundResource(R.drawable.bg_line_lightgray)
                vh.tv_date.setBackgroundResource(0)
                when (position % 7) {
                    0 -> vh.tv_date.setTextColor(RED) // 일요일
                    6 -> vh.tv_date.setTextColor(BLUE)// 토요일
                    else -> vh.tv_date.setTextColor(GRAY)
                }
            }

            // 일정 표시 부분 일단 안 보이게
            for (idx in vh.tv_schedules.indices) {
                vh.tv_schedules[idx].visibility = View.INVISIBLE
            }

            // 해당 일자에 기록된 일정 갯수
            val scheduleCnt = schedules?.size ?: 0
            for (idx in 0 until scheduleCnt) {
                vh.tv_schedules[idx].visibility = View.VISIBLE

                if (idx == 2) {
                    vh.tv_schedules[idx].text = "+${scheduleCnt - 2}.."
                    break
                }
                vh.tv_schedules[idx].text = schedules!![idx].title
            }
        }

        return itemView
    }

}

class CalendarItemViewHolder(var v: View) {
    var lyt = v.layout_daySelect
    var tv_date = v.tv_date // 날짜 표시
    var tv_schedules = listOf(v.tv_schedule1, v.tv_schedule2, v.tv_extra)
    //                             일정 1,       일정 2,         그 외 갯수
}