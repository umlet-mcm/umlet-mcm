package at.ac.tuwien.model.change.management.core.model.dsl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class CoordinatesDSL {
    @XmlElement(required = true)
    private int x;
    @XmlElement(required = true)
    private int y;
    @XmlElement(required = true)
    private int w;
    @XmlElement(required = true)
    private int h;
}