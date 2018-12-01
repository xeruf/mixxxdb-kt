package com.sample;


public class PlaylistTracks {

  private long id;
  private long playlistId;
  private long trackId;
  private long position;
  private String plDatetimeAdded;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(long playlistId) {
    this.playlistId = playlistId;
  }


  public long getTrackId() {
    return trackId;
  }

  public void setTrackId(long trackId) {
    this.trackId = trackId;
  }


  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    this.position = position;
  }


  public String getPlDatetimeAdded() {
    return plDatetimeAdded;
  }

  public void setPlDatetimeAdded(String plDatetimeAdded) {
    this.plDatetimeAdded = plDatetimeAdded;
  }

}
