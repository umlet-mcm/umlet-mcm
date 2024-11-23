package at.ac.tuwien.model.change.management.core.model.utils;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Point;

public class RelationUtils {
    /**
     * Check if the endpoint of a relation is connected to a given element.
     * <p>
     * Currently, we are checking whether the bounding box of the element contains
     * the point. Umlet works differently, there the exact shape of each element is known
     * and the relation and the element is considered connected if the endpoint lies on
     * the outline. The only data we can work with is what's inside the uxf file, which is
     * only the bounding box. This may result in inconsistencies between Umlet and the stored
     * model.
     *
     * @param element  The node to be checked
     * @param endPoint One endpoint of a relation
     * @return True if the relation and the node is considered connected, false otherwise
     */
    public static boolean isConnected(Node element, Point endPoint) {
        Point tl = new Point(
                element.getUmletPosition().getX(),
                element.getUmletPosition().getY());
        Point br = new Point(
                element.getUmletPosition().getX() + element.getUmletPosition().getWidth(),
                element.getUmletPosition().getY() + element.getUmletPosition().getHeight());

        boolean b1 = endPoint.x() >= tl.x();
        boolean b2 =endPoint.x() <= br.x();
        boolean b3 = endPoint.y() <= br.y();
        boolean b4 =endPoint.y() >= tl.y();

        return
                b1 && b2 &&
                         b3&& b4;
    }
}
