package com.liveramp.pmd_extensions;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.StringProperty;

/**
 * See example configuration in example_ruleset.xml
 */
public class NoLoggingInClasses extends AbstractJavaRule {
  private static final String CLASS_LIST = "NoLoggingInClasses.ClassesToInspect";

  //  TODO figure out if we can catch a Logger in here...
  private static final Set<String> LOG_IMAGES = Sets.newHashSet(
      "System.out.println",
      "System.out.print",
      "System.err.println",
      "System.err.print"
  );

  public NoLoggingInClasses(){
    definePropertyDescriptor(new StringProperty(CLASS_LIST, "List of classes in which to ban logging", "", 0));
  }

  @Override
  public void start(RuleContext ctx) {
    List<String> blacklistedClasses = Lists.newArrayList();
    Object prop = getProperty(getPropertyDescriptor(CLASS_LIST));
    for (String className : prop.toString().split(",")) {
      blacklistedClasses.add(className.trim());
    }
    ctx.setAttribute(CLASS_LIST, blacklistedClasses);

    super.start(ctx);
  }

  private static List<String> getFromContext(Object data){
    RuleContext ctx = (RuleContext)data;
    return (List<String>)ctx.getAttribute(CLASS_LIST);
  }

  @Override
  public Object visit(ASTPrimaryPrefix node, Object data) {
    if (node.jjtGetNumChildren() == 0) {
      return super.visit(node, data);
    }

    Node node1 = node.jjtGetChild(0);
    String image = node1.getImage();

    if (image == null) {
      return super.visit(node, data);
    }

    if (LOG_IMAGES.contains(image)) {
      List<String> relevantClasses = getFromContext(data);
      PmdHelper.checkParentClasses(node, data, relevantClasses, node.jjtGetParent(), this);
    }

    return super.visit(node, data);
  }

}