package jp.co.altec.openingactionsample;

/**
 * Created by tokue on 2015/11/29.
 */
public final class Point {
    String x, y, z;

    public  Point() {
        this.x = "-1";
        this.y = "-1";
        this.z = "-1";
    }

    public Point(final String x, final String y, final String z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
