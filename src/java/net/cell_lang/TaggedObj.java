package net.cell_lang;

import java.io.Writer;


class TaggedObj extends Obj {
  int tag;
  Obj obj;
  int minPrintedSize = -1;

  public TaggedObj(int tag, Obj obj) {
    Miscellanea.Assert(obj != null);
    this.tag = tag;
    this.obj = obj;
  }

  public TaggedObj(Obj tag, Obj obj) {
    this(tag.GetSymbId(), obj);
  }

  public boolean IsTagged() {
    return true;
  }

  public boolean IsSyntacticSugaredString() {
    if (tag != SymbTable.StringSymbId | !obj.IsSeq())
      return false;
    int len = obj.GetSize();
    for (int i=0 ; i < len ; i++) {
      Obj item = obj.GetItem(i);
      if (!item.IsInt())
        return false;
      long value = item.GetLong();
      if (value < 0 | value > 65535)
        return false;
    }
    return true;
  }

  public boolean HasField(int id) {
    return obj.HasField(id);
  }

  public int GetTagId() {
    return tag;
  }

  public Obj GetTag() {
    return SymbObj.Get(tag);
  }

  public Obj GetInnerObj() {
    return obj;
  }

  public Obj LookupField(int id) {
    return obj.LookupField(id);
  }

  public String GetString() {
    if (tag != SymbTable.StringSymbId)
      throw new UnsupportedOperationException();
    long[] codes = obj.GetLongArray();
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

  public void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      if (IsSyntacticSugaredString()) {
        long[] codes = obj.GetLongArray();
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

      String tagStr = SymbTable.IdxToStr(tag);
      writer.write(tagStr);

      if (obj.IsNeRecord() | (obj.IsNeSeq() && obj.GetSize() > 1)) {
        obj.Print(writer, maxLineLen, false, indentLevel);
        return;
      }

      boolean breakLine = MinPrintedSize() > maxLineLen;
      if (breakLine)
        breakLine = (obj.IsTagged() & !obj.IsSyntacticSugaredString()) | obj.MinPrintedSize() <= maxLineLen;

      writer.write('(');
      if (breakLine) {
        Miscellanea.WriteIndentedNewLine(writer, indentLevel + 1);
        obj.Print(writer, maxLineLen, breakLine, indentLevel + 1);
        Miscellanea.WriteIndentedNewLine(writer, indentLevel);
      }
      else
        obj.Print(writer, maxLineLen, breakLine, indentLevel);
      writer.write(')');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int MinPrintedSize() {
    if (minPrintedSize == -1)
      if (!IsSyntacticSugaredString()) {
        boolean skipPars = obj.IsNeRecord() | obj.IsNeSeq();
        minPrintedSize = SymbTable.IdxToStr(tag).length() + obj.MinPrintedSize() + (skipPars ? 0 : 2);
      }
      else {
        long[] codes = obj.GetLongArray();
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

  public ValueBase GetValue() {
    return new TaggedValue(tag, obj.GetValue());
  }

  protected int TypeId() {
    return 8;
  }

  protected int InternalCmp(Obj other) {
    return other.CmpTaggedObj(tag, obj);
  }

  public int CmpTaggedObj(int other_tag, Obj other_obj) {
    if (other_tag != tag)
      return SymbTable.CompSymbs(other_tag, tag);
    else
      return other_obj.Cmp(obj);
  }
}
