package com.liveramp.pmd_extensions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
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
      return simpleName(clazz).equals(((Node)n).getImage()) || clazz.equals(((Node)n).getImage());
    }

    if (type.getName().equals(clazz)) {
      return true;
    }

    Collection<String> implementors = Collections2.transform(Arrays.asList(type.getInterfaces()), new Function<Class, String>() {
      @Override
      public String apply(Class aClass) {
        return aClass.getName();
      }
    });

    if (implementors.contains(clazz)) {
      return true;
    }

    List<Class<?>> supers = Lists.<Class<?>>newArrayList(type.getSuperclass());
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
          System.out.println("comparing "+declaration.getType()+" "+mrParentClass);
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
