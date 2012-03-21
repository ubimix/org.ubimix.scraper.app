/**
 * 
 */
package org.webreformatter.scrapper.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.geo.TileInfo;
import org.webreformatter.commons.geo.TilesLoader;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.resources.IWrfResource;

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

        protected void handleError(String msg, Throwable t) {
            log.log(Level.SEVERE, msg, t);
        }

        @Override
        public void onTile(TileInfo tile) {
            String str = tile.getTilePath();
            Uri tileUri = fMapServerUrl
                .getBuilder()
                .appendFullPath(str)
                .build();
            IWrfResource resource = fContext.getResource("maps", tileUri, null);
            try {
                fContext.getAdapter(DownloadAdapter.class).loadResource(
                    tileUri,
                    resource);
                Path path = new Path.Builder(fPathPrefix)
                    .appendPath(str)
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