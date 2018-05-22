package net.cell_lang;

import java.io.Writer;


class RecordObj extends Obj {
  int[] labels;
  Obj[] values;

  NeBinRelObj binRelRepr;


  public RecordObj(int[] labels, Obj[] values) {
    Miscellanea._assert(labels.length > 0);
    for (int i=1 ; i < labels.length ; i++)
      Miscellanea._assert(SymbTable.compSymbs(labels[i-1], labels[i]) == 1);

    this.labels = labels;
    this.values = values;
  }


  public boolean isBinRel() {
    return true;
  }

  public boolean isNeBinRel() {
    return true;
  }

  public boolean isNeMap() {
    return true;
  }

  public boolean hasKey(Obj obj) {
    return obj.isSymb() && hasField(obj.getSymbId());
  }

  public boolean hasField(int symbId) {
    return getFieldIdx(symbId) != -1;
  }

  public boolean hasPair(Obj obj1, Obj obj2) {
    if (!obj1.isSymb())
      return false;
    int keyId = obj1.getSymbId();
    int idx = getFieldIdx(obj1.getSymbId());
    return idx != -1 && values[idx].compareTo(obj2) == 0;
  }


  public int getSize() {
    return labels.length;
  }

  public BinRelIter getBinRelIter() {
    return asBinRel().getBinRelIter();
  }

  public BinRelIter getBinRelIterByCol1(Obj obj) {
    return asBinRel().getBinRelIterByCol1(obj);
  }

  public BinRelIter getBinRelIterByCol2(Obj obj) {
    return asBinRel().getBinRelIterByCol2(obj);
  }

  public Obj lookup(Obj key) {
    if (key.isSymb()) {
      int idx = getFieldIdx(key.getSymbId());
      if (idx != -1)
        return values[idx];
    }
    throw Miscellanea.softFail("Key not found:", "collection", this, "key", key);
  }

  public Obj lookupField(int symbId) {
    return values[getFieldIdx(symbId)];
  }

  public int hashCode() {
    int len = labels.length;
    int hashcodesSum = 0;
    for (int i=0 ; i < len ; i++)
      hashcodesSum += SymbObj.hashCode(labels[i]) + values[i].hashCode();
    return hashcodesSum ^ len;
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    asBinRel().print(writer, maxLineLen, newLine, indentLevel);
  }

  public int minPrintedSize() {
    return asBinRel().minPrintedSize();
  }

  public ValueBase getValue() {
    return asBinRel().getValue();
  }

  protected int typeId() {
    return 6;
  }

  protected int internalCmp(Obj other) {
    return other.cmpRecord(labels, values);
  }

  public int cmpRecord(int[] otherLabels, Obj[] otherValues) {
    int len = labels.length;
    int otherLen = otherLabels.length;
    if (otherLen != len)
      return otherLen < len ? 1 : -1;
    for (int i=0 ; i < len ; i++) {
      int res = SymbTable.compSymbs(otherLabels[i], labels[i]);
      if (res != 0)
        return res;
    }
    for (int i=0 ; i < len ; i++) {
      int res = otherValues[i].cmp(values[i]);
      if (res != 0)
        return res;
    }
    return 0;
  }

  public int cmpNeBinRel(Obj[] otherCol1, Obj[] otherCol2) {
    int len = labels.length;
    int otherLen = otherCol1.length;
    if (otherLen != len)
      return otherLen < len ? 1 : -1;
    for (int i=0 ; i < len ; i++) {
      int res = otherCol1[i].cmp(SymbTable.get(labels[i]));
      if (res != 0)
        return res;
    }
    for (int i=0 ; i < len ; i++) {
      int res = otherCol2[i].cmp(values[i]);
      if (res != 0)
        return res;
    }
    return 0;
  }

  public boolean isNeRecord() {
    return true;
  }

  //////////////////////////////////////////////////////////////////////////////

  int getFieldIdx(int symbId) {
    int len = labels.length;
    for (int i=0 ; i < len ; i++)
      if (labels[i] == symbId)
        return i;
    return -1;
  }

  NeBinRelObj asBinRel() {
    if (binRelRepr == null) {
      int len = labels.length;
      Obj[] labelObjs = new Obj[len];
      for (int i=0 ; i < len ; i++)
        labelObjs[i] = SymbTable.get(labels[i]);
      binRelRepr = new NeBinRelObj(labelObjs, values, true);
    }
    return binRelRepr;
  }
}
