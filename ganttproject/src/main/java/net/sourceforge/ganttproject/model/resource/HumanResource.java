/*
Copyright 2003 GanttProject Team

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.sourceforge.ganttproject.model.resource;

import com.google.common.base.Strings;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.model.CustomProperty;
import net.sourceforge.ganttproject.model.CustomPropertyDefinition;
import net.sourceforge.ganttproject.model.CustomPropertyHolder;
import net.sourceforge.ganttproject.model.calendar.GanttDaysOff;
import net.sourceforge.ganttproject.model.roles.Role;
import net.sourceforge.ganttproject.model.task.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author barmeier
 */
public class HumanResource implements CustomPropertyHolder {
  private static class CustomPropertyImpl implements CustomProperty {
    private final CustomPropertyDefinition myDefinition;
    private final Object myValue;

    CustomPropertyImpl(CustomPropertyDefinition definition, Object value) {
      myDefinition = definition;
      myValue = value;
    }

    @Override
    public CustomPropertyDefinition getDefinition() {
      return myDefinition;
    }

    @Override
    public Object getValue() {
      return myValue;
    }

    @Override
    public String getValueAsString() {
      return HumanResourceManager.getValueAsString(myValue);
    }
  }

  private class ResourceAssignmentImpl implements ResourceAssignment {

    private final ResourceAssignment myAssignmentToTask;
    private boolean myCoordinator;
    private float myLoad;
    private Role myRoleForAssignment;

    private ResourceAssignmentImpl(ResourceAssignment assignmentToTask) {
      myAssignmentToTask = assignmentToTask;
    }

    /**
     * Removes all related assignments
     */
    @Override
    public void delete() {
      HumanResource.this.myAssignments.remove(this);
      HumanResource.this.fireAssignmentChanged();
    }

    @Override
    public float getLoad() {
      return myLoad;
    }

    @Override
    public void setLoad(float load) {
      myLoad = load;
      HumanResource.this.fireAssignmentChanged();
    }

    @Override
    public HumanResource getResource() {
      return HumanResource.this;
    }

    @Override
    public Role getRoleForAssignment() {

      return myRoleForAssignment;
    }

    @Override
    public void setRoleForAssignment(Role role) {
      myRoleForAssignment = role;
    }

    @Override
    public Task getTask() {
      return myAssignmentToTask.getTask();
    }

    @Override
    public boolean isCoordinator() {
      return myCoordinator;
    }

    @Override
    public void setCoordinator(boolean responsible) {
      myCoordinator = responsible;
    }

    @Override
    public String toString() {
      return this.getResource().getName() + " -> " + this.getTask().getName();
    }
  }

  private final List<ResourceAssignment> myAssignments = new ArrayList<>();
  private final CustomColumnsValues myCustomProperties;
  private final DefaultListModel<GanttDaysOff> myDaysOffList = new DefaultListModel<>();
  private final HumanResourceManager myManager;
  /**
   * Can be turned (temporarily) off to prevent (a flood of) events
   */
  private boolean areEventsEnabled = true;
  private String description;
  private String email = "";
  private int id = -1;
  private LoadDistribution myLoadDistribution;
  private Role myRole;
  private BigDecimal myStandardPayRate;
  private String name;
  private String phone = "";

  HumanResource(HumanResourceManager manager) {
    this("", -1, manager);
  }

  /**
   * Creates a new instance of HumanResource
   */
  public HumanResource(String name, int id, HumanResourceManager manager) {
    this.id = id;
    this.name = name;
    myManager = manager;
    myCustomProperties = new CustomColumnsValues(myManager.getCustomPropertyManager());
  }

  private HumanResource(HumanResource copy) {
    areEventsEnabled = false;
    setId(-1);
    String newName = GanttLanguage.getInstance().formatText("resource.copy.prefix",
                                                            GanttLanguage.getInstance().getText("copy2"), copy.getName()
    );
    setName(newName);
    setDescription(copy.getDescription());
    setMail(copy.getMail());
    setPhone(copy.getPhone());
    setRole(copy.getRole());
    setStandardPayRate(copy.getStandardPayRate());
    myManager = copy.myManager;
    DefaultListModel<GanttDaysOff> copyDaysOff = copy.getDaysOff();
    for (int i = 0; i < copyDaysOff.getSize(); i++) {
      myDaysOffList.addElement(copyDaysOff.get(i));
    }
    areEventsEnabled = true;
    myCustomProperties = (CustomColumnsValues) copy.myCustomProperties.clone();
  }

  @Override
  public CustomProperty addCustomProperty(CustomPropertyDefinition definition, String valueAsString) {
    final CustomPropertyDefinition stubDefinition = CustomPropertyManager.PropertyTypeEncoder.decodeTypeAndDefaultValue(
      definition.getTypeAsString(), valueAsString);
    setCustomField(definition, stubDefinition.getDefaultValue());
    return new CustomPropertyImpl(definition, stubDefinition.getDefaultValue());
  }

  public void addDaysOff(GanttDaysOff gdo) {
    myDaysOffList.addElement(gdo);
    fireResourceChanged();
  }

  public ResourceAssignment createAssignment(ResourceAssignment assignmentToTask) {
    ResourceAssignment result = new ResourceAssignmentImpl(assignmentToTask);
    myAssignments.add(result);
    resetLoads();
    fireAssignmentsChanged();
    return result;
  }

  public void delete() {
    removeAllAssignments();
    myManager.remove(this);
  }

  @Override
  public boolean equals(Object obj) {
    boolean result = false;
    if (obj instanceof HumanResource) {
      HumanResource pr = (HumanResource) obj;
      result = pr.id == id;
    }
    return result;
  }

  public ResourceAssignment[] getAssignments() {
    return myAssignments.toArray(new ResourceAssignment[0]);
  }

  @Override
  public List<CustomProperty> getCustomProperties() {
    return myCustomProperties.getCustomProperties();
  }

  public DefaultListModel<GanttDaysOff> getDaysOff() {
    return myDaysOffList;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
    fireResourceChanged();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    if (this.id == -1) {
      // setting the id is only allowed when id is not assigned
      this.id = id;
    }
  }

  public LoadDistribution getLoadDistribution() {
    if (myLoadDistribution == null) {
      myLoadDistribution = new LoadDistribution(this);
    }
    return myLoadDistribution;
  }

  public String getMail() {
    return email;
  }

  public void setMail(String email) {
    if (email == null) {
      return;
    }
    this.email = email;
    fireResourceChanged();
  }

  public String getName() {
    return Strings.nullToEmpty(name);
  }

  public void setName(String name) {
    this.name = name;
    fireResourceChanged();
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    if (phone == null) {
      return;
    }
    this.phone = phone;
    fireResourceChanged();
  }

  public Role getRole() {
    if (myRole == null) {
      System.err.println("[HumanResource] getRole(): I have no role :( name=" + getName());
    }
    return myRole;
  }

  public void setRole(Role role) {
    myRole = role;
    fireResourceChanged();
  }

  public BigDecimal getStandardPayRate() {
    return myStandardPayRate == null ? BigDecimal.ZERO : myStandardPayRate;
  }

  public void setStandardPayRate(BigDecimal rate) {
    myStandardPayRate = rate;
  }

  public BigDecimal getTotalCost() {
    BigDecimal cost = BigDecimal.ZERO;
    for (ResourceAssignment assignment: myAssignments) {
      int taskDuration = assignment.getTask().getDuration().getLength();
      BigDecimal assignmentCost =
        new BigDecimal(taskDuration * assignment.getLoad() / 100).multiply(getStandardPayRate());
      cost = cost.add(assignmentCost);
    }
    return cost;
  }

  public double getTotalLoad() {
    double totalLoad = 0.0;
    for (ResourceAssignment assignment: myAssignments) {
      totalLoad = totalLoad + assignment.getLoad() * assignment.getTask().getDuration().getLength() / 100.0;
    }
    return totalLoad;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public void resetLoads() {
    myLoadDistribution = null;
  }

  public void setCustomField(CustomPropertyDefinition def, Object value) {
    try {
      myCustomProperties.setValue(def, value);
      fireResourceChanged();
    } catch (CustomColumnsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void swapAssignments(ResourceAssignment a1, ResourceAssignment a2) {
    Collections.swap(myAssignments, myAssignments.indexOf(a1), myAssignments.indexOf(a2));
    resetLoads();
    fireAssignmentsChanged();
  }

  @Override
  public String toString() {
    return name;
  }

  public HumanResource unpluggedClone() {
    return new HumanResource(this);
  }

  Object getCustomField(CustomPropertyDefinition def) {
    return myCustomProperties.getValue(def);
  }

  private void fireAssignmentChanged() {
    resetLoads();
    fireAssignmentsChanged();
  }

  private void fireAssignmentsChanged() {
    if (areEventsEnabled) {
      myManager.fireAssignmentsChanged(this);
    }
  }

  private void fireResourceChanged() {
    if (areEventsEnabled) {
      myManager.fireResourceChanged(this);
    }
  }

  /**
   * Removes the assignment objects associated to this ProjectResource and those
   * associated to it's Tasks
   */
  private void removeAllAssignments() {
    List<ResourceAssignment> copy = new ArrayList<>(myAssignments);
    for (ResourceAssignment aCopy: copy) {
      ResourceAssignmentImpl next = (ResourceAssignmentImpl) aCopy;
      next.myAssignmentToTask.delete();
    }
    resetLoads();
  }
}
