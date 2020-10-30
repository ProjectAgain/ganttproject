/*
 * Created on 18.06.2004
 *
 */
package net.sourceforge.ganttproject.model.roles;

/**
 * @author bard
 */
public interface RoleSet {
  String DEFAULT = "Default";
  String SOFTWARE_DEVELOPMENT = "SoftwareDevelopment";

  void changeRole(String name, int roleID);

  void clear();

  Role createRole(String name);

  Role createRole(String name, int persistentID);

  void deleteRole(Role role);

  Role findRole(int roleID);

  String getName();

  Role[] getRoles();

  boolean isEmpty();

  boolean isEnabled();

  void setEnabled(boolean isEnabled);
}
