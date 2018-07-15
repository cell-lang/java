package net.cell_lang;

import java.io.Writer;


final class PackedObj extends Obj {
  private PackedObj(long data) {
    // Miscellanea._assert(isTagged(data));
    this.data = data;
    extraData = inlineTagObjExtraData();
  }

  //////////////////////////////////////////////////////////////////////////////

  public Obj getInnerObj() {
    return get(getInnerObj(data));
  }

  public Obj tagged(int tagId) {
    return canFitAnotherTag(data) ? new PackedObj(tag(tagId, data)) : super.tagged(tagId);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail();
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      long value = getPackedValue();
      int numTags = 0;
      while (isTagged(value)) {
        writer.write(SymbTable.idxToStr(getTagId(value)));
        writer.write("(");
        numTags++;
        value = getInnerObj(value);
      }
      writer.write(SymbTable.idxToStr(getSymbId(value)));
      for (int i=0 ; i < numTags ; i++)
        writer.write(")");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    long value = getPackedValue();
    int size = 0;
    while (isTagged(value)) {
      size += 2 + SymbTable.idxToStr(getTagId(value)).length();
      value = getInnerObj(value);
    }
    int symbId = getSymbId(value);
    size += SymbTable.idxToStr(symbId).length();
    // size += SymbTable.idxToStr(getSymbId(value)).length();
    return size;
  }

  public ValueBase getValue() {
    return getValue(getPackedValue());
  }

  public static ValueBase getValue(long value) {
    if (isSymb(value))
      return SymbObj.get(getSymbId(value)).getValue();
    else
      return new TaggedValue(getTagId(value), getValue(getInnerObj(value)));
  }

  //////////////////////////////////////////////////////////////////////////////

  public static Obj get(long value) {
    if (isSymb(value))
      return SymbObj.get(getSymbId(value));
    else
      return new PackedObj(value);
  }

  public static long packSymb(int symbId) {
    return 0xFFFFFFFFFFFF0000L | symbId;
    // long res = 0xFFFFFFFFFFFF0000L | symbId;
    // if (getSymbId(res) != symbId) {
    //   System.out.printf("symbId = %d, res = %d, getSymbId(res) = %d\n", symbId, res, getSymbId(res));
    //   System.exit(1);
    // }
    // if (!isSymb(res)) {
    //   System.out.printf("NOT A SYMBOL: symbId = %d, res = %d\n", symbId, res);
    //   System.exit(1);
    // }
    // return res;
  }

  public static long tag(int tag, long value) {
    // Miscellanea._assert(canFitAnotherTag(value));
    return (value << 16) | tag;
    // long res = (value << 16) | tag;
    // if (getTagId(res) != tag) {
    //   System.out.printf("tag = %d, value = %d, res = %d, getTagId(res) = %d\n", tag, value, res, getTagId(res));
    //   System.exit(1);
    // }
    // if (getInnerObj(res) != value) {
    //   System.out.printf("tag = %d, value = %d, res = %d, getInnerObj(res) = %d\n", tag, value, res, getInnerObj(res));
    //   System.out.printf(
    //     "tag = %s\nvalue = %s\nres = %s\ngetInnerObj(res) = %s\n",
    //     Integer.toBinaryString(tag),
    //     Long.toBinaryString(value),
    //     Long.toBinaryString(res),
    //     Long.toBinaryString(getInnerObj(res))
    //   );
    //   System.exit(1);
    // }
    // if (!isTagged(res)) {
    //   System.out.printf("NOT A TAGGED VALUE: tag = %d, value = %d, res = %d\n", tag, value, res);
    //   System.exit(1);
    // }
    // return res;
  }

  public static boolean canFitAnotherTag(long value) {
    // Miscellanea._assert(((value >> 48) == -1) == ((value >>> 48) == 0xFFFF));
    return (value >>> 48) == 0xFFFF;
  }

  public static boolean isSymb(long value) {
    // Miscellanea._assert(((value >>> 16) == 0xFFFFFFFFFFFFL) == ((value >> 16) == -1));
    return (value >>> 16) == 0xFFFFFFFFFFFFL;
  }

  public static boolean isTagged(long value) {
    return !isSymb(value);
  }

  public static int getSymbId(long value) {
    // Miscellanea._assert(isSymb(value));
    return (int) (value & 0xFFFF);
  }

  public static int getTagId(long value) {
    // Miscellanea._assert(isTagged(value));
    return (int) (value & 0xFFFF);
  }

  public static long getInnerObj(long value) {
    // Miscellanea._assert(isTagged(value));
    return (value >> 16) | 0xFFFF000000000000L;
  }

  public static int compare(long value1, long value2) {
    return IntObj.compare(value1, value2);
  }
}
