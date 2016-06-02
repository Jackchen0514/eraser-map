package com.mapzen.erasermap.controller

import android.location.Location
import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.gson.Feature
import com.mapzen.tangram.LngLat
import com.mapzen.valhalla.Route

interface MainViewController {
    fun showSearchResults(features: List<Feature>?)
    fun addSearchResultsToMap(features: List<Feature>?, activeIndex: Int)
    fun showDirectionsList()
    fun hideDirectionsList()
    fun centerOnCurrentFeature(features: List<Feature>?)
    fun centerOnFeature(features: List<Feature>?, position: Int)
    fun showAllSearchResults(features: List<Feature>?)
    fun hideSearchResults()
    fun hideReverseGeolocateResult()
    fun showReverseGeocodeFeature(features: List<Feature>?)
    fun showPlaceSearchFeature(features: List<Feature>)
    fun showProgress()
    fun hideProgress()
    fun showActionViewAll()
    fun hideActionViewAll()
    fun collapseSearchView()
    fun expandSearchView()
    fun clearQuery()
    fun showRoutePreview(location: ValhallaLocation, feature: Feature)
    fun hideRoutePreview()
    fun hideRoutingMode()
    fun startRoutingMode(feature: Feature)
    fun resumeRoutingMode(feature: Feature)
    fun shutDown()
    fun centerMapOnLocation(lngLat: LngLat, zoom: Float)
    fun setMapTilt(radians: Float)
    fun resetMute()
    fun toggleMute()
    fun setMapRotation(radians: Float)
    fun drawRoute(route: Route)
    fun clearRoute()
    fun rotateCompass()
    fun reverseGeolocate(screenX: Float, screenY: Float)
    fun placeSearch(gid: String)
    fun emptyPlaceSearch()
    fun overridePlaceFeature(feature: Feature)
    fun drawTappedPoiPin()
    fun hideSettingsBtn()
    fun showSettingsBtn()
    fun onBackPressed()
    fun stopSpeaker()
    fun checkPermissionAndEnableLocation()
    fun executeSearch(query: String)
    fun onCloseAllSearchResults()
}
