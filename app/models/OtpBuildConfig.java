package models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * Created by demory on 3/8/15.
 */

public class OtpBuildConfig implements Serializable {

    public Boolean fetchElevationUS;

    public Boolean stationTransfers;

    public Double subwayAccessTime;
}
