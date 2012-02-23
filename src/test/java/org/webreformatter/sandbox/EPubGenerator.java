/**
 * 
 */
package org.webreformatter.sandbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.webreformatter.resources.adapters.zip.ZipBuilder;

/**
 * @author kotelnikov
 */
public class EPubGenerator {

    public static void main(String[] args) throws IOException {
        File outFolder = new File("./tmp/test.epub");
        ZipBuilder builder = new ZipBuilder(new FileOutputStream(outFolder));
        try {
            builder.addStoredEntry("mimetype", "application/epub+zip");
            builder
                .addEntry(
                    "container.xml",
                    "<?xml version=\"1.0\"?>\n"
                        + "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n"
                        + "   <rootfiles>\n"
                        + "      <rootfile full-path=\"content.opf\"\n"
                        + "      media-type=\"application/oebps-package+xml\"/>\n"
                        + "   </rootfiles>\n"
                        + "</container>");
            builder
                .addEntry(
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
                        + "</package>");

            builder
                .addEntry(
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
                        + "      <text>Hello File</text>\n"
                        + "   </docTitle>\n"
                        + "\n"
                        + "   <navMap>\n"
                        + "      <navPoint id=\"navPoint-1\" playOrder=\"1\">\n"
                        + "         <navLabel>\n"
                        + "            <text>Hello World</text>\n"
                        + "         </navLabel>\n"
                        + "         <content src=\"hello.html\"/>\n"
                        + "      </navPoint>\n"
                        + "   </navMap>\n"
                        + "\n"
                        + "</ncx>");

            builder.addEntry("main.css", ""
                + "/* This is a simple CSS file */"
                + "body {\n"
                + "   background-color:       white;\n"
                + "   text-align:             center;\n"
                + "   margin:                 0em;\n"
                + "   padding:                0em;\n"
                + "}\n"
                + "");
            builder
                .addEntry(
                    "hello.html",
                    ""
                        + "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en'>\n"
                        + " <head>\n"
                        + "     <link rel='stylesheet' type='text/css' href='../style.css' />\n"
                        + " </head>\n"
                        + " <body>\n"
                        + "     <h1>Hello world!</h1>\n"
                        + "     <p>This is a simple paragraph</p>\n"
                        + " </body>\n"
                        + "</html>");

        } finally {
            builder.close();
        }
    }

    /**
     * 
     */
    public EPubGenerator() {
    }

}
