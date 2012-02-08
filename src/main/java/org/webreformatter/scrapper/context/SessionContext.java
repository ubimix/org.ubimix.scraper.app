/**
 * 
 */
package org.webreformatter.scrapper.context;

import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.scrapper.protocol.AccessManager;

/**
 * @author kotelnikov
 */
public class SessionContext extends AbstractContext {

    /**
     * @param applicationContext
     */
    protected SessionContext(ApplicationContext applicationContext) {
        super();
        setValue(ApplicationContext.class, applicationContext);
    }

    @Override
    protected void checkFields() {
        assertTrue(
            "Application context can not be null",
            getApplicationContext() != null);
        if (getDownloadUrlTransformer() == null) {
            setDownloadUrlTransformer(IUrlTransformer.EMPTY);
        }
        if (getAccessManager() == null) {
            setAccessManager(new AccessManager());
        }
    }

    public AccessManager getAccessManager() {
        return getValue(AccessManager.class);
    }

    @Override
    public IAdapterFactory getAdapterFactory() {
        return getApplicationContext().getAdapterFactory();
    }

    public ApplicationContext getApplicationContext() {
        return getValue(ApplicationContext.class);
    }

    public IUrlTransformer getDownloadUrlTransformer() {
        return getValue(IUrlTransformer.class);
    }

    @Override
    public AbstractContext getParentContext() {
        return getApplicationContext();
    }

    public IWrfResource getResource(String storeName, Uri url, String suffix) {
        ApplicationContext applicationContext = getApplicationContext();
        IWrfRepository repository = applicationContext.getRepository();
        IWrfResourceProvider store = repository.getResourceProvider(
            storeName,
            true);
        Path path = UriToPath.getPath(url);
        Path.Builder builder = path.getBuilder();
        if (suffix != null) {
            builder.appendPath("$").appendPath(suffix);
        }
        Path targetResultPath = builder.build();
        IWrfResource targetResource = store.getResource(targetResultPath, true);
        return targetResource;
    }

    public RuntimeContext newRuntimeContext() {
        return newRuntimeContext(Uri.EMPTY);
    }

    public RuntimeContext newRuntimeContext(Uri uri) {
        return new RuntimeContext(this).setUrl(uri);
    }

    public <T extends SessionContext> T setAccessManager(
        AccessManager accessManager) {
        return setValue(AccessManager.class, accessManager);
    }

    public <T extends SessionContext> T setDownloadUrlTransformer(
        IUrlTransformer downloadUrlTransformer) {
        return setValue(IUrlTransformer.class, downloadUrlTransformer);
    }

}
