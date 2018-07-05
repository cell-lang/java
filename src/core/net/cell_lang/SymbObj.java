package net.cell_lang;

import java.io.Writer;



final class SymbObj extends Obj {
  int minPrintedSize;
  SymbValue valueObj;


  public SymbObj(int id) {
    data = Long.MIN_VALUE + id;
    Miscellanea._assert(getSymbId() == id);
    minPrintedSize = SymbTable.idxToStr(id).length();
    valueObj = new SymbValue(id);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int extraData() {
    return symbObjExtraData();
  }

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(SymbTable.idxToStr(id));
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
