package at.ac.tuwien.model.change.management.core.model.intermediary;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "coordinates")
@XmlAccessorType(XmlAccessType.FIELD)
public class UmletPositionUxf {
    @XmlElement
    int x;
    @XmlElement
    int y;
    @XmlElement(name = "w")
    int width;
    @XmlElement(name = "h")
    int height;

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }

        UmletPositionUxf pos = (UmletPositionUxf) obj;
        return pos.x == this.x && pos.y == this.y && pos.height == this.height && pos.width == this.width;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, height, width);
    }

}
