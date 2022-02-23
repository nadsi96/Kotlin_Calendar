package com.example.calendar.data

import android.os.Parcel
import android.os.Parcelable

/**
 * @param isSelected - 선택된 칸인지 확인 -true - 선택된 칸
 *                                      - false - 선택되지 않은 칸
 * @param date - 해당 칸에 들어갈 날짜
 * @param schedules - 해당 날짜의 일정 리스트
 */
data class CalendarItem(
    var isSelected: Boolean = false,
    var date: String = "",
    var schedules: MutableList<Schedule>? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readString()?: "",
        parcel.readList(mutableListOf<Schedule>(), ClassLoader.getSystemClassLoader()) as MutableList<Schedule>
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isSelected) 1 else 0)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CalendarItem> {
        override fun createFromParcel(parcel: Parcel): CalendarItem {
            return CalendarItem(parcel)
        }

        override fun newArray(size: Int): Array<CalendarItem?> {
            return arrayOfNulls(size)
        }
    }

}