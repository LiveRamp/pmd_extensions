package com.liveramp.pmd_extensions;

public class BlacklistedCall {

  private final String ruleClass;
  private final String ruleClassName;
  private final String ruleMethodName;
  private final Integer argumentCount;

  private final String staticImported;
  private final String staticFull;

  public BlacklistedCall(String ruleClass, String ruleMethodName) {
    this(ruleClass, ruleMethodName, null);
  }

  public BlacklistedCall(String ruleClass, String ruleMethodName, Integer argumentCount) {
    this.ruleClass = ruleClass;
    this.ruleMethodName = ruleMethodName;
    this.argumentCount  = argumentCount;

    String[] parts = ruleClass.split("\\.");
    if(parts.length < 1){
      throw new RuntimeException("Unrecognized classname: "+ruleClass);
    }
    ruleClassName = parts[parts.length-1];

    this.staticImported = ruleClassName+"."+ruleMethodName;
    this.staticFull = ruleClass+"."+ruleMethodName;

  }

  public String getRuleClass() {
    return ruleClass;
  }

  public String getRuleMethodName() {
    return ruleMethodName;
  }

  public Integer getArgumentCount() {
    return argumentCount;
  }

  public String getRuleClassName() {
    return ruleClassName;
  }

  public String getImportedStaticImage() {
    return staticImported;
  }

  public String getFullStaticImage(){
    return staticFull;
  }
}
