package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.*;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.*;

import static ca.ubc.cs.cpsc210.translink.util.Geometry.rectangleContainsPoint;
import static ca.ubc.cs.cpsc210.translink.util.Geometry.rectangleIntersectsLine;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /** overlay used to display bus route legend text on a layer above the map */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /** overlays used to plot bus routes */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     * @param context   the application context
     * @param mapView   the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */

        public void plotRoutes(int zoomLevel) {
            updateVisibleArea();

            busRouteOverlays.clear();
            busRouteLegendOverlay.clear();

            Stop selectedStop = StopManager.getInstance().getSelected();

            List<GeoPoint> listPoints = new ArrayList<>();
            boolean firstOutOfMap = true;

            GeoPoint pointOutOfMap = new GeoPoint(0.0,0.0);


            if (selectedStop != null) {

                for (Route route : selectedStop.getRoutes()) {

                    busRouteLegendOverlay.add(route.getNumber());

                    for (RoutePattern routePattern : route.getPatterns()) {

                        Polyline line = new Polyline(context);
                        line.setColor(busRouteLegendOverlay.getColor(route.getNumber()));
                        line.setWidth(getLineWidth(zoomLevel));



                        for (int i = 0; i < routePattern.getPath().size(); i++) {

                            LatLon a = routePattern.getPath().get(i);

                            GeoPoint point = new GeoPoint(a.getLatitude(), a.getLongitude());

                            if (rectangleContainsPoint(northWest, southEast, a)) {

                                if (!firstOutOfMap) {
                                    listPoints.add(pointOutOfMap);
                                }

                                listPoints.add(point);
                                firstOutOfMap = true;

                            } else if(firstOutOfMap){
                                listPoints.add(point);
                                line.setPoints(listPoints);
                                listPoints.clear();
                                line.setWidth(getLineWidth(zoomLevel));
                                busRouteOverlays.add(line);
                                line = new Polyline(context);
                                line.setColor(busRouteLegendOverlay.getColor(route.getNumber()));
                                firstOutOfMap = false;
                                pointOutOfMap = point;
                                }
                                else
                                    pointOutOfMap = point;
                        }
                        line.setPoints(listPoints);
                        line.setWidth(getLineWidth(zoomLevel));
                        listPoints.clear();
                        firstOutOfMap = true;
                        busRouteOverlays.add(line);

                        }

                    }
                }
            }



    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     * @param zoomLevel   the zoom level of the map
     * @return            width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if(zoomLevel > 14)
            return 7.0f * BusesAreUs.dpiFactor();
        else if(zoomLevel > 10)
            return 5.0f * BusesAreUs.dpiFactor();
        else
            return 2.0f * BusesAreUs.dpiFactor();
    }
}
