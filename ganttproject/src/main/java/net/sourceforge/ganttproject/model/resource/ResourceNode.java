/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 GanttProject Team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.model.resource;

import net.sourceforge.ganttproject.model.CustomPropertyDefinition;
import net.sourceforge.ganttproject.model.roles.Role;
import net.sourceforge.ganttproject.ui.chart.ResourceDefaultColumn;
import net.sourceforge.ganttproject.ui.chart.TreeUtil;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

public class ResourceNode extends ResourceTableNode {
  private static final Set<ResourceDefaultColumn> ourApplicableColumns = EnumSet.complementOf(
    EnumSet.of(ResourceDefaultColumn.ROLE_IN_TASK));
  private static final long serialVersionUID = 3834033541318392117L;

  private final HumanResource resource;

  public ResourceNode(HumanResource res) {
    super(res, ourApplicableColumns);
    assert res != null;
    resource = res;
  }

  @Override
  public boolean equals(Object obj) {
    boolean res = false;
    if (this == obj) {
      return true;
    }
    if (obj instanceof ResourceNode) {
      ResourceNode rn = (ResourceNode) obj;
      res = rn.getUserObject() != null && rn.getUserObject().equals(this.getUserObject());
    }
    return res;
  }

  /**
   * @return the value of a custom field referenced by its title
   */
  @Override
  public Object getCustomField(CustomPropertyDefinition def) {
    return resource.getCustomField(def);
  }

  public Role getDefaultRole() {
    return resource.getRole();
  }

  public void setDefaultRole(Role defRole) {
    resource.setRole(defRole);
  }

  public String getEMail() {
    return resource.getMail();
  }

  public void setEMail(String email) {
    resource.setMail(email);
  }

  public String getName() {
    return resource.getName();
  }

  public void setName(String name) {
    resource.setName(name);
  }

  public String getPhone() {
    return resource.getPhone();
  }

  public void setPhone(String phoneNumber) {
    resource.setPhone(phoneNumber);
  }

  public HumanResource getResource() {
    return resource;
  }

  @Override
  public Object getStandardField(ResourceDefaultColumn def) {
    switch (def) {
      case NAME:
        return getName();
      case ROLE:
        return getDefaultRole();
      case EMAIL:
        return getEMail();
      case PHONE:
        return getPhone();
      case STANDARD_RATE:
        return getResource().getStandardPayRate();
      case TOTAL_COST:
        return getResource().getTotalCost();
      case TOTAL_LOAD:
        return getResource().getTotalLoad();
      default:
        return "";
    }
  }

  public void removeAllChildren() {
    TreeUtil.removeAllChildren(this);
  }

  /**
   * sets the new value to the custom field referenced by its title
   */
  @Override
  public void setCustomField(CustomPropertyDefinition def, Object val) {
    resource.setCustomField(def, val);
  }

  @Override
  public void setStandardField(ResourceDefaultColumn def, Object value) {
    switch (def) {
      case NAME:
        setName(value.toString());
        return;
      case EMAIL:
        setEMail(value.toString());
        return;
      case PHONE:
        setPhone(value.toString());
        return;
      case ROLE:
        setDefaultRole((Role) value);
        return;
      case STANDARD_RATE:
        assert value instanceof Double : "Rate accepts numeric values";
        getResource().setStandardPayRate(BigDecimal.valueOf((Double) value));
    }
  }

  @Override
  public String toString() {
    if (resource != null) {
      return resource.getName();
    }
    return "-";
  }
}
