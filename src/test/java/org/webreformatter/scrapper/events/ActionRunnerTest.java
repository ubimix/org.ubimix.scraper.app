/**
 * 
 */
package org.webreformatter.scrapper.events;

import java.io.IOException;
import java.util.Set;

import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.calls.CallListener;
import org.webreformatter.commons.events.server.CallBarrier;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.string.StringAdapter;
import org.webreformatter.scrapper.app.AbstractExectionContextTest;
import org.webreformatter.scrapper.context.ApplicationContext;
import org.webreformatter.scrapper.protocol.AccessManager;

/**
 * @author kotelnikov
 */
public class ActionRunnerTest extends AbstractExectionContextTest {

    public static class TestAction extends ProcessResource {
        public TestAction(ActionRequest request) {
            super(request);
        }
    }

    /**
     * @param name
     */
    public ActionRunnerTest(String name) {
        super(name);
    }

    protected ProcessResource.ActionResponse call(
        IEventManager eventManager,
        ProcessResource action) {
        final ProcessResource.ActionResponse[] result = { null };
        CallBarrier barrier = new CallBarrier();
        eventManager.fireEvent(
            action,
            barrier.add(new CallListener<ProcessResource>() {
                @Override
                protected void handleResponse(ProcessResource event) {
                    result[0] = event.getResponse();
                }
            }));
        barrier.await();
        Set<Throwable> errors = action.getErrors();
        if (errors != null && !errors.isEmpty()) {
            for (Throwable t : errors) {
                t.printStackTrace();
            }
        }
        assertNotNull(result[0]);
        return result[0];
    }

    public void test() throws IOException {
        // Path sourcePath = new Path("sources/abc.toto");
        // IWrfResource sourceResource = fResourceProvider.getResource(
        // sourcePath,
        // true);
        // Path targetPath = new Path("target/abc.toto");
        // IWrfResource targetResource = fResourceProvider.getResource(
        // targetPath,
        // true);
        //

        ApplicationContext context = newApplicationContext();
        IEventManager eventManager = context.getEventManager();
        eventManager.addListener(ApplyAction.class, new ApplyActionHandler(
            context));
        eventManager.addListener(
            CopyResourceAction.class,
            new CopyResourceHandler());
        eventManager.addListener(
            FormatHtmlAction.class,
            new FormatHtmlHandler());
        final boolean[] handled = { false };
        eventManager.addListener(
            TestAction.class,
            new ProcessResourceHandler<TestAction>() {
                @Override
                public String[] getActionNames() {
                    return array("test");
                }

                @Override
                public String[] getMimeTypes() {
                    return array("text");
                }

                @Override
                protected void handleRequest(TestAction event) {
                    ProcessResource.ActionResponse response = new ProcessResource.ActionResponse();
                    try {
                        ProcessResource.ActionRequest request = event
                            .getRequest();
                        IWrfResource initialResource = request
                            .getInitialResource();
                        response.setResultResource(initialResource);
                        // IWrfResource targetResource = setResponseResource(
                        // response,
                        // request);
                        // ImageAdapter inStringAdapter = initialResource
                        // .getAdapter(ImageAdapter.class);
                        // String str = inStringAdapter.getContentAsString();
                        // ImageAdapter outStringAdapter = targetResource
                        // .getAdapter(ImageAdapter.class);
                        // outStringAdapter.setContentAsString(str);
                        handled[0] = true;
                    } finally {
                        event.setResponse(response);
                    }
                }
            });

        Uri url = new Uri("http://en.wikipedia.org/wiki/Semantic_Web");
        IUrlTransformer downloadUrlTransformer = IUrlTransformer.EMPTY;
        IUrlTransformer localUrlTransformer = IUrlTransformer.EMPTY;
        PageSetConfig pageSetConfig = PageSetConfig
            .builder()
            .setDownloadUrlTransformer(downloadUrlTransformer)
            .setLocalizeUrlTransformer(localUrlTransformer)
            .setAccessManager(new AccessManager())
            .build();
        ProcessResource.ActionRequest request = ProcessResource.ActionRequest
            .builder(context)
            .setPageSetConfig(pageSetConfig)
            .setUrl(url)
            .setParameter("action", "test")
            .build();

        ProcessResource.ActionResponse result = call(
            eventManager,
            new TestAction(request));
        assertTrue(handled[0]);

        handled[0] = false;
        result = call(eventManager, new ApplyAction(request));
        assertTrue(handled[0]);

        IWrfResource targetResource = result.getResultResource();
        assertNotNull(targetResource);
        StringAdapter outStringAdapter = targetResource
            .getAdapter(StringAdapter.class);
        String str = outStringAdapter.getContentAsString();
        System.out.println(str);

    }
}
