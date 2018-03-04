package net.cell_lang;

import java.io.Writer;



class NullObj extends Obj {
  public boolean isNullObj() {
    return true;
  }

  public int hashCode() {
    throw new UnsupportedOperationException();
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write("Null");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return "Null".length();
  }

  protected int typeId() {
    return -1;
  }

  protected int internalCmp(Obj o) {
    throw new UnsupportedOperationException();
  }

  static NullObj singleton = new NullObj();

  public static NullObj singleton() {
    return singleton;
  }
}
