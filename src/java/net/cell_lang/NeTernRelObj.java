package net.cell_lang;

import java.io.Writer;



class NeTernRelObj extends Obj {
  Obj[] col1;
  Obj[] col2;
  Obj[] col3;
  int[] idxs231;
  int[] idxs312;
  int minPrintedSize = -1;

  public void Dump() {
    if (idxs231 == null)
      idxs231 = Algs.SortedIndexes(col2, col3, col1);
    if (idxs312 == null)
      idxs312 = Algs.SortedIndexes(col3, col1, col2);

    System.out.println("");

    for (int i=0 ; i < col1.length ; i++)
      System.out.printf("(%s, %s, %s", col1[i].toString(), col2[i].toString(), col3[i].toString());
    System.out.println("");

    for (int i=0 ; i < idxs231.length ; i++) {
      int idx = idxs231[i];
      System.out.printf("(%d, %d, %d)", col1[idx], col2[idx], col3[idx]);
    }
    System.out.println("");

    for (int i=0 ; i < idxs312.length ; i++) {
      int idx = idxs312[i];
      System.out.printf("(%d, %d, %d)", col1[idx], col2[idx], col3[idx]);
    }
  }

  public NeTernRelObj(Obj[] col1, Obj[] col2, Obj[] col3) {
    Miscellanea.Assert(col1 != null && col2 != null && col3 != null);
    Miscellanea.Assert(col1.length == col2.length && col1.length == col3.length);
    Miscellanea.Assert(col1.length > 0);
    this.col1 = col1;
    this.col2 = col2;
    this.col3 = col3;
  }

  public boolean IsTernRel() {
    return true;
  }

  public boolean IsNeTernRel() {
    return true;
  }

  public boolean HasTriple(Obj obj1, Obj obj2, Obj obj3) {
    int[] first_and_count = Algs.BinSearchRange(col1, 0, col1.length, obj1);
    int first = first_and_count[0];
    int count = first_and_count[1];
    if (count == 0)
      return false;

    first_and_count = Algs.BinSearchRange(col2, first, count, obj2);
    first = first_and_count[0];
    count = first_and_count[1];
    if (count == 0)
      return false;

    int idx = Algs.BinSearch(col3, first, count, obj3);
    return idx != -1;
  }

  public int GetSize() {
    return col1.length;
  }

  public TernRelIter GetTernRelIter() {
    return new TernRelIter(col1, col2, col3);
  }

  public TernRelIter GetTernRelIterByCol1(Obj val) {
    int[] first_and_count = Algs.BinSearchRange(col1, 0, col1.length, val);
    int first = first_and_count[0];
    int count = first_and_count[1];
    return new TernRelIter(col1, col2, col3, null, first, first+count-1);
  }

  public TernRelIter GetTernRelIterByCol2(Obj val) {
    if (idxs231 == null)
      idxs231 = Algs.SortedIndexes(col2, col3, col1);
    int[] first_and_count = Algs.BinSearchRange(idxs231, col2, val);
    int first = first_and_count[0];
    int count = first_and_count[1];
    return new TernRelIter(col1, col2, col3, idxs231, first, first+count-1);
  }

  public TernRelIter GetTernRelIterByCol3(Obj val) {
    if (idxs312 == null)
      idxs312 = Algs.SortedIndexes(col3, col1, col2);
    int[] first_and_count = Algs.BinSearchRange(idxs312, col3, val);
    int first = first_and_count[0];
    int count = first_and_count[1];
    return new TernRelIter(col1, col2, col3, idxs312, first, first+count-1);
  }

  public TernRelIter GetTernRelIterByCol12(Obj val1, Obj val2) {
    int[] first_and_count = Algs.BinSearchRange(col1, col2, val1, val2);
    int first = first_and_count[0];
    int count = first_and_count[1];
    return new TernRelIter(col1, col2, col3, null, first, first+count-1);
  }

  public TernRelIter GetTernRelIterByCol13(Obj val1, Obj val3) {
    if (idxs312 == null)
      idxs312 = Algs.SortedIndexes(col3, col1, col2);
    int[] first_and_count = Algs.BinSearchRange(idxs312, col3, col1, val3, val1);
    int first = first_and_count[0];
    int count = first_and_count[1];
    return new TernRelIter(col1, col2, col3, idxs312, first, first+count-1);
  }

  public TernRelIter GetTernRelIterByCol23(Obj val2, Obj val3) {
    if (idxs231 == null)
      idxs231 = Algs.SortedIndexes(col2, col3, col1);
    int[] first_and_count = Algs.BinSearchRange(idxs231, col2, col3, val2, val3);
    int first = first_and_count[0];
    int count = first_and_count[1];
    return new TernRelIter(col1, col2, col3, idxs231, first, first+count-1);
  }

  public int hashCode() {
    int hashcodesSum = 0;
    for (int i=0 ; i < col1.length ; i++)
      hashcodesSum += col1[i].hashCode() + col2[i].hashCode() + col3[i].hashCode();
    return hashcodesSum ^ (int) col1.length;
  }

  public void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      int len = col1.length;
      boolean breakLine = MinPrintedSize() > maxLineLen;

      writer.write('[');

      if (breakLine) {
        // If we are on a fresh line, we start writing the first element
        // after the opening bracket, with just a space in between
        // Otherwise we start on the next line
        if (newLine)
          writer.write(" ");
        else
          Miscellanea.WriteIndentedNewLine(writer, indentLevel + 1);
      }

      for (int i=0 ; i < len ; i++) {
        int arg1Len = col1[i].MinPrintedSize();
        int arg2Len = col2[i].MinPrintedSize();
        int arg3Len = col3[i].MinPrintedSize();
        int entryLen = 4 + arg1Len + arg2Len + arg3Len;

        boolean eachArgFits = arg1Len <= maxLineLen & arg2Len <= maxLineLen & arg3Len <= maxLineLen;
        boolean breakLineBetweenArgs = eachArgFits & entryLen > maxLineLen;

        // Writing the first argument, followed by the separator
        col1[i].Print(writer, maxLineLen, newLine | (i > 0), indentLevel + 2);

        if (breakLineBetweenArgs) {
          // If each argument fits into the maximum line length, but
          // the whole entry doesn't, then we break the line before
          // we start printing each of the following arguments
          Miscellanea.WriteIndentedNewLine(writer, ",", indentLevel);
          col2[i].Print(writer, maxLineLen, true, indentLevel);
          Miscellanea.WriteIndentedNewLine(writer, ",", indentLevel);
          col3[i].Print(writer, maxLineLen, true, indentLevel);
        }
        else {
          // Otherwise we just insert a space and start printing the second argument
          writer.write(", ");
          col2[i].Print(writer, maxLineLen, false, indentLevel);
          writer.write(", ");
          col3[i].Print(writer, maxLineLen, false, indentLevel);
        }

        // We print the entry separator/terminator when appropriate
        boolean lastLine = i == len - 1;
        if (!lastLine | len == 1)
          writer.write(';');

        // Either we break the line, or insert a space if this is not the last entry
        if (breakLine)
          Miscellanea.WriteIndentedNewLine(writer, indentLevel + (lastLine ? 0 : 1));
        else if (!lastLine)
          writer.write(' ');
      }

      writer.write(']');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int MinPrintedSize() {
    if (minPrintedSize == -1) {
      int len = col1.length;
      minPrintedSize = 6 * len + (len == 1 ? 1 : 0);
      for (int i=0 ; i < len ; i++)
        minPrintedSize += col1[i].MinPrintedSize() + col2[i].MinPrintedSize() + col3[i].MinPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase GetValue() {
    int size = col1.length;
    ValueBase[] values1 = new ValueBase[size];
    ValueBase[] values2 = new ValueBase[size];
    ValueBase[] values3 = new ValueBase[size];
    for (int i=0 ; i < size ; i++) {
      values1[i] = col1[i].GetValue();
      values2[i] = col2[i].GetValue();
      values3[i] = col3[i].GetValue();
    }
    return new NeTernRelValue(values1, values2, values3);
  }

  protected int TypeId() {
    return 7;
  }

  protected int InternalCmp(Obj other) {
    return other.CmpNeTernRel(col1, col2, col3);
  }

  public int CmpNeTernRel(Obj[] other_col_1, Obj[] other_col_2, Obj[] other_col_3) {
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
    for (int i=0 ; i < len ; i++) {
      int res = other_col_3[i].Cmp(col3[i]);
      if (res != 0)
        return res;
    }
    return 0;
  }
}
