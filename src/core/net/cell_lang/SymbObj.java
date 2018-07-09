package net.cell_lang;

import java.io.Writer;



final class SymbObj extends Obj {
  int minPrintedSize;
  String string;
  SymbValue valueObj;


  public SymbObj(int id) {
    data = symbObjData(id);
    extraData = symbObjExtraData();
    Miscellanea._assert(getSymbId() == id);
    string = SymbTable.idxToStr(id);
    minPrintedSize = string.length();
    valueObj = new SymbValue(id);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(string);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return minPrintedSize;
  }

  public ValueBase getValue() {
    return valueObj;
  }

  //////////////////////////////////////////////////////////////////////////////

  public static SymbObj get(int id) {
    return SymbTable.get(id);
  }

  //## THIS COULD BE OPTIMIZED
  public static SymbObj get(boolean b) {
    return SymbTable.get(b ? SymbTable.TrueSymbId : SymbTable.FalseSymbId);
  }
}
