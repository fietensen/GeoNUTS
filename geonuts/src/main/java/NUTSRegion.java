import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;

public class NUTSRegion {
    private MultiPolygon geometry;
    private String id;
    private String name;

    public NUTSRegion(String id, String name, MultiPolygon geometry) {
        this.id = id;
        this.name = name;
        this.geometry = geometry;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public MultiPolygon getGeometry() {
        return this.geometry;
    }

    public boolean contains(Point point) {
        return this.geometry.contains(point);
    }

    public boolean overlaps(MultiPolygon polygon) {
        return this.geometry.overlaps(polygon) || this.geometry.coveredBy(polygon) || this.geometry.contains(polygon);
    }

    public String toString() {
        return String.format("NUTSRegion(name=\"%s\", id=\"%s\")", name, id);
    }
}
