package org.webreformatter.scrapper.normalizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.webreformatter.scrapper.context.RuntimeContext;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

public class CompositeDocumentNormalizer implements IDocumentNormalizer {

    private List<IDocumentNormalizer> fList = new ArrayList<IDocumentNormalizer>();

    private int fPriority;

    public void addNormalizer(IDocumentNormalizer normalizer) {
        synchronized (fList) {
            fList.add(normalizer);
        }
        updatePriority();
    }

    public AtomFeed getNormalizedContent(
        RuntimeContext context,
        XmlWrapper doc) throws XmlException, IOException {
        List<IDocumentNormalizer> list = getNormalizerList();
        AtomFeed result = null;
        for (IDocumentNormalizer normalizer : list) {
            result = normalizer.getNormalizedContent(context, doc);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * @return a list of all managed normalizers ordered by their priorities
     */
    private List<IDocumentNormalizer> getNormalizerList() {
        List<IDocumentNormalizer> result = new ArrayList<IDocumentNormalizer>();
        synchronized (fList) {
            result.addAll(fList);
        }
        Collections.sort(result, new Comparator<IDocumentNormalizer>() {
            public int compare(IDocumentNormalizer o1, IDocumentNormalizer o2) {
                return o2.getPriority() - o1.getPriority();
            }
        });
        return result;
    }

    public int getPriority() {
        return fPriority;
    }

    public void removeNormalizer(IDocumentNormalizer normalizer) {
        synchronized (fList) {
            fList.remove(normalizer);
        }
        updatePriority();
    }

    private void updatePriority() {
        synchronized (fList) {
            int priority = 0;
            for (IDocumentNormalizer normalizer : fList) {
                int normalizerPriority = normalizer.getPriority();
                if (normalizerPriority > priority) {
                    priority = normalizerPriority;
                }
            }
            fPriority = priority;
        }
    }

}