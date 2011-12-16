/**
 * 
 */
package org.webreformatter.server.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * @author kotelnikov
 */
public class XmlTagExtractor {

    public static class HtmlBlockElementsAcceptor extends HtmlNamedNodeAcceptor {

        public HtmlBlockElementsAcceptor() {
            super();
            addNames("h1", "h2", "h3", "h4", "h5", "h6");
            addNames("hr");
            addNames("table", "tr", "th", "td");
            addNames("ol", "ul", "dd", "dt", "li");
        }
    }

    public static abstract class HtmlElementAcceptor
        implements
        IElementAcceptor {

        public static final String _NS_XHTML = "http://www.w3.org/1999/xhtml";

        protected String getHTMLName(Element tag) {
            String ns = tag.getNamespaceURI();
            String name = null;
            if (_NS_XHTML.equals(ns)) {
                name = tag.getLocalName();
            } else if (ns == null) {
                name = tag.getNodeName();
            }
            if (name != null) {
                name = name.toLowerCase();
            }
            return name;
        }

    }

    public static class HtmlNamedNodeAcceptor extends HtmlElementAcceptor {

        private Set<String> fNames = new HashSet<String>();

        public HtmlNamedNodeAcceptor(String... names) {
            addNames(names);
        }

        public boolean accept(Element element) {
            String name = getHTMLName(element);
            return fNames.contains(name);
        }

        public void addNames(String... names) {
            fNames.addAll(Arrays.asList(names));
        }

        public void removeNames(String... names) {
            fNames.removeAll(Arrays.asList(names));
        }
    }

    public interface IElementAcceptor {

        public static IElementAcceptor NO_ACCEPTOR = new IElementAcceptor() {
            public boolean accept(Element element) {
                return false;
            }
        };

        public static IElementAcceptor YES_ACCEPTOR = new IElementAcceptor() {
            public boolean accept(Element element) {
                return true;
            }
        };

        boolean accept(Element element);
    }

    public static class SimpleElementAcceptor implements IElementAcceptor {

        private Set<Element> fTags = new HashSet<Element>();

        public SimpleElementAcceptor(Element... tags) {
            addTags(tags);
        }

        public boolean accept(Element element) {
            return fTags.contains(element);
        }

        public void addTags(Element... tags) {
            fTags.addAll(Arrays.asList(tags));
        }

        public void removeTags(Element... tags) {
            fTags.removeAll(Arrays.asList(tags));
        }

    }

    public List<Element> loadElements(
        Element element,
        IElementAcceptor elementAcceptor) {
        return loadElements(
            element,
            elementAcceptor,
            IElementAcceptor.YES_ACCEPTOR,
            IElementAcceptor.NO_ACCEPTOR);
    }

    public List<Element> loadElements(
        Element element,
        IElementAcceptor elementAcceptor,
        IElementAcceptor end) {
        return loadElements(
            element,
            elementAcceptor,
            IElementAcceptor.YES_ACCEPTOR,
            end);
    }

    /**
     * Loads elements corresponding to the specified search criteria.
     * 
     * @param element the root element to visit;
     * @param elementAcceptor this object is used to detect searched elements
     * @param begin this acceptor is used to detect the beginning of the XML
     *        range from which required elements should be found
     * @param end the end acceptor
     * @return a list of elements corresponding to the specified search criteria
     */
    public List<Element> loadElements(
        Element element,
        final IElementAcceptor elementAcceptor,
        final IElementAcceptor begin,
        final IElementAcceptor end) {
        final List<Element> result = new ArrayList<Element>();
        XmlAcceptor.accept(element, new XmlAcceptor.XmlVisitor() {

            private boolean fStarted;

            private boolean fStopped;

            @Override
            public void visit(Element node) {
                if (fStopped) {
                    return;
                }
                boolean checkStop = true;
                if (!fStarted) {
                    fStarted = begin.accept(node);
                    checkStop = false;
                }
                if (fStarted) {
                    if (elementAcceptor.accept(node)) {
                        result.add(node);
                    } else {
                        fStopped = checkStop && end.accept(node);
                        if (!fStopped) {
                            super.visit(node);
                        }
                    }
                } else {
                    super.visit(node);
                }
            }
        });
        return result;
    }

}
