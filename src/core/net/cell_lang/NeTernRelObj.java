package net.cell_lang;

import java.io.Writer;



final class NeTernRelObj extends Obj {
  Obj[] col1;
  Obj[] col2;
  Obj[] col3;
  int[] idxs231;
  int[] idxs312;
  int minPrintedSize = -1;

  public void dump() {
    if (idxs231 == null)
      idxs231 = Algs.sortedIndexes(col2, col3, col1);
    if (idxs312 == null)
      idxs312 = Algs.sortedIndexes(col3, col1, col2);

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
    Miscellanea._assert(col1 != null && col2 != null && col3 != null);
    Miscellanea._assert(col1.length == col2.length && col1.length == col3.length);
    Miscellanea._assert(col1.length > 0);

    int size = col1.length;
    long hashcode = 0;
    for (int i=0 ; i < size ; i++)
      hashcode += col1[i].data + col2[i].data + col3[i].data;
    data = ternRelObjData(size, hashcode);
    extraData = neTernRelObjExtraData();

    this.col1 = col1;
    this.col2 = col2;
    this.col3 = col3;
  }

  public boolean hasTriple(Obj obj1, Obj obj2, Obj obj3) {
    int[] firstAndCount = Algs.binSearchRange(col1, 0, col1.length, obj1);
    int first = firstAndCount[0];
    int count = firstAndCount[1];
    if (count == 0)
      return false;

    firstAndCount = Algs.binSearchRange(col2, first, count, obj2);
    first = firstAndCount[0];
    count = firstAndCount[1];
    if (count == 0)
      return false;

    int idx = Algs.binSearch(col3, first, count, obj3);
    return idx != -1;
  }

  public TernRelIter getTernRelIter() {
    return new TernRelIter(col1, col2, col3);
  }

  public TernRelIter getTernRelIterByCol1(Obj val) {
    int[] firstAndCount = Algs.binSearchRange(col1, 0, col1.length, val);
    int first = firstAndCount[0];
    int count = firstAndCount[1];
    return new TernRelIter(col1, col2, col3, null, first, first+count-1);
  }

  public TernRelIter getTernRelIterByCol2(Obj val) {
    if (idxs231 == null)
      idxs231 = Algs.sortedIndexes(col2, col3, col1);
    int[] firstAndCount = Algs.binSearchRange(idxs231, col2, val);
    int first = firstAndCount[0];
    int count = firstAndCount[1];
    return new TernRelIter(col1, col2, col3, idxs231, first, first+count-1);
  }

  public TernRelIter getTernRelIterByCol3(Obj val) {
    if (idxs312 == null)
      idxs312 = Algs.sortedIndexes(col3, col1, col2);
    int[] firstAndCount = Algs.binSearchRange(idxs312, col3, val);
    int first = firstAndCount[0];
    int count = firstAndCount[1];
    return new TernRelIter(col1, col2, col3, idxs312, first, first+count-1);
  }

  public TernRelIter getTernRelIterByCol12(Obj val1, Obj val2) {
    int[] firstAndCount = Algs.binSearchRange(col1, col2, val1, val2);
    int first = firstAndCount[0];
    int count = firstAndCount[1];
    return new TernRelIter(col1, col2, col3, null, first, first+count-1);
  }

  public TernRelIter getTernRelIterByCol13(Obj val1, Obj val3) {
    if (idxs312 == null)
      idxs312 = Algs.sortedIndexes(col3, col1, col2);
    int[] firstAndCount = Algs.binSearchRange(idxs312, col3, col1, val3, val1);
    int first = firstAndCount[0];
    int count = firstAndCount[1];
    return new TernRelIter(col1, col2, col3, idxs312, first, first+count-1);
  }

  public TernRelIter getTernRelIterByCol23(Obj val2, Obj val3) {
    if (idxs231 == null)
      idxs231 = Algs.sortedIndexes(col2, col3, col1);
    int[] firstAndCount = Algs.binSearchRange(idxs231, col2, col3, val2, val3);
    int first = firstAndCount[0];
    int count = firstAndCount[1];
    return new TernRelIter(col1, col2, col3, idxs231, first, first+count-1);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    Miscellanea._assert(getSize() == other.getSize());

    NeTernRelObj otherRel = (NeTernRelObj) other;
    int size = getSize();

    Obj[] col = col1;
    Obj[] otherCol = otherRel.col1;
    for (int i=0 ; i < size ; i++) {
      int ord = col[i].quickOrder(otherCol[i]);
      if (ord != 0)
        return ord;
    }

    col = col2;
    otherCol = otherRel.col2;
    for (int i=0 ; i < size ; i++) {
      int ord = col[i].quickOrder(otherCol[i]);
      if (ord != 0)
        return ord;
    }

    col = col3;
    otherCol = otherRel.col3;
    for (int i=0 ; i < size ; i++) {
      int ord = col[i].quickOrder(otherCol[i]);
      if (ord != 0)
        return ord;
    }

    return 0;
  }

  public TypeCode getTypeCode() {
    return TypeCode.NE_TERN_REL;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      int len = col1.length;
      boolean breakLine = minPrintedSize() > maxLineLen;

      writer.write('[');

      if (breakLine) {
        // If we are on a fresh line, we start writing the first element
        // after the opening bracket, with just a space in between
        // Otherwise we start on the next line
        if (newLine)
          writer.write(" ");
        else
          Miscellanea.writeIndentedNewLine(writer, indentLevel + 1);
      }

      for (int i=0 ; i < len ; i++) {
        int arg1Len = col1[i].minPrintedSize();
        int arg2Len = col2[i].minPrintedSize();
        int arg3Len = col3[i].minPrintedSize();
        int entryLen = 4 + arg1Len + arg2Len + arg3Len;

        boolean eachArgFits = arg1Len <= maxLineLen & arg2Len <= maxLineLen & arg3Len <= maxLineLen;
        boolean breakLineBetweenArgs = eachArgFits & entryLen > maxLineLen;

        // Writing the first argument, followed by the separator
        col1[i].print(writer, maxLineLen, newLine | (i > 0), indentLevel + 2);

        if (breakLineBetweenArgs) {
          // If each argument fits into the maximum line length, but
          // the whole entry doesn't, then we break the line before
          // we start printing each of the following arguments
          Miscellanea.writeIndentedNewLine(writer, ",", indentLevel);
          col2[i].print(writer, maxLineLen, true, indentLevel);
          Miscellanea.writeIndentedNewLine(writer, ",", indentLevel);
          col3[i].print(writer, maxLineLen, true, indentLevel);
        }
        else {
          // Otherwise we just insert a space and start printing the second argument
          writer.write(", ");
          col2[i].print(writer, maxLineLen, false, indentLevel);
          writer.write(", ");
          col3[i].print(writer, maxLineLen, false, indentLevel);
        }

        // We print the entry separator/terminator when appropriate
        boolean lastLine = i == len - 1;
        if (!lastLine | len == 1)
          writer.write(';');

        // Either we break the line, or insert a space if this is not the last entry
        if (breakLine)
          Miscellanea.writeIndentedNewLine(writer, indentLevel + (lastLine ? 0 : 1));
        else if (!lastLine)
          writer.write(' ');
      }

      writer.write(']');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    if (minPrintedSize == -1) {
      int len = col1.length;
      minPrintedSize = 6 * len + (len == 1 ? 1 : 0);
      for (int i=0 ; i < len ; i++)
        minPrintedSize += col1[i].minPrintedSize() + col2[i].minPrintedSize() + col3[i].minPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase getValue() {
    int size = col1.length;
    ValueBase[] values1 = new ValueBase[size];
    ValueBase[] values2 = new ValueBase[size];
    ValueBase[] values3 = new ValueBase[size];
    for (int i=0 ; i < size ; i++) {
      values1[i] = col1[i].getValue();
      values2[i] = col2[i].getValue();
      values3[i] = col3[i].getValue();
    }
    return new NeTernRelValue(values1, values2, values3);
  }
}
