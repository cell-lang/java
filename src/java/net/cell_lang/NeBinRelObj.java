package net.cell_lang;

import java.io.Writer;



class NeBinRelObj extends Obj {
  Obj[] col1;
  Obj[] col2;
  int[] revIdxs;
  boolean isMap;
  int minPrintedSize = -1;

  public NeBinRelObj(Obj[] col1, Obj[] col2, boolean isMap) {
    Miscellanea._assert(col1 != null && col2 != null);
    Miscellanea._assert(col1.length > 0);
    Miscellanea._assert(col1.length == col2.length);
    this.col1 = col1;
    this.col2 = col2;
    this.isMap = isMap;
  }

  public boolean isBinRel() {
    return true;
  }

  public boolean isNeBinRel() {
    return true;
  }

  public boolean isNeMap() {
    return isMap;
  }

  public boolean hasKey(Obj obj) {
    Miscellanea._assert(isMap);
    return Algs.binSearch(col1, obj) != -1;
  }

  public boolean hasField(int symb_id) {
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (col1[i].isSymb(symb_id))
        return true;
    return false;
  }

  public boolean hasPair(Obj obj1, Obj obj2) {
    if (isMap) {
      int idx = Algs.binSearch(col1, obj1);
      return idx != -1 && col2[idx].isEq(obj2);
    }
    else {
      int[] first_and_count = Algs.binSearchRange(col1, 0, col1.length, obj1);
      int first = first_and_count[0];
      int count = first_and_count[1];
      if (count == 0)
        return false;
      int idx = Algs.binSearch(col2, first, count, obj2);
      return idx != -1;
    }
  }

  public int getSize() {
    return col1.length;
  }

  public BinRelIter getBinRelIter() {
    return new BinRelIter(col1, col2);
  }

  public BinRelIter getBinRelIterByCol1(Obj obj) {
    int[] first_and_count = Algs.binSearchRange(col1, 0, col1.length, obj);
    int first = first_and_count[0];
    int count = first_and_count[1];
    return new BinRelIter(col1, col2, first, first+count-1);
  }

  public BinRelIter getBinRelIterByCol2(Obj obj) {
    if (revIdxs == null)
      revIdxs = Algs.sortedIndexes(col2, col1);
    int[] first_and_count = Algs.binSearchRange(revIdxs, col2, obj);
    int first = first_and_count[0];
    int count = first_and_count[1];
    return new BinRelIter(col1, col2, revIdxs, first, first+count-1);
  }

  public Obj lookup(Obj key) {
    int idx = Algs.binSearch(col1, key);
    if (idx == -1)
      throw new RuntimeException();
    if (!isMap)
      if ((idx > 0 && col1[idx-1].isEq(key)) || (idx+1 < col1.length && col1[idx+1].isEq(key)))
        throw new RuntimeException();
    return col2[idx];
  }

  public Obj lookupField(int symb_id) {
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (col1[i].isSymb(symb_id))
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

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      int len = col1.length;
      boolean isRec = isNeRecord();
      boolean breakLine = minPrintedSize() > maxLineLen;
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
          Miscellanea.writeIndentedNewLine(writer, indentLevel + 1);
      }

      for (int i=0 ; i < len ; i++) {
        Obj arg1 = col1[i];
        Obj arg2 = col2[i];

        // Writing the first argument, followed by the separator
        arg1.print(writer, maxLineLen, newLine | (i > 0), indentLevel + 1);
        writer.write(argSep);

        int arg1Len = arg1.minPrintedSize();
        int arg2Len = arg2.minPrintedSize();

        if (arg1Len + arg2Len + argSep.length() <= maxLineLen) {
          // The entire entry fits into one line
          // We just insert a space and start printing the second argument
          writer.write(' ');
          arg2.print(writer, maxLineLen, false, indentLevel);
        }
        else if (arg1Len <= maxLineLen) {
          // The first argument fits into one line, but the whole entry doesn't.
          if ((arg2.isTagged() & !arg2.isSyntacticSugaredString()) | arg2Len <= maxLineLen) {
            // If the second argument fits into one line (and therefore cannot break itself)
            // or if it's an unsugared tagged object, we break the line.
            Miscellanea.writeIndentedNewLine(writer, indentLevel + 2);
            arg2.print(writer, maxLineLen, false, indentLevel + 2);
          }
          else {
            // Otherwise we keep going on the same line, and let the second argument break itself
            writer.write(' ');
            arg2.print(writer, maxLineLen, false, indentLevel + 1);
          }
        }
        else if (arg2.isTagged() & !arg2.isSyntacticSugaredString() & arg2Len > maxLineLen) {
          // The first argument does not fit into a line, and the second one
          // is a multiline unsugared tagged object, so we break the line
          Miscellanea.writeIndentedNewLine(writer, indentLevel + 1);
          arg2.print(writer, maxLineLen, true, indentLevel + 1);
        }
        else {
          // The first argument doesn't fit into a line, and the second
          // one is not special, so we just keep going on the same line
          // and let the second argument break itself is need be
          writer.write(' ');
          arg2.print(writer, maxLineLen, true, indentLevel + 1);
        }

        // We print the entry separator/terminator when appropriate
        boolean lastLine = i == len - 1;
        if (!lastLine | (!isMap & (len == 1)))
          writer.write(entrySep);

        // Either we break the line, or insert a space if this is not the last entry
        if (breakLine)
          Miscellanea.writeIndentedNewLine(writer, indentLevel + (lastLine ? 0 : 1));
        else if (!lastLine)
          writer.write(' ');
      }

      writer.write(isRec ? ')' : ']');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    if (minPrintedSize == -1) {
      int len = col1.length;
      boolean isRec = isNeRecord();
      minPrintedSize = (2 + (isMap & !isRec ? 4 : 2)) * len + ((!isMap & len == 1) ? 1 : 0);
      for (int i=0 ; i < len ; i++)
        minPrintedSize += col1[i].minPrintedSize() + col2[i].minPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase getValue() {
    int size = col1.length;
    ValueBase[] values1 = new ValueBase[size];
    ValueBase[] values2 = new ValueBase[size];
    for (int i=0 ; i < size ; i++) {
      values1[i] = col1[i].getValue();
      values2[i] = col2[i].getValue();
    }
    return new NeBinRelValue(values1, values2, isMap);
  }

  protected int typeId() {
    return 6;
  }

  protected int internalCmp(Obj other) {
    return other.cmpNeBinRel(col1, col2);
  }

  public int cmpNeBinRel(Obj[] other_col_1, Obj[] other_col_2) {
    int len = col1.length;
    int other_len = other_col_1.length;
    if (other_len != len)
      return other_len < len ? 1 : -1;
    for (int i=0 ; i < len ; i++) {
      int res = other_col_1[i].cmp(col1[i]);
      if (res != 0)
        return res;
    }
    for (int i=0 ; i < len ; i++) {
      int res = other_col_2[i].cmp(col2[i]);
      if (res != 0)
        return res;
    }
    return 0;
  }

  public boolean isNeRecord() {
    if (!isMap)
      return false;
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (!col1[i].isSymb())
        return false;
    return true;
  }
}
