package com.sample;


public class TrackLocations {

  private long id;
  private String location;
  private String filename;
  private String directory;
  private long filesize;
  private long fsDeleted;
  private long needsVerification;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }


  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }


  public String getDirectory() {
    return directory;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }


  public long getFilesize() {
    return filesize;
  }

  public void setFilesize(long filesize) {
    this.filesize = filesize;
  }


  public long getFsDeleted() {
    return fsDeleted;
  }

  public void setFsDeleted(long fsDeleted) {
    this.fsDeleted = fsDeleted;
  }


  public long getNeedsVerification() {
    return needsVerification;
  }

  public void setNeedsVerification(long needsVerification) {
    this.needsVerification = needsVerification;
  }

}
