package net.cell_lang;

import java.io.Writer;


final class EmptyRelObj extends Obj {
  public static final EmptyRelObj singleton = new EmptyRelObj();

  private EmptyRelObj() {
    extraData = emptyRelObjExtraData();
  }

  //////////////////////////////////////////////////////////////////////////////

  public Obj insert(Obj obj) {
    return new NeTreeSetObj(obj);
  }

  public Obj remove(Obj obj) {
    return this;
  }

  public Obj setKeyValue(Obj key, Obj value) {
    return new NeTreeMapObj(key, value);
  }

  public Obj dropKey(Obj key) {
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////

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

  public SetIter getSetIter() {
    return iter1;
  }

  public Obj[] getArray(Obj[] buffer) {
    return emptyObjArray;
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

  public SeqObj internalSort() {
    return EmptySeqObj.singleton;
  }

  public Obj lookup(Obj key) {
    throw Miscellanea.softFail("Key not found:", "collection", this, "key", key);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }

  public TypeCode getTypeCode() {
    return TypeCode.EMPTY_REL;
  }

  //////////////////////////////////////////////////////////////////////////////

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
    return valueObj;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static final EmptyRelValue valueObj = new EmptyRelValue();

  private static final Obj[] emptyObjArray = new Obj[0];

  private static final SetIter     iter1 = new SetIter(new Obj[0], 0, -1);
  private static final BinRelIter  iter2 = new BinRelIter(new Obj[0], new Obj[0]);
  private static final TernRelIter iter3 = new TernRelIter(new Obj[0], new Obj[0], new Obj[0]);
}
