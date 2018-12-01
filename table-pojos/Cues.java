package com.sample;


public class Cues {

  private long id;
  private long trackId;
  private long type;
  private long position;
  private long length;
  private long hotcue;
  private String label;
  private long color;


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


  public long getType() {
    return type;
  }

  public void setType(long type) {
    this.type = type;
  }


  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    this.position = position;
  }


  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }


  public long getHotcue() {
    return hotcue;
  }

  public void setHotcue(long hotcue) {
    this.hotcue = hotcue;
  }


  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }


  public long getColor() {
    return color;
  }

  public void setColor(long color) {
    this.color = color;
  }

}
