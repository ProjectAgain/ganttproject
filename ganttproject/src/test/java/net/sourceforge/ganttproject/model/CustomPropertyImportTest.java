package net.sourceforge.ganttproject.model;

import net.sourceforge.ganttproject.model.task.CustomColumnsManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomPropertyImportTest {
    @Test
    public void testImportDuplicatedProperties() {
        {
            CustomColumnsManager target = new CustomColumnsManager();
            target.createDefinition(CustomPropertyClass.TEXT.getID(), "col1", null);
            target.createDefinition(CustomPropertyClass.TEXT.getID(), "col2", null);

            CustomColumnsManager source = new CustomColumnsManager();
            source.createDefinition(CustomPropertyClass.TEXT.getID(), "col1", null);
            source.createDefinition(CustomPropertyClass.TEXT.getID(), "col3", null);

            target.importData(source);
            List<CustomPropertyDefinition> definitions = target.getDefinitions();
            assertEquals(3, definitions.size());
        }
        {
            CustomColumnsManager target = new CustomColumnsManager();
            target.createDefinition(CustomPropertyClass.TEXT.getID(), "col1", null);
            target.createDefinition(CustomPropertyClass.TEXT.getID(), "col2", null);

            CustomColumnsManager source = new CustomColumnsManager();
            source.createDefinition(CustomPropertyClass.DATE.getID(), "col1", null);
            source.createDefinition(CustomPropertyClass.TEXT.getID(), "col3", null);

            target.importData(source);
            List<CustomPropertyDefinition> definitions = target.getDefinitions();
            assertEquals(4, definitions.size());
        }
    }
}
