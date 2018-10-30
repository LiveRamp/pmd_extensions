package com.liveramp.pmd_extensions;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.StringProperty;

public class BlacklistedStringLiterals extends AbstractJavaRule {
  private static final String LIST_NAME = "BlacklistedStringLiterals.BlacklistedLiterals";

  public BlacklistedStringLiterals() {
    definePropertyDescriptor(new StringProperty(LIST_NAME, "List of literals to blacklist", "", 0));
  }

  @Override
  public void start(RuleContext ctx) {
    List<String> blacklistedClasses = new ArrayList<>();
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
  public Object visit(ASTLiteral node, Object data) {

    if(node.isStringLiteral()){
      String img = node.getImage();

      if (img.length() > 2) {
        String imgStr = img.substring(1, img.length() - 1);

        for (String literal : getFromContext(data)) {
          if(imgStr.equals(literal)){
            addViolation(data, node, literal);
          }
        }
      }
    }

    return super.visit(node, data);
  }


}
