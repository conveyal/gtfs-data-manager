package models;

import java.io.Serializable;

/**
 * Created by demory on 3/8/15.
 */

public class OtpBuildConfig implements Serializable {
    public static final long serialVersionUID = -2210046696074999545l;

    public Boolean fetchElevationUS;

    public Boolean stationTransfers;

    public Double subwayAccessTime;

    /** Currently only supports no-configuration fares, e.g. New York or San Francisco */
    public String fares;
}
