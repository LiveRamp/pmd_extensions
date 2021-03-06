package com.liveramp.pmd_extensions;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.TypeNode;
import net.sourceforge.pmd.lang.java.symboltable.JavaNameOccurrence;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;

public class BlacklistMethodHelper {

  public static void checkForMethods(ASTVariableDeclaratorId node, Object data, List<BlacklistedCall> blockedCalls, List<String> affectedClasses, AbstractJavaRule rule) {
    for (BlacklistedCall call : blockedCalls) {
      String ruleClass = call.getRuleClass();

      if (PmdHelper.isSubclass(node, ruleClass)) {
        boolean isArray = node.isArray();
        for (NameOccurrence occ : node.getUsages()) {
          JavaNameOccurrence jocc = (JavaNameOccurrence) occ;
          NameOccurrence qualifier = jocc.getNameForWhichThisIsAQualifier();
          JavaNameOccurrence jqualifier = (JavaNameOccurrence) qualifier;

          if (qualifier != null) {
            String ruleMethodName = call.getRuleMethodName();

            if (!isArray && qualifier.getImage().equals(ruleMethodName)) {
              Integer argumentCount = call.getArgumentCount();

              if (argumentCount == null || argumentCount == jqualifier.getArgumentCount()) {

                if(affectedClasses.isEmpty()){
                  markViolation(rule, data, occ.getLocation(), call);
                }else{
                  PmdHelper.checkParentClasses(node, data, affectedClasses, occ.getLocation(), rule);
                }

              }
            }
            //  I don't know what this case catches...
            else if (isArray
                && qualifier.getLocation() != null
                && !ASTName.class.equals(qualifier.getLocation().getClass())
                && qualifier.getImage().equals(ruleMethodName)) {
              markViolation(rule, data, occ.getLocation(), call);
            }
          }
        }
      }
    }
  }

  private static void markViolation(AbstractJavaRule rule, Object data, Node location, BlacklistedCall call) {
    rule.addViolationWithMessage(
        data,
        location,
        rule.getMessage() + " Suggested alternative: " + call.getAlternativeMethod());
  }

  public static void setContext(String methodProp, String classProp, RuleContext ctx, AbstractJavaRule rule) {

    if(methodProp != null) {
      List<BlacklistedCall> blockedCalls = new ArrayList<>();
      Object prop = rule.getProperty(rule.getPropertyDescriptor(methodProp));
      for (String reference : prop.toString().split(",")) {
        blockedCalls.add(BlacklistedCallFactory.from(reference.trim()));
      }
      ctx.setAttribute(methodProp, blockedCalls);
    }

    if(classProp != null) {
      List<String> blacklistedClasses = new ArrayList<>();
      Object classes = rule.getProperty(rule.getPropertyDescriptor(classProp));
      if (classes != null) {
        for (String className : classes.toString().split(",")) {
          blacklistedClasses.add(className.trim());
        }
      }
      ctx.setAttribute(classProp, blacklistedClasses);
    }

  }

  public static boolean checkStaticMethods(ASTPrimaryPrefix node, Object data, List<BlacklistedCall> blockedCalls, List<String> affectedClasses, AbstractJavaRule rule) {
    if (node.jjtGetNumChildren() == 0) {
      return true;
    }

    Node node1 = node.jjtGetChild(0);
    String image = node1.getImage();

    if(image == null){
      return true;
    }

    for (BlacklistedCall blacklistedCall : blockedCalls) {
      if (node1 instanceof TypeNode) {
        TypeNode tn = (TypeNode) node1;
        if (PmdHelper.isSubclass(tn, blacklistedCall.getRuleClass())) {
          if (image.equals(blacklistedCall.getImportedStaticImage())
              || image.equals(blacklistedCall.getFullStaticImage())) {
            markViolation(rule, data, node, blacklistedCall);
            if (!affectedClasses.isEmpty()) {
              PmdHelper.checkParentClasses(node, data, affectedClasses, node.jjtGetParent(), rule);
            }
          }
        }
      }
    }
    return false;
  }


  public static List<BlacklistedCall> getCallsFromContext(Object data, String listName){
    RuleContext ctx = (RuleContext)data;
    return (List<BlacklistedCall>)ctx.getAttribute(listName);
  }

  public static List<String> getClassesFromContext(Object data, String classList){
    RuleContext ctx = (RuleContext)data;
    return (List<String>)ctx.getAttribute(classList);
  }

}
