package at.ac.tuwien.model.change.management.core.model.dsl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PropertyDSL {
    @XmlElement(required = true)
    private String key;
    @XmlElement(required = true)
    private String value;
}