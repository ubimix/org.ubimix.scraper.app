package org.webreformatter.resources.adapters.images;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/**
 * @author kotelnikov
 */
public class ImageResizer {

    public static Point getImageSize(BufferedImage img) {
        return new Point(img.getWidth(null), img.getHeight(null));
    }

    public static BufferedImage readImage(InputStream input) throws IOException {
        try {
            ImageInputStream imageInput = ImageIO.createImageInputStream(input);
            BufferedImage image = ImageIO.read(imageInput);
            return image;
        } finally {
            input.close();
        }
    }

    public static void writeImage(
        BufferedImage image,
        OutputStream output,
        ImageResizeStrategy.Format format) throws IOException {
        try {
            ImageIO.write(image, format.toString(), output);
        } finally {
            output.close();
        }
    }

    public BufferedImage resizeImage(
        BufferedImage img,
        ImageResizeStrategy resizeStrategy) {
        Point imageSize = getImageSize(img);
        Point newImageSize = resizeStrategy.getImageSize(imageSize);
        BufferedImage newImage = resizeImage(
            img,
            newImageSize.x,
            newImageSize.y);
        Point canvasSize = resizeStrategy.getCanvaSize(imageSize, newImageSize);
        if (canvasSize.x < newImageSize.x || canvasSize.y < newImageSize.y) {
            throw new IllegalArgumentException(
                "Canvas can not be smaller than resized image.");
        }

        BufferedImage result = newImage;

        // Draw the border if canvas size is not the same as the size
        // of the new image
        if (newImageSize.x != canvasSize.x || newImageSize.y != canvasSize.y) {
            result = new BufferedImage(
                canvasSize.x,
                canvasSize.y,
                BufferedImage.TYPE_INT_RGB);
            Graphics2D g = result.createGraphics();
            try {
                Color canvasColor = resizeStrategy.getBackgroundColor();
                g.setColor(canvasColor);
                g.fillRect(0, 0, canvasSize.x, canvasSize.y);
                g.setComposite(AlphaComposite.SrcOver);
                int thumbX = (canvasSize.x - newImageSize.x) / 2;
                int thumbY = (canvasSize.y - newImageSize.y) / 2;
                g.drawImage(newImage, thumbX, thumbY, null);
            } finally {
                g.dispose();
            }
        }
        return result;
    }

    protected BufferedImage resizeImage(
        BufferedImage image,
        int newWidth,
        int newHeight) {
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        if (newWidth == imageWidth && newHeight == imageHeight) {
            return image;
        }
        BufferedImage thumbImage = new BufferedImage(
            newWidth,
            newHeight,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbImage.createGraphics();
        graphics2D.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, newWidth, newHeight, null);
        return thumbImage;
    }

    public void resizeImage(
        BufferedImage image,
        OutputStream output,
        ImageResizeStrategy resizeStrategy) throws IOException {
        try {
            BufferedImage thumb = resizeImage(image, resizeStrategy);
            ImageResizeStrategy.Format format = resizeStrategy
                .getNewImageFormat();
            writeImage(thumb, output, format);
        } finally {
            output.close();
        }
    }

    public void resizeImage(
        File file,
        File output,
        ImageResizeStrategy resizeStrategy) throws IOException {
        BufferedInputStream input = new BufferedInputStream(
            new FileInputStream(file));
        try {
            BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(output));
            try {
                resizeImage(input, out, resizeStrategy);
            } finally {
                out.close();
            }
        } finally {
            input.close();
        }
    }

    public void resizeImage(
        InputStream input,
        OutputStream output,
        ImageResizeStrategy resizeStrategy) throws IOException {
        try {
            try {
                BufferedImage image = readImage(input);
                resizeImage(image, output, resizeStrategy);
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
    }

}