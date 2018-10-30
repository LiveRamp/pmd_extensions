package com.liveramp.pmd_extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.properties.StringProperty;

//  TODO this is horribly non-generic, but a first shot at catching call chains
public class BlacklistCallChain extends AbstractJavaRule {

  private static final String CALL_CHAIN = "BlacklistCallChain.CallChain";
  private static final String CLASS_LIST = "BlacklistCallChain.ClassesToInspect";

  public BlacklistCallChain() {
    definePropertyDescriptor(new StringProperty(CALL_CHAIN, "List of calls to blacklist", "", 0));
    definePropertyDescriptor(new StringProperty(CLASS_LIST, "List of classes to check", "", 0));
  }

  @Override
  public void start(RuleContext ctx) {
    Set<List<String>> chains = new HashSet<>();
    Object prop = getProperty(getPropertyDescriptor(CALL_CHAIN));
    for (String reference : prop.toString().split(",")) {
      chains.add(Arrays.asList(reference.trim().split("\\.")));
    }
    ctx.setAttribute(CALL_CHAIN, chains);
    BlacklistMethodHelper.setContext(null, CLASS_LIST, ctx, this);

    super.start(ctx);
  }

  public static Set<List<String>> getCallsFromContext(Object data){
    RuleContext ctx = (RuleContext)data;
    return (Set<List<String>>)ctx.getAttribute(CALL_CHAIN);
  }

  @Override
  public Object visit(ASTPrimaryExpression node, Object data) {
    Set<List<String>> badCalls = getCallsFromContext(data);
    List<String> affectedClasses = BlacklistMethodHelper.getClassesFromContext(data, CLASS_LIST);

    List<String> parts = new ArrayList<>();
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      List<String> image = image(node.jjtGetChild(i));
      if(!image.isEmpty()){
        parts.addAll(image);
      }
    }

    if(badCalls.contains(parts)){
      if(affectedClasses.isEmpty()){
        addViolation(data, node);
      }else{
        PmdHelper.checkParentClasses(node, data, affectedClasses, node.jjtGetParent(), this);
      }
    }

    return super.visit(node, data);
  }

  private List<String> image(Node node){
    if(node instanceof ASTPrimaryPrefix){
      ASTPrimaryPrefix prefix = (ASTPrimaryPrefix) node;

      List<String> results = new ArrayList<>();
      for (int i = 0; i < prefix.jjtGetNumChildren(); i++) {
        results.addAll(image(prefix.jjtGetChild(i)));
      }
      return results;

    }else if(node instanceof ASTPrimarySuffix){
      if(node.getImage() != null) {
        return new ArrayList<>(Arrays.asList(node.getImage().split("\\.")));
      }
    }else if(node instanceof ASTName){
      if(node.getImage() != null) {
        return new ArrayList<>(Arrays.asList(node.getImage().split("\\.")));
      }
    }
    return new ArrayList<>();
  }

}
