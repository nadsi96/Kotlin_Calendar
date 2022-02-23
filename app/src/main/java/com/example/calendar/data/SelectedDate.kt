package com.example.calendar.data

import android.os.Parcel
import android.os.Parcelable
import com.example.calendar.getDayOfWeekString
import com.example.calendar.getFirstDayOfWeek

/**
 * 달력에서 선택된 날짜
 * 년/월/일
 */
class SelectedDate(
    var year: Int,
    var month: Int,
    var date: Int
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(year)
        parcel.writeInt(month)
        parcel.writeInt(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SelectedDate> {
        override fun createFromParcel(parcel: Parcel): SelectedDate {
            return SelectedDate(parcel)
        }

        override fun newArray(size: Int): Array<SelectedDate?> {
            return arrayOfNulls(size)
        }
    }

    /**
     * 선택된 날짜의 요일
     * return 일 ~ 토
     */
    fun getDayOfWeek(): String? {
        return getDayOfWeekString(year, month, date)
    }
    fun getFirstDayOfWeek(): Int {
        return getFirstDayOfWeek(year, month)
    }
    fun setDate(date: String){
        this.date = Integer.parseInt(date)
    }
}