package at.ac.tuwien.model.change.management.core.model.utils;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Point;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RelationUtils {
    public static final Pattern SPLIT_LINE_TYPE_PATTERN = Pattern.compile("(.*<+|.*]|.*\\))?(\\.\\.|\\.|-)(>+.*|\\[.*|\\(.*)?");

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
     * @param tolerance Consider points within +-x pixels connected
     * @return True if the relation and the node is considered connected, false otherwise
     */
    public static boolean isConnected(Node element, Point endPoint, int tolerance) {
        Point tl = new Point(
                element.getUmletPosition().getX() -tolerance,
                element.getUmletPosition().getY()-tolerance);
        Point br = new Point(
                element.getUmletPosition().getX() + element.getUmletPosition().getWidth()+tolerance,
                element.getUmletPosition().getY() + element.getUmletPosition().getHeight()+tolerance);

//        tl=offset(tl);
//        br=offset(br);
//        endPoint=offset(endPoint);

        boolean b1 = endPoint.x() >= tl.x();
        boolean b2 = endPoint.x() <= br.x();
        boolean b3 = endPoint.y() <= br.y();
        boolean b4 = endPoint.y() >= tl.y();

        return b1 && b2 && b3 && b4;
    }

//    private static Point offset(Point p){
//        int zoomlevel = 7;
//        int ori = 10;
//
//        int remX = p.x() % zoomlevel;
//        int remY = p.y() % zoomlevel;
//        Point res = new Point(
//                remX >= zoomlevel / 2 ? p.x() - remX + zoomlevel : p.y() - remX,
//                remY >= zoomlevel / 2 ? p.y() - remY + zoomlevel : p.y() - remY
//        );
//
//        res = new Point(
//                p.x()* zoomlevel / ori,
//                p.y()* zoomlevel / ori
//        );
//
//        return res;
//
//    }

    /**
     * Create real relations in the model. After the model is parsed relations are stored
     * as nodes. These nodes must be turned into {@link Relation}s. A relation is stored in
     * the source node, the target is stored in the relation itself. For bidirectional
     * relations (e.g. <<->>, -) two relation are created, one for each direction.
     *
     * @param original The model that contains relations as nodes.
     * @return The new model that has the actual relations.
     */
    public static Model processRelations(Model original) {
        // copy attributes
        Model res = new Model();
        res.setId(original.getId());
        res.setTags(original.getTags());
        res.setOriginalText(original.getOriginalText());
        res.setTitle(original.getTitle());
        res.setDescription(original.getDescription());
        res.setMcmAttributes(original.getMcmAttributes());
        res.setZoomLevel(original.getZoomLevel());
        res.setNodes(new HashSet<>());

        // extract relations to a separate list, add nodes to the new model otherwise
        ArrayList<Node> relations = new ArrayList<>();
        for (Node n : original.getNodes()) {
            if (n.getElementType().equals("Relation")) {
                relations.add(n);
            } else {
                res.getNodes().add(n);
            }
        }

        // process relations
        for (Node relationNode : relations) {
            Relation newRelation = Relation.fromNode(relationNode);
            if (newRelation == null) {
                continue;
            }

            // check if the relation is bidirectional
            // a relation is bidirectional if it can bit split into three parts: end cap, line, end cap
            String lineType = relationNode.getUmletAttributes().get(AttributeKeys.LINE_TYPE);
            boolean bidirectional = false;
            String[] splitLineType = splitLineType(lineType);

            if (splitLineType == null) {
                // the line type could not be split, the relation is invalid so ignore it
                log.error("Failed to process element '" + relationNode.getId() + "' into a relation.");
                continue;
            }

            if (splitLineType.length == 1) {
                // the relation is a simple line e.g. "-", "..", "."
                bidirectional = true;
            }

            if (splitLineType.length == 3) {
                bidirectional = true;
                // replace the line type with the forward relation
                newRelation.setType(splitLineType[1] + splitLineType[2]);
            }

            boolean forward = true;
            if (!bidirectional) {
                Boolean fw = isRelationForward(splitLineType);
                if (fw == null) {
                    continue;
                }

                if (!fw) {
                    forward = false;
                }
            }

            // find the end nodes
            Node relSource = null;
            for (Node n : res.getNodes()) {
                log.debug(n.toString());
                // find source
                if (RelationUtils.isConnected(n, newRelation.getStartPoint(),3)) {
                    if (forward) {
                        n.getRelations().add(newRelation);
                    }

                    relSource = n;
                    break;
                }
            }

            if (relSource == null) {
                log.warn("Relation with id: '" + newRelation.getId() + "' has no source.");
            }

            Node relTarget = null;
            for (Node n : res.getNodes()) {
                // find target
                if (RelationUtils.isConnected(n, newRelation.getEndPoint(), 3)) {
                    relTarget = n;
                    break;
                }
            }

            if (relTarget == null) {
                log.warn("Relation with id: '" + newRelation.getId() + "' has no target.");
            }

            // set target and source
            // flip it if the relation is backwards
            if (forward) {
                if (relSource != null) {
                    relSource.getRelations().add(newRelation);
                }
                newRelation.setTarget(relTarget);
            } else {
                if (relTarget != null) {
                    relTarget.getRelations().add(newRelation);
                }
                newRelation.setTarget(relSource);
            }

            // if the relation was bidirectional and the target exists create a new relation pointing
            // the opposite direction
            if (bidirectional && newRelation.getTarget() != null) {
                Relation reverse = Relation.fromNode(relationNode);
                if (reverse == null) {
                    log.error("Could not generate backward relation from '" + relationNode.getId() + "'");
                    continue;
                }

                if (splitLineType.length == 1) {
                    reverse.setType(splitLineType[0]);
                } else {
                    reverse.setType(splitLineType[0] + splitLineType[1]);
                }

                reverse.setTarget(relSource);
                newRelation.getTarget().getRelations().add(reverse);
            }
        }

        return res;
    }

    public static ElementUxf mergeRelationElements(ElementUxf e1, ElementUxf e2) {
        if (!e1.getElementType().equals("Relation") || !e2.getElementType().equals("Relation")) {
            throw new IllegalArgumentException("Elements must be of type Relation");
        }

        if (!e1.getUmletPosition().equals(e2.getUmletPosition())) {
            throw new IllegalArgumentException("Elements must have the same umlet position");
        }

        if (e1.getAttributes().getUmletAttributes() == null ||
                e2.getAttributes().getUmletAttributes() == null
        ) {
            return e1;
        }

        String lt1 = e1.getAttributes().getUmletAttributes().get(AttributeKeys.LINE_TYPE);
        String lt2 = e2.getAttributes().getUmletAttributes().get(AttributeKeys.LINE_TYPE);
        if (lt1 == null || lt2 == null) {
            throw new IllegalArgumentException("Line type must not be null");
        }

        String[] split1 = splitLineType(lt1);
        String[] split2 = splitLineType(lt2);
        if (split1 == null || split2 == null) {
            return e1;
        }

        Boolean fw1 = isRelationForward(split1);
        Boolean fw2 = isRelationForward(split2);
        if (fw1 == null || fw2 == null) {
            return e1;
        }

        if (fw1 == fw2) {
            return e1;
        }

        if (fw1) {
            e1.getAttributes().getMcmAttributes().put(
                    AttributeKeys.LINE_TYPE, split2[0] + split1[1] + split1[2]
            );
        } else {
            e1.getAttributes().getMcmAttributes().put(
                    AttributeKeys.LINE_TYPE, split1[0] + split2[1] + split2[2]
            );
        }

        return e1;
    }

    @Nullable
    public static String[] splitLineType(String lineType) {
        Matcher matcher = SPLIT_LINE_TYPE_PATTERN.matcher(lineType);

        if (!matcher.matches()) {
            log.error("Failed to split line type: " + lineType);
            return null;
        }

        // skip group 0, as that contains the whole input

        // simple line
        if (matcher.group(1) == null && matcher.group(3) == null) {
            return new String[]{matcher.group(2)};
        }

        // line with special end type on the right
        if (matcher.group(1) == null) {
            return new String[]{matcher.group(2), matcher.group(3)};
        }

        // line with special end type on the left
        if (matcher.group(3) == null) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }

        // both ends are special
        return new String[]{matcher.group(1), matcher.group(2), matcher.group(3)};
    }

    @Nullable
    public static String joinLineTypes(String lt1, String lt2) {
        String[] split1 = splitLineType(lt1);
        String[] split2 = splitLineType(lt2);

        if (split1 == null || split2 == null) {
            return null;
        }

        if (split1.length != split2.length) {
            log.error("Failed to join relations: '" + lt1 + "', '" + lt2 + "'");
            return null;
        }

        // error if the relations were bidirectional
        if (split1.length > 2) {
            log.error("Failed to join relations: '" + lt1 + "', '" + lt2 + "'. Relations must be unidirectional.");
            return null;
        }

        // the line type was a simple line e.g. "-", ".", ".."
        if (split1.length == 1) {
            return split1[0];
        }

        // we assume the line was a unidirectional relation with something
        // at one end e.g. ->>, |<-, [text]<<-

        // figure out which way the relation is pointing
        StringBuilder sb = new StringBuilder();
        Boolean forward = isRelationForward(split1);
        if (forward == null) {
            return null; // shouldn't happen because we know the relation is valid and directed
        }
        if (forward) {
            // the first element is the line type, the second is the directed end
            // the relation is pointing to the right
            sb.append(split2[0]);
            sb.append(split1[0]);
            sb.append(split1[1]);
        } else {
            sb.append(split1[0]);
            sb.append(split2[0]);
            sb.append(split2[1]);
        }
        return sb.toString();
    }

    /**
     * Is the relation pointing from left to right?
     */
    @Nullable
    public static Boolean isRelationForward(String[] splitTineType) {
        // only process valid and directed relations
        if (splitTineType == null || splitTineType.length != 2) {
            return null;
        }

        return !splitTineType[0].contains("]") && // make sure the chars are not coming from the custom text
                (splitTineType[0].contains("-") || splitTineType[0].contains("."));
    }
}
