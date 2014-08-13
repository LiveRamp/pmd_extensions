package com.liveramp.pmd_extensions;

import java.util.List;

import com.google.common.collect.Lists;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.StringProperty;

/**
 * Prevent a class from being instantiated or imported.  See example usage in example_ruleset.xml
 *
 * TODO prevent target from being used statically fully qualified
 */
public class BlacklistClassUsages extends AbstractJavaRule {
  private static final String LIST_NAME = "BlacklistClassUsages.BlacklistedClasses";

  public BlacklistClassUsages(){
    definePropertyDescriptor(new StringProperty(LIST_NAME, "List of classes to blacklist", "", 0));
  }

  @Override
  public void start(RuleContext ctx) {
    List<String> blacklistedClasses = Lists.newArrayList();
    Object prop = getProperty(getPropertyDescriptor(LIST_NAME));
    for (String className : prop.toString().split(",")) {
      blacklistedClasses.add(className.trim());
    }
    ctx.setAttribute(LIST_NAME, blacklistedClasses);

    super.start(ctx);
  }

  private static List<String> getFromContext(Object data){
    RuleContext ctx = (RuleContext)data;
    return (List<String>)ctx.getAttribute(LIST_NAME);
  }

  /**
   * Check whether the class was imported
   */
  @Override
  public Object visit(ASTImportDeclaration node, Object data) {

    String img = node.jjtGetChild(0).getImage();
    for (String blacklistedClass : getFromContext(data)) {
      if (img.startsWith(blacklistedClass)) {
        addViolation(data, node);
      }
    }

    return super.visit(node, data);
  }

  /**
   * Check if the class was instantiated
   */
  @Override
  public Object visit(ASTAllocationExpression node, Object data) {

    if (!(node.jjtGetChild(0) instanceof ASTClassOrInterfaceType)) {
      return super.visit(node, data);

    }

    for (String blockedClass : getFromContext(data)) {
      if (PmdHelper.isSubclass((ASTClassOrInterfaceType) node.jjtGetChild(0), blockedClass)){
        addViolation(data, node);
      }
    }

    return super.visit(node, data);
  }
}