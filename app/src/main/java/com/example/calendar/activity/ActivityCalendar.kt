package com.example.calendar.activity

//import kotlinx.android.synthetic.main.activity_main.*

import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.calendar.*
import com.example.calendar.adapter.MainCalendarAdapter
import com.example.calendar.data.CalendarItem
import com.example.calendar.data.Schedule
import com.example.calendar.data.SelectedDate
import com.example.calendar.dialog.DailyScheduleListDialog
import com.example.calendar.dialog.YearMonthSpinnerDialog
import kotlinx.android.synthetic.main.activity_calendar.*
import kotlinx.android.synthetic.main.layout_calendar.view.*
import kotlinx.android.synthetic.main.layout_day.view.*
import java.util.*
import android.view.WindowInsets

import android.view.WindowMetrics
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener


class ActivityCalendar : AppCompatActivity(), View.OnClickListener {


    // 앱 실행 날짜
    var todayYear = 0
    var todayMonth = 0
    var todayDate = 0

    // 달력에서 선택된 년, 월, 일
    lateinit var selectedDate: SelectedDate
    var firstDayOfWeek = 0


    // 년/월 선택창
    lateinit var pickYearMonthDialog: YearMonthSpinnerDialog

    // 선택된 년/월의 일정들
    // 일을 키값으로 해당 일자의 일정을 리스트로 저장
    lateinit var schedules: MutableMap<Int, MutableList<Schedule>>

    lateinit var calendarGridAdapter: MainCalendarAdapter
    var calendarItems = mutableListOf<CalendarItem>()

    // 선택된 칸 인덱스
    var selected = 0

    // 날짜 선택시 일정 목록창
    lateinit var dailyScheduleListDialog: DailyScheduleListDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        Log.i("today", CURRENTDATE)

        db_handler = setDB_Handler(applicationContext)
//        setWorkManager(applicationContext)

        for (idx in 0 until 42) {
            calendarItems.add(CalendarItem())
        }

        calendarGridAdapter = MainCalendarAdapter()
        // GridView item 높이 지정하기 위함
        // 뷰가 그려진 후 그리드뷰의 높이를 가져와서 전달
        // 전달 후, 해당 리스너가 무한히 호출되는 것을 막기위해 remove
        grid_calendar.getViewTreeObserver().addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val height: Int = grid_calendar.getMeasuredHeight() / 6
                if(height != 0){
                    calendarGridAdapter.setItemHeight(height)
                    Log.i("measured", "$height")
                    calendarGridAdapter.notifyDataSetChanged()
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        grid_calendar.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    } else {
                        grid_calendar.getViewTreeObserver().removeGlobalOnLayoutListener(this)
                    }
                }
            }
        })
        grid_calendar.adapter = calendarGridAdapter


        Log.i("cal.get", "${cal.get(Calendar.YEAR)}")
        Log.i("cal.get", "${cal.get(Calendar.MONTH)}")
        Log.i("cal.get", "${cal.get(Calendar.DATE)}")

        val isFromNotification = intent?.getBooleanExtra("isFromNotification", false)
        if(isFromNotification?: false){
            selectedDate = SelectedDate(
                currentYear,
                currentMonth,
                currentDate
            )
        }else {
            selectedDate = SelectedDate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DATE)
            )
        }
        firstDayOfWeek = selectedDate.getFirstDayOfWeek()

        todayYear = selectedDate.year
        todayMonth = selectedDate.month
        todayDate = selectedDate.date
        setButtons()
        setDialog()

    }

    // 년, 월, 달력에 표시할 데이터 바뀌면 업데이트
    fun updateCalendarItem() {

        // 선택된 월의 일정들 받아옴
        // (날짜 : 일정리스트)
        schedules = db_handler.getMonthlySchedules(selectedDate.year, selectedDate.month)

        // selected = 선택한 칸의 idx
        // 선택된 칸 인덱스가 0이상인 경우
        // -> 기존에 선택된 칸이 있는 경우
        //      기존 선택된 칸의 isSelected를 false로
        //  새로 선택한 칸의 isSelected를 true로
        if (selected >= 0) {
            calendarItems[selected].isSelected = false
        }
        selected = selectedDate.date + firstDayOfWeek - 2
        calendarItems[selected].isSelected = true


        // 선택된 월의 1일에 해당하는 idx
        var firstDateIdx = firstDayOfWeek - 1
        val lastDate = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        // 선택된 월의 마지막 날에 해당하는 idx + 1
        val lastIdx = lastDate + firstDayOfWeek - 1

        var dayCnt = 1 // 칸에 입력될 날짜
        for (idx in 0 until calendarItems.size) {
            // 1일보다 이전 칸이거나 마지막 날 이후의 칸인 경우
            // date를 빈 칸으로
            if (idx < firstDateIdx || idx >= lastIdx) {
                calendarItems[idx].date = ""
                calendarItems[idx].schedules = null
            } else {
                // 1일부터 마지막 날까지 값 입력
                // 해당 날짜에 일정이 있으면 입력, 없으면 null
                calendarItems[idx].date = "${dayCnt}"
                calendarItems[idx].schedules = schedules[dayCnt]
                dayCnt++
            }
        }

        calendarGridAdapter.calendarItems = calendarItems
        calendarGridAdapter.notifyDataSetChanged()

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_newSchedule -> {
                Log.i("dayOfWeek", cal.get(Calendar.DAY_OF_WEEK).toString())
                Log.i(
                    "cal date",
                    "${cal.get(Calendar.YEAR)} ${cal.get(Calendar.MONTH)} ${cal.get(Calendar.DATE)} ${
                        cal.get(Calendar.DAY_OF_WEEK)
                    }"
                )

                val bundle = Bundle().apply {
                    putParcelable("selectedDate", selectedDate)
                }
                var intent = Intent(this, Activity_New_Schedule::class.java)
                intent.putExtra("bundle", bundle)
                intent.putExtra("dayOfWeek", cal.get(Calendar.DAY_OF_WEEK))
                startActivity(intent)
            }
            R.id.btn_scheduleList -> {
                startActivity(Intent(this, Activity_Whole_Schedule::class.java))
            }
            R.id.tv_cal_YearMonth -> {
                pickYearMonthDialog.show()
            }

            else -> {
                return
            }
        }
    }

    fun setButtons() {

        btn_scheduleList.setOnClickListener(this)
        btn_newSchedule.setOnClickListener(this)

        grid_calendar.setOnItemClickListener { parent, view, position, id ->

            var firstIdx = firstDayOfWeek - 1
            val lastIdx = cal.getActualMaximum(Calendar.DAY_OF_MONTH) + firstDayOfWeek - 2
            Log.i("clicked grid position", "$position")

            if (position < firstIdx || position > lastIdx) {
                // 날짜 범위 밖의 칸이 선택된 경우
                //아무 동작 안 함
                return@setOnItemClickListener
            }
            if (selected == position) {
                // 현재 선택한 칸을 한 번 더 클릭한 경우
                // 해당 날짜의 스케줄 목록 dialog 띄움
                dailyScheduleListDialog.show(schedules[selectedDate.date] ?: mutableListOf())
            } else {
                // 현재 선택된 칸과 다른 칸을 선택한 경우
                // 선택되어있던 칸의 isSelected 값을 false로
                // 선택한 칸의 isSelected 값을 true로 바꾼 뒤, 화면 업데이트
                // calendar 값 수정
                calendarItems[selected].isSelected = false
                selected = position
                calendarItems[position].isSelected = true
                selectedDate.setDate(calendarItems[position].date)
                cal.set(Calendar.DATE, selectedDate.date)
                Log.i(
                    "selected date",
                    "${selectedDate.year}, ${selectedDate.month}, ${selectedDate.date}"
                )
                Log.i("selected Idx", "$selected")

                calendarGridAdapter.calendarItems = calendarItems
                calendarGridAdapter.notifyDataSetChanged()

            }
        }


        // 달력 좌우로 스왑해서 전환
        val chgCal = { y: Int, m: Int ->
            changeCalendar(y, m, true)
        }
        grid_calendar.setOnTouchListener(
            SwipeTouchListner(
                this, chgCal
            )
        )

        //  날짜 dialog
        tv_cal_YearMonth.setOnClickListener(this)


    }

    /**
     * @param type - true - 달력 스왑
     *             - false - 다이얼로그로 년/월 선택
     */
    fun changeCalendar(y: Int, m: Int, type: Boolean) {

        if (type) {
            selectedDate.year += y
            selectedDate.month += m
        } else {
            selectedDate.year = y
            selectedDate.month = m
        }

        // 월이 1 ~ 12가 아닌 경우 확인
        if (selectedDate.month > 12) {
            selectedDate.month = 1
            selectedDate.year += 1
        } else if (selectedDate.month < 1) {
            selectedDate.month = 12
            selectedDate.year -= 1
        }


        // 선택된 일자가 바뀐 달의 마지막 날보다 크면 마지막 날을 선택
        cal.set(Calendar.DATE, 1)
        cal.set(selectedDate.year, selectedDate.month - 1, 1)
        val lastDate = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (lastDate < selectedDate.date) {
            selectedDate.date = lastDate
        }
        cal.set(Calendar.DATE, selectedDate.date)
        firstDayOfWeek = getFirstDayOfWeek(selectedDate.year, selectedDate.month)
        tv_cal_YearMonth.text = "${selectedDate.year}.${NUM_FORMAT(selectedDate.month)}"
        updateCalendarItem()
    }

    /**
     * 년/월 선택하는 다이얼로그 화면 생성
     * 화면 상단에 년.월 텍스트 터치시 열림
     *
     * 날짜 선택시 일정 목록 보여주는 다이얼로그 생성
     */
    fun setDialog() {
        val chgCal = { y: Int, m: Int ->
            changeCalendar(y, m, false)
        }
        pickYearMonthDialog = YearMonthSpinnerDialog(this, selectedDate, chgCal).apply {
            setView(LayoutInflater.from(context).inflate(R.layout.dialog_date_spinner, null))
        }
        pickYearMonthDialog.create()


        dailyScheduleListDialog = DailyScheduleListDialog(this, selectedDate).apply {
            setView(LayoutInflater.from(context).inflate(R.layout.dialog_daily_schedule_list, null))
        }
        // 다이얼로그 layout margin 주기
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 30, 100, 30, 100)
        dailyScheduleListDialog.getWindow()?.setBackgroundDrawable(inset)

        dailyScheduleListDialog.create()
    }

    override fun onResume() {
        changeCalendar(selectedDate.year, selectedDate.month, false)

        // 다이얼로그가 떠있었다면 업데이트 시킨 내용으로 보여줌
        if (dailyScheduleListDialog.isShowing) {
            dailyScheduleListDialog.updateSchedules(schedules[selectedDate.date] ?: mutableListOf())
        }

        super.onResume()
    }
}

