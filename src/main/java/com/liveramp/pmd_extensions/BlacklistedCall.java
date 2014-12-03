package com.liveramp.pmd_extensions;

public class BlacklistedCall {

  private final String ruleClass;
  private final String ruleClassName;
  private final String ruleMethodName;
  private final Integer argumentCount;

  private final String staticImported;
  private final String staticFull;

  private final String alternativeMethod;

  public BlacklistedCall(String ruleClass, String ruleMethodName, String alternativeMethod) {
    this(ruleClass, ruleMethodName, null, alternativeMethod);
  }

  public BlacklistedCall(String ruleClass, String ruleMethodName, Integer argumentCount, String alternativeMethod) {
    this.ruleClass = ruleClass;
    this.ruleMethodName = ruleMethodName;
    this.argumentCount  = argumentCount;
    this.alternativeMethod = alternativeMethod;

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

  public String getAlternativeMethod() {
    return alternativeMethod;
  }

  @Override
  public String toString() {
    return "BlacklistedCall{" +
        "ruleClass='" + ruleClass + '\'' +
        ", ruleClassName='" + ruleClassName + '\'' +
        ", ruleMethodName='" + ruleMethodName + '\'' +
        ", argumentCount=" + argumentCount +
        ", staticImported='" + staticImported + '\'' +
        ", staticFull='" + staticFull + '\'' +
        ", alternativeMethod='" + getAlternativeMethod() + '\'' +
        '}';
  }
}
