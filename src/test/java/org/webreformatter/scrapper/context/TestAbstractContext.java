/**
 * 
 */
package org.webreformatter.scrapper.context;

import junit.framework.TestCase;

import org.webreformatter.commons.adapters.IAdapterFactory;

/**
 * @author kotelnikov
 */
public class TestAbstractContext extends TestCase {

    public static class TestContext extends AbstractContext {

        @Override
        public IAdapterFactory getAdapterFactory() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AbstractContext getParentContext() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    /**
     * @param name
     */
    public TestAbstractContext(String name) {
        super(name);
    }

    public void test() throws Exception {
        class ClassA {
        }
        class ClassB extends ClassA {
        }
        class ClassC {
        }
        AbstractContext context = new TestContext();
        ClassC objectC = new ClassC();
        ClassA objectA = new ClassB();
        TestContext ctxt = context
            .setValue(ClassA.class, objectA)
            .setValue(objectC)
            .build();
        assertSame(context, ctxt);
        assertSame(objectA, context.getValue(ClassA.class));
        assertSame(objectC, context.getValue(ClassC.class));
        assertNull(context.getValue(ClassB.class));

    }
}
