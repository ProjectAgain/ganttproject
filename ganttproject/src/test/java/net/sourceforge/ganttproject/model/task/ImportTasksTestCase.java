/*
 * Created on 24.02.2005
 */
package net.sourceforge.ganttproject.model.task;

import net.sourceforge.ganttproject.model.CustomPropertyClass;
import net.sourceforge.ganttproject.model.CustomPropertyDefinition;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author bard
 */
public class ImportTasksTestCase extends TaskTestCase {
  private static CustomPropertyDefinition findCustomPropertyByName(CustomPropertyManager mgr, String name) {
    for (CustomPropertyDefinition def: mgr.getDefinitions()) {
      if (def.getName().equals(name)) {
        return def;
      }
    }
    return null;
  }

  @Test
  public void testImportCustomColumns() {
    TaskManager importTo = getTaskManager();
    {
      CustomPropertyDefinition def = importTo.getCustomPropertyManager().createDefinition(
        "col1", CustomPropertyClass.TEXT.getID(), "foo", "foo");
      Task root = importTo.getTaskHierarchy().getRootTask();
      importTo.createTask(1).move(root);
      assertEquals("foo", importTo.getTask(1).getCustomValues().getValue(def));
    }
    TaskManager importFrom = newTaskManager();
    {
      CustomPropertyDefinition def = importFrom.getCustomPropertyManager().createDefinition(
        "col1", CustomPropertyClass.TEXT.getID(), "bar", "bar");
      Task root = importTo.getTaskHierarchy().getRootTask();
      importFrom.createTask(1).move(root);
      assertEquals("bar", importFrom.getTask(1).getCustomValues().getValue(def));
    }
    Map<CustomPropertyDefinition, CustomPropertyDefinition> customDefsMapping =
      importTo.getCustomPropertyManager().importData(importFrom.getCustomPropertyManager());
    importTo.importData(importFrom, customDefsMapping);

    CustomPropertyDefinition fooDef = findCustomPropertyByName(importTo.getCustomPropertyManager(), "foo");
    assertNotNull(fooDef);
    assertEquals("foo", importTo.getTask(1).getCustomValues().getValue(fooDef));

    CustomPropertyDefinition barDef = findCustomPropertyByName(importTo.getCustomPropertyManager(), "bar");
    assertNotNull(barDef);
    assertEquals("bar", importTo.getTask(1).getCustomValues().getValue(barDef));
  }

  @Test
  public void testImportingPreservesIDs() {
    TaskManager taskManager = getTaskManager();
    {
      Task root = taskManager.getTaskHierarchy().getRootTask();
      Task[] nestedTasks = taskManager.getTaskHierarchy().getNestedTasks(root);
      assertEquals(0, nestedTasks.length, "Unexpected count of the root's children BEFORE importing");
    }
    TaskManager importFrom = newTaskManager();
    {
      Task importRoot = importFrom.getTaskHierarchy().getRootTask();
      importFrom.createTask(2).move(importRoot);
      importFrom.createTask(3).move(importRoot);
    }

    taskManager.importData(importFrom, Collections.emptyMap());
    {
      Task root = taskManager.getTaskHierarchy().getRootTask();
      Task[] nestedTasks = taskManager.getTaskHierarchy().getNestedTasks(
        root);
      assertEquals(2, nestedTasks.length, "Unexpected count of the root's children AFTER importing. root="
                                          + root);
      List<Integer> expectedIDs = Arrays.asList(
        2,
        3
      );
      List<Integer> actualIds = new ArrayList<Integer>(2);
      actualIds.add(nestedTasks[0].getTaskID());
      actualIds.add(nestedTasks[1].getTaskID());
      assertEquals(new HashSet<Integer>(
        expectedIDs), new HashSet<Integer>(actualIds), "Unexpected IDs of the imported tasks");
    }
  }
}
