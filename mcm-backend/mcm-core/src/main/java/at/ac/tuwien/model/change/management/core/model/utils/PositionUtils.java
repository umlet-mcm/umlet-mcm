package at.ac.tuwien.model.change.management.core.model.utils;

import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class PositionUtils {
    public static final int DEFAULT_ZOOM_LEVEL = 10;
    // the amount of padding between models if they have to be repositioned due to overlap
    public static final int MODEL_PADDING = 50;

    /**
     * Normalize coordinates at a given zoom level to the {@link PositionUtils#DEFAULT_ZOOM_LEVEL}.
     * Normally used on coordinates coming from uxf files.
     */
    @SuppressWarnings("unused") // this function is called in the position mapper but the ide can't detect that
    public static int normalizeCoordinate(int val, int zoomLevel) {
        return Math.round(val / (zoomLevel / (float) DEFAULT_ZOOM_LEVEL));
    }

    /**
     * Transform coordinates from the {@link PositionUtils#DEFAULT_ZOOM_LEVEL} to a target zoom level.
     * Normally used on coordinates before exporting them to uxf.
     */
    @SuppressWarnings("unused") // this function is called in the position mapper but the ide can't detect that
    public static int denormalizeCoordinate(int val, int zoomLevel) {
        return Math.round(val / (DEFAULT_ZOOM_LEVEL / (float) zoomLevel));
    }

    /**
     * Update the position of Models so that they don't overlap. Non overlapping models are left in place,
     * overlapping models are repositioned.
     *
     * @param models The models to be aligned
     */
    public static void alignModels(List<ModelUxf> models) {
        ArrayList<Pair<ModelUxf, UmletPositionUxf>> modelsWithBB = new ArrayList<>();
        for (ModelUxf model : models) {
            modelsWithBB.add(
                    new MutablePair<>(model,
                            getBoundingBox(List.of(model))) {
                    });
        }

        // split the models into two lists
        ArrayList<Pair<ModelUxf, UmletPositionUxf>> overlappingModels = new ArrayList<>();
        ArrayList<ModelUxf> nonOverlappingModels = new ArrayList<>();

        for (int i = 0; i < modelsWithBB.size(); i++) {
            boolean isOverlapping = false;

            for (int j = 0; j < modelsWithBB.size(); j++) {
                UmletPositionUxf m1bb = modelsWithBB.get(i).getRight();
                UmletPositionUxf m2bb = modelsWithBB.get(j).getRight();
                if (i != j && overlap(m1bb, m2bb)) {
                    isOverlapping = true;
                    break;
                }
            }

            if (isOverlapping) {
                overlappingModels.add(modelsWithBB.get(i));
            } else {
                nonOverlappingModels.add(modelsWithBB.get(i).getLeft());
            }
        }

        if (overlappingModels.isEmpty()) {
            return; // no overlap, we don't have to move models
        }

        // elements will be laid out going from left to right
        if (!nonOverlappingModels.isEmpty()) {
            // if we have models that don't overlap at all we leave them in place
            // starting point for aligning the other models:
            // x: the x coordinate of the right side of the non-overlapping models' bounding box + padding
            // y: the horizontal center line of the non-overlapping models' bounding box
            UmletPositionUxf baseBB = getBoundingBox(nonOverlappingModels);
            int startX = baseBB.getX() + baseBB.getWidth() + MODEL_PADDING;
            final int startY = baseBB.getY() + baseBB.getHeight() / 2;
            layOutModelsLeftToRight(overlappingModels, startX, startY);
        } else {
            // all elements overlap, take the first one and set the starting point based on that
            if (overlappingModels.size() < 2) {
                return; // the aligning only makes sense if we have at least 2 elements
            }

            // first element will be left in place, that will be the starting point
            var startingModel = overlappingModels.removeFirst();
            UmletPositionUxf baseBB = startingModel.getRight();
            // same idea as in the other branch
            int startX = baseBB.getX() + baseBB.getWidth() + MODEL_PADDING;
            final int startY = baseBB.getY() + baseBB.getHeight() / 2;
            layOutModelsLeftToRight(overlappingModels, startX, startY);
        }
    }

    /**
     * Get the combined bounding box of multiple models
     */
    public static UmletPositionUxf getBoundingBox(List<ModelUxf> models) {
        int minTopLeftX = Integer.MAX_VALUE;
        int minTopLeftY = Integer.MAX_VALUE;
        int maxBottomRightX = Integer.MIN_VALUE;
        int maxBottomRightY = Integer.MIN_VALUE;

        for (var model : models) {
            for (var element : model.getElements()) {
                minTopLeftX = Math.min(element.getUmletPosition().getX(), minTopLeftX);
                minTopLeftY = Math.min(element.getUmletPosition().getY(), minTopLeftY);
                maxBottomRightX = Math.max(element.getUmletPosition().getX() + element.getUmletPosition().getWidth(), maxBottomRightX);
                maxBottomRightY = Math.max(element.getUmletPosition().getY() + element.getUmletPosition().getHeight(), maxBottomRightY);
            }
        }

        return new UmletPositionUxf(
                minTopLeftX,
                minTopLeftY,
                maxBottomRightX - minTopLeftX,
                maxBottomRightY - minTopLeftY
        );
    }

    /**
     * Lay out models from left to right so they don't overlap, starting at given x,
     * horizontally centered on a given y
     *
     * @param modelsWithBoundingBoxes List of pairs of models with their bounding boxes
     * @param startX                  The x coordinate where the laid out models should start
     * @param y                       The y coordinate on which the elements will be vertically centered on
     */
    public static void layOutModelsLeftToRight(List<Pair<ModelUxf, UmletPositionUxf>> modelsWithBoundingBoxes, int startX, final int y) {
        int nextX = startX;
        for (var mbb : modelsWithBoundingBoxes) {
            ModelUxf m = mbb.getLeft();
            UmletPositionUxf bb = mbb.getRight();

            // move model into position
            int dx = nextX - bb.getX();
            int dy = y - (bb.getY() + bb.getHeight() / 2); // vertically center it on y
            translateModel(m, dx, dy);

            // move bounding box, next position will be calculated based on this
            translatePosition(bb, dx, dy);

            // update next position
            nextX = bb.getX() + bb.getWidth() + MODEL_PADDING;
        }
    }

    /**
     * Shift the UmletPosition of the elements in the model
     *
     * @param model the model to be moved
     * @param dx    x distance of the shift
     * @param dy    y distance of the shift
     */
    public static void translateModel(ModelUxf model, int dx, int dy) {
        for (var element : model.getElements()) {
            translatePosition(element.getUmletPosition(), dx, dy);
        }
    }

    /**
     * Shift a position
     *
     * @param position the position to be moved
     * @param dx       x distance of the shift
     * @param dy       y distance of the shift
     */
    public static void translatePosition(UmletPositionUxf position, int dx, int dy) {
        position.setX(position.getX() + dx);
        position.setY(position.getY() + dy);
    }

    /**
     * Check if two rectangles (bounding boxes) overlap
     */
    public static boolean overlap(UmletPositionUxf bb1, UmletPositionUxf bb2) {
        if (bb1.getX() + bb1.getWidth() <= bb2.getX() || bb2.getX() + bb2.getWidth() <= bb1.getX()) {
            return false;
        }

        if (bb1.getY() + bb1.getHeight() <= bb2.getY() || bb2.getY() + bb2.getHeight() <= bb1.getY()) {
            return false;
        }

        return true;
    }
}
