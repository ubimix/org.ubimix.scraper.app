/**
 * 
 */
package org.webreformatter.scrapper.context;

import org.webreformatter.commons.adapters.AdapterFactoryUtils;
import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.adapters.IAdapterRegistry;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.scrapper.protocol.IProtocolHandler;

/**
 * @author kotelnikov
 */
public class ApplicationContext extends AbstractContext {

    public static ApplicationContext builder(IAdapterFactory factory) {
        return new ApplicationContext(factory);
    }

    public static void registerAdapter(IAdapterRegistry registry, Class<?> type) {
        AdapterFactoryUtils.registerAdapter(
            registry,
            ApplicationContext.class,
            type,
            type);
    }

    public static void registerAdapter(
        IAdapterRegistry registry,
        Class<?> type,
        Class<?> implementation) {
        AdapterFactoryUtils.registerAdapter(
            registry,
            ApplicationContext.class,
            type,
            implementation);
    }

    private IAdapterFactory fAdapterFactory;

    public ApplicationContext(IAdapterFactory factory) {
        fAdapterFactory = factory;
    }

    @Override
    protected void checkFields() {
        assertTrue("Repository is not defined", getRepository() != null);
        assertTrue(
            "PropertyProvider is not defined",
            getPropertyProvider() != null);
        assertTrue(
            "Download manager is not defined",
            getProtocolHandler() != null);
    }

    @Override
    public IAdapterFactory getAdapterFactory() {
        return fAdapterFactory;
    }

    @Override
    public AbstractContext getParentContext() {
        return null;
    }

    public IVariableProvider getPropertyProvider() {
        return getValue(IVariableProvider.class);
    }

    public IProtocolHandler getProtocolHandler() {
        return getValue(IProtocolHandler.class);
    }

    public IWrfRepository getRepository() {
        return getValue(IWrfRepository.class);
    }

    public SessionContext newSessionContext() {
        SessionContext sessionContext = new SessionContext(this);
        return sessionContext;
    }

    public <T extends ApplicationContext, V> T setPropertyProvider(
        IVariableProvider propertyProvider) {
        return setValue(IVariableProvider.class, propertyProvider);
    }

    public <T extends ApplicationContext, V> T setProtocolHandler(
        IProtocolHandler protocolHandler) {
        return setValue(IProtocolHandler.class, protocolHandler);
    }

    public <T extends ApplicationContext, V> T setRepository(
        IWrfRepository repository) {
        return setValue(IWrfRepository.class, repository);
    }

}
