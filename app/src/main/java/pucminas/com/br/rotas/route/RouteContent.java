package pucminas.com.br.rotas.route;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
public class RouteContent {

    /**
     * An array of sample (route) items.
     */
    public static final List<RouteItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample (route) items, by ID.
     */
    public static final Map<String, RouteItem> ITEM_MAP = new HashMap<>();

    public static void addItem(RouteItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static void clearItems() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    public static RouteItem createRouteItem(String id, ArrayList<LatLng> content) {
        return new RouteItem(id, content);
    }
}
