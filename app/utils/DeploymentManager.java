package utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import play.Play;

/**
 * Handles the details of where deployments should be put.
 */
public class DeploymentManager {
    /** Get all of the names for the deployments */
    public static Set<String> getDeploymentNames () {
        return ((Map<String, Object>) Play.application().configuration().getObject("application.deployment.servers")).keySet();
    };
    
    /** Get the servers for a particular deployment */
    public static List<String> getDeploymentUrls (String name) {
        Map<String, Map<String, Object>> servers = 
                (Map<String, Map<String, Object>>) Play.application().configuration().getObject("application.deployment.servers");
        return (List<String>) servers.get(name).get("internal");
    }

    /** Get the public otp.js server for a particular deployment */
    public static String getPublicUrl(String name) {
        Map<String, Map<String, Object>> servers = 
                (Map<String, Map<String, Object>>) Play.application().configuration().getObject("application.deployment.servers");
        return (String) servers.get(name).get("public");
    }
}
