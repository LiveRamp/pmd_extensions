package com.liveramp.pmd_extensions;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.Comment;
import net.sourceforge.pmd.lang.java.rule.comments.AbstractCommentRule;
import net.sourceforge.pmd.lang.rule.properties.BooleanProperty;
import net.sourceforge.pmd.lang.rule.properties.IntegerProperty;
import net.sourceforge.pmd.lang.rule.properties.StringMultiProperty;

/**
 * A rule that warns on TODOs
 */
public class WarnOnTodo extends AbstractCommentRule {
  private static final IntegerProperty MAX_TODOS = new IntegerProperty("maxTodos", "Maximum number of Todos", 1, 100, 10, 2.0f);
  private static final BooleanProperty REPORT_INDIVIDUALLY = new BooleanProperty("reportIndividually", "Set to true if each violation should be marked", false, 2.0f);
  private static final StringMultiProperty TODO_STRINGS = new StringMultiProperty("todoStrings", "The strings that count as todos", new String[]{"todo", "fixme"}, 2.0f, '|');

  public WarnOnTodo() {
    definePropertyDescriptor(MAX_TODOS);
    definePropertyDescriptor(REPORT_INDIVIDUALLY);
    definePropertyDescriptor(TODO_STRINGS);
  }
  
  @Override
  public Object visit(ASTCompilationUnit unit, Object data) {
    int numTodos = 0;
    for (Comment comment : unit.getComments()) {
      String commentString = comment.getImage();
      final String[] todoStrings = getProperty(TODO_STRINGS);
      for (String todoString : todoStrings) {
        if (commentString.toLowerCase().contains(todoString)) {
          if (getProperty(REPORT_INDIVIDUALLY)) {
            addViolationWithMessage(data, unit, "Fix or remove TODO: \"" + commentString+"\"", comment.getBeginLine(), comment.getEndLine());
          }
          numTodos++;
        }
      }
    }

    if (numTodos > getProperty(MAX_TODOS)) {
      addViolationWithMessage(data, unit, "Too many TODOs. Fix previous ones before adding more.");
    }

    return super.visit(unit, data);
  }
}
