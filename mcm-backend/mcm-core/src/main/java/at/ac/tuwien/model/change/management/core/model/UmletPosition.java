package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UmletPosition {
    int x;
    int y;
    int width;
    int height;

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }

        UmletPosition pos = (UmletPosition) obj;
        return pos.x == this.x && pos.y == this.y && pos.height == this.height && pos.width == this.width;
    }
}
