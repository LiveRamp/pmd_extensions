package com.liveramp.pmd_extensions;

import java.util.List;

import com.google.common.collect.Lists;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.TypeNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.rule.properties.StringProperty;

/**
 * Fail any build which uses certain methods on particular classes.  Example configuration in rule set:
 *
        <property name="BlacklistedMethods" value="
              java.lang.String:getBytes:0,
              java.nio.ByteBuffer:array,
           "/>

 *   Here, the getBytes method on String with arity 0 is banned, and the array method on ByteBuffer (with any arity)
 *   is banned
 */
public class BlacklistMethods extends AbstractJavaRule {
  private static final String LIST_NAME = "BlacklistedMethods";

  public BlacklistMethods() {
    definePropertyDescriptor(new StringProperty(LIST_NAME, "List of methods to blacklist", "", 0));
  }

  @Override
  public void start(RuleContext ctx) {
    List<BlacklistedCall> blockedCalls = Lists.newArrayList();
    Object prop = getProperty(getPropertyDescriptor(LIST_NAME));
    for (String reference : prop.toString().split(",")) {
      blockedCalls.add(parseRef(reference.trim()));
    }
    ctx.setAttribute(LIST_NAME, blockedCalls);
  }

  private BlacklistedCall parseRef(String s){
    String[] parts = s.split(":");
    if(parts.length == 2){
      return new BlacklistedCall(parts[0], parts[1]);
    }
    if(parts.length == 3){
      return new BlacklistedCall(parts[0], parts[1], Integer.parseInt(parts[2]));
    }
    throw new RuntimeException("Cannot parse method reference: "+s+"!");
  }

  public Object visit(ASTVariableDeclaratorId node, Object data) {
    List<BlacklistedCall> blockedCalls = getFromContext(data);

    for (BlacklistedCall call : blockedCalls) {
      String ruleClass = call.getRuleClass();

      if (PmdHelper.isSubclass(node, ruleClass)) {
        boolean isArray = node.isArray();
        for (NameOccurrence occ : node.getUsages()) {
          NameOccurrence qualifier = occ.getNameForWhichThisIsAQualifier();

          if (qualifier != null) {
            String ruleMethodName = call.getRuleMethodName();

            if (!isArray && qualifier.getImage().equals(ruleMethodName)) {
              Integer argumentCount = call.getArgumentCount();

              if (argumentCount == null || argumentCount == qualifier.getArgumentCount()) {
                addViolation(data, occ.getLocation(),
                    "Blacklisted method call: "+ ruleClass +"."+ ruleMethodName +"{"+ argumentCount +"}");
              }
            }
            //  I don't know what this case catches...
            else if (isArray
                && qualifier.getLocation() != null
                && !ASTName.class.equals(qualifier.getLocation().getClass())
                && qualifier.getImage().equals(ruleMethodName)) {
              addViolation(data, occ.getLocation());
            }
          }
        }
      }
    }
    return data;
  }

  private static List<BlacklistedCall> getFromContext(Object data){
    RuleContext ctx = (RuleContext)data;
    return (List<BlacklistedCall>)ctx.getAttribute(LIST_NAME);
  }

  /**
   * Check for static methods
   */
  @Override
  public Object visit(ASTPrimaryPrefix node, Object data) {

    if (node.jjtGetNumChildren() == 0) {
      return data;
    }

    Node node1 = node.jjtGetChild(0);
    String image = node1.getImage();

    if(image == null){
      return data;
    }

    for (BlacklistedCall blacklistedCall : getFromContext(data)) {
      if (node1 instanceof TypeNode) {
        TypeNode tn = (TypeNode) node1;
        if (PmdHelper.isSubclass(tn, blacklistedCall.getRuleClass())) {
          if (image.equals(blacklistedCall.getImportedStaticImage())
              || image.equals(blacklistedCall.getFullStaticImage())) {
            addViolation(data, node);
          }
        }
      }
    }

    return data;
  }
}