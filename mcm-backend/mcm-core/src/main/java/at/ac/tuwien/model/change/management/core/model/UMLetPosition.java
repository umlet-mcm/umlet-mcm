package at.ac.tuwien.model.change.management.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UMLetPosition {
    int x;
    int y;
    int width;
    int height;

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }

        UMLetPosition pos = (UMLetPosition) obj;
        return pos.x == this.x && pos.y == this.y && pos.height == this.height && pos.width == this.width;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x,y,height,width);
    }
}
