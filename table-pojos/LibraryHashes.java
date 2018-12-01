package com.sample;


public class LibraryHashes {

  private String directoryPath;
  private long hash;
  private long directoryDeleted;
  private long needsVerification;


  public String getDirectoryPath() {
    return directoryPath;
  }

  public void setDirectoryPath(String directoryPath) {
    this.directoryPath = directoryPath;
  }


  public long getHash() {
    return hash;
  }

  public void setHash(long hash) {
    this.hash = hash;
  }


  public long getDirectoryDeleted() {
    return directoryDeleted;
  }

  public void setDirectoryDeleted(long directoryDeleted) {
    this.directoryDeleted = directoryDeleted;
  }


  public long getNeedsVerification() {
    return needsVerification;
  }

  public void setNeedsVerification(long needsVerification) {
    this.needsVerification = needsVerification;
  }

}
