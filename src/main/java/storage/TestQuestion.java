package storage;

public class TestQuestion {
    private int page;
    private String points;
    private CanvasRectangle[] regions;

    public int getPage() {
        return page;
    }

    public String getPoints() {
        return points;
    }

    public CanvasRectangle[] getRegions() {
        return regions;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public void setRegions(CanvasRectangle[] regions) {
        this.regions = regions;
    }

    public static class CanvasRectangle {
        private double x;
        private double y;
        private int layer;
        private double width;
        private double height;
        private String color;
        private String outline_color;
        private String label;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public int getLayer() {
            return layer;
        }

        public void setLayer(int layer) {
            this.layer = layer;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getOutlineColor() {
            return outline_color;
        }

        public void setOutlineColor(String outline_color) {
            this.outline_color = outline_color;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
