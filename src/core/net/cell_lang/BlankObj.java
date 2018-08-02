package net.cell_lang;

import java.io.Writer;


final class BlankObj extends Obj {
  public static final BlankObj singleton = new BlankObj();

  private BlankObj() {
    extraData = blankObjExtraData();
  }

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }

  public TypeCode getTypeCode() {
    throw Miscellanea.internalFail(this);
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
    return 5;
  }

  public ValueBase getValue() {
    throw Miscellanea.internalFail(this);
  }
}
