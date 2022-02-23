package com.example.calendar

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener


class SwipeTouchListner(context: Context?, val changeCalendar: (Int, Int) -> Unit) :
    OnTouchListener {
    private val gestureDetector: GestureDetector

    // 화면 드래그할 최소 거리
    // 100 이상 드래그 하면 동작
    private val minDist = 200
    private val minVel = 100

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            /*
            touch -> click -> longClick 순으로 이벤트 전달
            터치만 하고 끝내고 싶다면
            true 반환

            true - 뒤의 listner까지 이벤트를 전달하지 않음
            false - 뒤 listner까지 이벤트 전달
             */
            return false
        }

        override fun onFling(
            e1: MotionEvent, e2: MotionEvent, velocityX: Float,
            velocityY: Float
        ): Boolean {
            try {
                // 터치시작부터 땐 순간까지
                // 좌우 좌표 차이
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                // 상하 이동거리보다 좌우 이동거리가 더 큰 경우만 인정
                if(Math.abs(diffY) < Math.abs(diffX)) {
                    if (Math.abs(diffX) > minDist // 드래그한 거리
                        && Math.abs(velocityX) > minVel // 드래그 속도
                    ) {
                        if (diffX > 0) {
                            // 이전 달로 이동
                            changeCalendar(0, -1)
                        } else {
                            // 다음 달로 이동
                            changeCalendar(0, 1)
                        }
                    } else {
                        return true
                    }
                }else{
                    return true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return false
        }

    }

    /*fun onSwipeRight() {
        // 이전 달로 이동
        changeCalendar(0, -1)
    }

    fun onSwipeLeft() {
        // 다음 달로 이동
        changeCalendar(0, 1)
    }*/

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        // TODO Auto-generated method stub
        return gestureDetector.onTouchEvent(event)
    }


}