/**
 * 
 */
package org.webreformatter.resources;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author kotelnikov
 */
public class ZipExample {

    public static void main(String a[]) throws IOException {
        File inFolder = new File("./src");
        File outFolder = new File("./out.zip");
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
            new FileOutputStream(outFolder)));
        try {
            zip(out, inFolder);
        } finally {
            out.close();
        }
    }

    private static void zip(ZipOutputStream out, File root) throws IOException {
        zip(out, root, root);
    }

    private static void zip(ZipOutputStream out, File root, File file)
        throws IOException {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list != null) {
                for (File child : list) {
                    zip(out, root, child);
                }
            }
        } else if (file.isFile()) {
            String path = file.toString();
            String rootPath = root.toString();
            path = path.substring(rootPath.length());
            out.putNextEntry(new ZipEntry(path));
            InputStream in = new FileInputStream(file);
            int len;
            byte[] buf = new byte[1000 * 10];
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
        }
    }
}
