package net.cell_lang;

import java.io.Writer;


abstract class NeBinRelObj extends Obj {
  public Obj[] col1;
  public Obj[] col2;
  public int[] col1Hashes;
  long[] revHashIdxs;
  int minPrintedSize = -1;

  static protected final BinRelIter nullIter = new BinRelIter(new Obj[0], new Obj[0]);


  protected NeBinRelObj(Obj[] col1, Obj[] col2, int[] col1Hashes) {
    data = binRelObjData(col1.length, Utils.int32Sum(col1Hashes) + hashcode(col2));
    extraData = neBinRelObjExtraData();

    this.col1 = col1;
    this.col2 = col2;
    this.col1Hashes = col1Hashes;
  }

  protected NeBinRelObj() {

  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean isNeRecord() {
    if (!isNeMap())
      return false;
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (!col1[i].isSymb())
        return false;
    return true;
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean hasField(int symbId) {
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (col1[i].isSymb(symbId))
        return true;
    return false;
  }

  public BinRelIter getBinRelIter() {
    return new BinRelIter(col1, col2);
  }

  public BinRelIter getBinRelIterByCol2(Obj obj) {
    if (revHashIdxs == null) {
      Objs12 sorter = new Objs12(col2, col1);
      revHashIdxs = sorter.sortedLeftHashIdxPairs(0, col1.length);
    }

    int idx = Objs12.lookupFirst(obj, revHashIdxs, col2);
    if (idx == -1)
      return nullIter;
    int count = 1 + Objs12.countEqUpward(idx + 1, obj, revHashIdxs, col2);

    Obj[] slice1 = new Obj[count];
    Obj[] slice2 = new Obj[count];
    for (int i=0 ; i < count ; i++) {
      int directIdx = (int) revHashIdxs[idx+i];
      slice1[i] = col1[directIdx];
      slice2[i] = obj;
    }
    return new BinRelIter(slice1, slice2, 0, count-1);
  }

  public Obj lookupField(int symbId) {
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (col1[i].isSymb(symbId))
        return col2[i];
    // We should never get here. The typechecker should prevent it.
    throw Miscellanea.internalFail(this);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    Miscellanea._assert(getSize() == other.getSize());

    if (other instanceof RecordObj)
      return -other.internalOrder(this);

    NeBinRelObj otherRel = (NeBinRelObj) other;
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

    return 0;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      int len = col1.length;
      boolean isRec = isNeRecord();
      boolean isMap = isNeMap();
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
      boolean isMap = isNeMap();
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
    return new NeBinRelValue(values1, values2, isNeMap());
  }
}
