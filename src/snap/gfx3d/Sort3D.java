/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx3d;
import java.util.Collections;
import java.util.List;

/**
 * This class supports various 3D sorting functionality.
 */
public class Sort3D {

    // Constants for comparison/ordering of Path3Ds
    public static final int ORDER_BACK_TO_FRONT = -1;
    public static final int ORDER_FRONT_TO_BACK = 1;
    public static final int ORDER_SAME = 0;
    public static final int ORDER_INEDETERMINATE = 2;

    /**
     * Resorts a Path3D list from back to front using Depth Sort Algorithm.
     */
    public static void sortPaths(List<Path3D> thePaths)
    {
        // Get list of paths and sort from front to back with simple Z min sort
        Collections.sort(thePaths, (p0, p1) -> p0.compareZMin(p1));

        // Sort again front to back with exhaustive sort satisfying Depth Sort Algorithm
        for (int i=thePaths.size()-1; i>0; i--) {

            // Get loop path
            Path3D path1 = thePaths.get(i);
            int i2 = i;

            // Iterate over remaining paths
            for (int j=0; j<i; j++) {

                // Get loop path (if same path, just skip)
                Path3D path2 = thePaths.get(j);
                if (path2 == path1)
                    continue;

                // If no X/Y/Z overlap, just continue
                if (path1.getMinZ() >= path2.getMaxZ())
                    continue;
                if (path1.getMaxX() <= path2.getMinX() || path1.getMinX() >= path2.getMaxX())
                    continue;
                if (path1.getMaxY() <= path2.getMinY() || path1.getMinY() >= path2.getMaxY())
                    continue;

                // Test path planes - if on same plane or in correct order, they don't overlap
                int comp1 = path1.comparePlane(path2);
                if (comp1 == ORDER_SAME || comp1 == ORDER_BACK_TO_FRONT)
                    continue;
                int comp2 = path2.comparePlane(path1);
                if (comp2 == ORDER_FRONT_TO_BACK)
                    continue;

                // If 2d paths don't intersect, just continue
                if (!path1.getPath().intersects(path2.getPath(),0))
                    continue;

                // If all five tests fail, try next path up from path1
                if (i2 == 0) {  // Not sure why this can happen
                    System.err.println("Path3D.sort: Sort fail.");
                    i = 0;
                }
                else {
                    path1 = thePaths.get(--i2);
                    j = -1;
                }
            }

            // Move poly
            if (i2 != i) {
                thePaths.remove(i2);
                thePaths.add(i, path1);
            }
        }

        // Reverse child list so it is back to front (so front most shape will be drawn last)
        Collections.reverse(thePaths);
    }

    /**
     * Resorts a Path3D list from back to front using Depth Sort Algorithm.
     */
    public static void sortPaths2(List<Path3D> thePaths)
    {
        // Get list of paths and sort from front to back with simple Z min sort
        Collections.sort(thePaths, (p0,p1) -> p0.compareZMin(p1));

        // Sort again front to back with exhaustive sort satisfying Depth Sort Algorithm
        for (int i=thePaths.size()-1; i>0; i--) {

            // Get loop path
            Path3D path2 = thePaths.get(i);
            int i2 = i;

            // Iterate over remaining paths
            for (int j=0; j<i; j++) {

                // Get loop path (if same path, just skip)
                Path3D path1 = thePaths.get(j);
                if (path1 == path2)
                    continue;

                // If no X/Y/Z overlap, just continue
                if (path2.getMinZ() >= path1.getMaxZ())
                    continue;
                if (path2.getMaxX() <= path1.getMinX() || path2.getMinX() >= path1.getMaxX())
                    continue;
                if (path2.getMaxY() <= path1.getMinY() || path2.getMinY() >= path1.getMaxY())
                    continue;

                // Test path planes - if on same plane or in correct order, they don't overlap
                int comp1 = path2.comparePlane(path1);
                if (comp1 == ORDER_SAME || comp1 == ORDER_FRONT_TO_BACK)
                    continue;
                int comp2 = path1.comparePlane(path2);
                if (comp2 == ORDER_BACK_TO_FRONT)
                    continue;

                // If 2d paths don't intersect, just continue
                if (!path2.getPath().intersects(path1.getPath(),0))
                    continue;

                // If all five tests fail, try next path up from path1
                if (i2 == 0) {  // Not sure why this can happen
                    System.err.println("Path3D.sort: Sort fail.");
                    i = 0;
                }
                else {
                    path2 = thePaths.get(--i2);
                    j = -1;
                }
            }

            // Move poly
            if (i2 != i) {
                thePaths.remove(i2);
                thePaths.add(i, path2);
            }
        }
    }
}
