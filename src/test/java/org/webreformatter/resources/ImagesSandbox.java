/**
 * 
 */
package org.webreformatter.resources;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.webreformatter.resources.adapters.images.ImageResizeStrategy;
import org.webreformatter.resources.adapters.images.ImageResizer;

/**
 * @author kotelnikov
 */
public class ImagesSandbox {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new ImagesSandbox().run();
    }

    /**
     * 
     */
    public ImagesSandbox() {
        // TODO Auto-generated constructor stub
    }

    public void run() throws IOException {
        Class<?> cls = getClass();
        String imageName = "/"
            + cls.getPackage().getName().replace('.', '/')
            + "/800px-Conseil_d'Etat_Paris_WA.jpg";
        File outputFile = new File("./test.png");
        InputStream input = cls.getResourceAsStream(imageName);
        try {
            OutputStream output = new FileOutputStream(outputFile);
            try {
                ImageResizer resizer = new ImageResizer();
                resizer.resizeImage(input, output, new ImageResizeStrategy() {
                    @Override
                    public Point getCanvaSize(
                        Point originalImageSize,
                        Point newImageSize) {
                        return new Point(202, 102);
                    }

                    @Override
                    public Point getImageSize(Point originalImageSize) {
                        return new Point(200, 100);
                        // return notMoreThan(originalImageSize, 100, 100);
                    }
                });
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
    }

}
