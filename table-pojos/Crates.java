package com.sample;


public class Crates {

  private long id;
  private String name;
  private long count;
  private long show;
  private long locked;
  private long autodjSource;


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


  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }


  public long getShow() {
    return show;
  }

  public void setShow(long show) {
    this.show = show;
  }


  public long getLocked() {
    return locked;
  }

  public void setLocked(long locked) {
    this.locked = locked;
  }


  public long getAutodjSource() {
    return autodjSource;
  }

  public void setAutodjSource(long autodjSource) {
    this.autodjSource = autodjSource;
  }

}
