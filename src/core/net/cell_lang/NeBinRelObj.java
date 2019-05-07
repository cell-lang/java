package net.cell_lang;

import java.io.Writer;


class NeBinRelObj extends Obj {
  Obj[] col1;
  Obj[] col2;
  int[] hashcodes1;
  int[] revIdxs;
  boolean isMap;
  int minPrintedSize = -1;


  private NeBinRelObj(Obj[] col1, Obj[] col2, int[] hashcodes1, boolean isMap) {
    Miscellanea._assert(col1 != null && col2 != null);
    Miscellanea._assert(col1.length > 0);
    Miscellanea._assert(col1.length == col2.length);

    data = binRelObjData(col1.length);
    extraData = neBinRelObjExtraData();

    this.col1 = col1;
    this.col2 = col2;
    this.hashcodes1 = hashcodes1;
    this.isMap = isMap;
  }

  protected NeBinRelObj() {

  }

  //////////////////////////////////////////////////////////////////////////////

  public Obj setKeyValue(Obj key, Obj value) {
    if (!isMap)
      throw Miscellanea.internalFail(this);

    NeTreeMapObj tree = new NeTreeMapObj(col1, col2, hashcodes1, 0, col1.length);
    return tree.setKeyValue(key, value);
  }

  public Obj dropKey(Obj key) {
    if (!isMap)
      throw Miscellanea.internalFail(this);

    if (!contains1(key))
      return this;

    NeTreeMapObj tree = new NeTreeMapObj(col1, col2, hashcodes1, 0, col1.length);
    return tree.dropKey(key);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean isNeMap() {
    return isMap;
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

  //////////////////////////////////////////////////////////////////////////////

  public boolean contains1(Obj key) {
    Miscellanea._assert(isMap);
    return keyRangeStart(col1, hashcodes1, key) >= 0;
  }

  public boolean contains2(Obj obj) {
    if (revIdxs == null)
      revIdxs = Algs.sortedIndexes(col2, col1);
    return Algs.binSearchRange(revIdxs, col2, obj)[1] > 0;
  }

  public boolean contains(Obj obj1, Obj obj2) {
    if (isMap) {
      int idx = keyRangeStart(col1, hashcodes1, obj1);
      return idx >= 0 && col2[idx].isEq(obj2);
    }
    else {
      int idx = keyRangeStart(col1, hashcodes1, obj1);
      if (idx >= 0) {
        int endIdx = keyRangeEnd(idx, col1, hashcodes1, obj1);
        //## BAD BAD BAD: LINEAR SEARCH, INEFFICIENT
        for (int i=idx ; i < endIdx ; i++)
          if (col2[i].isEq(obj2))
            return true;
      }
      return false;
    }
  }

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

  public BinRelIter getBinRelIterByCol1(Obj obj) {
    int startIdx = keyRangeStart(col1, hashcodes1, obj);
    if (startIdx < 0)
      return BinRelIter.emptyIter;
    int endIdx = keyRangeEnd(startIdx, col1, hashcodes1, obj);
    return new BinRelIter(col1, col2, startIdx, endIdx-1);
  }


  public BinRelIter getBinRelIterByCol2(Obj obj) {
    if (revIdxs == null)
      revIdxs = Algs.sortedIndexes(col2, col1);
    int[] firstAndCount = Algs.binSearchRange(revIdxs, col2, obj);
    int first = firstAndCount[0];
    int count = firstAndCount[1];
    return new BinRelIter(col1, col2, revIdxs, first, first+count-1);
  }

  public Obj lookup(Obj key) {
    int idx = keyRangeStart(col1, hashcodes1, key);
    if (idx < 0)
      throw Miscellanea.softFail("Key not found:", "collection", this, "key", key);
    if (!isMap && idx < col1.length - 1 && col1[idx+1].isEq(key))
        throw Miscellanea.softFail("Duplicate key:", "collection", this, "key", key);
    return col2[idx];
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

    if (other instanceof NeTreeMapObj)
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

  public TypeCode getTypeCode() {
    return TypeCode.NE_BIN_REL;
  }

  //////////////////////////////////////////////////////////////////////////////

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

  //////////////////////////////////////////////////////////////////////////////

  Obj[] getCol1() {
    return col1;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static int keyRangeStart(Obj[] objs, int[] hashcodes, Obj key) {
    int hashcode = key.hashcode();
    int idx = Miscellanea.anyIndexOrEncodeInsertionPointIntoSortedArray(hashcodes, hashcode);
    if (idx < 0)
      return idx;

    while (idx > 0 && hashcodes[idx-1] == hashcode)
      idx--;

    do {
      int ord = key.quickOrder(objs[idx]);
      if (ord > 0) // objs[idx] < key, checking the next slot
        idx++;
      else if (ord < 0) // key < objs[idx], search failed
        return -1;
      else
        return idx;
    } while (idx < hashcodes.length && hashcodes[idx] == hashcode);

    return -1;
  }

  private static int keyRangeEnd(int rangeStart, Obj[] objs, int[] hashcodes, Obj key) {
    int idx = rangeStart + 1;
    int hashcode = hashcodes[rangeStart];
    while (idx < objs.length && hashcodes[idx] == hashcode && objs[idx].isEq(key))
      idx++;
    return idx;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private void selfCheck(Obj[] _col1, Obj[] _col2, int _count) {
    int len = col1.length;
    check(len <= _count);
    check(col2.length == len);
    check(hashcodes1.length == len);

    for (int i=0 ; i < len ; i++)
      check(hashcodes1[i] == col1[i].hashcode());

    boolean _isMap = true;
    for (int i=1 ; i < len ; i++) {
      check(hashcodes1[i-1] <= hashcodes1[i]);

      if (hashcodes1[i] == hashcodes1[i-1]) {
        int ord1 = col1[i-1].quickOrder(col1[i]);
        check(ord1 <= 0);
        if (ord1 == 0) {
          _isMap = false;
          check(col2[i-1].quickOrder(col2[i]) < 0);
        }
      }
    }

    check(isMap == _isMap);

    for (int i=0 ; i < len ; i++) {
      Obj key = col1[i];
      int start = keyRangeStart(col1, hashcodes1, key);
      check(start >= 0);
      int end = keyRangeEnd(start, col1, hashcodes1, key);
      check(!isMap || start + 1 == end);
      check(i >= start);
      check(i < end);
    }

    if (isMap) {
      for (int i=0 ; i < len ; i++) {
        Obj key = col1[i];
        Obj value = col2[i];
        check(lookup(key).isEq(value));
      }

      for (int i=0 ; i < _count ; i++)
        check(lookup(_col1[i]).isEq(_col2[i]));
    }

    for (int i=0 ; i < _count ; i++) {
      check(contains(_col1[i], _col2[i]));
    }
  }

  private static void dump(NeBinRelObj rel, Obj[] _col1, Obj[] _col2, int _count) {
    System.out.println(rel.toString());
    System.out.println();
    for (int i=0 ; i < _count ; i++)
      System.out.printf("%s -> %s\n", _col1[i].toString(), _col2[i].toString());
  }

  private static void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }

  /////////////////////////////////////////////////////////////////////////////

  public static NeBinRelObj create(Obj[] col1, Obj[] col2, int count) {
    Miscellanea._assert(count > 0);

    IdxSorter sorter1 = null;
    IdxSorter sorter2 = null;

    long[] keysIdxs = indexesSortedByHashcode(col1, count);

    boolean isMap = true;

    int writeIdx = 0;
    int hashStartIdx = 0;
    do {
      int hashcode = mostSignificant(keysIdxs[hashStartIdx]);
      int hashEndIdx = hashStartIdx + 1;
      while (hashEndIdx < count && mostSignificant(keysIdxs[hashEndIdx]) == hashcode)
        hashEndIdx++;

      if (hashEndIdx - hashStartIdx > 1) {
        if (sorter1 == null)
          sorter1 = new IdxSorter(col1);
        sorter1.sort(keysIdxs, hashStartIdx, hashEndIdx);

        int keyStartIdx = hashStartIdx;
        do {
          Obj key = col1[leastSignificant(keysIdxs[keyStartIdx])];
          int keyEndIdx = keyStartIdx + 1;
          while (keyEndIdx < hashEndIdx && key.isEq(col1[leastSignificant(keysIdxs[keyEndIdx])]))
            keyEndIdx++;

          int uniqueKeyEndIdx = keyEndIdx;
          if (keyEndIdx - keyStartIdx > 1) {
            for (int i=keyStartIdx ; i < keyEndIdx ; i++) {
              int idx = leastSignificant(keysIdxs[i]);
              keysIdxs[i] = (((long) col2[idx].hashcode()) << 32) | idx;
            }
            Miscellanea.sort(keysIdxs, keyStartIdx, keyEndIdx);

            if (sorter2 == null)
              sorter2 = new IdxSorter(col2);
            uniqueKeyEndIdx = sortHashcodeRangeUnique(keysIdxs, keyStartIdx, keyEndIdx, col2, sorter2);

            if (uniqueKeyEndIdx != keyStartIdx + 1)
              isMap = false;

            for (int i=keyStartIdx ; i < uniqueKeyEndIdx ; i++)
              keysIdxs[i] = (((long) hashcode) << 32) | leastSignificant(keysIdxs[i]);
          }

          if (keyStartIdx != writeIdx)
            for (int i=keyStartIdx ; i < uniqueKeyEndIdx ; i++)
              keysIdxs[writeIdx++] = keysIdxs[i];
          else
            writeIdx += uniqueKeyEndIdx - keyStartIdx;

          keyStartIdx = keyEndIdx;
        } while (keyStartIdx < hashEndIdx);
      }
      else {
        if (hashStartIdx != writeIdx)
          keysIdxs[writeIdx] = keysIdxs[hashStartIdx];
        writeIdx++;
      }

      hashStartIdx = hashEndIdx;
    } while (hashStartIdx < count);

    int[] hashcodes = new int[writeIdx];
    Obj[] sortedCol1 = new Obj[writeIdx];
    Obj[] sortedCol2 = new Obj[writeIdx];
    for (int i=0 ; i < writeIdx ; i++) {
      long keyIdx = keysIdxs[i];
      hashcodes[i] = mostSignificant(keyIdx);
      int idx = leastSignificant(keyIdx);
      sortedCol1[i] = col1[idx];
      sortedCol2[i] = col2[idx];
    }

    NeBinRelObj relObj = new NeBinRelObj(sortedCol1, sortedCol2, hashcodes, isMap);
    // relObj.selfCheck(col1, col2, count);
    return relObj;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static int sortHashcodeRangeUnique(long[] keysIdxs, int start, int end, Obj[] objs, IdxSorter sorter) {
    int writeIdx = start;
    int hashStartIdx = start;
    do {
      int hashcode = mostSignificant(keysIdxs[hashStartIdx]);
      int hashEndIdx = hashStartIdx + 1;
      while (hashEndIdx < end && mostSignificant(keysIdxs[hashStartIdx]) == hashcode)
        hashEndIdx++;

      if (hashEndIdx - hashStartIdx > 1) {
        sorter.sort(keysIdxs, hashStartIdx, hashEndIdx);

        int idx = hashStartIdx;
        do {
          if (idx != writeIdx)
            keysIdxs[writeIdx] = keysIdxs[idx];
          writeIdx++;

          Obj obj = objs[leastSignificant(keysIdxs[idx++])];
          while (idx < hashEndIdx && obj.isEq(objs[leastSignificant(keysIdxs[idx])]))
            idx++;
        } while (idx < hashEndIdx);
      }
      else {
        if (hashStartIdx != writeIdx)
          keysIdxs[writeIdx] = keysIdxs[hashStartIdx];
        writeIdx++;
      }

      hashStartIdx = hashEndIdx;
    } while (hashStartIdx < end);

    return writeIdx;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static long[] indexesSortedByHashcode(Obj[] objs, int count) {
    long[] keysIdxs = new long[count];
    for (int i=0 ; i < count ; i++) {
      keysIdxs[i] = (((long) objs[i].hashcode()) << 32) | i;
    }
    Miscellanea.sort(keysIdxs);
    return keysIdxs;
  }

  private final static class IdxSorter extends AbstractLongSorter {
    Obj[] objs;

    public IdxSorter(Obj[] objs) {
      this.objs = objs;
    }

    protected boolean isGreater(long value1, long value2) {
      int idx1 = (int) (value1 & 0xFFFFFFFF);
      int idx2 = (int) (value2 & 0xFFFFFFFF);
      return objs[idx1].quickOrder(objs[idx2]) > 0;
    }
  }

  private static int mostSignificant(long value) {
    return (int) (value >>> 32);
  }

  private static int leastSignificant(long value) {
    return (int) (value & 0xFFFFFFFF);
  }
}
