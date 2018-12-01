package com.sample;


public class TrackAnalysis {

  private long id;
  private long trackId;
  private String type;
  private String description;
  private String version;
  private String created;
  private String dataChecksum;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getTrackId() {
    return trackId;
  }

  public void setTrackId(long trackId) {
    this.trackId = trackId;
  }


  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }


  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }


  public String getDataChecksum() {
    return dataChecksum;
  }

  public void setDataChecksum(String dataChecksum) {
    this.dataChecksum = dataChecksum;
  }

}
