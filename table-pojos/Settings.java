package com.sample;


public class Settings {

  private String name;
  private String value;
  private long locked;
  private long hidden;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }


  public long getLocked() {
    return locked;
  }

  public void setLocked(long locked) {
    this.locked = locked;
  }


  public long getHidden() {
    return hidden;
  }

  public void setHidden(long hidden) {
    this.hidden = hidden;
  }

}
