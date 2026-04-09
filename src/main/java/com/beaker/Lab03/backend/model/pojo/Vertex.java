package com.beaker.Lab03.backend.model.pojo;

import lombok.Data;

import java.util.Objects;

/**
 * 棋盘顶点，表示一个具体的图块坐标及其图案类型。
 */
@Data
public class Vertex {

    private int row;
    private int col;
    private int type;

    public Vertex() {
    }

    public Vertex(int row, int col, int type) {
        this.row = row;
        this.col = col;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vertex)) {
            return false;
        }
        Vertex vertex = (Vertex) o;
        return row == vertex.row
                && col == vertex.col
                && type == vertex.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col, type);
    }

    @Override
    public String toString() {
        return "Vertex{"
                + "row=" + row
                + ", col=" + col
                + ", type=" + type
                + '}';
    }
}
