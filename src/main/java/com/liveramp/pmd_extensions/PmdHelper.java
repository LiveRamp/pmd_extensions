package com.liveramp.pmd_extensions;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.TypeNode;

import java.util.Arrays;
import java.util.Collection;

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
      return simpleName(clazz).equals(((Node) n).getImage()) || clazz.equals(((Node) n).getImage());
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

    Class<?> superC = type.getSuperclass();
    while (superC != null && !superC.equals(Object.class)) {
      String superCName = superC.getName();
      if (superCName.equals(clazz)) {
        return true;
      }
      superC = superC.getSuperclass();
    }
    return false;
  }
}
