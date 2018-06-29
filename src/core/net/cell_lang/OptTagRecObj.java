package net.cell_lang;

import java.io.Writer;


abstract class OptTagRecObj extends Obj {
  RecordObj innerObj;


  public boolean isTagged() {
    return true;
  }

  public Obj getTag() {
    return SymbObj.get(getTagId());
  }

  protected int typeId() {
    return 8;
  }

  public Obj getInnerObj() {
    if (innerObj == null)
      innerObj = new RecordObj(getLabels(), getValues());
    return innerObj;
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    String tagStr = SymbTable.idxToStr(getTagId());
    try {
      writer.write(tagStr);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    getInnerObj().print(writer, maxLineLen, false, indentLevel);
  }

  public int minPrintedSize() {
    return SymbTable.idxToStr(getTagId()).length() + getInnerObj().minPrintedSize();
  }

  public ValueBase getValue() {
    return new TaggedValue(getTagId(), getInnerObj().getValue());
  }

  protected int internalCmp(Obj other) {
    int tag = getTagId();
    int otherTag = other.getTagId();
    if (tag != otherTag)
      return SymbTable.compSymbs(tag, otherTag);
    else
      //## THIS COULD BE OPTIMIZED, BUT IT MAY NOT MATTER THAT MUCH
      return getInnerObj().cmp(other.getInnerObj());
  }


  public int cmpTaggedObj(int otherTag, Obj otherObj) {
    int tag = getTagId();
    if (otherTag != tag)
      return SymbTable.compSymbs(otherTag, tag);

    int otherObjTypeId = otherObj.typeId();
    if (otherObjTypeId != 6)
      return otherObjTypeId < 6 ? 1 : -1;

    //## THIS COULD BE OPTMIZED, BUT IT MAY NOT MATTER THAT MUCH
    return otherObj.cmp(getInnerObj());
  }

  // public int cmpOptTagRecObj(OptTagRecObj otherObj) {
  //   int tag = getTagId();
  //   int otherTag = otherObj.getTagId();
  //   if (otherTag != getTagId())
  //     return SymbTable.compSymbs(otherTag, tag);

  //   int size = countFields();
  //   int otherSize = otherObj.countFields();
  //   if (otherSize != size)
  //     return otherSize < size ? 1 : -1;

  //   int[] labels = getLabels();
  //   int[] otherLabels = otherObj.getLabels();
  //   Miscellanea._assert(labels.length == otherLabels.length);

  //   for (int i=0 ; i < size ; i++) {
  //     int res = SymbTable.compSymbs(otherLabels[i], labels[i]);
  //     if (res != 0)
  //       return res;
  //   }

  //   for (int i=0 ; i < size ; i++) {
  //     int label = labels[i];
  //     int res = otherObj.lookupField(label).cmp(lookupField(label));
  //     if (res != 0)
  //       return res;
  //   }

  //   return 0;
  // }

  protected Obj[] getValues() {
    int[] labels = getLabels();
    int len = labels.length;
    Obj[] values = new Obj[len];
    for (int i=0 ; i < len ; i++)
      values[i] = lookupField(labels[i]);
    return values;
  }

  protected abstract int countFields();
  protected abstract int[] getLabels();
}
