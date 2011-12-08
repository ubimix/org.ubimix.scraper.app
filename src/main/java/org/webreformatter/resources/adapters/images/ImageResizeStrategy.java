package org.webreformatter.resources.adapters.images;

import java.awt.Color;
import java.awt.Point;

/**
 * This strategy is used to re-calculate image size
 * 
 * @author kotelnikov
 */
public abstract class ImageResizeStrategy {

    /**
     * Possible image formats.
     */
    public static enum Format {
        JPG, PNG;

        @Override
        public String toString() {
            String name = super.toString();
            return name.toLowerCase();
        }

    }

    /**
     * Returns a new size of the image to fit to the specified limits. This
     * method changes dimensions proportionally to the original image.
     * 
     * @param imageSize the size of the original image
     * @param maxWidth max width of the new image
     * @param maxHeight max height of the image
     * @param onlyBiggerImages if this parameter is <code>true</code> then this
     *        method returns new dimensions only for images bigger than the
     *        specified limits; dimensions of smaller images are returned as is
     * @return a new image dimensions
     */
    private static Point newSize(
        Point imageSize,
        int maxWidth,
        int maxHeight,
        boolean onlyBiggerImages) {
        int width = imageSize.x;
        int height = imageSize.y;
        if (onlyBiggerImages && width <= maxWidth && height <= maxHeight) {
            return imageSize;
        }
        double thumbRatio = (double) maxWidth / (double) maxHeight;
        double imageRatio = (double) width / (double) height;
        if (thumbRatio < imageRatio) {
            maxHeight = (int) (maxWidth / imageRatio);
        } else {
            maxWidth = (int) (maxHeight * imageRatio);
        }
        return new Point(maxWidth, maxHeight);
    }

    /**
     * Returns a new size of the image only if the current image is bigger than
     * the specified limits; otherwise the original image dimensions are
     * returned. This method changes dimensions proportionally to the original
     * image.
     * 
     * @param imageSize the size of the original image
     * @param maxWidth max width of the new image
     * @param maxHeight max height of the image
     * @return a new image dimensions
     */
    protected static Point notMoreThan(
        Point imageSize,
        int maxWidth,
        int maxHeight) {
        return newSize(imageSize, maxWidth, maxHeight, true);
    }

    /**
     * Resize the given image to fit to the specified limits. If the image is
     * smaller than the specified dimensions then it will be scaled up.
     * 
     * @param imageSize the size of the original image
     * @param maxWidth max width of the new image
     * @param maxHeight max height of the image
     * @return a new image dimensions
     */
    protected static Point resizeTo(Point imageSize, int maxWidth, int maxHeight) {
        return newSize(imageSize, maxWidth, maxHeight, false);
    }

    private ImageResizeStrategy.Format fFormat;

    public ImageResizeStrategy() {
        this(Format.PNG);
    }

    public ImageResizeStrategy(ImageResizeStrategy.Format format) {
        fFormat = format;
    }

    /**
     * Returns the canvas color; this method is used only if the size of the
     * canvas (returned by the {@link #getCanvaSize(Point, Point)} is bigger
     * than the new image size
     * 
     * @return the color of the background to fill the canvas.
     */
    public Color getBackgroundColor() {
        return Color.WHITE;
    }

    /**
     * Returns the size of the canvas for the image.
     * 
     * @param originalImageSize the original size of the image
     * @param newImageSize a new image size returned by the
     *        {@link #getImageSize(Point)} method
     * @return a new canvas size
     */
    public Point getCanvaSize(Point originalImageSize, Point newImageSize) {
        return newImageSize;
    }

    /**
     * Calculates and returns a new size of the image.
     * 
     * @param originalImageSize the original size of the image
     * @return a new image size
     */
    public abstract Point getImageSize(Point originalImageSize);

    /**
     * Returns the name of the new image format. Possible values are
     * 
     * @return the name of the new image format
     */
    public ImageResizeStrategy.Format getNewImageFormat() {
        return fFormat;
    }
}