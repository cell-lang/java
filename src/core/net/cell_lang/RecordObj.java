package net.cell_lang;

import java.io.Writer;


//## THE RecordObj CLASS HAS BEEN TEMPORARILY DISABLED, DUE TO ISSUES WITH
//## THE ORDER OF FIELDS, WHICH AFFECTS COMPARISONS, ITERATION AND OTHER MATTERS

class RecordObj extends NeBinRelObj {
  int[] labels;


  private RecordObj(int[] labels, Obj[] values) {
    Miscellanea._assert(labels.length > 0);
    for (int i=1 ; i < labels.length ; i++)
      Miscellanea._assert(SymbObj.compSymbs(labels[i-1], labels[i]) == 1);

    data = binRelObjData(labels.length);
    extraData = neBinRelObjExtraData();

    this.labels = labels;
    col2 = values;
    isMap = true;
  }

  //////////////////////////////////////////////////////////////////////////////

  public Obj setKeyValue(Obj key, Obj value) {
    buildCol1();
    return super.setKeyValue(key, value);
  }

  public Obj dropKey(Obj key) {
    if (!hasKey(key))
      return this;
    buildCol1();
    return super.dropKey(key);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean isNeMap() {
    return true;
  }

  public boolean isNeRecord() {
    return true;
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean contains1(Obj obj) {
    return obj.isSymb() && hasField(obj.getSymbId());
  }

  public boolean contains2(Obj obj) {
    for (int i=0 ; i < col2.length ; i++)
      if (col2[i].isEq(obj))
        return true;
    return false;
  }

  public boolean contains(Obj obj1, Obj obj2) {
    if (!obj1.isSymb())
      return false;
    int keyId = obj1.getSymbId();
    int idx = getFieldIdx(obj1.getSymbId());
    return idx != -1 && col2[idx].isEq(obj2);
  }

  public boolean hasField(int symbId) {
    return getFieldIdx(symbId) != -1;
  }

  public BinRelIter getBinRelIter() {
    buildCol1();
    return super.getBinRelIter();
  }

  public BinRelIter getBinRelIterByCol1(Obj obj) {
    buildCol1();
    return super.getBinRelIterByCol1(obj);
  }

  public BinRelIter getBinRelIterByCol2(Obj obj) {
    buildCol1();
    return super.getBinRelIterByCol2(obj);
  }

  public Obj lookup(Obj key) {
    if (key.isSymb()) {
      int idx = getFieldIdx(key.getSymbId());
      if (idx != -1)
        return col2[idx];
    }
    throw Miscellanea.softFail("Key not found:", "collection", this, "key", key);
  }

  public Obj lookupField(int symbId) {
    return col2[getFieldIdx(symbId)];
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    Miscellanea._assert(getSize() == other.getSize());

    Obj[] col, otherCol;
    int size = getSize();
    NeBinRelObj otherRel = (NeBinRelObj) other;

    if (other instanceof RecordObj) {
      RecordObj otherRecord = (RecordObj) other;

      int[] otherLabels = otherRecord.labels;
      if (labels != otherLabels)
        for (int i=0 ; i < size ; i++) {
          int res = SymbObj.compSymbs(labels[i], otherLabels[i]);
          if (res != 0)
            return res;
        }
    }
    else {
      buildCol1();

      col = col1;
      otherCol = otherRel.col1;
      for (int i=0 ; i < size ; i++) {
        int ord = col[i].quickOrder(otherCol[i]);
        if (ord != 0)
          return ord;
      }
    }

    col = col2;
    otherCol = otherRel.col2;
    for (int i=0 ; i < size ; i++) {
      int ord = col[i].quickOrder(otherCol[i]);
      if (ord != 0)
        return ord;
    }

    return 0;
  }

  public int hashcode() {
    if (hashcode == Integer.MIN_VALUE) {
      long hcode = 0;
      for (int i=0 ; i < labels.length ; i++)
        hcode += Hashing.hashcode(SymbObj.get(labels[i]).hashcode(), col2[i].hashcode());
      hashcode = Hashing.hashcode64(hcode);
      if (hashcode == Integer.MIN_VALUE)
        hashcode++;
    }
    return hashcode;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    buildCol1();
    super.print(writer, maxLineLen, newLine, indentLevel);
  }

  public int minPrintedSize() {
    buildCol1();
    return super.minPrintedSize();
  }

  //////////////////////////////////////////////////////////////////////////////

  Obj[] getCol1() {
    buildCol1();
    return col1;
  }

  //////////////////////////////////////////////////////////////////////////////

  private int getFieldIdx(int symbId) {
    int len = labels.length;
    for (int i=0 ; i < len ; i++)
      if (labels[i] == symbId)
        return i;
    return -1;
  }

  private void buildCol1() {
    if (col1 == null) {
      int len = labels.length;
      col1 = new Obj[len];
      hashcodes1 = new int[len];
      for (int i=0 ; i < len ; i++) {
        Obj symbObj = SymbObj.get(labels[i]);
        col1[i] = symbObj;
        hashcodes1[i] = symbObj.hashcode();
      }
    }
  }
}
