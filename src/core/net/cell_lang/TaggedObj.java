package net.cell_lang;

import java.io.Writer;


final class TaggedObj extends Obj {
  Obj obj;
  int minPrintedSize = -1;

  public TaggedObj(int tag, Obj obj) {
    data = tagObjData(tag);
    extraData = refTagObjExtraData();
    this.obj = obj;
  }

  public boolean hasField(int id) {
    return obj.hasField(id);
  }

  public Obj getInnerObj() {
    return obj;
  }

  public long getInnerLong() {
    return obj.getLong();
  }

  public Obj lookupField(int id) {
    return obj.lookupField(id);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    Miscellanea._assert(getTagId() == other.getTagId());
    return obj.quickOrder(((TaggedObj) other).obj);
  }

  @Override
  public int hashcode() {
    return Hashing.hashcode(getTagId(), obj.hashcode());
  }

  public TypeCode getTypeCode() {
    return TypeCode.TAGGED_VALUE;
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean isSyntacticSugaredString() {
    if (getTagId() != SymbTable.StringSymbId | !obj.isSeq())
      return false;
    int len = obj.getSize();
    for (int i=0 ; i < len ; i++) {
      Obj elt = obj.getObjAt(i);
      if (!elt.isInt())
        return false;
      long value = elt.getLong();
      if (value < 0 | value > 65535)
        return false;
    }
    return true;
  }

  public String getString() {
    if (getTagId() != SymbTable.StringSymbId)
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

  public void printSyntacticSugaredString(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
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
            char ch = (char) (hexDigit < 10 ? '0' + hexDigit : 'a' - 10 + hexDigit);
            writer.write(ch);
          }
        }
      }
      writer.write('"');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private int syntacticSugaredStringMinPrintedSize() {
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
    return minPrintedSize;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    if (obj.isInt()) {
      ObjVisitor printer = new ObjPrinter(writer, maxLineLen);
      printer.taggedIntObj(getTagId(), getInnerLong());
      return;
    }

    try {
      if (!isSyntacticSugaredString()) {
        String tagStr = SymbTable.idxToStr(getTagId());
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
      else
        printSyntacticSugaredString(writer, maxLineLen, newLine, indentLevel);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    if (minPrintedSize == -1) {
      if (!isSyntacticSugaredString()) {
        boolean skipPars = obj.isNeRecord() | obj.isNeSeq();
        minPrintedSize = SymbTable.idxToStr(getTagId()).length() + obj.minPrintedSize() + (skipPars ? 0 : 2);
      }
      else
        minPrintedSize = syntacticSugaredStringMinPrintedSize();
    }
    return minPrintedSize;
  }
}
