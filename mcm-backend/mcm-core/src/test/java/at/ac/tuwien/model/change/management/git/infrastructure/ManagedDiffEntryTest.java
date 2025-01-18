package at.ac.tuwien.model.change.management.git.infrastructure;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ManagedDiffEntryTest {

    @Mock
    private ManagedRepositoryObject oldObject;

    @Mock
    private ManagedRepositoryObject newObject;

    @Mock
    private ManagedRepositoryObject unchangedObject;

    @Test
    public void testAdd_getDiffType_shouldReturnAdd() {
        var addDiff = new ManagedDiffEntry.Add(newObject, "diff");
        Assertions.assertThat(addDiff.getDiffType()).isEqualTo(ManagedDiffType.ADD);
    }

    @Test
    public void testAdd_getAffectedObjectType_shouldReturnNewObject() {
        var addDiff = new ManagedDiffEntry.Add(newObject, "diff");
        Assertions.assertThat(addDiff.getAffectedObjectType()).isEqualTo(AffectedObjectType.NEW);
    }

    @Test
    public void testAdd_getDiff_shouldReturnDiff() {
        var addDiff = new ManagedDiffEntry.Add(newObject, "diff");
        Assertions.assertThat(addDiff.getDiff()).isEqualTo("diff");
    }

    @Test
    public void testAdd_getOldObject_shouldReturnEmptyOptional() {
        var addDiff = new ManagedDiffEntry.Add(newObject, "diff");
        Assertions.assertThat(addDiff.getOldObject()).isEmpty();
    }

    @Test
    public void testAdd_getNewObject_shouldReturnOptionalOfNewObject() {
        var addDiff = new ManagedDiffEntry.Add(newObject, "diff");
        Assertions.assertThat(addDiff.getNewObject()).isPresent().contains(newObject);
    }

    @Test
    public void testAdd_getAffectedObject_shouldReturnNewObject() {
        var addDiff = new ManagedDiffEntry.Add(newObject, "diff");
        Assertions.assertThat(addDiff.getAffectedObject()).isEqualTo(newObject);
    }


    @Test
    public void testDelete_getDiffType_shouldReturnDelete() {
        var deleteDiff = new ManagedDiffEntry.Delete(oldObject, "diff");
        Assertions.assertThat(deleteDiff.getDiffType()).isEqualTo(ManagedDiffType.DELETE);
    }

    @Test
    public void testDelete_getAffectedObjectType_shouldReturnOldObject() {
        var deleteDiff = new ManagedDiffEntry.Delete(oldObject, "diff");
        Assertions.assertThat(deleteDiff.getAffectedObjectType()).isEqualTo(AffectedObjectType.OLD);
    }

    @Test
    public void testDelete_getDiff_shouldReturnDiff() {
        var deleteDiff = new ManagedDiffEntry.Delete(oldObject, "diff");
        Assertions.assertThat(deleteDiff.getDiff()).isEqualTo("diff");
    }

    @Test
    public void testDelete_getOldObject_shouldReturnOptionalOfOldObject() {
        var deleteDiff = new ManagedDiffEntry.Delete(oldObject, "diff");
        Assertions.assertThat(deleteDiff.getOldObject()).isPresent().contains(oldObject);
    }

    @Test
    public void testDelete_getNewObject_shouldReturnEmptyOptional() {
        var deleteDiff = new ManagedDiffEntry.Delete(oldObject, "diff");
        Assertions.assertThat(deleteDiff.getNewObject()).isEmpty();
    }

    @Test
    public void testDelete_getAffectedObject_shouldReturnOldObject() {
        var deleteDiff = new ManagedDiffEntry.Delete(oldObject, "diff");
        Assertions.assertThat(deleteDiff.getAffectedObject()).isEqualTo(oldObject);
    }

    @Test
    public void testModified_getDiffType_shouldReturnModify() {
        var modifyDiff = new ManagedDiffEntry.Modify(oldObject, newObject, "diff");
        Assertions.assertThat(modifyDiff.getDiffType()).isEqualTo(ManagedDiffType.MODIFY);
    }

    @Test
    public void testModified_getAffectedObjectType_shouldReturnNewObject() {
        var modifyDiff = new ManagedDiffEntry.Modify(oldObject, newObject, "diff");
        Assertions.assertThat(modifyDiff.getAffectedObjectType()).isEqualTo(AffectedObjectType.NEW);
    }

    @Test
    public void testModified_getDiff_shouldReturnDiff() {
        var modifyDiff = new ManagedDiffEntry.Modify(oldObject, newObject, "diff");
        Assertions.assertThat(modifyDiff.getDiff()).isEqualTo("diff");
    }

    @Test
    public void testModified_getOldObject_shouldReturnOptionalOfOldObject() {
        var modifyDiff = new ManagedDiffEntry.Modify(oldObject, newObject, "diff");
        Assertions.assertThat(modifyDiff.getOldObject()).isPresent().contains(oldObject);
    }

    @Test
    public void testModified_getNewObject_shouldReturnOptionalOfNewObject() {
        var modifyDiff = new ManagedDiffEntry.Modify(oldObject, newObject, "diff");
        Assertions.assertThat(modifyDiff.getNewObject()).isPresent().contains(newObject);
    }

    @Test
    public void testModified_getAffectedObject_shouldReturnNewObject() {
        var modifyDiff = new ManagedDiffEntry.Modify(oldObject, newObject, "diff");
        Assertions.assertThat(modifyDiff.getAffectedObject()).isEqualTo(newObject);
    }

    @Test
    public void testUnchanged_getDiffType_shouldReturnUnchanged() {
        var unchangedDiff = new ManagedDiffEntry.Unchanged(unchangedObject, "content");
        Assertions.assertThat(unchangedDiff.getDiffType()).isEqualTo(ManagedDiffType.UNCHANGED);
    }

    @Test
    public void testUnchanged_getAffectedObjectType_shouldReturnBoth() {
        var unchangedDiff = new ManagedDiffEntry.Unchanged(unchangedObject, "content");
        Assertions.assertThat(unchangedDiff.getAffectedObjectType()).isEqualTo(AffectedObjectType.BOTH);
    }

    @Test
    public void testUnchanged_getDiff_shouldReturnDiff() {
        var unchangedDiff = new ManagedDiffEntry.Unchanged(unchangedObject, "content");
        Assertions.assertThat(unchangedDiff.getDiff()).isEqualTo("content");
    }

    @Test
    public void testUnchanged_getOldObject_shouldReturnOptionalOfObject() {
        var unchangedDiff = new ManagedDiffEntry.Unchanged(unchangedObject, "content");
        Assertions.assertThat(unchangedDiff.getOldObject()).isPresent().contains(unchangedObject);
    }

    @Test
    public void testUnchanged_getNewObject_shouldReturnOptionalOfObject() {
        var unchangedDiff = new ManagedDiffEntry.Unchanged(unchangedObject, "content");
        Assertions.assertThat(unchangedDiff.getNewObject()).isPresent().contains(unchangedObject);
    }

    @Test
    public void testUnchanged_getAffectedObject_shouldReturnObject() {
        var unchangedDiff = new ManagedDiffEntry.Unchanged(unchangedObject, "content");
        Assertions.assertThat(unchangedDiff.getAffectedObject()).isEqualTo(unchangedObject);
    }
}
