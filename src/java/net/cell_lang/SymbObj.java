package net.cell_lang;

import java.io.Writer;



class SymbObj extends Obj {
  int id;

  public SymbObj(int id) {
    this.id = id;
  }

  public static SymbObj Get(int id) {
    return SymbTable.Get(id);
  }

  public static SymbObj Get(boolean b) {
    return SymbTable.Get(b ? SymbTable.TrueSymbId : SymbTable.FalseSymbId);
  }

  public boolean IsSymb() {
    return true;
  }

  public boolean IsSymb(int id) {
    return this.id == id;
  }

  public int GetSymbId() {
    return id;
  }

  public boolean GetBool() {
    if (id == SymbTable.FalseSymbId)
      return false;
    if (id == SymbTable.TrueSymbId)
      return true;
    throw new UnsupportedOperationException();
  }

  public boolean IsEq(Obj obj) {
    return obj.IsSymb(id);
  }

  public Obj Negate() {
    if (id == SymbTable.FalseSymbId)
      return SymbObj.Get(SymbTable.TrueSymbId);
    if (id == SymbTable.TrueSymbId)
      return SymbObj.Get(SymbTable.FalseSymbId);
    throw new UnsupportedOperationException();
  }

  public int hashCode() {
    return (int) id; //## BAD HASHCODE, IT'S NOT STABLE
  }

  public void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(SymbTable.IdxToStr(id));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int MinPrintedSize() {
    return SymbTable.IdxToStr(id).length();
  }

  public ValueBase GetValue() {
    return new SymbValue(id);
  }

  protected int TypeId() {
    return 0;
  }

  protected int InternalCmp(Obj other) {
    return SymbTable.CompSymbs(id, other.GetSymbId());
  }
}
