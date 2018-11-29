package com.liveramp.pmd_extensions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This factory keeps a mapping of call declaration to constructed {@link BlacklistedCall} objects to avoid
 * re-parsing declarations.
 */
public class BlacklistedCallFactory {

  private final Map<String, BlacklistedCall> blacklistedCalls = new HashMap<>();

  public synchronized BlacklistedCall getBlacklistedCall(String callDeclaration) {
    if (blacklistedCalls.containsKey(callDeclaration)) {
      return blacklistedCalls.get(callDeclaration);
    }
    BlacklistedCall blacklistedCall = from(callDeclaration);
    blacklistedCalls.put(callDeclaration, blacklistedCall);
    return blacklistedCall;
  }

  public static BlacklistedCall from(String s) {
    String[] origAlt = s.split(";");

    if (origAlt.length != 2) {
      throw new RuntimeException("Blacklisting " + s + ": Must supply alternative");
    }

    String[] parts = origAlt[0].split(":");
    if (parts.length == 2){
      return new BlacklistedCall(parts[0], parts[1], origAlt[1]);
    }
    if (parts.length == 3){
      return new BlacklistedCall(parts[0], parts[1], Integer.parseInt(parts[2]), origAlt[1]);
    }
    throw new RuntimeException("Cannot parse method reference: "+ origAlt[0] +". Parts: " + Arrays.toString(parts));
  }
}
