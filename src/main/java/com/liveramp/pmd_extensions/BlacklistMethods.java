package com.liveramp.pmd_extensions;

import java.util.Collections;
import java.util.List;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
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
  private static final String LIST_NAME = "BlacklistMethods.BlacklistedMethods";

  public BlacklistMethods() {
    definePropertyDescriptor(new StringProperty(LIST_NAME, "List of methods to blacklist", "", 0));
  }


  @Override
  public void start(RuleContext ctx) {
    BlacklistMethodHelper.setContext(LIST_NAME, null, ctx, this);
    super.start(ctx);
  }

  @Override
  public Object visit(ASTVariableDeclaratorId node, Object data) {
    BlacklistMethodHelper.checkForMethods(node, data,
        BlacklistMethodHelper.getCallsFromContext(data, LIST_NAME),
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
    List<BlacklistedCall> blockedCalls = BlacklistMethodHelper.getCallsFromContext(data, LIST_NAME);

    if (BlacklistMethodHelper.checkStaticMethods(node, data, blockedCalls, Collections.<String>emptyList(), this)) {
      return super.visit(node, data);
    }

    return super.visit(node, data);
  }

}