package com.liveramp.pmd_extensions;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.ScopedNode;

import com.liveramp.commons.collections.map.MapBuilder;

public class BlacklistLossyIncrementCast extends AbstractJavaRule {

  private static final Map<String, Integer> TYPE_TO_BITS = new MapBuilder<String, Integer>()
      .put("long", 64)
      .put("int", 32)
      .put("short", 16)
      .put("byte", 8)
      .get();

  private static final Set<String> LOSSY_ASSIGNMENTS = Sets.newHashSet("+=", "-=", "*=", "^=", "|=", "/=", "%=");

  @Override
  public Object visit(ASTStatementExpression node, Object data) {

    if (node.jjtGetNumChildren() == 3) {
      Node assignment = node.jjtGetChild(1);

      if (LOSSY_ASSIGNMENTS.contains(assignment.getImage())) {

        String lhsType = getVariableType(node.jjtGetChild(0));
        String rhsType = getVariableType(node.jjtGetChild(2).jjtGetChild(0));

        Integer lhsBits = TYPE_TO_BITS.get(lhsType);
        Integer rhsBits = TYPE_TO_BITS.get(rhsType);

        if (lhsBits != null && rhsBits != null) {
          if (lhsBits < rhsBits) {
            addViolation(data, node);
          }
        }
      }

    }

    return super.visit(node, data);
  }

  private String getVariableType(Node lhs) {

    Node prefix = lhs.jjtGetChild(0);
    if (prefix instanceof ASTPrimaryPrefix) {
      Node name = prefix.jjtGetChild(0);
      if (name instanceof ASTName) {
        ASTName nameNode = (ASTName)name;
        NameDeclaration declaration = nameNode.getNameDeclaration();

        if (declaration != null) {
          ScopedNode declNode = declaration.getNode();

          if (declNode instanceof ASTVariableDeclaratorId) {
            ASTVariableDeclaratorId declarator = (ASTVariableDeclaratorId)declNode;
            return declarator.getTypeNode().getTypeImage();
          } else if (declNode instanceof ASTMethodDeclarator) {
            ASTMethodDeclarator declarator = (ASTMethodDeclarator)declNode;
            Node parent = declarator.jjtGetParent();

            if (parent instanceof ASTMethodDeclaration) {
              ASTMethodDeclaration methodParent = (ASTMethodDeclaration)parent;
              //  ResultType.Type.X.image
              return methodParent.getResultType().jjtGetChild(0).jjtGetChild(0).getImage();

            }

          }
        }
      }
    }

    return null;
  }

}
