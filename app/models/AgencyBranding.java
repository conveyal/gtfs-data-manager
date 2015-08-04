package models;

import play.Play;

import java.io.Serializable;

/**
 * Created by demory on 4/3/15.
 */

public class AgencyBranding implements Serializable {
    private static final long serialVersionUID = 1L;

    public String agencyId;

    public Boolean hasLogo;

    public String urlRoot;

    // TODO: route-level branding

    public AgencyBranding(String agencyId) {
        this.agencyId = agencyId;
        urlRoot = Play.application().configuration().getString("application.data.branding_public");
    }

    public AgencyBranding() {
    }
}
