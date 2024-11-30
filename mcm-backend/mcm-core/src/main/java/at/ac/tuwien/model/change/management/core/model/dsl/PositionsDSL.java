package at.ac.tuwien.model.change.management.core.model.dsl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PositionsDSL {
    @XmlElement(name = "relative_start_point")
    private RelativePositionDSL relativeStartPoint;

    @XmlElementWrapper(name = "relative_mid_points")
    @XmlElement(name = "relative_mid_point")
    private List<RelativePositionDSL> relativeMidPoints;

    @XmlElement(name = "relative_end_point")
    private RelativePositionDSL relativeEndPoint;
}