package com.pinterest.cmp.cmdb.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * This class is used as a POJO to write to elastic search.
 * EsInstanceCountRecord represents a instance count record for
 * a specific zone + instance type + date combination
 */
public class EsInstanceCountRecord implements EsDocument {

  @JsonProperty("region")
  private String region;

  @JsonProperty("instance_type")
  private String instanceType;

  @JsonProperty("reserved_count")
  private int reservedInstancesCount;

  @JsonProperty("active_count")
  private int activeInstancesCount;

  @JsonProperty("spot_count")
  private int spotInstancesCount;

  @JsonProperty("ondemand_count")
  private int ondemandInstancesCount;

  @JsonProperty("unused_count")
  private int unusedInstancesCount;

  @JsonProperty("date_time")
  private Date dateTime;

  @JsonIgnore
  private String id;

  @JsonIgnore
  private long version;

  public EsInstanceCountRecord() {}

  public EsInstanceCountRecord(String region, String instanceType,
                               int reservedInstancesCount,
                               int activeInstancesCount, int spotInstancesCount,
                               int ondemandInstancesCount, int unusedInstancesCount,
                               Date dateTime) {

    this.region = region;
    this.instanceType = instanceType;
    this.reservedInstancesCount = reservedInstancesCount;
    this.activeInstancesCount = activeInstancesCount;
    this.spotInstancesCount = spotInstancesCount;
    this.ondemandInstancesCount = ondemandInstancesCount;
    this.unusedInstancesCount = unusedInstancesCount;
    this.dateTime = dateTime;
  }

  @Override
  public String toString() {
    return String.format("%s %s %d %d %d %d %d %s",
        region, instanceType,
        reservedInstancesCount, activeInstancesCount,
        spotInstancesCount, ondemandInstancesCount,
        unusedInstancesCount, dateTime.toString());
  }

  public Date getDateTime() {
    return dateTime;
  }

  public void setDateTime(Date dateTime) {
    this.dateTime = dateTime;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String availabilityZone) {
    this.region = availabilityZone;
  }

  public String getInstanceType() {
    return instanceType;
  }

  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }

  public int getReservedInstancesCount() {
    return reservedInstancesCount;
  }

  public void setReservedInstancesCount(int reservedInstancesCount) {
    this.reservedInstancesCount = reservedInstancesCount;
  }

  public int getActiveInstancesCount() {
    return activeInstancesCount;
  }

  public void setActiveInstancesCount(int activeInstancesCount) {
    this.activeInstancesCount = activeInstancesCount;
  }

  public int getSpotInstancesCount() {
    return spotInstancesCount;
  }

  public void setSpotInstancesCount(int spotInstancesCount) {
    this.spotInstancesCount = spotInstancesCount;
  }

  public int getOndemandInstancesCount() {
    return ondemandInstancesCount;
  }

  public void setOndemandInstancesCount(int ondemandInstancesCount) {
    this.ondemandInstancesCount = ondemandInstancesCount;
  }

  public int getUnusedInstancesCount() {
    return unusedInstancesCount;
  }

  public void setUnusedInstancesCount(int unusedInstancesCount) {
    this.unusedInstancesCount = unusedInstancesCount;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public long getVersion() {
    return version;
  }

  @Override
  public void setVersion(long version) {
    this.version = version;
  }
}
