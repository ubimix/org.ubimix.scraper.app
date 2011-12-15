package org.webreformatter.scrapper.protocol;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.commons.uri.path.PathManager;

/**
 * This object is used to get credentials(login/password) corresponding to an
 * URL.
 * 
 * @author kotelnikov
 */
public class AccessManager {
    /**
     * This class is used as a container for user credentials.
     * 
     * @author kotelnikov
     */
    public static class CredentialInfo {

        /**
         * User's login
         */
        private final String fLogin;

        /**
         * User's password
         */
        private final String fPassword;

        /**
         * This constructor initializes the internal fields.
         * 
         * @param login the login to set
         * @param password the password to set
         */
        public CredentialInfo(String login, String password) {
            fLogin = login;
            fPassword = password;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CredentialInfo)) {
                return false;
            }
            CredentialInfo o = (CredentialInfo) obj;
            return equals(fLogin, o.fLogin) && equals(fPassword, o.fPassword);
        }

        /**
         * Compares two strings and returns <code>true</code> if they are equal.
         * 
         * @param first the first string to compare
         * @param second the second string to compare
         * @return <code>true</code> if the compared strings are equal
         */
        private boolean equals(String first, String second) {
            return first != null && second != null
                ? first.equals(second)
                : first == second;
        }

        /**
         * Returns the login
         * 
         * @return the login
         */
        public String getLogin() {
            return fLogin;
        }

        /**
         * Returns the password
         * 
         * @return the password
         */
        public String getPassword() {
            return fPassword;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int first = fLogin != null ? fLogin.hashCode() : 0;
            int second = fPassword != null ? fPassword.hashCode() : 0;
            return first ^ second;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            String first = fLogin != null ? fLogin : "";
            String second = fPassword != null ? fPassword : "";
            return first + ":" + second;
        }
    }

    /**
     * This object is used to map base URLs to the corresponding credentials.
     */
    private PathManager<CredentialInfo> fMap = new PathManager<CredentialInfo>();

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AccessManager)) {
            return false;
        }
        AccessManager o = (AccessManager) obj;
        return fMap.equals(o.fMap);
    }

    /**
     * Returns credentials corresponding to the given URL
     * 
     * @param url the URL for which the credentials should be returned
     * @return credentials corresponding to the given URL
     */
    public CredentialInfo getCredentials(Uri url) {
        if (url == null) {
            return null;
        }
        String path = getPath(url);
        CredentialInfo result = fMap.getNearestValue(path);
        return result;
    }

    /**
     * Returns the path corresponding to the given URL
     * 
     * @param url the URL to transform to a path
     * @return the path corresponding to the given URL
     */
    private String getPath(Uri url) {
        Path path = UriToPath.getPath(url);
        return path.toString();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return fMap.hashCode();
    }

    /**
     * Sets new credentials corresponding to the given URL
     * 
     * @param url the URL for which the credentials should be defined
     * @param credentials the credentials to set
     */
    public void setCredentials(Uri url, CredentialInfo credentials) {
        String path = getPath(url);
        fMap.add(path, credentials);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return fMap.toString();
    }
}
