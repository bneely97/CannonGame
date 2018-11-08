package com.deitel.cannongame;

import android.graphics.Point;

public class Line {

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point start; //start member variable for starting point
    public Point end; //end member variable for ending point

    //default constructor initializes Points to the origin (0,0)
    public Line()
    {
        start = new Point(0,0);
        end = new Point(0,0);
    }

    public Line(Point start, Point end) {
        setStart(start);
        this.end=end;
    }
}
