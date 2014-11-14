package com.liveramp.pmd_extensions;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.Comment;
import net.sourceforge.pmd.lang.java.rule.comments.AbstractCommentRule;
import net.sourceforge.pmd.lang.rule.properties.IntegerProperty;

/**
 * A rule that warns on TODOs
 */
public class WarnOnTodo extends AbstractCommentRule {
  public static final String TODO = "todo";
  public static final IntegerProperty MAX_TODOS = new IntegerProperty("maxTodos", "Maximum number of Todos", 1, 100, 10, 2.0f);

  public WarnOnTodo() {
    
  }
  
  @Override
  public Object visit(ASTCompilationUnit unit, Object data) {
    int numTodos = 0;
    for (Comment comment : unit.getComments()) {
      if (comment.getImage().toLowerCase().contains(TODO)) {
        addViolationWithMessage(data, unit, ": TODO present", comment.getBeginLine(), comment.getEndLine());
        numTodos++;
      };
    }

    if (numTodos > getProperty(MAX_TODOS)) {
      addViolationWithMessage(data, unit, ": Too many TODOs");
    }

    return super.visit(unit, data);
  }
}
