package com.liveramp.pmd_extensions;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symboltable.JavaNameOccurrence;
import net.sourceforge.pmd.lang.rule.properties.StringProperty;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;

/**
 * Mostly copied from UnusedLocalVariableRule
 */
public class SpecificUnusedLocalVariable extends AbstractJavaRule {

  private static final String CLASS_LIST = "SpecificUnusedLocalVariable.ForbiddenUnusedClasses";

  public SpecificUnusedLocalVariable() {
    definePropertyDescriptor(new StringProperty(CLASS_LIST, "List of which cannot be unused", "", 0));
  }

  @Override
  public void start(RuleContext ctx) {
    Set<String> classes = Sets.newHashSet();
    Object prop = getProperty(getPropertyDescriptor(CLASS_LIST));
    for (String reference : prop.toString().split(",")) {
      classes.add(reference.trim());
    }
    ctx.setAttribute(CLASS_LIST, classes);
    super.start(ctx);
  }

  public static Set<String> getCallsFromContext(Object data) {
    RuleContext ctx = (RuleContext)data;
    return (Set<String>)ctx.getAttribute(CLASS_LIST);
  }

  public Object visit(ASTLocalVariableDeclaration decl, Object data) {

    Set<String> specific = getCallsFromContext(data);

    for (int i = 0; i < decl.jjtGetNumChildren(); i++) {
      if (!(decl.jjtGetChild(i) instanceof ASTVariableDeclarator)) {
        continue;
      }

      ASTVariableDeclaratorId node = (ASTVariableDeclaratorId)decl.jjtGetChild(i).jjtGetChild(0);
      // TODO this isArray() check misses some cases
      // need to add DFAish code to determine if an array
      // is initialized locally or gotten from somewhere else
      Class<?> type = node.getType();
      if (!node.getNameDeclaration().isArray()
          && !actuallyUsed(node.getUsages())
          && type != null
          && specific.contains(type.getName())) {
        addViolation(data, node, node.getNameDeclaration().getImage());
      }
    }
    return data;
  }

  private boolean actuallyUsed(List<NameOccurrence> usages) {
    for (NameOccurrence occ : usages) {
      JavaNameOccurrence jocc = (JavaNameOccurrence) occ;
      if (jocc.isOnLeftHandSide()) {
        continue;
      } else {
        return true;
      }
    }
    return false;
  }

}
