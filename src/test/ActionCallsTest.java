package org.ubimix.scraper.events;

import java.util.HashSet;
import java.util.Set;

import org.ubimix.commons.events.EventManager;
import org.ubimix.commons.events.IEventManager;
import org.ubimix.commons.events.calls.CallEvent;
import org.ubimix.commons.events.calls.CallListener;
import org.ubimix.commons.uri.Path;
import org.ubimix.commons.uri.Uri;
import org.ubimix.commons.uri.UriToPath;
import org.ubimix.resources.AbstractResourceTest;
import org.ubimix.resources.IWrfRepository;
import org.ubimix.resources.IWrfResource;
import org.ubimix.resources.IWrfResourceProvider;
import org.ubimix.scraper.context.ApplicationContext;
import org.ubimix.scraper.events.LoadResource;
import org.ubimix.scraper.events.LoadResource.Request;
import org.ubimix.scraper.events.ServiceCall;

public class ActionCallsTest extends AbstractResourceTest {

    public static class AtomCleanupCall extends TransformResourceCall {

        public AtomCleanupCall(ExecutionContext context, Uri uri) {
            this(new AtomCleanupCall.Request(context, uri, getResource(
                context,
                "download",
                uri), getResource(context, "atom", uri)));
        }

        public AtomCleanupCall(
            ExecutionContext context,
            Uri uri,
            IWrfResource source,
            IWrfResource target) {
            this(new AtomCleanupCall.Request(context, uri, source, target));
        }

        public AtomCleanupCall(Request request) {
            super(request);
        }
    }

    public static class FormatCall extends TransformResourceCall {

        public FormatCall(ExecutionContext context, Uri uri) {
            this(new FormatCall.Request(context, uri, getResource(
                context,
                "atom",
                uri), getResource(context, "formatted", uri)));
        }

        public FormatCall(Request request) {
            super(request);
        }
    }

    public static class MainCall
        extends
        CallEvent<MainCall.Request, MainCall.Response> {

        public static class Request {
        }

        public static class Response {
        }

        public MainCall(Request request) {
            super(request);
        }

        public Uri getUri() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public static class MainCallListener extends CallListener<MainCall> {

        public MainCallListener() {
        }

        private void callSpecificAction(MainCall originalEvent) {
            // TODO
            // * Get the name of the action
            // * Load the event associated with this action
            // * Instantiate an event registered for this type of actions
            // * If there is no events for this action then - instantiate a
            // default call
            // * Perform the call
            // * Call back the original event with results of thecleanupToAtom
            // call
        }

        private void cleanupToAtom(final MainCall originalEvent) {
            fEventManager.fireEvent(
                new AtomCleanupCall(),
                new CallListener<AtomCleanupCall>() {
                    @Override
                    protected void handleResponse(AtomCleanupCall event) {
                        if (event.hasErrors()) {
                            replyWithErrors(originalEvent, event.getErrors());
                        } else {
                            callSpecificAction(originalEvent);
                        }
                    }
                });
        }

        private void download(final MainCall originalEvent) {
            Uri uri = originalEvent.getUri();
            IWrfResourceProvider resourceProvider = fRepository
                .getResourceProvider("download", true);
            Path path = UriToPath.getPath(uri);
            IWrfResource resource = resourceProvider.getResource(path, true);
            fEventManager.fireEvent(
                new LoadResource(uri, resource),
                new CallListener<LoadResource>() {
                    @Override
                    protected void handleResponse(LoadResource event) {
                        if (event.hasErrors()) {
                            replyWithErrors(originalEvent, event.getErrors());
                        } else {
                            cleanupToAtom(originalEvent);
                        }
                    }
                });
        }

        private void format(final MainCall originalEvent, IWrfResource resource) {
            Uri uri = originalEvent.getUri();
            ExecutionContext context = null;
            fEventManager.fireEvent(new FormatCall(uri, context, resource));
        }

        @Override
        protected void handleRequest(final MainCall originalEvent) {
            try {
                IEventManager eventManager = originalEvent.getEventManager();
                Uri url = originalEvent.getUri();
                ExecutionContext context = originalEvent.getExecutionContext();
                IWrfResource resourceProvider = context.getResource(
                    url,
                    "download",
                    true);
                String actionName = originalEvent.getParameter("action");
                if (actionName != null) {

                }

                eventManager.fireEvent(
                    new LoadResource(),
                    new CallListener<LoadResource>() {
                    });
                download(originalEvent);
            } catch (Throwable t) {
                Set<Throwable> errors = new HashSet<Throwable>();
                errors.add(t);
                replyWithErrors(originalEvent, errors);
            }
        }

        public void replyWithErrors(
            final MainCall originalEvent,
            Set<Throwable> errors) {
            try {
                for (Throwable error : errors) {
                    originalEvent.onError(error);
                }
            } finally {
                originalEvent.setResponse(new MainCall.Response());
            }
        }
    }

    // TransformResourceCall:
    // * NormalizationCall
    // * FormatCall
    // * ImageTransformationCall
    // *
    public static abstract class TransformResourceCall
        extends
        ServiceCall<TransformResourceCall.Request, TransformResourceCall.Response> {

        public static class Request {

            private ExecutionContext fContext;

            private IWrfResource fSource;

            private IWrfResource fTarget;

            private Uri fUri;

            public Request(
                ExecutionContext context,
                Uri uri,
                IWrfResource source,
                IWrfResource target) {
                setContext(context);
                setUri(uri);
                setSource(source);
                setTarget(target);
            }

            public ExecutionContext getContext() {
                return fContext;
            }

            public IWrfResource getSource() {
                return fSource;
            }

            public IWrfResource getTarget() {
                return fTarget;
            }

            public Uri getUri() {
                return fUri;
            }

            public Request setContext(ExecutionContext context) {
                fContext = context;
                return this;
            }

            public Request setSource(IWrfResource resource) {
                fSource = resource;
                return this;
            }

            public Request setTarget(IWrfResource resource) {
                fTarget = resource;
                return this;
            }

            public Request setUri(Uri uri) {
                fUri = uri;
                return this;
            }
        }

        public static class Response {
        }

        protected static IWrfResource getResource(
            ExecutionContext context,
            String name,
            Uri uri) {
            IWrfRepository repository = context.getRepository();
            IWrfResourceProvider provider = repository.getResourceProvider(
                name,
                true);
            Path path = UriToPath.getPath(uri);
            IWrfResource resource = provider.getResource(path, true);
            return resource;
        }

        public TransformResourceCall(TransformResourceCall.Request request) {
            super(request);
        }

    }

    public static class ZipAction extends ActionCall {
        public ZipAction(Request request) {
            super(request);
        }
    }

    public ActionCallsTest(String name) {
        super(name);
    }

    public void test() throws Exception {
        // ActionRunner => CallAction event handler:
        // * CallDownload
        // * Get the name of the action
        // * Get the call event type corresponding to the specified action
        // * Instantiate the event with the initial (downloaded) resource
        // and with the target resource
        // * Call the event

        IEventManager eventManager = new EventManager();
        eventManager.addListener(
            ActionCall.class,
            new CallListener<ActionCall>() {
                @Override
                protected void handleRequest(ActionCall event) {
                    System.out.println("ActionCall: HasResponse="
                        + event.hasResponse());
                    if (!event.hasResponse()) {
                        event.setResponse(new ActionCall.Response());
                    }
                }
            });
        eventManager.addListener(
            ZipAction.class,
            new CallListener<ZipAction>() {
                @Override
                protected void handleRequest(ZipAction event) {
                    System.out.println("ZipAction: HasResponse="
                        + event.hasResponse());
                    event.setResponse(new ZipAction.Response());
                }
            });

        ZipAction.Request request = new ZipAction.Request();
        eventManager.fireEvent(
            new ZipAction(request),
            new CallListener<ZipAction>() {
                @Override
                protected void handleResponse(ZipAction event) {
                    System.out.println("Callback: HasResponse="
                        + event.hasResponse());
                }
            });

    }
}
