/**
 * 
 */
package org.ubimix.scrapper.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ubimix.commons.geo.TileInfo;
import org.ubimix.commons.geo.TilesLoader;
import org.ubimix.commons.uri.Path;
import org.ubimix.commons.uri.Uri;
import org.ubimix.resources.IWrfResource;

/**
 * @author kotelnikov
 */
public class MapAdapter extends AppContextAdapter {

    /**
     * @author kotelnikov
     */
    public static class MapTilesLoaderListener extends TilesLoader.LoadListener {

        private final static Logger log = Logger
            .getLogger(MapTilesLoaderListener.class.getName());

        private AppContext fContext;

        private Uri fMapServerUrl;

        private String fPathPrefix;

        private Map<Path, IWrfResource> fResults;

        public MapTilesLoaderListener(
            String pathPrefix,
            AppContext context,
            Uri mapServerUrl) {
            this(
                pathPrefix,
                context,
                mapServerUrl,
                new HashMap<Path, IWrfResource>());
        }

        public MapTilesLoaderListener(
            String pathPrefix,
            AppContext context,
            Uri mapServerUrl,
            Map<Path, IWrfResource> results) {
            fPathPrefix = pathPrefix;
            fResults = results;
            fContext = context;
            fMapServerUrl = mapServerUrl;
        }

        public Map<Path, IWrfResource> getMapTiles() {
            return fResults;
        }

        public Uri getTileUrl(TileInfo tile) {
            String str = tile.getTilePath();
            Uri tileUri = fMapServerUrl
                .getBuilder()
                .appendFullPath(str)
                .build();
            return tileUri;
        }

        protected void handleError(String msg, Throwable t) {
            log.log(Level.SEVERE, msg, t);
        }

        @Override
        public void onTile(TileInfo tile) {
            Uri tileUri = getTileUrl(tile);
            IWrfResource resource = fContext.getResource("maps", tileUri, null);
            try {
                fContext.getAdapter(DownloadAdapter.class).loadResource(
                    tileUri,
                    resource);
                String tilePath = tile.getTilePath();
                Path path = new Path.Builder(fPathPrefix)
                    .appendPath(tilePath)
                    .build();
                fResults.put(path, resource);
            } catch (IOException e) {
                handleError("Can not load a tile " + tile + ".", e);
            }
        }

    }

    public MapAdapter(AppContext appContext) {
        super(appContext);
    }

    public MapTilesLoaderListener newTilesListener(Uri mapServerUrl) {
        return newTilesListener(mapServerUrl, "maps");
    }

    public MapTilesLoaderListener newTilesListener(
        Uri mapServerUrl,
        String pathPrefix) {
        return new MapTilesLoaderListener(pathPrefix, fContext, mapServerUrl);

    }

}
