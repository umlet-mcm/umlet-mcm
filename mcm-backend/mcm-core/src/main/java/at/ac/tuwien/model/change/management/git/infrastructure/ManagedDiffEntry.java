package at.ac.tuwien.model.change.management.git.infrastructure;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;


@RequiredArgsConstructor
@Getter
public abstract class ManagedDiffEntry {

    @NonNull
    private final String diff;

    @NonNull
    private final ManagedDiffType diffType;

    @NonNull
    private final AffectedObjectType affectedObjectType;


    public abstract Optional<ManagedRepositoryObject> getOldObject();

    public abstract Optional<ManagedRepositoryObject> getNewObject();

    public abstract ManagedRepositoryObject getAffectedObject();

    public static class Add extends ManagedDiffEntry {
        private final ManagedRepositoryObject newObject;

        public Add(@NonNull ManagedRepositoryObject newObject, @NonNull String diff) {
            super(diff, ManagedDiffType.ADD, AffectedObjectType.NEW);
            this.newObject = newObject;
        }

        @Override
        public Optional<ManagedRepositoryObject> getOldObject() {
            return Optional.empty();
        }

        @Override
        public Optional<ManagedRepositoryObject> getNewObject() {
            return Optional.of(newObject);
        }

        @Override
        public ManagedRepositoryObject getAffectedObject() {
            return newObject;
        }
    }

    public static class Modify extends ManagedDiffEntry {
        private final ManagedRepositoryObject oldObject;
        private final ManagedRepositoryObject newObject;

        public Modify(@NonNull ManagedRepositoryObject oldObject, @NonNull ManagedRepositoryObject newObject, @NonNull String diff) {
            super(diff, ManagedDiffType.MODIFY, AffectedObjectType.NEW);
            this.oldObject = oldObject;
            this.newObject = newObject;
        }

        @Override
        public Optional<ManagedRepositoryObject> getOldObject() {
            return Optional.of(oldObject);
        }

        @Override
        public Optional<ManagedRepositoryObject> getNewObject() {
            return Optional.of(newObject);
        }

        @Override
        public ManagedRepositoryObject getAffectedObject() {
            return newObject;
        }
    }

    public static class Delete extends ManagedDiffEntry {
        private final ManagedRepositoryObject oldObject;

        public Delete(@NonNull ManagedRepositoryObject oldObject, @NonNull String diff) {
            super(diff, ManagedDiffType.DELETE, AffectedObjectType.OLD);
            this.oldObject = oldObject;
        }

        @Override
        public Optional<ManagedRepositoryObject> getOldObject() {
            return Optional.of(oldObject);
        }

        @Override
        public Optional<ManagedRepositoryObject> getNewObject() {
            return Optional.empty();
        }

        @Override
        public ManagedRepositoryObject getAffectedObject() {
            return oldObject;
        }
    }

    public static class Unchanged extends ManagedDiffEntry {
        private final ManagedRepositoryObject object;

        public Unchanged(@NonNull ManagedRepositoryObject object, @NonNull String diff) {
            super(diff, ManagedDiffType.UNCHANGED, AffectedObjectType.BOTH);
            this.object = object;
        }

        @Override
        public Optional<ManagedRepositoryObject> getOldObject() {
            return Optional.of(object);
        }

        @Override
        public Optional<ManagedRepositoryObject> getNewObject() {
            return Optional.of(object);
        }

        @Override
        public ManagedRepositoryObject getAffectedObject() {
            return object;
        }
    }
}
