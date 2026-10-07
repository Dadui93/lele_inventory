package com.lele.leleinventory.domain;

import java.time.Instant;

public final class UtcTimes {
  private UtcTimes() {}

  public static Instant now() {
    return Instant.now();
  }

  public static Instant orNow(Instant value) {
    return value != null ? value : Instant.now();
  }
}
