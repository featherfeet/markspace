/*
 * Copyright 2020 Oliver Trevor and Suchin Ravi.
 *
 * This file is part of MarkSpace.
 *
 * MarkSpace is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MarkSpace is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MarkSpace.  If not, see <https://www.gnu.org/licenses/>.
 */

package storage;

public class TestQuestion {
    private int test_question_id;
    private int page;
    private String points;
    private CanvasRectangle[] regions;
    private boolean extra_credit;

    public int getTestQuestionId() {
        return test_question_id;
    }

    public void setTestQuestionId(int test_question_id) {
        this.test_question_id = test_question_id;
    }

    public int getPage() {
        return page;
    }

    public String getPoints() {
        return points;
    }

    public boolean getExtraCredit() {
        return extra_credit;
    }

    public void setExtraCredit(boolean extra_credit) {
        this.extra_credit = extra_credit;
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

    public boolean isIdentificationQuestion() {
        return this.regions[0].getLabel().equals("NAME");
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
