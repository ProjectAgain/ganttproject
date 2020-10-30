// Copyright (C) 2017 BarD Software
package net.sourceforge.ganttproject.impex.csv;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import net.sourceforge.ganttproject.io.CSVOptions;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.model.CustomPropertyDefinition;
import net.sourceforge.ganttproject.model.GanttTask;
import net.sourceforge.ganttproject.model.resource.HumanResource;
import net.sourceforge.ganttproject.model.resource.HumanResourceManager;
import net.sourceforge.ganttproject.model.roles.RoleManager;
import net.sourceforge.ganttproject.model.roles.RoleManagerImpl;
import net.sourceforge.ganttproject.model.task.*;
import net.sourceforge.ganttproject.ui.table.task.TaskDefaultColumn;
import net.sourceforge.ganttproject.ui.viewmodel.option.BooleanOption;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static net.sourceforge.ganttproject.impex.csv.SpreadsheetFormat.CSV;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author dbarashev@bardsoftware.com
 */

public class GPCsvExportTest extends TaskTestCase {
    private static CSVOptions enableOnly(String... fields) {
        CSVOptions csvOptions = new CSVOptions();
        Set fieldSet = ImmutableSet.copyOf(fields);
        for (BooleanOption option: csvOptions.getTaskOptions().values()) {
            if (!fieldSet.contains(option.getID())) {
                option.setValue(false);
            }
        }
        for (BooleanOption option: csvOptions.getResourceOptions().values()) {
            if (!fieldSet.contains(option.getID())) {
                option.setValue(false);
            }
        }
        return csvOptions;
    }

    @Test
    public void testBomOption() throws Exception {
        TaskManager taskManager = getTaskManager();
        GanttTask task = taskManager.createTask();
        CSVOptions csvOptions = enableOnly(
            TaskDefaultColumn.NAME.getStub().getID());
        csvOptions.getBomOption().setValue(true);
        {
            GanttCSVExport exporter = new GanttCSVExport(
                taskManager,
                new HumanResourceManager(null, new CustomColumnsManager()),
                new RoleManagerImpl(),
                csvOptions
            );
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (SpreadsheetWriter writer = new CsvWriterImpl(outputStream, CSVFormat.DEFAULT, true)) {
                exporter.save(writer);
            }

            byte[] bytes = outputStream.toByteArray();
            // Binary representation of Unicode FEFF
            assertEquals((byte) 0xef, bytes[0]);
            assertEquals((byte) 0xbb, bytes[1]);
            assertEquals((byte) 0xbf, bytes[2]);
        }
        csvOptions.getBomOption().setValue(false);
        {
            GanttCSVExport exporter = new GanttCSVExport(
                taskManager,
                new HumanResourceManager(null, new CustomColumnsManager()),
                new RoleManagerImpl(),
                csvOptions
            );
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (SpreadsheetWriter writer = new CsvWriterImpl(outputStream, CSVFormat.DEFAULT)) {
                exporter.save(writer);
            }

            byte[] bytes = outputStream.toByteArray();
            // No BOM in the first bytes
            assertEquals('t', bytes[0]);
        }
    }

    @Test
    public void testResourceAssignments() throws Exception {
        HumanResourceManager hrManager = new HumanResourceManager(null, new CustomColumnsManager());
        TaskManager taskManager = getTaskManager();
        CSVOptions csvOptions =
            enableOnly(TaskDefaultColumn.ID.getStub().getID(), TaskDefaultColumn.RESOURCES.getStub().getID());

        Task task1 = createTask();
        Task task2 = createTask();
        Task task3 = createTask();

        HumanResource alice = hrManager.create("Alice", 1);
        HumanResource bob = hrManager.create("Bob", 2);

        task1.getAssignmentCollection().addAssignment(alice).setLoad(100f);
        task2.getAssignmentCollection().addAssignment(alice).setLoad(45.457f);
        task2.getAssignmentCollection().addAssignment(bob);

        Callable<String[]> exportJob = () -> {
            GanttCSVExport exporter = new GanttCSVExport(taskManager, hrManager, new RoleManagerImpl(), csvOptions);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (SpreadsheetWriter writer = new CsvWriterImpl(outputStream, CSVFormat.DEFAULT)) {
                exporter.save(writer);
            }
            return new String(outputStream.toByteArray(), Charsets.UTF_8.name()).split("\\n");
        };

        Consumer<String[]> verifier = lines -> {
            assertEquals(9, lines.length);
            assertEquals("tableColID,resources,Assignments", lines[0].trim());
            assertEquals("0,Alice,1:100.00", lines[1].trim());
            assertEquals("1,Alice;Bob,1:45.46;2:0.00", lines[2].trim());
            assertEquals("2,,", lines[3].trim());
        };

        verifier.accept(exportJob.call());

        // Change the locale to test decimal separators.
        GanttLanguage.getInstance().setLocale(Locale.forLanguageTag("ru-RU"));
        verifier.accept(exportJob.call());
    }

    @Test
    public void testResourceCustomFields() throws Exception {
        HumanResourceManager hrManager = new HumanResourceManager(null, new CustomColumnsManager());
        TaskManager taskManager = getTaskManager();
        RoleManager roleManager = new RoleManagerImpl();
        CSVOptions csvOptions = enableOnly("id");
        CustomPropertyDefinition prop1 = hrManager.getCustomPropertyManager().createDefinition(
            CustomPropertyManager.PropertyTypeEncoder.encodeFieldType(String.class),
            "prop1",
            null
        );
        CustomPropertyDefinition prop2 = hrManager.getCustomPropertyManager().createDefinition(
            CustomPropertyManager.PropertyTypeEncoder.encodeFieldType(String.class),
            "prop2",
            null
        );
        CustomPropertyDefinition prop3 = hrManager.getCustomPropertyManager().createDefinition(
            CustomPropertyManager.PropertyTypeEncoder.encodeFieldType(String.class),
            "prop3",
            null
        );
        hrManager.create("HR1", 1);
        hrManager.create("HR2", 2);
        hrManager.create("HR3", 3);

        hrManager.getById(1).addCustomProperty(prop3, "1");
        hrManager.getById(2).addCustomProperty(prop2, "2");
        hrManager.getById(3).addCustomProperty(prop1, "3");

        GanttCSVExport exporter = new GanttCSVExport(taskManager, hrManager, roleManager, csvOptions);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (SpreadsheetWriter writer = exporter.createWriter(outputStream, CSV)) {
            exporter.save(writer);
        }
        String[] lines = new String(outputStream.toByteArray(), Charsets.UTF_8.name()).split("\\n");
        assertEquals(7, lines.length);
// FIXME: this is flappy: assertEquals("tableColID,prop1,prop2,prop3", lines[3].trim());
//        with: assertEquals("ID,prop1,prop2,prop3", lines[3].trim());
        assertEquals("1,,,1", lines[4].trim());
        assertEquals("2,,2,", lines[5].trim());
        assertEquals("3,3,,", lines[6].trim());
    }

    @Test
    public void testTaskColor() throws Exception {
        TaskManager taskManager = getTaskManager();
        GanttTask task0 = taskManager.createTask();
        GanttTask task1 = taskManager.createTask();
        GanttTask task2 = taskManager.createTask();
        GanttTask task3 = taskManager.createTask();
        task0.setColor(Color.RED);
        task1.setColor(Color.GREEN);
        task2.setColor(new Color(42, 42, 42));
        // Leave task3 color default

        CSVOptions csvOptions =
            enableOnly(TaskDefaultColumn.ID.getStub().getID(), TaskDefaultColumn.COLOR.getStub().getID());
        GanttCSVExport exporter = new GanttCSVExport(
            taskManager,
            new HumanResourceManager(null, new CustomColumnsManager()),
            new RoleManagerImpl(),
            csvOptions
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (SpreadsheetWriter writer = new CsvWriterImpl(outputStream, CSVFormat.DEFAULT)) {
            exporter.save(writer);
        }
        String[] lines = new String(outputStream.toByteArray(), Charsets.UTF_8.name()).split("\\n");
        assertEquals(5, lines.length);
        assertEquals("tableColID,option.taskDefaultColor.label", lines[0].trim());
        assertEquals("0,\"#ff0000\"", lines[1].trim());
        assertEquals("1,\"#00ff00\"", lines[2].trim());
        assertEquals("2,\"#2a2a2a\"", lines[3].trim());
        assertEquals("3,", lines[4].trim());
    }

    @Test
    public void testTaskCustomFields() throws Exception {
        HumanResourceManager hrManager = new HumanResourceManager(null, new CustomColumnsManager());
        TaskManager taskManager = getTaskManager();
        RoleManager roleManager = new RoleManagerImpl();
        CSVOptions csvOptions = enableOnly(TaskDefaultColumn.ID.getStub().getID());

        CustomPropertyDefinition prop1 = taskManager.getCustomPropertyManager().createDefinition(
            CustomPropertyManager.PropertyTypeEncoder.encodeFieldType(String.class), "prop1", null);
        CustomPropertyDefinition prop2 = taskManager.getCustomPropertyManager().createDefinition(
            CustomPropertyManager.PropertyTypeEncoder.encodeFieldType(String.class), "prop2", null);
        CustomPropertyDefinition prop3 = taskManager.getCustomPropertyManager().createDefinition(
            CustomPropertyManager.PropertyTypeEncoder.encodeFieldType(String.class), "prop3", null);
        Task task1 = createTask();
        Task task2 = createTask();
        Task task3 = createTask();
        task1.getCustomValues().addCustomProperty(prop3, "a");
        task2.getCustomValues().addCustomProperty(prop2, "b");
        task3.getCustomValues().addCustomProperty(prop1, "c");

        GanttCSVExport exporter = new GanttCSVExport(taskManager, hrManager, roleManager, csvOptions);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (SpreadsheetWriter writer = exporter.createWriter(outputStream, CSV)) {
            exporter.save(writer);
        }
        String[] lines = new String(outputStream.toByteArray(), Charsets.UTF_8.name()).split("\\n");
        assertEquals(4, lines.length);
        assertEquals("tableColID,prop1,prop2,prop3", lines[0].trim());
        assertEquals("0,,,a", lines[1].trim());
        assertEquals("1,,b,", lines[2].trim());
        assertEquals("2,c,,", lines[3].trim());
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        TaskDefaultColumn.setLocaleApi(null);
    }
}
