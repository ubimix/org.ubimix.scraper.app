/**
 * 
 */
package org.webreformatter.scrapper.context;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.scrapper.protocol.AccessManager;
import org.webreformatter.scrapper.protocol.AccessManager.CredentialInfo;

/**
 * This class (and its subclasses) are used to configure {@link SessionContext}
 * instances.
 * 
 * @author kotelnikov
 */
public class SessionContextConfigurator {

    /**
     * The access manager containing credentials for external resources
     */
    private AccessManager fAccessManager = new AccessManager();

    private ApplicationContext fApplicationContext;

    /**
     * This URL transformer is used to map logical URLs (URLs defined in
     * documents) to real URLs used to physically download the corresponding
     * resources.
     */
    private IUrlTransformer fUrlTransformer;

    public SessionContextConfigurator(ApplicationContext applicationContext) {
        fApplicationContext = applicationContext;
    }

    /**
     * This method is used to associate credentials with all resources starting
     * with the specified base URL.
     * 
     * @param baseUrl the basic URL associated with the specified credentials
     * @param login the login used to access resources
     * @param pwd the password associated with the specified login
     */
    public void addCredentials(String baseUrl, String login, String pwd) {
        Uri url = new Uri(baseUrl);
        fAccessManager.setCredentials(url, new CredentialInfo(login, pwd));
    }

    /**
     * @return <code>true</code> if the download module ignore already existing
     *         cache
     */
    public boolean clearDownloadCache() {
        return true;
    }

    public SessionContext configureSessionContext() {
        SessionContext fSessionContext = fApplicationContext
            .newSessionContext();
        if (clearDownloadCache()) {
            fSessionContext.setParameter("clearcache", "yes");
        }
        AccessManager accessManager = getAccessManager();
        IUrlTransformer downloadUrlTransformer = getDownloadUrlTransformer();
        fSessionContext
            .setAccessManager(accessManager)
            .setDownloadUrlTransformer(downloadUrlTransformer)
            .build();
        return fSessionContext;
    }

    /**
     * @return the access manager containing credentials for external resources
     */
    public AccessManager getAccessManager() {
        return fAccessManager;
    }

    /**
     * Returns the URL transformer used to map "logical" resource URLs to real
     * URLs used to download resources. For example the resulting URLs could
     * have additional parameters, not defined in the original documents.
     * 
     * @return the URL transformer used to map "logical" resource URLs to real
     *         URLs used to download resources.
     */
    public IUrlTransformer getDownloadUrlTransformer() {
        if (fUrlTransformer == null) {
            fUrlTransformer = newDownloadUrlTransformer();
        }
        return fUrlTransformer;
    }

    protected IUrlTransformer newDownloadUrlTransformer() {
        return IUrlTransformer.EMPTY;
    }

}
