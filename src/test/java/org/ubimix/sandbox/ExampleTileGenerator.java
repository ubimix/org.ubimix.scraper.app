/**
 * 
 */
package org.ubimix.sandbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ubimix.commons.geo.GeoPoint;
import org.ubimix.commons.geo.ImagePoint;
import org.ubimix.commons.geo.TileInfo;
import org.ubimix.commons.geo.TilesLoader;
import org.ubimix.commons.geo.ZoomLevel;
import org.ubimix.commons.json.JsonObject;
import org.ubimix.commons.json.JsonValue;
import org.ubimix.commons.uri.Path;
import org.ubimix.commons.uri.Uri;
import org.ubimix.commons.xml.XmlException;
import org.ubimix.resources.IWrfResource;
import org.ubimix.resources.adapters.cache.CachedResourceAdapter;
import org.ubimix.resources.adapters.cache.DateUtil;
import org.ubimix.resources.adapters.zip.ZipAdapter;
import org.ubimix.scrapper.core.AppContext;
import org.ubimix.scrapper.core.MapAdapter.MapTilesLoaderListener;

/**
 * @author kotelnikov
 */
public class ExampleTileGenerator {

    public static void main(String[] args) throws IOException, XmlException {
        CachedResourceAdapter.setRefreshTimeout(DateUtil.MIN * 10);
        CachedResourceAdapter.setExpirationTimeout(DateUtil.DAY * 2);
        new ExampleTileGenerator().exportTiles();
        System.exit(0);
    }

    private AppContext fAppContext = new AppContext("./data", true);

    /**
     * 
     */
    public ExampleTileGenerator() {
    }

    private void exportTiles() throws IOException {
        ZoomLevel minZoomLevel = ZoomLevel.CITY;
        ZoomLevel maxZoomLevel = ZoomLevel.STREET;
        final Uri mapServerUrl = new Uri("http://tile.openstreetmap.org/");
        File zipFile = new File("./tmp/maps.zip");

        JsonObject params = JsonObject.newValue("{"
            + "mapZoomMin: 'city',"
            + "mapZoomMax: 'building',"
            + "mapTop: [48.85915, 2.33983],"
            + "mapBottom: [48.86576, 2.33490]"
            + "}");

        GeoPoint mapTop = getGeoPoint(params, "mapTop");
        GeoPoint mapBottom = getGeoPoint(params, "mapBottom");
        String str = params.getString("mapZoomMin");
        ZoomLevel a = ZoomLevel.toZoomLevel(str, minZoomLevel);
        str = params.getString("mapZoomMax");
        ZoomLevel b = ZoomLevel.toZoomLevel(str, maxZoomLevel);
        ZoomLevel minZoom = ZoomLevel.min(a, b);
        ZoomLevel maxZoom = ZoomLevel.max(a, b);
        final Set<TileInfo> set = new HashSet<TileInfo>();
        MapTilesLoaderListener tilesListener = new MapTilesLoaderListener(
            "maps",
            fAppContext,
            mapServerUrl) {

            @Override
            public void begin(
                TileInfo minTile,
                TileInfo maxTile,
                GeoPoint min,
                GeoPoint max) {
                ImagePoint tileNumbers = TileInfo.getTileNumber(
                    minTile,
                    maxTile);
                String msg = String.format(
                    "Download %d (%d x %d) tiles for zoom level %d ...",
                    tileNumbers.getX() * tileNumbers.getY(),
                    tileNumbers.getX(),
                    tileNumbers.getY(),
                    minTile.getZoom());
                System.out.println(msg);
            }

            @Override
            public void end(
                TileInfo minTile,
                TileInfo maxTile,
                GeoPoint min,
                GeoPoint max) {
                System.out.println("Level " + minTile.getZoom() + " is done.");
            }

            @Override
            public void onTile(TileInfo tile) {
                if (set.contains(tile)) {
                    throw new IllegalArgumentException(
                        "Tile already exists in the specified set!");
                }
                set.add(tile);
                System.out.println("  * " + tile.getTilePath());
                super.onTile(tile);
            }
        };
        TilesLoader loader = new TilesLoader(
            mapTop,
            mapBottom,
            minZoom.getLevel(),
            maxZoom.getLevel());
        loader.load(tilesListener);
        Map<Path, IWrfResource> tiles = tilesListener.getMapTiles();

        zipFile.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(zipFile);
        try {
            ZipAdapter.zip(tiles.entrySet(), out);
        } finally {
            out.close();
        }
    }

    private GeoPoint getGeoPoint(JsonObject params, String key) {
        ArrayList<String> list = params.getList(key, JsonValue.STRING_FACTORY);
        try {
            return GeoPoint.newPoint(list.get(0), list.get(1));
        } catch (Throwable t) {
            return null;
        }
    }

}
