package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.List;

@Getter
@Setter
@Slf4j
public class Relation extends BaseAttributes {
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
}