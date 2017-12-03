package pucminas.com.br.rotas.route;

/**
 * Created by geovane on 03/12/17.
 */
public class RouteItem {
    public final String id;
    public final String content;
    public final String details;

    public RouteItem(String id, String content, String details) {
        this.id = id;
        this.content = content;
        this.details = details;
    }

    @Override
    public String toString() {
        return content;
    }
}