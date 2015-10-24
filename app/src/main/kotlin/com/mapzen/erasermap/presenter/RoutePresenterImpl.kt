package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.helpers.RouteEngine
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route

public class RoutePresenterImpl(private val routeEngine: RouteEngine,
        private val routeEngineListener: RouteEngineListener) : RoutePresenter {

    override var routeController: RouteViewController? = null
        set(value) {
            field = value
            routeEngineListener.controller = value
        }

    override var currentInstructionIndex: Int = 0

    private var route: Route? = null
    private var isTrackingCurrentLocation: Boolean = true

    override fun onLocationChanged(location: Location) {
        routeEngine.onLocationChanged(location)
    }

    override fun onRouteStart(route: Route?) {
        this.route = route
        routeEngine.route = route
        routeEngine.setListener(routeEngineListener)
        currentInstructionIndex = 0
        routeController?.setCurrentInstruction(0)
    }

    override fun onRouteResume(route: Route?) {
        routeController?.setCurrentInstruction(currentInstructionIndex)
    }

    override fun onMapGesture() {
        isTrackingCurrentLocation = false
        routeController?.showResumeButton()
    }

    override fun onResumeButtonClick() {
        isTrackingCurrentLocation = true
        routeController?.hideResumeButton()
        routeController?.centerMapOnCurrentLocation()
    }

    override fun onInstructionPagerTouch() {
        isTrackingCurrentLocation = false
        routeController?.showResumeButton()
    }

    override fun onInstructionSelected(instruction: Instruction) {
        if (!isTrackingCurrentLocation) {
            routeController?.centerMapOnLocation(instruction.location)
        }
    }

    override fun onUpdateSnapLocation(location: Location) {
        routeController?.showRouteIcon(location)
        if (isTrackingCurrentLocation) {
            routeController?.centerMapOnLocation(location)
        }
    }
}