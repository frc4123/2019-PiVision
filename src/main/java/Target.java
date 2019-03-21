
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.opencv.core.Rect;

/**
 * A version of {@link org.opencv.core.Rect} designed for the 2017 FRC game's vision targets.
 * - Filters out all but largest two potential targets
 * - Determines Goal Type based on orientation
 * - Acts as a Rect that is the entire target
 */
public class Target extends Rect {

    /**
     * Possible Goal Types the robot might encounter during a match
     */
    public enum GoalType {
        HIGH_GOAL, GEAR, UNKNOWN
    }

    // Set up default goalType
    public GoalType goalType = GoalType.UNKNOWN;

    // Allow Target to be constructed with or without starting Rects
    public Target() {
    }

    public Target(ArrayList<Rect> rects) {
        set(rects);
    }

    /**
     * Stores x, y, width, and height of a rectangle that contains all
     * rectangles in the ArrayList
     *
     * @param rects
     *            A list of Rects that will be used to determine the GoalType
     *            and create the Target
     */
    public void set(ArrayList<Rect> rects) {

        // Determine and set GoalType
        goalType = determineGoalType(rects);

        // Set this to a rectangle that contains all visible remaining targets
        union(rects, this);
    }

    /**
     * Removes everything but the largest two rects
     * TODO Should we also take into account the targets' proximity to each other?
     */
    public static void filterRects(ArrayList<Rect> rects) {
        // Sort based on area
        Collections.sort(rects, new Comparator<Rect>() {
            public int compare(Rect r1, Rect r2) {
                if (r1.area() == r2.area())
                    return 0;
                return r1.area() > r2.area() ? -1 : 1;
            }
        });

        // Remove everything after and including index 2
        try {
            rects.subList(2, rects.size()).clear();
        } catch (IllegalArgumentException e){
            //If the list is < 2 in size, we can't sublist a list of 2
            e.printStackTrace();
        }
    }

    /**
     * Determines Target's GoalType
     * For now, simply does a basic comparison of each rect's orientation
     * In the future, we could look at their positional relationship to each other
     */
    private static GoalType determineGoalType(ArrayList<Rect> rects) {

        // Measure how "vertical" the target is. If the target is wider than it
        // is tall, verticalness will be negative.
        int verticalness = 0;

        // Add (height - width) to verticalness.
        // Do this for each rect to allow for error (one target partially
        // blocked, etc..)
        for (Rect rect : rects) {
            verticalness += rect.height - rect.width;
        }

        if (verticalness > 0) {
            // The gear goal has vertical stripes ( || )
            return GoalType.GEAR;
        } else if (verticalness < 0) {
            // The high goal has horizontal rings ( = )
            return GoalType.HIGH_GOAL;
        } else {
            // If verticalness is perfectly 0, return unknown.
            return GoalType.UNKNOWN;
        }
    }

    /**
     * Creates one Rect that contains all rects in @param rects
     */
    private static void union(ArrayList<Rect> rects, Rect dest) {
        // Initialize initial points
        int p1x = -1;
        int p1y = -1;
        int p2x = -1;
        int p2y = -1;

        // Iterate through all of our rectangles
        for (Rect rect : rects) {
            // If p1x is unassigned or this rectangle's x is lower than p1x, p1x=rect.x. Otherwise, p1x=p1x.
            p1x = (p1x == -1 || rect.x < p1x) ? rect.x : p1x;
            // Same for y
            p1y = (p1y == -1 || rect.y < p1y) ? rect.y : p1y;
            // Same for p2x; this time we're comparing the rightmost (or
            // leftmost, if width is negative) edge (x + width)
            p2x = (p2x == -1 || (rect.x + rect.width) > p2x) ? (rect.x + rect.width) : p2x;
            p2y = (p2y == -1 || (rect.y + rect.height) > p2y) ? (rect.y + rect.height) : p2y;
        }

        // Set these to our output Rect
        dest.x = p1x;
        dest.y = p1y;
        dest.width = p2x - p1x;
        dest.height = p2y - p1y;

    }

    public float getCenterX() {
        return this.x + (this.width / 2);
    }

    public float getCenterY() {
        return this.y + (this.height / 2);
    }

    public double getDegreesToTarget(){
        double radians = Math.atan((getCenterX() - (Constants.cameraWidth / 2)) / Constants.cameraFocalLength);
        double degrees = Math.toDegrees(radians);
        return degrees;
    }

    /**
     * Computes distance from target in inches
     * @return distance from target
     */
//    public double getDistanceInches() {
//        switch(goalType){
//            case GEAR:
//                return Constants.TARGET_GEAR_WIDTH * Constants.IMG_WIDTH / ( 2 * this.width * Math.tan( Constants.CAM_HORIZ_FOV / 2 ) );
//
//            case HIGH_GOAL:
//                return Constants.TARGET_HIGH_GOAL_WIDTH * Constants.IMG_WIDTH / ( 2 * this.width * Math.tan( Constants.CAM_HORIZ_FOV / 2 ) );
//
//            default:
//                return -1;
//        }
//    }

}