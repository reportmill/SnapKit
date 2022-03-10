package snap.gfx3d;

/**
 * Utility methods for VertexArray.
 */
public class VertexArrayUtils {

    /**
     * Adds triangles to VertexArray for a stroked line between given points.
     */
    public static void addLineStrokePoints(VertexArray vertexArray, Point3D p0, Point3D p1, Vector3D lineNormal, double lineWidth)
    {
        // Get vector across line and perpendicular to line
        Vector3D acrossVector = new Vector3D(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z).normalize();
        Vector3D downVector = lineNormal.getCrossProduct(acrossVector).normalize();

        // Scale by lineWidth
        acrossVector.scale(lineWidth);
        downVector.scale(lineWidth);

        // Get offset so line moves 'above' path triangles
        Vector3D offsetNormal = lineNormal.clone();
        offsetNormal.scale(.5);

        // Upper left point
        Point3D p0a = p0.clone();
        p0a.x += downVector.x - acrossVector.x + offsetNormal.x;
        p0a.y += downVector.y - acrossVector.y + offsetNormal.y;
        p0a.z += downVector.z - acrossVector.z + offsetNormal.z;

        // Lower left point
        Point3D p0b = p0.clone();
        p0b.x += -downVector.x - acrossVector.x + offsetNormal.x;
        p0b.y += -downVector.y - acrossVector.y + offsetNormal.y;
        p0b.z += -downVector.z - acrossVector.z + offsetNormal.z;

        // Upper right point
        Point3D p1a = p1.clone();
        p1a.x += downVector.x + acrossVector.x + offsetNormal.x;
        p1a.y += downVector.y + acrossVector.y + offsetNormal.y;
        p1a.z += downVector.z + acrossVector.z + offsetNormal.z;

        // Lower right point
        Point3D p1b = p1.clone();
        p1b.x += -downVector.x + acrossVector.x + offsetNormal.x;
        p1b.y += -downVector.y + acrossVector.y + offsetNormal.y;
        p1b.z += -downVector.z + acrossVector.z + offsetNormal.z;

        // Get triangle A. If not aligned with normal, swap points
        Point3D[] triangleA = { p0a, p0b, p1b };
        Vector3D pointsNormal = Vector3D.getNormalForPoints(new Vector3D(0, 0, 0), triangleA);
        if (!pointsNormal.equals(lineNormal)) {
            triangleA[1] = p1b; triangleA[2] = p0b; }

        // Get triangle A. If not aligned with normal, swap points
        Point3D[] triangleB = { p1a, p0a, p1b };
        Vector3D.getNormalForPoints(pointsNormal, triangleB);
        if (!pointsNormal.equals(lineNormal)) {
            triangleB[1] = p1b; triangleB[2] = p0a; }

        // Add triangle points
        for (Point3D p3d : triangleA)
            vertexArray.addPoint(p3d.x, p3d.y, p3d.z);
        for (Point3D p3d : triangleB)
            vertexArray.addPoint(p3d.x, p3d.y, p3d.z);
    }
}
