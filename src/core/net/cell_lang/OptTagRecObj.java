package net.cell_lang;

import java.io.Writer;


abstract class OptTagRecObj extends Obj {
  int hashcode = Integer.MIN_VALUE;
  RecordObj innerObj;


  public Obj getInnerObj() {
    if (innerObj == null)
      innerObj = new RecordObj(getLabels(), getValues());
    return innerObj;
  }

  //////////////////////////////////////////////////////////////////////////////

  @Override
  public int hashcode() {
    if (hashcode == Integer.MIN_VALUE) {
      int[] labels = getLabels();
      long hcode = 0;
      for (int i=0 ; i < labels.length ; i++)
        hcode += Hashing.hashcode(SymbObj.hashcode(labels[i]), lookupField(labels[i]).hashcode());
      int untaggeHashcode = Hashing.hashcode64(hcode);
      if (untaggeHashcode == Integer.MIN_VALUE)
        untaggeHashcode++;
      hashcode = Hashing.hashcode(getTagId(), untaggeHashcode);
    }
    return hashcode;
  }

  public TypeCode getTypeCode() {
    return TypeCode.TAGGED_VALUE;
  }

  //////////////////////////////////////////////////////////////////////////////

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

  //////////////////////////////////////////////////////////////////////////////

  protected Obj[] getValues() {
    int[] labels = getLabels();
    int len = labels.length;
    Obj[] values = new Obj[len];
    for (int i=0 ; i < len ; i++)
      values[i] = lookupField(labels[i]);
    return values;
  }

  protected abstract int countFields(); //## IS THIS STILL NEEDED?
  protected abstract int[] getLabels();
}
