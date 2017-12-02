package pucminas.com.br.rotas.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import pucminas.com.br.rotas.tasks.DrawMapRouteTask;


public class RouteTrackService extends IntentService {
    public static volatile boolean isTracking = true;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RouteTrackService() {
        super(RouteTrackService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        RouteTrackService.isTracking = true;
        DrawMapRouteTask drawMapRouteTask = new DrawMapRouteTask();

        while (RouteTrackService.isTracking) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
