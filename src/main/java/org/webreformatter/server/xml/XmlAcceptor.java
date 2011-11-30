/**
 * 
 */
package org.webreformatter.server.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * @author kotelnikov
 */
public class XmlAcceptor {

    /**
     * @author kotelnikov
     */
    public static abstract class AbstractXmlSerializer extends XmlVisitor {

        private final static Logger log = Logger
            .getLogger(AbstractXmlSerializer.class.getName());

        protected abstract Writer getWriter();

        private RuntimeException handleError(String msg, Throwable e) {
            log.log(Level.FINE, msg, e);
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                return (RuntimeException) e;
            }
            return new RuntimeException(msg, e);
        }

        @Override
        public void visit(Attr node) {
            String text = node.getValue();
            write(text);
        }

        @Override
        public void visit(Element node) {
            boolean omitxmldeclaration = true;
            boolean indent = false;
            try {
                Transformer transformer = TransformerFactory
                    .newInstance()
                    .newTransformer();
                if (omitxmldeclaration) {
                    transformer
                        .setOutputProperty("omit-xml-declaration", "yes");
                }
                transformer.setOutputProperty("encoding", "UTF-8");
                if (indent) {
                    transformer.setOutputProperty("indent", "yes");
                    try {
                        transformer.setOutputProperty(
                            "{http://xml.apache.org/xslt}indent-amount",
                            "4");
                    } catch (Exception e) {
                        //
                    }
                }
                Source input = new DOMSource(node);
                Writer writer = getWriter();
                Result output = new StreamResult(writer);
                transformer.transform(input, output);
                writer.flush();
            } catch (Throwable e) {
                throw handleError("Can not serialize an XML element.", e);
            }
        }

        @Override
        public void visit(Text node) {
            String text = node.getData();
            write(text);
        }

        protected void write(String text) {
            try {
                getWriter().write(text);
            } catch (IOException e) {
                throw handleError("Can not write text.", e);
            }
        }
    }

    /**
     * @author kotelnikov
     */
    public interface IXmlVisitor {

        void visit(Attr node);

        void visit(CDATASection node);

        void visit(Comment node);

        void visit(Document node);

        void visit(Element node);

        void visit(Entity node);

        void visit(ProcessingInstruction node);

        void visit(Text node);
    }

    public static class XmlSerializer extends AbstractXmlSerializer {

        private StringWriter fWriter = new StringWriter();

        @Override
        protected Writer getWriter() {
            return fWriter;
        }

        @Override
        public String toString() {
            return fWriter.toString();
        }
    }

    /**
     * @author kotelnikov
     */
    public static class XmlVisitor implements IXmlVisitor {

        protected void doVisit(Node node) {
        }

        public void visit(Attr node) {
            doVisit(node);
        }

        public void visit(CDATASection node) {
            doVisit(node);
        }

        public void visit(Comment node) {
            doVisit(node);
        }

        public void visit(Document node) {
            Element e = node.getDocumentElement();
            if (e != null) {
                visit(e);
            }
        }

        public void visit(Element node) {
            Node child = node.getFirstChild();
            while (child != null) {
                Node next = child.getNextSibling();
                accept(child, this);
                child = next;
            }
        }

        public void visit(Entity node) {
            doVisit(node);
        }

        public void visit(Node node) {
            doVisit(node);
        }

        public void visit(ProcessingInstruction node) {
            doVisit(node);
        }

        public void visit(Text node) {
            doVisit(node);
        }

    }

    public static void accept(Node node, IXmlVisitor visitor) {
        if (node instanceof Document) {
            visitor.visit((Document) node);
        } else if (node instanceof Element) {
            visitor.visit((Element) node);
        } else if (node instanceof CDATASection) {
            visitor.visit((CDATASection) node);
        } else if (node instanceof Text) {
            visitor.visit((Text) node);
        } else if (node instanceof Comment) {
            visitor.visit((Comment) node);
        } else if (node instanceof Entity) {
            visitor.visit((Entity) node);
        } else if (node instanceof ProcessingInstruction) {
            visitor.visit((ProcessingInstruction) node);
        } else if (node instanceof Attr) {
            visitor.visit((Attr) node);
        }
    }

}
