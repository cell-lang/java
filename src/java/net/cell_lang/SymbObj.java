package net.cell_lang;

import java.io.Writer;



class SymbObj extends Obj {
  int id;

  public SymbObj(int id) {
    this.id = id;
  }

  public static SymbObj get(int id) {
    return SymbTable.get(id);
  }

  public static SymbObj get(boolean b) {
    return SymbTable.get(b ? SymbTable.TrueSymbId : SymbTable.FalseSymbId);
  }

  public boolean isSymb() {
    return true;
  }

  public boolean isSymb(int id) {
    return this.id == id;
  }

  public int getSymbId() {
    return id;
  }

  public boolean getBool() {
    if (id == SymbTable.FalseSymbId)
      return false;
    if (id == SymbTable.TrueSymbId)
      return true;
    throw new UnsupportedOperationException();
  }

  public boolean isEq(Obj obj) {
    return obj.isSymb(id);
  }

  public Obj negate() {
    if (id == SymbTable.FalseSymbId)
      return SymbObj.get(SymbTable.TrueSymbId);
    if (id == SymbTable.TrueSymbId)
      return SymbObj.get(SymbTable.FalseSymbId);
    throw new UnsupportedOperationException();
  }

  public int hashCode() {
    return (int) id; //## BAD HASHCODE, IT'S NOT STABLE
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(SymbTable.idxToStr(id));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return SymbTable.idxToStr(id).length();
  }

  public ValueBase getValue() {
    return new SymbValue(id);
  }

  protected int typeId() {
    return 0;
  }

  protected int internalCmp(Obj other) {
    return SymbTable.compSymbs(id, other.getSymbId());
  }
}
