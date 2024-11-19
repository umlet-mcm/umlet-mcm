package at.ac.tuwien.model.change.management.core.model;

import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.attributes.ElementAttributes;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Slf4j
public class Relation {
    private String type; // line type e.g. "<<-"
    @Nullable
    private Node target;
    private UMLetPosition umletPosition; // bounding box (including the handle circles) top left corner
    private ElementAttributes attributes;

    // position of the start of the line relative to the umletPosition
    // this is usually (10, 10) which is the default size of handle circles at the ends of the relation
    private RelativePosition relativeStartPoint;
    private List<RelativePosition> relativeMidPoints; // midpoints relative to umletPosition
    private RelativePosition relativeEndpoint; // position of the end of the line relative to the umletPosition

    public Point getStartPoint() {
        return new Point(
                relativeStartPoint.getAbsX(),
                relativeStartPoint.getAbsY()
        );
    }

    public Point getEndPoint() {
        return new Point(
                relativeEndpoint.getAbsX(),
                relativeEndpoint.getAbsY()
        );
    }

    /**
     * Relations are represented as elements in the uxf, which are parsed into nodes first.
     * This function can be used to transform those nodes to a relation.
     *
     * @param source The node to be converted
     * @return The new relation or null if the conversion failed
     */
    @Nullable
    public static Relation fromNode(Node source) {
        if(!source.getElementType().equals("Relation")){
            log.error("Error attempting to parse element type '"+source.getElementType()+"' to relation");
            return null;
        }
        Relation r = new Relation();

        String lt = source.getAttributes().getUmletAttributes().get(AttributeKeys.LINE_TYPE);
        r.setType(lt);
        r.setUmletPosition(source.getUmletPosition());

        // The points of the relation are stored in the additional_attributes
        // Each valid relation contains at least 2 points so 4 values
        int pointCount = source.getGeneratedAttributes().size();
        if (pointCount < 4 || pointCount % 2 != 0) {
            log.error("Could not parse relation attributes to points: " + source.getGeneratedAttributes().toString());
            return null;
        }

        // Values must be integers
        for (Object o : source.getGeneratedAttributes()) {
            if (!(o instanceof Integer)) {
                log.error("Could not parse relation attribute to int: " + o);
                return null;
            }
        }

        // The first 2 values are the start point of the line relative to the umletPosition
        r.setRelativeStartPoint(new RelativePosition(
                (int) source.getGeneratedAttributes().get(0),
                (int) source.getGeneratedAttributes().get(1),
                r.getUmletPosition().x,
                r.getUmletPosition().y
        ));

        // The last 2 values are the end point of the line relative to the umletPosition
        r.setRelativeEndpoint(new RelativePosition(
                (int) source.getGeneratedAttributes().get(pointCount - 2),
                (int) source.getGeneratedAttributes().get(pointCount - 1),
                r.getUmletPosition().x,
                r.getUmletPosition().y
        ));

        // Parse any additional midpoints if the line isn't straight
        if (pointCount > 4) {
            r.setRelativeMidPoints(new ArrayList<>());
            // process i and i+1 at once so upper bound -1
            for (int i = 2; i < pointCount - 2 - 1; i++) {
                r.getRelativeMidPoints().add(new RelativePosition(
                        (int) source.getGeneratedAttributes().get(i),
                        (int) source.getGeneratedAttributes().get(i + 1),
                        r.getUmletPosition().x,
                        r.getUmletPosition().y
                ));
            }
        }

        return r;
    }
}