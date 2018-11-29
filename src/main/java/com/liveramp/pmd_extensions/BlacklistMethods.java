package com.liveramp.pmd_extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.StringMultiProperty;

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
  private static final PropertyDescriptor<String[]> METHODS_LIST_DESCRIPTOR =
      new WhitespaceStrippingStringDescriptor(
          new StringMultiProperty(
              "BlacklistMethods.BlacklistedMethods",
              "List of methods to blacklist",
              new String[]{},
              0,
              ','));

  private final BlacklistedCallFactory blacklistedCallFactory = new BlacklistedCallFactory();

  public BlacklistMethods() {
    definePropertyDescriptor(METHODS_LIST_DESCRIPTOR);
  }

  @Override
  public Object visit(ASTVariableDeclaratorId node, Object data) {
    BlacklistMethodHelper.checkForMethods(
        node,
        data,
        getBlacklistedCalls(),
        Collections.<String>emptyList(),
        this
    );
    return super.visit(node, data);
  }

  /**
   * Check for static methods
   */
  @Override
  public Object visit(ASTPrimaryPrefix node, Object data) {
    if (BlacklistMethodHelper.checkStaticMethods(
        node,
        data,
        getBlacklistedCalls(),
        Collections.<String>emptyList(),
        this)) {
      return super.visit(node, data);
    }

    return super.visit(node, data);
  }

  private List<BlacklistedCall> getBlacklistedCalls() {
    List<BlacklistedCall> blacklistedCalls = new ArrayList<>();
    for (String blackListedCallsDeclaration : getProperty(METHODS_LIST_DESCRIPTOR)) {
      blacklistedCalls.add(blacklistedCallFactory.getBlacklistedCall(blackListedCallsDeclaration));
    }
    return blacklistedCalls;
  }

}