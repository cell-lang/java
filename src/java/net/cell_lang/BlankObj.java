package net.cell_lang;

import java.io.Writer;



class BlankObj extends Obj {
  public boolean isBlankObj() {
    return true;
  }

  public int hashCode() {
    throw new UnsupportedOperationException();
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write("Blank");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return "Blank".length();
  }

  protected int typeId() {
    return -2;
  }

  protected int internalCmp(Obj o) {
    throw new UnsupportedOperationException();
  }

  static BlankObj singleton = new BlankObj();

  public static BlankObj singleton() {
    return singleton;
  }
}
