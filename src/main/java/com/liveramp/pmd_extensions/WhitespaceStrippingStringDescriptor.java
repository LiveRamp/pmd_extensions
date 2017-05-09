package com.liveramp.pmd_extensions;

import java.util.Map;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.Rule;

public class WhitespaceStrippingStringDescriptor implements PropertyDescriptor<String[]> {

  private final PropertyDescriptor<String[]> internal;

  public WhitespaceStrippingStringDescriptor(PropertyDescriptor<String[]> internal){
    this.internal = internal;
  }

  @Override
  public String name() {
    return internal.name();
  }

  @Override
  public String description() {
    return internal.description();
  }

  @Override
  public Class<String[]> type() {
    return internal.type();
  }

  @Override
  public boolean isMultiValue() {
    return internal.isMultiValue();
  }

  @Override
  public String[] defaultValue() {
    return internal.defaultValue();
  }

  @Override
  public boolean isRequired() {
    return internal.isRequired();
  }

  @Override
  public String errorFor(Object value) {
    return internal.errorFor(value);
  }

  @Override
  public float uiOrder() {
    return internal.uiOrder();
  }

  @Override
  public String[] valueFrom(String propertyString) throws IllegalArgumentException {
    return internal.valueFrom(propertyString.replaceAll("\\s+",""));
  }

  @Override
  public String asDelimitedString(String[] value) {
    return internal.asDelimitedString(value);
  }

  @Override
  public Object[][] choices() {
    return internal.choices();
  }

  @Override
  public String propertyErrorFor(Rule rule) {
    return internal.propertyErrorFor(rule);
  }

  @Override
  public char multiValueDelimiter() {
    return internal.multiValueDelimiter();
  }

  @Override
  public int preferredRowCount() {
    return internal.preferredRowCount();
  }

  @Override
  public Map<String, String> attributeValuesById() {
    return internal.attributeValuesById();
  }

  @Override
  public int compareTo(PropertyDescriptor<?> o) {
    return internal.compareTo(o);
  }
}