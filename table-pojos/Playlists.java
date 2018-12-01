package com.sample;


public class Playlists {

  private long id;
  private String name;
  private long position;
  private long hidden;
  private java.sql.Timestamp dateCreated;
  private java.sql.Timestamp dateModified;
  private long locked;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    this.position = position;
  }


  public long getHidden() {
    return hidden;
  }

  public void setHidden(long hidden) {
    this.hidden = hidden;
  }


  public java.sql.Timestamp getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(java.sql.Timestamp dateCreated) {
    this.dateCreated = dateCreated;
  }


  public java.sql.Timestamp getDateModified() {
    return dateModified;
  }

  public void setDateModified(java.sql.Timestamp dateModified) {
    this.dateModified = dateModified;
  }


  public long getLocked() {
    return locked;
  }

  public void setLocked(long locked) {
    this.locked = locked;
  }

}
