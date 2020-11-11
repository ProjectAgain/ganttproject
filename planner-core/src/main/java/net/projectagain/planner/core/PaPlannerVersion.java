package net.projectagain.planner.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaPlannerVersion {
  private static String version;
  private static String buildDate;
  private static String buildNumber;

  public PaPlannerVersion(
    @Value("${planner.version.semversion}") String semVersion,
    @Value("${planner.version.build-date}") final String buildDate,
    @Value("${planner.version.build-number}") final String buildNumber

  ) {
    PaPlannerVersion.version = semVersion;
    PaPlannerVersion.buildDate = buildDate;
    PaPlannerVersion.buildNumber = buildNumber;
  }

  public String getVersion() {
    return version;
  }

  public String getBuildDate() {
    return buildDate;
  }

  public String getBuildNumber() {
    return buildNumber;
  }
}
