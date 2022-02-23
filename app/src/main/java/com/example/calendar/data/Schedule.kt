package com.example.calendar.data

import android.os.Parcel
import android.os.Parcelable

/**
 * 일정 데이터
 * @param id - db에서 알아서 지정
 * @param year - 일정 년/월/일
 * @param month
 * @param date
 * @param hour - 일정 시/분 - nullable
 * @param min
 * @param title - 일정 타이틀
 * @param content - 일정에 대한 상세내용/설명 - nullable
 * @param alarmYear, alarmMonth, alarmDate, alarmHour, alarmMin - 알람 설정한 경우 년/월/일 시/분 - nullable
 */
class Schedule(
    var id: Int? = null,
    var year: Int = 0,
    var month: Int = 0,
    var date: Int = 0,
    var hour: Int? = null,
    var min: Int? = null,
    var title: String = "",
    var content: String? = null,
    var alarmYear: Int? = null,
    var alarmMonth: Int? = null,
    var alarmDate: Int? = null,
    var alarmHour: Int? = null,
    var alarmMin: Int? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()?: "",
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeInt(year)
        parcel.writeInt(month)
        parcel.writeInt(date)
        parcel.writeValue(hour)
        parcel.writeValue(min)
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeValue(alarmYear)
        parcel.writeValue(alarmMonth)
        parcel.writeValue(alarmDate)
        parcel.writeValue(alarmHour)
        parcel.writeValue(alarmMin)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Schedule> {
        override fun createFromParcel(parcel: Parcel): Schedule {
            return Schedule(parcel)
        }

        override fun newArray(size: Int): Array<Schedule?> {
            return arrayOfNulls(size)
        }
    }

}