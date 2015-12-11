package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route

public interface RoutePresenter {
    companion object {
        public val GESTURE_MIN_DELTA = 0.0001f
    }

    public var routeController: RouteViewController?
    public var currentInstructionIndex: Int

    public fun onLocationChanged(location: Location)
    public fun onRouteStart(route: Route?)
    public fun onRouteResume(route: Route?)
    public fun onMapGesture(action: Int, pointerCount: Int, deltaX: Float, deltaY: Float)
    public fun onResumeButtonClick()
    public fun onInstructionPagerTouch()
    public fun onInstructionSelected(instruction: Instruction)
    public fun onUpdateSnapLocation(location: Location)
    public fun onRouteClear()
}
