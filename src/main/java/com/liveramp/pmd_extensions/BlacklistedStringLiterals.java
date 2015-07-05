package com.liveramp.pmd_extensions;

import java.util.List;

import com.google.common.collect.Lists;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.StringProperty;
import org.apache.commons.lang3.StringUtils;

public class BlacklistedStringLiterals extends AbstractJavaRule {
  private static final String LIST_NAME = "BlacklistedStringLiterals.BlacklistedLiterals";

  public BlacklistedStringLiterals() {
    definePropertyDescriptor(new StringProperty(LIST_NAME, "List of literals to blacklist", "", 0));
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
  public Object visit(ASTLiteral node, Object data) {

    if(node.isStringLiteral()){
      String img = node.getImage();

      for (String literal : getFromContext(data)) {
        String imgStr = StringUtils.substring(img, 1, -1);
        if(imgStr.equals(literal)){
          addViolation(data, node);
        }
      }
    }

    return super.visit(node, data);
  }


}
