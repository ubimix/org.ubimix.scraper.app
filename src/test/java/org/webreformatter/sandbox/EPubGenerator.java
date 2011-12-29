/**
 * 
 */
package org.webreformatter.sandbox;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author kotelnikov
 */
public class EPubGenerator {

    private static void addEntry(
        String name,
        InputStream in,
        ZipOutputStream out) throws IOException {
        try {
            out.putNextEntry(new ZipEntry(name));
            int len;
            byte[] buf = new byte[1000 * 10];
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
        } finally {
            in.close();
        }
    }

    private static void addEntry(
        String name,
        String content,
        ZipOutputStream out) throws IOException {
        InputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));
        addEntry(name, input, out);
    }

    public static void main(String[] args) throws IOException {
        File inFolder = new File("./src");
        File outFolder = new File("./out.zip");
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
            new FileOutputStream(outFolder)));
        try {
            out.setMethod(ZipOutputStream.STORED);
            addEntry("mimetype", "application/epub+zip", out);

            out.setMethod(ZipOutputStream.DEFLATED);

            addEntry(
                "container.xml",
                "<?xml version=\"1.0\"?>\n"
                    + "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n"
                    + "   <rootfiles>\n"
                    + "      <rootfile full-path=\"content.opf\"\n"
                    + "      media-type=\"application/oebps-package+xml\"/>\n"
                    + "   </rootfiles>\n"
                    + "</container>",
                out);

            addEntry(
                "content.opf",
                ""
                    + "<?xml version=\"1.0\"?>\n"
                    + "\n"
                    + "<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"dcidid\" \n"
                    + "   version=\"2.0\">\n"
                    + "\n"
                    + "   <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"
                    + "      xmlns:dcterms=\"http://purl.org/dc/terms/\"\n"
                    + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    + "      xmlns:opf=\"http://www.idpf.org/2007/opf\">\n"
                    + "      <dc:title>Epub Format Construction Guide</dc:title>\n"
                    + "      <dc:language xsi:type=\"dcterms:RFC3066\">en</dc:language>\n"
                    + "      <dc:identifier id=\"dcidid\" opf:scheme=\"URI\">\n"
                    + "         http://www.hxa7241.org/articles/content/epup-guide_hxa7241_2007_2.epub\n"
                    + "         </dc:identifier>\n"
                    + "      <dc:subject>Non-fiction, technical article, tutorial, Epub, IDPF, ebook\n"
                    + "         </dc:subject>\n"
                    + "      <dc:description>A guide for making Epub ebooks/publications, sufficient\n"
                    + "         for most purposes. It requires understanding of XHTML, CSS, XML.\n"
                    + "         </dc:description>\n"
                    + "      <dc:relation>http://www.hxa.name/</dc:relation>\n"
                    + "      <dc:creator>Harrison Ainsworth / HXA7241</dc:creator>\n"
                    + "      <dc:publisher>Harrison Ainsworth / HXA7241</dc:publisher>\n"
                    + "      <dc:date xsi:type=\"dcterms:W3CDTF\">2007-12-28</dc:date>\n"
                    + "      <dc:date xsi:type=\"dcterms:W3CDTF\">2010-08-27</dc:date>\n"
                    + "      <dc:rights>Creative Commons BY-SA 3.0 License.</dc:rights>\n"
                    + "   </metadata>\n"
                    + "\n"
                    + "   <manifest>\n"
                    + "      <item id=\"ncx\"      href=\"toc.ncx\"                 \n"
                    + "         media-type=\"application/x-dtbncx+xml\" />\n"
                    // Resources:
                    // - main.css
                    // - hello.html
                    + "      <item id=\"css\"      href=\"main.css\"           \n"
                    + "         media-type=\"text/css\" />\n"
                    + "      <item id=\"hello\"    href=\"hello.html\"    \n"
                    + "         media-type=\"application/xhtml+xml\" />\n"
                    //
                    + "   </manifest>\n"
                    + "\n"
                    + "   <spine toc=\"ncx\">\n"
                    + "      <itemref idref=\"hello\" />\n"
                    + "   </spine>\n"
                    + "\n"
                    + "   <guide>\n"
                    + "      <reference type=\"text\"       title=\"Hello\"              \n"
                    + "         href=\"hello.html\" />\n"
                    + "   </guide>\n"
                    + "\n"
                    + "</package>",
                out);

            addEntry(
                "toc.ncx",
                "<?xml version=\"1.0\"?>\n"
                    + "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \n"
                    + "   \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n"
                    + "\n"
                    + "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n"
                    + "\n"
                    + "   <head>\n"
                    + "      <meta name=\"dtb:uid\" content=\"http://www.hxa7241.org/articles/content/epup-guide_hxa7241_2007_2.epub\"/>\n"
                    + "      <meta name=\"dtb:depth\" content=\"2\"/>\n"
                    + "      <meta name=\"dtb:totalPageCount\" content=\"0\"/>\n"
                    + "      <meta name=\"dtb:maxPageNumber\" content=\"0\"/>\n"
                    + "   </head>\n"
                    + "\n"
                    + "   <docTitle>\n"
                    + "      <text>Epub Format Construction Guide</text>\n"
                    + "   </docTitle>\n"
                    + "\n"
                    + "   <navMap>\n"
                    + "      <navPoint id=\"navPoint-1\" playOrder=\"1\">\n"
                    + "         <navLabel>\n"
                    + "            <text>Hello World</text>\n"
                    + "         </navLabel>\n"
                    + "         <content src=\"hello.html\"/>\n"
                    + "      </navPoint>\n"
                    + "      <navPoint id=\"navPoint-2\" playOrder=\"2\">\n"
                    + "         <navLabel>\n"
                    + "            <text>Table of Contents</text>\n"
                    + "         </navLabel>\n"
                    + "         <content src=\"EpubGuide-contents.html\"/>\n"
                    + "      </navPoint>\n"
                    + "      <navPoint id=\"navPoint-3\" playOrder=\"3\">\n"
                    + "         <navLabel>\n"
                    + "            <text>Introduction</text>\n"
                    + "         </navLabel>\n"
                    + "         <content src=\"EpubGuide-intro.html\"/>\n"
                    + "      </navPoint>\n"
                    + "      <navPoint id=\"navPoint-4\" playOrder=\"4\">\n"
                    + "         <navLabel>\n"
                    + "            <text>1: XHTML Documents</text>\n"
                    + "         </navLabel>\n"
                    + "         <content src=\"EpubGuide-1.html\"/>\n"
                    + "      </navPoint>\n"
                    + "      <navPoint id=\"navPoint-5\" playOrder=\"5\">\n"
                    + "         <navLabel>\n"
                    + "            <text>2: Package And Container Files</text>\n"
                    + "         </navLabel>\n"
                    + "         <content src=\"EpubGuide-2.html\"/>\n"
                    + "         <navPoint id=\"navPoint-6\" playOrder=\"6\">\n"
                    + "            <navLabel>\n"
                    + "               <text>mimetype</text>\n"
                    + "            </navLabel>\n"
                    + "            <content src=\"EpubGuide-2.html#mimetype\"/>\n"
                    + "         </navPoint>\n"
                    + "         <navPoint id=\"navPoint-7\" playOrder=\"7\">\n"
                    + "            <navLabel>\n"
                    + "               <text>container.xml</text>\n"
                    + "            </navLabel>\n"
                    + "            <content src=\"EpubGuide-2.html#containerxml\"/>\n"
                    + "         </navPoint>\n"
                    + "         <navPoint id=\"navPoint-8\" playOrder=\"8\">\n"
                    + "            <navLabel>\n"
                    + "               <text>content.opf</text>\n"
                    + "            </navLabel>\n"
                    + "            <content src=\"EpubGuide-2.html#contentopf\"/>\n"
                    + "         </navPoint>\n"
                    + "         <navPoint id=\"navPoint-9\" playOrder=\"9\">\n"
                    + "            <navLabel>\n"
                    + "               <text>toc.ncx</text>\n"
                    + "            </navLabel>\n"
                    + "            <content src=\"EpubGuide-2.html#tocncx\"/>\n"
                    + "         </navPoint>\n"
                    + "      </navPoint>\n"
                    + "      <navPoint id=\"navPoint-10\" playOrder=\"10\">\n"
                    + "         <navLabel>\n"
                    + "            <text>3: ADE stylesheet</text>\n"
                    + "         </navLabel>\n"
                    + "         <content src=\"EpubGuide-3.html\"/>\n"
                    + "      </navPoint>\n"
                    + "      <navPoint id=\"navPoint-11\" playOrder=\"11\">\n"
                    + "         <navLabel>\n"
                    + "            <text>4: Container Structure</text>\n"
                    + "         </navLabel>\n"
                    + "         <content src=\"EpubGuide-4.html\"/>\n"
                    + "      </navPoint>\n"
                    + "      <navPoint id=\"navPoint-12\" playOrder=\"12\">\n"
                    + "         <navLabel>\n"
                    + "            <text>Specifications List</text>\n"
                    + "         </navLabel>\n"
                    + "         <content src=\"EpubGuide-specs.html\"/>\n"
                    + "      </navPoint>\n"
                    + "   </navMap>\n"
                    + "\n"
                    + "</ncx>",
                out);

        } finally {
            out.close();
        }
    }

    /**
     * 
     */
    public EPubGenerator() {
    }

}
