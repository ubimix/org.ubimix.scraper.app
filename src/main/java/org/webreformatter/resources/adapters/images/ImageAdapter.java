/**
 * 
 */
package org.webreformatter.resources.adapters.images;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IContentAdapter.ContentChangeEvent;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.WrfResourceAdapter;
import org.webreformatter.resources.adapters.images.ImageResizeStrategy.Format;

/**
 * This adapter is used to represent a resource as a string.
 * 
 * @author kotelnikov
 */
public class ImageAdapter extends WrfResourceAdapter {

    private static ImageResizer fResizer = new ImageResizer();

    private BufferedImage fImage;

    private IWrfResource fResource;

    public ImageAdapter(IWrfResource instance) {
        fResource = instance;
    }

    public BufferedImage getImage() throws IOException {
        synchronized (getMutex()) {
            if (fImage == null) {
                IContentAdapter contentAdapter = fResource
                    .getAdapter(IContentAdapter.class);
                if (contentAdapter.exists()) {
                    InputStream input = contentAdapter.getContentInput();
                    try {
                        fImage = ImageResizer.readImage(input);
                    } finally {
                        input.close();
                    }
                }
            }
            return fImage;
        }
    }

    public Point getImageSize() throws IOException {
        BufferedImage image = getImage();
        Point result = image != null ? ImageResizer.getImageSize(image) : null;
        return result;
    }

    protected Object getMutex() {
        return this;
    }

    @Override
    public void handleEvent(Object event) {
        synchronized (getMutex()) {
            if (event instanceof ContentChangeEvent) {
                fImage = null;
            }
        }
    }

    public void resizeImage(
        ImageAdapter target,
        ImageResizeStrategy resizeStrategy) throws IOException {
        BufferedImage image = getImage();
        BufferedImage resizedImage = fResizer
            .resizeImage(image, resizeStrategy);
        Format format = resizeStrategy.getNewImageFormat();
        target.setImage(resizedImage, format);
    }

    public void resizeImage(
        IWrfResource target,
        ImageResizeStrategy resizeStrategy) throws IOException {
        ImageAdapter targetImageAdapter = target.getAdapter(ImageAdapter.class);
        resizeImage(targetImageAdapter, resizeStrategy);
    }

    public void setImage(BufferedImage resizedImage) throws IOException {
        setImage(resizedImage, ImageResizeStrategy.Format.JPG);
    }

    public void setImage(
        BufferedImage resizedImage,
        ImageResizeStrategy.Format format) throws IOException {
        IContentAdapter contentAdapter = fResource
            .getAdapter(IContentAdapter.class);
        OutputStream output = contentAdapter.getContentOutput();
        try {
            ImageResizer.writeImage(resizedImage, output, format);
        } finally {
            output.close();
        }
        synchronized (getMutex()) {
            fImage = resizedImage;
        }
    }

}
