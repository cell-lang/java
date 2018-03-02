package net.cell_lang;

import java.io.Writer;



class BlankObj extends Obj {
  public boolean IsBlankObj() {
    return true;
  }

  public int hashCode() {
    throw new UnsupportedOperationException();
  }

  public void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write("Blank");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int MinPrintedSize() {
    return "Blank".length();
  }

  protected int TypeId() {
    return -2;
  }

  protected int InternalCmp(Obj o) {
    throw new UnsupportedOperationException();
  }

  static BlankObj singleton = new BlankObj();

  public static BlankObj Singleton() {
    return singleton;
  }
}
