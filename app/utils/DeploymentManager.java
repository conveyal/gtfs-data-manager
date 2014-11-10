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
        return ((Map<String, List<String>>) Play.application().configuration().getObject("application.deployment.servers")).get(name);
    }
}
