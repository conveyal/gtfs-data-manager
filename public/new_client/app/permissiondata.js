export default class PermissionData {
  constructor (datatoolsJson) {
    this.projectLookup = {}
    if (datatoolsJson && datatoolsJson.projects) {
      for (var project of datatoolsJson.projects) {
        this.projectLookup[project.project_id] = project
      }
    }
  }

  hasProject (projectID) {
    return (projectID in this.projectLookup)
  }

  isProjectAdmin (projectID) {
    return this.hasProject(projectID) && this.getProjectPermission(projectID, 'administer-project') != null;
  }

  getProjectPermissions (projectID) {
    if (!this.hasProject(projectID)) return null
    return this.projectLookup[projectID].permissions
  }

  getProjectPermission (projectID, permissionType) {
    if (!this.hasProject(projectID)) return null
    var projectPermissions = this.getProjectPermissions(projectID)
    for (var permission of projectPermissions) {
      if(permission.type === permissionType) return permission
    }
    return null
  }
}
