package pucminas.com.br.rotas.route;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class RouteItem {
    public final String id;
    public final ArrayList<LatLng> content;

    public RouteItem(String id, ArrayList<LatLng> content) {
        this.id = id;
        this.content = content;
    }

    @Override
    public String toString() {
        return id;
    }
}