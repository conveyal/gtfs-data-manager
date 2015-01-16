package utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import play.Play;

/**
 * Handles the details of where deployments should be put.
 */
public class DeploymentManager {
    private static Map<String, Map<String, Object>> getServers () {
        return (Map<String, Map<String, Object>>) Play.application().configuration().getObject("application.deployment.servers");
    }
    
    /**
     * Get all of the names for the deployments
     * @param admin is the user an admin? if false, only show the deployments that don't require administrative privileges.
     */
    public static Set<String> getDeploymentNames (boolean admin) {
        Map<String, Map<String, Object>> servers = getServers();
        
        if (admin)
            return servers.keySet();
        else {
            Set<String> ret = new HashSet<String>();
            for (String server : servers.keySet()) {
                if (!isDeploymentAdmin(server)) {
                    ret.add(server);
                }                    
            }
            
            return ret;
        }
    };
    
    /** Get the servers for a particular deployment */
    public static List<String> getDeploymentUrls (String name) {
        return (List<String>) getServers().get(name).get("internal");
    }
    
    /**
     * If true, only admins should be allowed to deploy to this deployment.
     */
    public static boolean isDeploymentAdmin (String name) {
        Map<String, Object> server = getServers().get(name);
        if (!server.containsKey("admin"))
            return false;
        
        else return (Boolean) server.get("admin");
    }

    /** Get the public otp.js server for a particular deployment */
    public static String getPublicUrl(String name) {;
        return (String) getServers().get(name).get("public");
    }
}
