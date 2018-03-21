package com.liveramp.pmd_extensions;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.StringMultiProperty;

/**
 * Prevent a class from being instantiated or imported.  See example usage in example_ruleset.xml
 *
 * TODO prevent target from being used statically fully qualified
 */
public class BlacklistClassUsages extends AbstractJavaRule {

  private static final PropertyDescriptor<String[]> LIST_PROPERTY =
          new WhitespaceStrippingStringDescriptor(new StringMultiProperty(
                  "BlacklistClassUsages.BlacklistedClasses",
                  "List of classes to blacklist",
                  new String[]{},
                  0,
                  ','));


  public BlacklistClassUsages(){
    definePropertyDescriptor(LIST_PROPERTY);
  }

  /**
   * Check whether the class was imported
   */
  @Override
  public Object visit(ASTImportDeclaration node, Object data) {

    String img = node.jjtGetChild(0).getImage();
    for (String blacklistedClass : getProperty(LIST_PROPERTY)) {
      if (img.startsWith(blacklistedClass)) {
        addViolation(data, node, blacklistedClass);
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

    for (String blockedClass : getProperty(LIST_PROPERTY)) {
      if (PmdHelper.isSubclass((ASTClassOrInterfaceType) node.jjtGetChild(0), blockedClass)){
        addViolation(data, node);
      }
    }

    return super.visit(node, data);
  }
}