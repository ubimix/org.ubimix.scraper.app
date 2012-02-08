/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.adapters.AbstractAdaptableObject;

/**
 * @author kotelnikov
 */
public abstract class AbstractContext extends AbstractAdaptableObject {

    public static class ContextAdapter<C extends AbstractContext> {

        protected C fContext;

        /**
         * 
         */
        public ContextAdapter(C context) {
            fContext = context;
        }

        protected C getContext() {
            return fContext;
        }

    }

    private Map<String, Object> fValues = new HashMap<String, Object>();

    public AbstractContext() {
        super();
    }

    protected void assertTrue(String msg, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

    public <T extends AbstractContext, V> T build() {
        checkFields();
        return cast();
    }

    @SuppressWarnings("unchecked")
    protected <T extends AbstractContext> T cast() {
        return (T) this;
    }

    protected void checkFields() {
    }

    protected void copyFrom(AbstractContext context) {
        fValues.putAll(context.fValues);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractContext)) {
            return false;
        }
        AbstractContext o = (AbstractContext) obj;
        return fValues.equals(o.fValues);
    }

    public String getParameter(String key) {
        Map<String, String> params = getParams(false);
        String value = params != null ? params.get(key) : null;
        if (value == null) {
            AbstractContext parent = getParentContext();
            value = parent != null ? parent.getParameter(key) : null;
        }
        return value;
    }

    public boolean getParameter(String key, boolean defaultValue) {
        String str = getParameter(key);
        boolean result = defaultValue;
        if (str != null) {
            str = str.trim().toLowerCase();
            result = "1".equals(str) || "yes".equals(str) || "ok".equals(str);
        }
        return result;
    }

    public int getParameter(String key, int defaultValue) {
        String str = getParameter(key);
        int result = defaultValue;
        if (str != null) {
            str = str.trim();
            try {
                result = Integer.parseInt(str);
            } catch (Throwable t) {
            }
        }
        return result;
    }

    public Map<String, String> getParams() {
        return getParams(false);
    }

    private Map<String, String> getParams(boolean create) {
        Map<String, String> params = getValue("$params$");
        if (params == null && create) {
            params = new HashMap<String, String>();
            setValue("$params$", params);
        }
        return params;
    }

    public abstract AbstractContext getParentContext();

    protected <T> T getValue(Class<T> type) {
        return getValue(type.getName());
    }

    @SuppressWarnings("unchecked")
    protected <V> V getValue(String key) {
        return (V) fValues.get(key);
    }

    @Override
    public int hashCode() {
        return fValues.hashCode();
    }

    public <T extends AbstractContext> T setParameter(String key, String value) {
        Map<String, String> params = getParams(true);
        params.put(key, value);
        return cast();
    }

    public <T extends AbstractContext> T setParams(Map<String, String> params) {
        Map<String, String> map = getParams(true);
        map.putAll(params);
        return cast();
    }

    protected <T extends AbstractContext, V> T setValue(Class<V> type, V value) {
        return setValue(type.getName(), value);
    }

    protected <T extends AbstractContext> T setValue(String key, Object value) {
        fValues.put(key, value);
        return cast();
    }

    protected <T extends AbstractContext, V> T setValue(V value) {
        @SuppressWarnings("unchecked")
        Class<V> type = (Class<V>) value.getClass();
        return setValue(type, value);
    }

    @Override
    public String toString() {
        return fValues.toString();
    }

}
