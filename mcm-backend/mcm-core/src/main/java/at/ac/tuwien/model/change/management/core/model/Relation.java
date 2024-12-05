package at.ac.tuwien.model.change.management.core.model;

import lombok.AllArgsConstructor;
import at.ac.tuwien.model.change.management.core.model.attributes.ElementAttributes;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Relation extends ElementAttributes {
    private String type; // line type e.g. "<<-"
    @Nullable
    private Node target;
    private UMLetPosition umletPosition; // bounding box (including the handle circles) top left corner

    // position of the start of the line relative to the umletPosition
    // this is usually (10, 10) which is the default size of handle circles at the ends of the relation
    private RelativePosition relativeStartPoint;
    private List<RelativePosition> relativeMidPoints; // midpoints relative to umletPosition
    private RelativePosition relativeEndpoint; // position of the end of the line relative to the umletPosition

    private Point startPoint;
    private Point endPoint;

    // also updates the startPoint
    public void setRelativeStartPoint(RelativePosition point) {
        this.relativeStartPoint = point;
        this.startPoint = new Point(
                point.getAbsX(),
                point.getAbsY()
        );
    }

    // also updates the endPoint
    public void setRelativeEndPoint(RelativePosition point) {
        this.relativeEndpoint = point;
        this.endPoint = new Point(
                point.getAbsX(),
                point.getAbsY()
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
        if (!source.getElementType().equals("Relation")) {
            log.error("Error attempting to parse element type '" + source.getElementType() + "' to relation");
            return null;
        }
        Relation r = new Relation();

        String lt = source.getUmletAttributes().get(AttributeKeys.LINE_TYPE);
        lt = lt == null ? "-" : lt; // default to "-" if the line type was missing
        r.setType(lt);
        r.setUmletPosition(source.getUmletPosition());
        r.setUmletAttributes(source.getUmletAttributes());
        r.setOriginalText(source.getOriginalText());
        r.setId(source.getId());
        r.setTags(source.getTags());
        r.setMcmModel(source.getMcmModel());
        r.setMcmAttributes(source.getMcmAttributes());
        r.setTitle(source.getTitle());
        r.setDescription(source.getDescription());

        // The points of the relation are stored in the additional_attributes
        // Each valid relation contains at least 2 points so 4 values
        int pointCount = source.getGeneratedAttributes().size();
        if (pointCount < 4 || pointCount % 2 != 0) {
            log.error("Could not parse relation attributes to points: " + source.getGeneratedAttributes().toString());
            return null;
        }

        // The first 2 values are the start point of the line relative to the umletPosition
        r.setRelativeStartPoint(new RelativePosition(
                source.getGeneratedAttributes().get(0),
                source.getGeneratedAttributes().get(1),
                r.getUmletPosition().x,
                r.getUmletPosition().y
        ));

        // The last 2 values are the end point of the line relative to the umletPosition
        r.setRelativeEndPoint(new RelativePosition(
                source.getGeneratedAttributes().get(pointCount - 2),
                source.getGeneratedAttributes().get(pointCount - 1),
                r.getUmletPosition().x,
                r.getUmletPosition().y
        ));

        // Parse any additional midpoints if the line isn't straight
        if (pointCount > 4) {
            r.setRelativeMidPoints(new ArrayList<>());
            // process i and i+1 at once so upper bound -1
            for (int i = 2; i < pointCount - 2 - 1; i += 2) {
                r.getRelativeMidPoints().add(new RelativePosition(
                        source.getGeneratedAttributes().get(i),
                        source.getGeneratedAttributes().get(i + 1),
                        r.getUmletPosition().x,
                        r.getUmletPosition().y
                ));
            }
        }

        return r;
    }

    public ArrayList<Integer> getGeneratedAttributes() {
        ArrayList<Integer> genAttrs = new ArrayList<>();
        genAttrs.add(relativeStartPoint.getOffsetX());
        genAttrs.add(relativeStartPoint.getOffsetY());
        if (relativeMidPoints != null) {
            for (RelativePosition rp : relativeMidPoints) {
                genAttrs.add(rp.getOffsetX());
                genAttrs.add(rp.getOffsetY());
            }
        }
        genAttrs.add(relativeEndpoint.getOffsetX());
        genAttrs.add(relativeEndpoint.getOffsetY());

        return genAttrs;
    }
}