package util;

/**
 * A class representing a geometric rectangle (in an integer coordinate system where the origin is at the top-left corner of the screen).
 */
public class Rectangle {
    /**
     * X-coordinate of a corner of the rectangle.
     */
    private int x;
    /**
     * Y-coordinate of a corner of the rectangle.
     */
    private int y;
    /**
     * Width of the rectangle (can be negative, positive, or zero).
     */
    private int width;
    /**
     * Height of the rectangle (can be negative, positive, or zero).
     */
    private int height;

    /**
     * Create a new Rectangle with the specified location and dimensions.
     * @param x X-coordinate of a corner of the rectangle.
     * @param y Y-coordinate of a corner of the rectangle.
     * @param width Width of the rectangle (can be negative, positive, or zero).
     * @param height Height of the rectangle (can be negative, positive, or zero).
     */
    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Adjust the rectangle so that it's within the boundary area given (a rectangular area with corners at (0, 0) and (bounds_width, bounds_height).
     * @param bounds_width The width of the boundary area.
     * @param bounds_height The height of the boundary area.
     */
    public void cropToBounds(int bounds_width, int bounds_height) {
        if (x < 0) {
            x = 0;
        }
        else if (x >= bounds_width) {
            x = bounds_width - 1;
        }
        if (y < 0) {
            y = 0;
        }
        else if (y >= bounds_height) {
            y = bounds_height - 1;
        }
        if (x + width < 0) {
            width = -x;
        }
        else if (x + width >= bounds_width) {
            width = bounds_width - 1 - x;
        }
        if (y + height < 0) {
            height = -y;
        }
        else if (y + height >= bounds_height) {
            height = bounds_height - 1 - y;
        }
    }

    /**
     * Convert the rectangle so that (x, y) becomes the TOP-LEFT corner of the rectangle. This ensures that width and height will be positive, which is required for some image-related applications.
     */
    public void convertToTopLeftRectangle() {
        if (width < 0) {
            x += width;
            width = -width;
        }
        if (height < 0) {
            y += height;
            height = -height;
        }
    }

    /**
     * Get the x-coordinate of a corner of the rectangle.
     * @return An integer.
     */
    public int getX() {
        return x;
    }

    /**
     * Get the y-coordinate of a corner of the rectangle.
     * @return An integer.
     */
    public int getY() {
        return y;
    }

    /**
     * Get the width of the rectangle.
     * @return An integer.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the rectangle.
     * @return An integer.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Pretty-print the rectangle.
     * @return A human-readable string describing the rectangle.
     */
    public String toString() {
        return "Rectangle at coordinates (" + x + ", " + y + ") with width " + width + " and height " + height + ".";
    }
}
