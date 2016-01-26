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

module.exports = Auth0User;
