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

public class TestFile {
    private int id;
    private byte[] data;
    private String name;
    private String type;
    private int number_of_pages;

    public TestFile(int id, byte[] data, String name, String type, int number_of_pages) {
        this.id = id;
        this.data = data;
        this.name = name;
        this.type = type;
        this.number_of_pages = number_of_pages;
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getNumberOfPages() {
        return number_of_pages;
    }
}
