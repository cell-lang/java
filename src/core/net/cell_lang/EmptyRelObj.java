package net.cell_lang;

import java.io.Writer;



class EmptyRelObj extends Obj {
  EmptyRelObj() {

  }

  public boolean isEmptyRel() {
    return true;
  }

  public boolean isSet() {
    return true;
  }

  public boolean isBinRel() {
    return true;
  }

  public boolean isTernRel() {
    return true;
  }

  public boolean isEq(Obj obj) {
    return obj.isEmptyRel();
  }

  public boolean hasElem(Obj obj) {
    return false;
  }

  public boolean hasKey(Obj key) {
    return false;
  }

  public boolean hasField(int id) {
    return false;
  }

  public boolean hasPair(Obj obj1, Obj obj2) {
    return false;
  }

  public boolean hasTriple(Obj obj1, Obj obj2, Obj obj3) {
    return false;
  }

  public int getSize() {
    return 0;
  }

  public SeqOrSetIter getSeqOrSetIter() {
    return iter1;
  }

  public BinRelIter getBinRelIter() {
    return iter2;
  }

  public BinRelIter getBinRelIterByCol1(Obj obj) {
    return iter2;
  }

  public BinRelIter getBinRelIterByCol2(Obj obj) {
    return iter2;
  }

  public TernRelIter getTernRelIter() {
    return iter3;
  }

  public TernRelIter getTernRelIterByCol1(Obj val) {
    return iter3;
  }

  public TernRelIter getTernRelIterByCol2(Obj val) {
    return iter3;
  }

  public TernRelIter getTernRelIterByCol3(Obj val) {
    return iter3;
  }

  public TernRelIter getTernRelIterByCol12(Obj val1, Obj val2) {
    return iter3;
  }

  public TernRelIter getTernRelIterByCol13(Obj val1, Obj val3) {
    return iter3;
  }

  public TernRelIter getTernRelIterByCol23(Obj val2, Obj val3) {
    return iter3;
  }

  public Obj internalSort() {
    return SeqObj.empty();
  }

  public int hashCode() {
    return 0; //## FIND BETTER VALUE
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write("[]");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return 2;
  }

  public ValueBase getValue() {
    return new EmptyRelValue();
  }

  protected int typeId() {
    return 4;
  }

  protected int internalCmp(Obj other) {
    return 0;
  }

  static SeqOrSetIter iter1 = new SeqOrSetIter(new Obj[0], 0, -1);
  static BinRelIter   iter2 = new BinRelIter(new Obj[0], new Obj[0]);
  static TernRelIter  iter3 = new TernRelIter(new Obj[0], new Obj[0], new Obj[0]);

  static EmptyRelObj singleton = new EmptyRelObj();

  public static EmptyRelObj singleton() {
    return singleton;
  }
}
