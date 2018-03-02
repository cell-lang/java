package net.cell_lang;

import java.io.Writer;



class NullObj extends Obj {
  public boolean IsNullObj() {
    return true;
  }

  public int hashCode() {
    throw new UnsupportedOperationException();
  }

  public void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write("Null");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int MinPrintedSize() {
    return "Null".length();
  }

  protected int TypeId() {
    return -1;
  }

  protected int InternalCmp(Obj o) {
    throw new UnsupportedOperationException();
  }

  static NullObj singleton = new NullObj();

  public static NullObj Singleton() {
    return singleton;
  }
}
