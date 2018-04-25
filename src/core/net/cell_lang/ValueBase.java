package net.cell_lang;

import java.io.StringWriter;
import java.io.Writer;


abstract class ValueBase implements Value {
  public boolean isSymb() {
    return false;
  }

  public boolean isInt() {
    return false;
  }

  public boolean isFloat() {
    return false;
  }

  public boolean isSeq() {
    return false;
  }

  public boolean isSet() {
    return false;
  }

  public boolean isBinRel() {
    return false;
  }

  public boolean isTernRel() {
    return false;
  }

  public boolean isTagged() {
    return false;
  }

  public String asSymb() {
    throw new UnsupportedOperationException();
  }

  public long asLong() {
    throw new UnsupportedOperationException();
  }

  public double asDouble() {
    throw new UnsupportedOperationException();
  }

  public int size() {
    throw new UnsupportedOperationException();
  }

  public Value item(int index) {
    throw new UnsupportedOperationException();
  }

  public Value arg1(int index) {
    throw new UnsupportedOperationException();
  }

  public Value arg2(int index) {
    throw new UnsupportedOperationException();
  }

  public Value arg3(int index) {
    throw new UnsupportedOperationException();
  }

  public String tag() {
    throw new UnsupportedOperationException();
  }

  public Value untagged() {
    throw new UnsupportedOperationException();
  }

  public boolean isString() {
    return false;
  }

  public boolean isRecord() {
    return false;
  }

  public String asString() {
    throw new UnsupportedOperationException();
  }

  public Value lookup(String field) {
    throw new UnsupportedOperationException();
  }

  public void print(Writer writer) {
    Obj obj = asObj();
    obj.print(writer, 90, true, 0);
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    print(writer);
    return writer.toString();
  }

  public abstract Obj asObj();
}
