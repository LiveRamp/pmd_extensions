package com.liveramp.pmd_extensions;

import java.util.List;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.StringProperty;

public class BlacklistScopedMethods extends AbstractJavaRule {
  private static final String LIST_NAME = "BlacklistScopedMethods.BlacklistedMethods";
  private static final String CLASS_LIST = "BlacklistScopedMethods.ClassesToInspect";

  public BlacklistScopedMethods() {
    definePropertyDescriptor(new StringProperty(LIST_NAME, "List of methods to blacklist", "", 0));
    definePropertyDescriptor(new StringProperty(CLASS_LIST, "List of classes to inspect", "", 0));
  }

  @Override
  public void start(RuleContext ctx) {
    BlacklistMethodHelper.setContext(LIST_NAME, CLASS_LIST, ctx, this);
    super.start(ctx);
  }

  public Object visit(ASTVariableDeclaratorId node, Object data) {
    BlacklistMethodHelper.checkForMethods(node, data,
        BlacklistMethodHelper.getCallsFromContext(data, LIST_NAME),
        BlacklistMethodHelper.getClassesFromContext(data, CLASS_LIST),
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
    List<String> affectedClasses = BlacklistMethodHelper.getClassesFromContext(data, CLASS_LIST);

    if (BlacklistMethodHelper.checkStaticMethods(node, data, blockedCalls, affectedClasses, this)) {
      return super.visit(node, data);
    }

    return super.visit(node, data);
  }

}
