package net.cell_lang;

import java.io.Writer;


abstract class OptTagRecObj extends Obj {
  RecordObj innerObj;


  public Obj getInnerObj() {
    if (innerObj == null)
      innerObj = new RecordObj(getLabels(), getValues());
    return innerObj;
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

  public ValueBase getValue() {
    return new TaggedValue(getTagId(), getInnerObj().getValue());
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
