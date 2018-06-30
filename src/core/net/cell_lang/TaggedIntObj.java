package net.cell_lang;

import java.io.Writer;


class TaggedIntObj extends Obj {
  int tag;
  long value;

  public TaggedIntObj(int tag, long value) {
    this.tag = tag;
    this.value = value;
  }

  public boolean isTagged() {
    return true;
  }

  public int getTagId() {
    return tag;
  }

  public Obj getTag() {
    return SymbObj.get(tag);
  }

  public Obj getInnerObj() {
    return IntObj.get(value);
  }

  public long getInnerLong() {
    return value;
  }

  public int hashCode() {
    return ((int) tag) ^ IntObj.hashCode(value);
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(SymbTable.idxToStr(tag));
      writer.write('(');
      writer.write(Long.toString(value));
      writer.write(')');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return SymbTable.idxToStr(tag).length() + Long.toString(value).length() + 2;
  }

  public ValueBase getValue() {
    return new TaggedValue(tag, new IntValue(value));
  }

  protected int typeId() {
    return 8;
  }

  protected int internalCmp(Obj other) {
    int otherTag = other.getTagId();
    if (otherTag != tag)
      return SymbTable.compSymbs(tag, otherTag);

    if (other instanceof TaggedIntObj)
      return IntObj.compare(value, ((TaggedIntObj) other).value);

    //## THIS SHOULD BE OPTIMIZED...
    return other.cmpTaggedObj(tag, IntObj.get(value));
  }

  public int cmpTaggedObj(int otherTag, Obj otherObj) {
    if (otherTag != tag)
      return SymbTable.compSymbs(otherTag, tag);
    else
      //## THIS SHOULD BE OPTIMIZED
      return otherObj.cmp(IntObj.get(value));
  }
}
