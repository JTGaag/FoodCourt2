package com.aj.map;

import java.util.ArrayList;

/**
 * Created by Joost on 20/05/2015.
 */
public class CollisionMap {
    ArrayList<LineSegment> lineSegments = new ArrayList<LineSegment>();
    boolean finalized = false;

    public CollisionMap(ArrayList<LineSegment> lineSegments) {
        this.lineSegments = lineSegments;
    }

    public CollisionMap() {
    }

    public void addLineSegment(LineSegment lineSegment){
        lineSegments.add(lineSegment);
    }

    public ArrayList<LineSegment> getLineSegments() {
        return lineSegments;
    }
}
