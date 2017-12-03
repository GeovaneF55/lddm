package pucminas.com.br.rotas.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class RouteContent {

    /**
     * An array of sample (route) items.
     */
    public static final List<RouteItem> ITEMS = new ArrayList<RouteItem>();

    /**
     * A map of sample (route) items, by ID.
     */
    public static final Map<String, RouteItem> ITEM_MAP = new HashMap<String, RouteItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createRouteItem(i));
        }
    }

    private static void addItem(RouteItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static RouteItem createRouteItem(int position) {
        return new RouteItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }
}
