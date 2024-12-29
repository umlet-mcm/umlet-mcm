package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public class RelativePosition {
    private int absX;
    private int absY;
    private int offsetX;
    private int offsetY;

    public RelativePosition(int offsetX, int offsetY, int refX, int refY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.absX = refX + offsetX;
        this.absY = refY + offsetY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(absX, absY, offsetX, offsetY);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RelativePosition other)) {
            return false;
        }

        return other.getAbsX() == this.absX &&
                other.absY == this.absY &&
                other.offsetX == this.offsetX &&
                other.offsetY == this.offsetY;
    }
}
