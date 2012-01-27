/**
 * 
 */
package org.webreformatter.scrapper.context;

import org.webreformatter.commons.adapters.AdaptableObject;
import org.webreformatter.commons.adapters.AdapterFactoryUtils;
import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.adapters.IAdapterRegistry;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.scrapper.protocol.IProtocolHandler;

/**
 * @author kotelnikov
 */
public class ApplicationContext extends AdaptableObject {

    public static class Builder extends ApplicationContext {

        public Builder(IAdapterFactory factory) {
            this(factory, null);
        }

        public Builder(IAdapterFactory factory, ApplicationContext context) {
            super(factory, context);
        }

        public ApplicationContext build() {
            return new ApplicationContext(getAdapterFactory(), this);
        }

        @Override
        protected void checkFields() {
        }

        public ApplicationContext.Builder setPropertyProvider(
                IVariableProvider propertyProvider) {
            fPropertyProvider = propertyProvider;
            return this;
        }

        public ApplicationContext.Builder setProtocolHandler(
                IProtocolHandler protocolHandler) {
            fProtocolHandler = protocolHandler;
            return this;
        }

        public ApplicationContext.Builder setRepository(
                IWrfRepository repository) {
            fRepository = repository;
            return this;
        }

    }

    public static ApplicationContext.Builder builder(IAdapterFactory factory) {
        return new ApplicationContext.Builder(factory);
    }

    public static void registerAdapter(IAdapterRegistry registry, Class<?> type) {
        AdapterFactoryUtils.registerAdapter(registry, ApplicationContext.class,
                type, type);
    }

    public static void registerAdapter(IAdapterRegistry registry,
            Class<?> type, Class<?> implementation) {
        AdapterFactoryUtils.registerAdapter(registry, ApplicationContext.class,
                type, implementation);
    }

    protected IVariableProvider fPropertyProvider;

    protected IProtocolHandler fProtocolHandler;

    protected IWrfRepository fRepository;

    public ApplicationContext(IAdapterFactory factory,
            ApplicationContext context) {
        super(factory);
        if (context != null) {
            fRepository = context.fRepository;
            fPropertyProvider = context.fPropertyProvider;
            fProtocolHandler = context.fProtocolHandler;
        }
        checkFields();
    }

    private void assertTrue(String msg, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

    protected void checkFields() {
        assertTrue("Repository is not defined", fRepository != null);
        assertTrue("PropertyProvider is not defined", fPropertyProvider != null);
        assertTrue("Download manager is not defined", fProtocolHandler != null);
    }

    public IVariableProvider getPropertyProvider() {
        return fPropertyProvider;
    }

    public IProtocolHandler getProtocolHandler() {
        return fProtocolHandler;
    }

    public IWrfRepository getRepository() {
        return fRepository;
    }

}
