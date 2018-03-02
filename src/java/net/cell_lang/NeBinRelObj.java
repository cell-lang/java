package net.cell_lang;

import java.io.Writer;



class NeBinRelObj extends Obj {
  Obj[] col1;
  Obj[] col2;
  int[] revIdxs;
  boolean isMap;
  int minPrintedSize = -1;

  public NeBinRelObj(Obj[] col1, Obj[] col2, boolean isMap) {
    Miscellanea.Assert(col1 != null && col2 != null);
    Miscellanea.Assert(col1.length > 0);
    Miscellanea.Assert(col1.length == col2.length);
    this.col1 = col1;
    this.col2 = col2;
    this.isMap = isMap;
  }

  public boolean IsBinRel() {
    return true;
  }

  public boolean IsNeBinRel() {
    return true;
  }

  public boolean IsNeMap() {
    return isMap;
  }

  public boolean HasKey(Obj obj) {
    Miscellanea.Assert(isMap);
    return Algs.BinSearch(col1, obj) != -1;
  }

  public boolean HasField(int symb_id) {
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (col1[i].IsSymb(symb_id))
        return true;
    return false;
  }

  public boolean HasPair(Obj obj1, Obj obj2) {
    if (isMap) {
      int idx = Algs.BinSearch(col1, obj1);
      return idx != -1 && col2[idx].IsEq(obj2);
    }
    else {
      int[] first_and_count = Algs.BinSearchRange(col1, 0, col1.length, obj1);
      int first = first_and_count[0];
      int count = first_and_count[1];
      if (count == 0)
        return false;
      int idx = Algs.BinSearch(col2, first, count, obj2);
      return idx != -1;
    }
  }

  public int GetSize() {
    return col1.length;
  }

  public BinRelIter GetBinRelIter() {
    return new BinRelIter(col1, col2);
  }

  public BinRelIter GetBinRelIterByCol1(Obj obj) {
    int[] first_and_count = Algs.BinSearchRange(col1, 0, col1.length, obj);
    int first = first_and_count[0];
    int count = first_and_count[1];
    return new BinRelIter(col1, col2, first, first+count-1);
  }

  public BinRelIter GetBinRelIterByCol2(Obj obj) {
    if (revIdxs == null)
      revIdxs = Algs.SortedIndexes(col2, col1);
    int[] first_and_count = Algs.BinSearchRange(revIdxs, col2, obj);
    int first = first_and_count[0];
    int count = first_and_count[1];
    return new BinRelIter(col1, col2, revIdxs, first, first+count-1);
  }

  public Obj Lookup(Obj key) {
    int idx = Algs.BinSearch(col1, key);
    if (idx == -1)
      throw new RuntimeException();
    if (!isMap)
      if ((idx > 0 && col1[idx-1].IsEq(key)) || (idx+1 < col1.length && col1[idx+1].IsEq(key)))
        throw new RuntimeException();
    return col2[idx];
  }

  public Obj LookupField(int symb_id) {
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (col1[i].IsSymb(symb_id))
        return col2[i];
    // We should never get here. The typechecker should prevent it.
    throw new UnsupportedOperationException();
  }

  public int hashCode() {
    int hashcodesSum = 0;
    for (int i=0 ; i < col1.length ; i++)
      hashcodesSum += col1[i].hashCode() + col2[i].hashCode();
    return hashcodesSum ^ (int) col1.length;
  }

  public void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      int len = col1.length;
      boolean isRec = IsNeRecord();
      boolean breakLine = MinPrintedSize() > maxLineLen;
      String argSep = isMap ? (isRec ? ":" : " ->") : ",";
      String entrySep = isMap ? "," : ";";

      writer.write(isRec ? '(' : '[');

      if (breakLine) {
        // If we are on a fresh line, we start writing the first element
        // after the opening bracket, with just a space in between
        // Otherwise we start on the next line
        if (newLine)
          writer.write(' ');
        else
          Miscellanea.WriteIndentedNewLine(writer, indentLevel + 1);
      }

      for (int i=0 ; i < len ; i++) {
        Obj arg1 = col1[i];
        Obj arg2 = col2[i];

        // Writing the first argument, followed by the separator
        arg1.Print(writer, maxLineLen, newLine | (i > 0), indentLevel + 1);
        writer.write(argSep);

        int arg1Len = arg1.MinPrintedSize();
        int arg2Len = arg2.MinPrintedSize();

        if (arg1Len + arg2Len + argSep.length() <= maxLineLen) {
          // The entire entry fits into one line
          // We just insert a space and start printing the second argument
          writer.write(' ');
          arg2.Print(writer, maxLineLen, false, indentLevel);
        }
        else if (arg1Len <= maxLineLen) {
          // The first argument fits into one line, but the whole entry doesn't.
          if ((arg2.IsTagged() & !arg2.IsSyntacticSugaredString()) | arg2Len <= maxLineLen) {
            // If the second argument fits into one line (and therefore cannot break itself)
            // or if it's an unsugared tagged object, we break the line.
            Miscellanea.WriteIndentedNewLine(writer, indentLevel + 2);
            arg2.Print(writer, maxLineLen, false, indentLevel + 2);
          }
          else {
            // Otherwise we keep going on the same line, and let the second argument break itself
            writer.write(' ');
            arg2.Print(writer, maxLineLen, false, indentLevel + 1);
          }
        }
        else if (arg2.IsTagged() & !arg2.IsSyntacticSugaredString() & arg2Len > maxLineLen) {
          // The first argument does not fit into a line, and the second one
          // is a multiline unsugared tagged object, so we break the line
          Miscellanea.WriteIndentedNewLine(writer, indentLevel + 1);
          arg2.Print(writer, maxLineLen, true, indentLevel + 1);
        }
        else {
          // The first argument doesn't fit into a line, and the second
          // one is not special, so we just keep going on the same line
          // and let the second argument break itself is need be
          writer.write(' ');
          arg2.Print(writer, maxLineLen, true, indentLevel + 1);
        }

        // We print the entry separator/terminator when appropriate
        boolean lastLine = i == len - 1;
        if (!lastLine | (!isMap & (len == 1)))
          writer.write(entrySep);

        // Either we break the line, or insert a space if this is not the last entry
        if (breakLine)
          Miscellanea.WriteIndentedNewLine(writer, indentLevel + (lastLine ? 0 : 1));
        else if (!lastLine)
          writer.write(' ');
      }

      writer.write(isRec ? ')' : ']');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int MinPrintedSize() {
    if (minPrintedSize == -1) {
      int len = col1.length;
      boolean isRec = IsNeRecord();
      minPrintedSize = (2 + (isMap & !isRec ? 4 : 2)) * len + ((!isMap & len == 1) ? 1 : 0);
      for (int i=0 ; i < len ; i++)
        minPrintedSize += col1[i].MinPrintedSize() + col2[i].MinPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase GetValue() {
    int size = col1.length;
    ValueBase[] values1 = new ValueBase[size];
    ValueBase[] values2 = new ValueBase[size];
    for (int i=0 ; i < size ; i++) {
      values1[i] = col1[i].GetValue();
      values2[i] = col2[i].GetValue();
    }
    return new NeBinRelValue(values1, values2, isMap);
  }

  protected int TypeId() {
    return 6;
  }

  protected int InternalCmp(Obj other) {
    return other.CmpNeBinRel(col1, col2);
  }

  public int CmpNeBinRel(Obj[] other_col_1, Obj[] other_col_2) {
    int len = col1.length;
    int other_len = other_col_1.length;
    if (other_len != len)
      return other_len < len ? 1 : -1;
    for (int i=0 ; i < len ; i++) {
      int res = other_col_1[i].Cmp(col1[i]);
      if (res != 0)
        return res;
    }
    for (int i=0 ; i < len ; i++) {
      int res = other_col_2[i].Cmp(col2[i]);
      if (res != 0)
        return res;
    }
    return 0;
  }

  public boolean IsNeRecord() {
    if (!isMap)
      return false;
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (!col1[i].IsSymb())
        return false;
    return true;
  }
}
