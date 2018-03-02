package net.cell_lang;

import java.io.Writer;



class EmptyRelObj extends Obj {
  EmptyRelObj() {

  }

  public boolean IsEmptyRel() {
    return true;
  }

  public boolean IsSet() {
    return true;
  }

  public boolean IsBinRel() {
    return true;
  }

  public boolean IsTernRel() {
    return true;
  }

  public boolean IsEq(Obj obj) {
    return obj.IsEmptyRel();
  }

  public boolean HasElem(Obj obj) {
    return false;
  }

  public boolean HasKey(Obj key) {
    return false;
  }

  public boolean HasField(int id) {
    return false;
  }

  public boolean HasPair(Obj obj1, Obj obj2) {
    return false;
  }

  public boolean HasTriple(Obj obj1, Obj obj2, Obj obj3) {
    return false;
  }

  public int GetSize() {
    return 0;
  }

  public SeqOrSetIter GetSeqOrSetIter() {
    return iter1;
  }

  public BinRelIter GetBinRelIter() {
    return iter2;
  }

  public BinRelIter GetBinRelIterByCol1(Obj obj) {
    return iter2;
  }

  public BinRelIter GetBinRelIterByCol2(Obj obj) {
    return iter2;
  }

  public TernRelIter GetTernRelIter() {
    return iter3;
  }

  public TernRelIter GetTernRelIterByCol1(Obj val) {
    return iter3;
  }

  public TernRelIter GetTernRelIterByCol2(Obj val) {
    return iter3;
  }

  public TernRelIter GetTernRelIterByCol3(Obj val) {
    return iter3;
  }

  public TernRelIter GetTernRelIterByCol12(Obj val1, Obj val2) {
    return iter3;
  }

  public TernRelIter GetTernRelIterByCol13(Obj val1, Obj val3) {
    return iter3;
  }

  public TernRelIter GetTernRelIterByCol23(Obj val2, Obj val3) {
    return iter3;
  }

  public Obj InternalSort() {
    return SeqObj.Empty();
  }

  public int hashCode() {
    return 0; //## FIND BETTER VALUE
  }

  public void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write("[]");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int MinPrintedSize() {
    return 2;
  }

  public ValueBase GetValue() {
    return new EmptyRelValue();
  }

  protected int TypeId() {
    return 4;
  }

  protected int InternalCmp(Obj other) {
    return 0;
  }

  static SeqOrSetIter iter1 = new SeqOrSetIter(new Obj[0], 0, -1);
  static BinRelIter   iter2 = new BinRelIter(new Obj[0], new Obj[0]);
  static TernRelIter  iter3 = new TernRelIter(new Obj[0], new Obj[0], new Obj[0]);

  static EmptyRelObj singleton = new EmptyRelObj();

  public static EmptyRelObj Singleton() {
    return singleton;
  }
}
