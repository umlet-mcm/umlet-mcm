package at.ac.tuwien.model.change.management.core.model.dsl;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class RelativePositionDSL {

    @XmlElement(name = "abs_x", required = true)
    private int absX;

    @XmlElement(name = "abs_y", required = true)
    private int absY;

    @XmlElement(name = "offset_x", required = true)
    private int offsetX;

    @XmlElement(name = "offset_y", required = true)
    private int offsetY;

}

