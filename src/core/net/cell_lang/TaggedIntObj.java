package net.cell_lang;

import java.io.Writer;


final class TaggedIntObj extends Obj {
  public static boolean fits(long value) {
    return (value << 16) >> 16 == value;
  }

  public TaggedIntObj(int tag, long value) {
    Miscellanea._assert(fits(value));
    data = tagIntObjData(tag, value);
    extraData = tagIntObjExtraData();
    Miscellanea._assert(tag == getTagId());
    Miscellanea._assert(getInnerLong() == value);
  }

  public Obj getInnerObj() {
    return IntObj.get(getInnerLong());
  }

  public long getInnerLong() {
    return data >> 16;
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }

  @Override
  public int hashcode() {
    return Hashing.hashcode(getTagId(), IntObj.hashcode(getInnerLong()));
  }

  public TypeCode getTypeCode() {
    return TypeCode.TAGGED_VALUE;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(SymbTable.idxToStr(getTagId()));
      writer.write('(');
      writer.write(Long.toString(getInnerLong()));
      writer.write(')');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return 2 + SymbTable.idxToStr(getTagId()).length() + Long.toString(data).length();
  }

  public ValueBase getValue() {
    return new TaggedValue(getTagId(), new IntValue(getInnerLong()));
  }
}
