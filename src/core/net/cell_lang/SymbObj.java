package net.cell_lang;

import java.io.Writer;



class SymbObj extends Obj {
  int id;

  public SymbObj(int id) {
    this.id = id;
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

  //## REMOVE REMOVE REMOVE
  public Obj negate() {
    if (id == SymbTable.FalseSymbId)
      return SymbObj.get(SymbTable.TrueSymbId);
    if (id == SymbTable.TrueSymbId)
      return SymbObj.get(SymbTable.FalseSymbId);
    throw new UnsupportedOperationException();
  }

  public int hashCode() {
    return hashCode(id);
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

  //////////////////////////////////////////////////////////////////////////////

  public static SymbObj get(int id) {
    return SymbTable.get(id);
  }

  public static SymbObj get(boolean b) {
    return SymbTable.get(b ? SymbTable.TrueSymbId : SymbTable.FalseSymbId);
  }

  public static int hashCode(int symbId) {
    return symbId; //## BAD HASHCODE, IT'S NOT STABLE
  }

  public static int hashCode(boolean b) {
    return b ? SymbTable.TrueSymbId : SymbTable.FalseSymbId;
  }
}
