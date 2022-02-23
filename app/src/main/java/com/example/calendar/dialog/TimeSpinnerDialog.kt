package com.example.calendar.dialog

import android.app.AlertDialog
import android.content.Context
import android.widget.NumberPicker
import kotlinx.android.synthetic.main.dialog_time_spinner.*


/**
 * 일정/알람 시간 설정위한 dialog
 * h, m은 spinner의 초기값 지정위한 시와 분
 * @param type - 1 - 알람시간 설정
 *             - 0 - 일정 시간 설정
 */
class TimeSpinnerDialog(context: Context, time: List<MutableMap<String, Int>>, var type: Int) :
    AlertDialog(context) {

    // 1 - 알람 시간 설정
    // 0 - 일정 시간 설정
    var selectedHour = time[type]["hour"] ?: 0
    var selectedMin = time[type]["min"] ?: 0

    // 버튼 설정
    // ok버튼 클릭시 선택된 시, 분 반환
    fun setButton(ok: (h: Int, m: Int, isAlarm: Int) -> Unit) {
        btn_cancel.setOnClickListener { this.dismiss() }
        btn_ok.setOnClickListener {
            selectedHour = numPicker_hour.value
            selectedMin = numPicker_min.value

            ok(selectedHour, selectedMin, type)
            this.dismiss()
        }
    }

    // 창 열릴 때 현재 선택된 값 넣어줌
    override fun show() {
        super.show()
        numPicker_hour.value = selectedHour
        numPicker_min.value = selectedMin
    }

    override fun create() {
        super.create()

        numPicker_hour.wrapSelectorWheel = false
        numPicker_min.wrapSelectorWheel = false

        //  editText 설정 해제
        numPicker_hour.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        numPicker_min.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        //  최소값 설정
        numPicker_hour.minValue = 0
        numPicker_min.minValue = 0

        //  최대값 설정
        numPicker_hour.maxValue = 23
        numPicker_min.maxValue = 59

    }

}