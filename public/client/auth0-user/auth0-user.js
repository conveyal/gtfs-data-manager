function Auth0User (profile) {
  this.profile = profile;
  this.dt = this.profile.app_metadata.datatools;
}

Auth0User.prototype.getEmail = function () {
  return this.profile.email;
};

Auth0User.prototype.canAdminsterProject = function (projectID) {
  if(this.dt && this.dt.projects) {
    for(var i=0; i< this.dt.projects.length; i++) {
      var project = this.dt.projects[i];
      if(project.project_id === projectID) {
        for(var j=0; j<project.permissions.length; j++) {
          if(project.permissions[j].type === "administer-project") return true;
        }
      }
    }
  }
  return false;
};

Auth0User.prototype.canManageFeed = function (projectID, feedID) {
  if(this.dt && this.dt.projects) {
    for(var i=0; i< this.dt.projects.length; i++) {
      var project = this.dt.projects[i];
      if(project.project_id === projectID) {
        for(var j=0; j<project.permissions.length; j++) {
          var permission = project.permissions[j];
          if(permission.type === "administer-project") return true;
          if(permission.type === "manage-feed") {
            for (var k = 0; k < permission.feeds.length; k++) {
              if (permission.feeds[k] === feedID || permission.feeds[k] === "*") {
                return true;
              }
            }
          }
        }
      }
    }
  }
  return false;
};

Auth0User.prototype.canAdministerApp = function () {
  if(this.dt && this.dt.permissions) {
    for(var i=0; i<this.dt.permissions.length; i++) {
      var permission = this.dt.permissions[i];
      if(permission.type === "administer-application") return true;
    }
  }
  return false;
};

module.exports = Auth0User;
