import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.geojson.GeoJSONReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NUTSIndex {
    private final List<HashMap<String, NUTSRegion>> nutsRegions;

    public NUTSIndex(String source) throws IOException {
        nutsRegions = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            nutsRegions.addLast(readNutsRegions(String.format("%s/nutsrg_%d.geojson", source, i)));
        }
    }

    private HashMap<String, NUTSRegion> readNutsRegions(String fileName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(fileName);
        GeoJSONReader geoJSONReader = new GeoJSONReader(fileInputStream);

        SimpleFeatureCollection simpleFeatureCollection = geoJSONReader.getFeatures();
        SimpleFeatureIterator simpleFeatureIterator = simpleFeatureCollection.features();

        HashMap<String, NUTSRegion> nutsRegions = new HashMap<>();

        try {
            while (simpleFeatureIterator.hasNext()) {
                SimpleFeature simpleFeature = simpleFeatureIterator.next();
                Object geometry = simpleFeature.getDefaultGeometry();
                MultiPolygon multiPolygon;

                if (geometry instanceof Polygon)
                    multiPolygon = promotePolygon((Polygon)geometry);
                else if (geometry instanceof MultiPolygon)
                    multiPolygon = (MultiPolygon)geometry;
                else
                    throw new ClassCastException(String.format("NUTS Region geometry must be of type Polygon or MultiPolygon, not of type %s", geometry.getClass().getName()));

                String nutsName = (String)simpleFeature.getAttribute("na");
                String nutsId = (String)simpleFeature.getAttribute("id");

                nutsRegions.put(nutsId, new NUTSRegion(nutsId, nutsName, multiPolygon));
            }
        } finally {
            simpleFeatureIterator.close();
            geoJSONReader.close();
            fileInputStream.close();
        }

        return nutsRegions;
    }

    private MultiPolygon promotePolygon(Polygon polygon) {
        GeometryFactory gf = new GeometryFactory();
        Polygon[] polygons = new Polygon[1];
        polygons[0] = polygon;
        return gf.createMultiPolygon(polygons);
    }

    public List<NUTSRegion> getNUTSPath(Point point, int depth) {
        HashMap<String, NUTSRegion> searchRegions = nutsRegions.get(depth);
        List<NUTSRegion> nutsPath = new ArrayList<>();

        for (NUTSRegion region : searchRegions.values()) {
            if (region.contains(point)) {
                nutsPath.addFirst(region);
                break;
            }
        }

        if (nutsPath.isEmpty())
            return nutsPath;

        depth--;

        while (depth >= 0) {
            NUTSRegion containedRegion = nutsPath.getFirst();
            String containedId = containedRegion.getId();
            String nextId = containedId.substring(0, containedId.length()-1);
            NUTSRegion nextRegion = nutsRegions.get(depth).get(nextId);

            nutsPath.addFirst(nextRegion);

            depth--;
        }

        return nutsPath;
    }

    public List<NUTSRegion> getNUTSPath(Point point) {
        return getNUTSPath(point, 3);
    }

    public List<List<NUTSRegion>> getNUTSPaths(MultiPolygon polygon, int depth) {
        HashMap<String, NUTSRegion> searchRegions = nutsRegions.get(depth);
        List<List<NUTSRegion>> nutsPaths = new ArrayList<>();

        for (NUTSRegion region : searchRegions.values()) {
            if (region.overlaps(polygon)) {
                nutsPaths.addFirst(new ArrayList<>());
                nutsPaths.getFirst().addFirst(region);
            }
        }

        if (nutsPaths.isEmpty())
            return nutsPaths;

        depth--;

        while (depth >= 0) {
            for (List<NUTSRegion> nutsPath : nutsPaths) {
                NUTSRegion containedRegion = nutsPath.getFirst();
                String containedId = containedRegion.getId();
                String nextId = containedId.substring(0, containedId.length()-1);
                NUTSRegion nextRegion = nutsRegions.get(depth).get(nextId);

                nutsPath.addFirst(nextRegion);
            }
            depth--;
        }

        return nutsPaths;
    }

    public List<List<NUTSRegion>> getNUTSPaths(Polygon polygon, int depth) {
        return getNUTSPaths(promotePolygon(polygon), depth);
    }

    public List<List<NUTSRegion>> getNUTSPaths(MultiPolygon polygon) {
        return getNUTSPaths(polygon, 3);
    }

    public List<List<NUTSRegion>> getNUTSPaths(Polygon polygon) {
        return getNUTSPaths(promotePolygon(polygon));
    }
}
