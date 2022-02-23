package com.example.calendar.dialog

import android.app.AlertDialog
import android.content.Context
import android.widget.NumberPicker
import com.example.calendar.data.SelectedDate
import kotlinx.android.synthetic.main.dialog_date_spinner.*

class YearMonthSpinnerDialog(
    context: Context,
    val selectedDate: SelectedDate,
    val chgCal: (Int, Int) -> Unit
) : AlertDialog(context) {

    // 창 열릴 때 현재 선택된 값 넣어줌
    override fun show() {
        super.show()
        npkr_year.value = selectedDate.year
        npkr_month.value = selectedDate.month
    }

    override fun create() {
        super.create()


        //  순환 안되게 막기
        npkr_year.wrapSelectorWheel = false
        npkr_month.wrapSelectorWheel = false

        //  editText 설정 해제
        npkr_year.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        npkr_month.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        //  최소값 설정
        npkr_year.minValue = 1900
        npkr_month.minValue = 1

        //  최대값 설정
        npkr_year.maxValue = 2300
        npkr_month.maxValue = 12

        //  취소 버튼 클릭 시
        btn_cancel.setOnClickListener {
            this.dismiss()
            this.cancel()
        }

        //  확인 버튼 클릭 시
        btn_ok.setOnClickListener {
            chgCal(npkr_year.value, npkr_month.value)
            this.dismiss()
        }

    }
}