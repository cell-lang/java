package net.cell_lang;

import java.io.Writer;


final class NullObj extends Obj {
  public static final singleton = new NullObj();

  private NullObj() {

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
    return 4;
  }

  public int extraData() {
    return nullObjExtraData();
  }

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }
}
