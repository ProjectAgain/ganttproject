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
package net.sourceforge.ganttproject.model.roles;

import java.util.EventListener;
import java.util.EventObject;

/**
 * @author athomas
 */
public interface RoleManager {
  class Access {
    private static final RoleManager ourInstance = new RoleManagerImpl();

    public static RoleManager getInstance() {
      return ourInstance;
    }
  }

  class RoleEvent extends EventObject {
    private final RoleSet myChangedRoleSet;

    public RoleEvent(RoleManager source, RoleSet changedRoleSet) {
      super(source);
      myChangedRoleSet = changedRoleSet;
    }

    public RoleSet getChangedRoleSet() {
      return myChangedRoleSet;
    }
  }

  interface Listener extends EventListener {
    void rolesChanged(RoleEvent e);
  }
  int DEFAULT_ROLES_NUMBER = 11;

  void addRoleListener(Listener listener);

  /**
   * Clear the role list
   */
  void clear();

  RoleSet createRoleSet(String name);

  Role getDefaultRole();

  Role[] getEnabledRoles();

  /**
   * Return all roles except the default roles
   */
  // public String [] getRolesShort();
  Role[] getProjectLevelRoles();

  RoleSet getProjectRoleSet();

  Role getRole(String persistentID);

  RoleSet getRoleSet(String rolesetName);

  RoleSet[] getRoleSets();

  void importData(RoleManager roleManager);

  void removeRoleListener(Listener listener);
}
