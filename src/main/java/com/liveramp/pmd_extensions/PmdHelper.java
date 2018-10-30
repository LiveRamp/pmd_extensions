package com.liveramp.pmd_extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.TypeNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

public class PmdHelper {
  private static String simpleName(String className) {
    String[] parts = className.split("\\.");
    if (parts.length == 0) {
      return null;
    }

    return parts[parts.length - 1];
  }

  public static boolean isSubclass(TypeNode n, String clazz) {

    Class type = n.getType();
    if (type == null) {
      String image = ((Node)n).getImage();
      return simpleName(clazz).equals(image) || clazz.equals(image);
    }

    if (type.getName().equals(clazz)) {
      return true;
    }

    Collection<String> implementors = Stream.of(type.getInterfaces())
        .map(Class::getName)
        .collect(Collectors.toList());

    if (implementors.contains(clazz)) {
      return true;
    }

    List<Class<?>> supers = new ArrayList<>();
    supers.add(type.getSuperclass());
    supers.addAll(Arrays.<Class<?>>asList(type.getInterfaces()));

    for (Class<?> superC : supers) {
      while (superC != null && !superC.equals(Object.class)) {
        String superCName = superC.getName();
        if (superCName.equals(clazz)) {
          return true;
        }
        superC = superC.getSuperclass();
      }
    }

    return false;
  }

  public static void checkParentClasses(Node node, Object data, List<String> parentClasses, Node parent, AbstractJavaRule rule) {
    boolean inClass = false;
    boolean inConstructor = false;

    while (parent != null) {

      //  figure out if any of the parent classes extend the targets
      if (parent instanceof ASTClassOrInterfaceDeclaration) {
        ASTClassOrInterfaceDeclaration declaration = (ASTClassOrInterfaceDeclaration)parent;

        for (String mrParentClass : parentClasses) {
          if (PmdHelper.isSubclass(declaration, mrParentClass)) {
            inClass = true;
          }
        }
      }

      //  let it slide if it's in a constructor (called infrequently)
      if (parent instanceof ASTConstructorDeclaration) {
        inConstructor = true;
      }

      parent = parent.jjtGetParent();
    }

    if (inClass && !inConstructor) {
      rule.addViolation(data, node);
    }
  }

}
