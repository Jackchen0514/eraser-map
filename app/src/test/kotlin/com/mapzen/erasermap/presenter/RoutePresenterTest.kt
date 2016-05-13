package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.dummy.TestHelper
import com.mapzen.erasermap.dummy.TestHelper.getFixture
import com.mapzen.erasermap.model.event.RouteCancelEvent
import com.mapzen.erasermap.view.MapListToggleButton
import com.mapzen.erasermap.view.TestRouteController
import com.mapzen.helpers.RouteEngine
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test


class RoutePresenterTest {
    val routeEngine = RouteEngine()
    val routeListener = RouteEngineListener()
    val bus = Bus()
    val vsm = ViewStateManager()
    val routePresenter = RoutePresenterImpl(routeEngine, routeListener, bus, vsm)
    val routeController = TestRouteController()

    @Before fun setUp() {
        routePresenter.routeController = routeController
    }

    @Test fun shouldNotBeNull() {
        assertThat(routePresenter).isNotNull()
    }

    @Test fun onRouteStart_shouldResetRouteEngine() {
        val route1 = Route(getFixture("valhalla_route"))
        val route2 = Route(getFixture("valhalla_route"))
        routeEngine.setListener(routeListener)
        routeEngine.route = route1
        routePresenter.onRouteStart(route2)
        assertThat(routeEngine.route).isEqualTo(route2)
    }

    @Test fun onRouteStart_shouldResetTrackingLocation() {
        routePresenter.onRouteStart(Route(getFixture("valhalla_route")))
        assertThat(routePresenter.isTrackingCurrentLocation()).isTrue()
        routePresenter.onInstructionPagerTouch()
        assertThat(routePresenter.isTrackingCurrentLocation()).isFalse()
        routePresenter.onRouteStart(Route(getFixture("valhalla_route")))
        assertThat(routePresenter.isTrackingCurrentLocation()).isTrue()
    }

    @Test fun onRouteResume_shouldNotResetRouteEngine() {
        val route1 = Route(getFixture("valhalla_route"))
        val route2 = Route(getFixture("valhalla_route"))
        routeEngine.setListener(routeListener)
        routeEngine.route = route1
        routePresenter.onRouteResume(route2)
        assertThat(routeEngine.route).isEqualTo(route1)
    }

    @Test fun onMapPan_shouldShowResumeButton() {
        routeController.isResumeButtonVisible = false
        routePresenter.onMapPan(RoutePresenter.GESTURE_MIN_DELTA, -RoutePresenter.GESTURE_MIN_DELTA)
        assertThat(routeController.isResumeButtonVisible).isTrue()
    }

    @Test fun onMapPan_shouldDisableLocationTracking() {
        routePresenter.onResumeButtonClick()
        routePresenter.onMapPan(-RoutePresenter.GESTURE_MIN_DELTA, RoutePresenter.GESTURE_MIN_DELTA)
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNull()
    }

    @Test fun onMapPan_shouldNotDisableLocationTrackingIfDeltaIsLessThanMinDistance() {
        routePresenter.onResumeButtonClick()
        routePresenter.onMapPan(0f, 0f)
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNotNull()
    }

    @Test fun onResumeButtonClick_shouldHideResumeButton() {
        routeController.isResumeButtonVisible = true
        routePresenter.onResumeButtonClick()
        assertThat(routeController.isResumeButtonVisible).isFalse()
    }

    @Test fun onResumeButtonClick_shouldEnableLocationTracking() {
        routePresenter.onMapPan(RoutePresenter.GESTURE_MIN_DELTA, RoutePresenter.GESTURE_MIN_DELTA)
        routePresenter.onResumeButtonClick()
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNotNull()
    }

    @Test fun onInstructionPagerTouch_shouldShowResumeButton() {
        routeController.isResumeButtonVisible = false
        routePresenter.onInstructionPagerTouch()
        assertThat(routeController.isResumeButtonVisible).isTrue()
    }

    @Test fun onInstructionPagerTouch_shouldDisableLocationTracking() {
        routePresenter.onResumeButtonClick()
        routePresenter.onInstructionPagerTouch()
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNull()
    }

    @Test fun onInstructionSelected_shouldCenterMapOnLocation() {
        val instruction = TestHelper.getTestInstruction()
        val location = TestHelper.getTestLocation()
        instruction.location = location
        routePresenter.onInstructionPagerTouch()
        routePresenter.onInstructionSelected(instruction)
        assertThat(routeController.mapLocation).isEqualTo(location)
    }

    @Test fun onClearRoute_shouldHideRouteIcon() {
        routeController.isRouteIconVisible = true
        routePresenter.onRouteClear()
        assertThat(routeController.isRouteIconVisible).isFalse()
    }

    @Test fun onClearRoute_shouldHideRouteLine() {
        routeController.isRouteLineVisible = true
        routePresenter.onRouteClear()
        assertThat(routeController.isRouteLineVisible).isFalse()
    }

    @Test fun onMapListToggleClick_shouldToggleViewState() {
        routePresenter.onMapListToggleClick(MapListToggleButton.MapListState.LIST)
        assertThat(routeController.isDirectionListVisible).isTrue()

        routePresenter.onMapListToggleClick(MapListToggleButton.MapListState.MAP)
        assertThat(routeController.isDirectionListVisible).isFalse()
    }

    @Test fun onRouteCancelButtonClick_shouldPostRouteCancelEvent() {
        val subscriber = RouteCancelSubscriber()
        bus.register(subscriber)
        routePresenter.onRouteCancelButtonClick()
        assertThat(subscriber.event).isNotNull()
    }

    class RouteCancelSubscriber {
        var event: RouteCancelEvent? = null
        @Subscribe fun onRouteCancelEvent(event: RouteCancelEvent) {
            this.event = event
        }
    }

    @Test fun onCenterMapOnLocation_shouldDynamicallySetZoom() {
        val route = Route(getFixture("long_route"))
        var location = route.getRouteInstructions()?.get(0)?.location
        routeEngine.setListener(routeListener)
        routeEngine.route = route
        routePresenter.onRouteStart(route)
        var routingZoom = routePresenter.mapZoomLevelForCenterMapOnLocation(location as Location)
        assertThat(routingZoom).isEqualTo(MainPresenter.ROUTING_ZOOM)
        location = route.getRouteInstructions()?.get(1)?.location
        routeEngine.onLocationChanged(location)
        var longManeuverZoom = routePresenter.mapZoomLevelForCenterMapOnLocation(location as Location)
        assertThat(longManeuverZoom).isEqualTo(MainPresenter.LONG_MANEUVER_ZOOM)
    }

    @Test fun onCenterMapOnLocation_shouldNotChangeZoomLevelForRelativelyShortManeuvers() {
        val route = Route(getFixture("long_route"))
        var location = route.getRouteInstructions()?.get(0)?.location
        routeEngine.setListener(routeListener)
        routeEngine.route = route
        routePresenter.onRouteStart(route)
        var routingZoom = routePresenter.mapZoomLevelForCenterMapOnLocation(location as Location)
        assertThat(routingZoom).isEqualTo(MainPresenter.ROUTING_ZOOM)
        location = route.getRouteInstructions()?.get(2)?.location
        routeEngine.onLocationChanged(location)
        routingZoom = routePresenter.mapZoomLevelForCenterMapOnLocation(location as Location)
        assertThat(routingZoom).isEqualTo(MainPresenter.ROUTING_ZOOM)
    }

    @Test fun onSetCurrentInstruction_shouldDisplayInstruction() {
        val route = Route(getFixture("valhalla_route"))
        routePresenter.onRouteStart(route)
        routePresenter.onSetCurrentInstruction(1)
        assertThat(routeController.displayIndex).isEqualTo(1)
    }

    @Test fun onSetCurrentInstruction_shouldNotDisplayFinalInstruction() {
        val route = Route(getFixture("valhalla_route"))
        val finalInstructionIndex = route.getRouteInstructions()?.size?.minus(1) as Int
        routePresenter.onRouteStart(route)
        routePresenter.onSetCurrentInstruction(1)
        routePresenter.onSetCurrentInstruction(finalInstructionIndex)
        assertThat(routeController.displayIndex).isEqualTo(1)
    }
}
