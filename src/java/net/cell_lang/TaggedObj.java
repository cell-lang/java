package net.cell_lang;

import java.io.Writer;


class TaggedObj extends Obj {
  int tag;
  Obj obj;
  int minPrintedSize = -1;

  public TaggedObj(int tag, Obj obj) {
    Miscellanea._assert(obj != null);
    this.tag = tag;
    this.obj = obj;
  }

  public TaggedObj(Obj tag, Obj obj) {
    this(tag.getSymbId(), obj);
  }

  public boolean isTagged() {
    return true;
  }

  public boolean isSyntacticSugaredString() {
    if (tag != SymbTable.StringSymbId | !obj.isSeq())
      return false;
    int len = obj.getSize();
    for (int i=0 ; i < len ; i++) {
      Obj item = obj.getItem(i);
      if (!item.isInt())
        return false;
      long value = item.getLong();
      if (value < 0 | value > 65535)
        return false;
    }
    return true;
  }

  public boolean hasField(int id) {
    return obj.hasField(id);
  }

  public int getTagId() {
    return tag;
  }

  public Obj getTag() {
    return SymbObj.get(tag);
  }

  public Obj getInnerObj() {
    return obj;
  }

  public Obj lookupField(int id) {
    return obj.lookupField(id);
  }

  public String getString() {
    if (tag != SymbTable.StringSymbId)
      throw new UnsupportedOperationException();
    long[] codes = obj.getLongArray();
    char[] chars = new char[codes.length];
    for (int i=0 ; i < codes.length ; i++) {
      long code = codes[i];
      if (code < 0 | code > 65535)
        // Char.ConvertFromUtf32
        throw new UnsupportedOperationException();
      chars[i] = (char) code;
    }
    return new String(chars);
  }

  public int hashCode() {
    return ((int) tag) ^ obj.hashCode();
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      if (isSyntacticSugaredString()) {
        long[] codes = obj.getLongArray();
        int len = codes.length;
        writer.write('"');
        for (int i=0 ; i < len ; i++) {
          int code = (char) codes[i];
          if (code == '\n')
            writer.write("\\n");
          else if (code == '\t')
            writer.write("\\t");
          else if (code == '\\')
            writer.write("\\\\");
          else if (code == '"')
            writer.write("\\\"");
          else if (code >= 32 & code <= 126)
            writer.write((char) code);
          else {
            writer.write('\\');
            for (int j=0 ; j < 4 ; j++) {
              int hexDigit = (code >> (12 - 4 * j)) % 16;
              char ch = (char) ((hexDigit < 10 ? '0' : 'A') + hexDigit);
              writer.write(ch);
            }
          }
        }
        writer.write('"');
        return;
      }

      String tagStr = SymbTable.idxToStr(tag);
      writer.write(tagStr);

      if (obj.isNeRecord() | (obj.isNeSeq() && obj.getSize() > 1)) {
        obj.print(writer, maxLineLen, false, indentLevel);
        return;
      }

      boolean breakLine = minPrintedSize() > maxLineLen;
      if (breakLine)
        breakLine = (obj.isTagged() & !obj.isSyntacticSugaredString()) | obj.minPrintedSize() <= maxLineLen;

      writer.write('(');
      if (breakLine) {
        Miscellanea.writeIndentedNewLine(writer, indentLevel + 1);
        obj.print(writer, maxLineLen, breakLine, indentLevel + 1);
        Miscellanea.writeIndentedNewLine(writer, indentLevel);
      }
      else
        obj.print(writer, maxLineLen, breakLine, indentLevel);
      writer.write(')');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    if (minPrintedSize == -1)
      if (!isSyntacticSugaredString()) {
        boolean skipPars = obj.isNeRecord() | obj.isNeSeq();
        minPrintedSize = SymbTable.idxToStr(tag).length() + obj.minPrintedSize() + (skipPars ? 0 : 2);
      }
      else {
        long[] codes = obj.getLongArray();
        int len = codes.length;
        minPrintedSize = 2;
        for (int i=0 ; i < len ; i++) {
          int code = (char) codes[i];
          if (code == '"' | code == '\n' | code == '\t')
            minPrintedSize += 2;
          else if (code == '\\')
            minPrintedSize += 4;
          else if (code < 32 | code > 126)
            minPrintedSize += 5;
          else
            minPrintedSize++;
        }
      }
    return minPrintedSize;
  }

  public ValueBase getValue() {
    return new TaggedValue(tag, obj.getValue());
  }

  protected int typeId() {
    return 8;
  }

  protected int internalCmp(Obj other) {
    return other.cmpTaggedObj(tag, obj);
  }

  public int cmpTaggedObj(int otherTag, Obj otherObj) {
    if (otherTag != tag)
      return SymbTable.compSymbs(otherTag, tag);
    else
      return otherObj.cmp(obj);
  }
}
