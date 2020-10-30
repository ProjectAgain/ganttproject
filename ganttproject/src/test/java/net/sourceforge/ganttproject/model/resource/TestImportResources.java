package net.sourceforge.ganttproject.model.resource;

import net.sourceforge.ganttproject.model.resource.HumanResourceMerger.MergeResourcesOption;
import net.sourceforge.ganttproject.model.task.CustomColumnsManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImportResources {

    @Test
    public void testMergeByID() {
        MergeResourcesOption mergeOption = new MergeResourcesOption();
        mergeOption.setValue(MergeResourcesOption.BY_ID);

        HumanResourceManager mergeTo = new HumanResourceManager(null, new CustomColumnsManager());
        mergeTo.add(new HumanResource("joe", 1, mergeTo));
        mergeTo.add(new HumanResource("john", 2, mergeTo));

        HumanResourceManager mergeFrom = new HumanResourceManager(null, new CustomColumnsManager());
        mergeFrom.add(new HumanResource("jack", 1, mergeFrom));
        mergeFrom.add(new HumanResource("joe", 3, mergeFrom));

        mergeTo.importData(mergeFrom, new OverwritingMerger(mergeOption));

        assertEquals(3, mergeTo.getResources().size());
        assertEquals("jack", mergeTo.getById(1).getName());
        assertEquals("john", mergeTo.getById(2).getName());
        assertEquals("joe", mergeTo.getById(3).getName());
    }

    @Test
    public void testMergeResourcesByName() {
        MergeResourcesOption mergeOption = new MergeResourcesOption();
        mergeOption.setValue(MergeResourcesOption.BY_NAME);

        HumanResourceManager mergeTo = new HumanResourceManager(null, new CustomColumnsManager());
        mergeTo.add(new HumanResource("joe", 1, mergeTo));
        mergeTo.add(new HumanResource("john", 2, mergeTo));

        HumanResourceManager mergeFrom = new HumanResourceManager(null, new CustomColumnsManager());
        mergeFrom.add(new HumanResource("jack", 1, mergeFrom));
        mergeFrom.add(new HumanResource("joe", 2, mergeFrom));

        mergeTo.importData(mergeFrom, new OverwritingMerger(mergeOption));

        assertEquals(3, mergeTo.getResources().size());
        assertEquals("joe", mergeTo.getById(1).getName());
        assertEquals("john", mergeTo.getById(2).getName());
        assertEquals("jack", mergeTo.getById(3).getName());
    }
}
