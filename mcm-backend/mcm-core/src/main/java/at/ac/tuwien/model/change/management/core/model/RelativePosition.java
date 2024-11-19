package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;

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
}
