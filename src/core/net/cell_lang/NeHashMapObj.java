package net.cell_lang;

import java.io.Writer;


class NeHashMapObj extends Obj {
  Obj[] keys;
  Obj[] values;

  int[] hashtable;
  int[] buckets;

  int[] revIdxs;

  int minPrintedSize = -1;

  static final BinRelIter nullIter = new BinRelIter(new Obj[0], new Obj[0]);


  public NeHashMapObj(Obj[] keys, Obj[] values) {
    Miscellanea._assert(keys != null && values != null);
    Miscellanea._assert(keys.length > 0);
    Miscellanea._assert(keys.length == values.length);

    this.keys = keys;
    this.values = values;

    int len = keys.length;

    hashtable = new int[len];
    buckets = new int[len];

    for (int i=0 ; i < len ; i++) {
      int hashcode = keys[i].hashCode();
      int index = hashcode % len;
      int head = hashtable[index];
      hashtable[index] = i + 1;
      if (head != 0)
        buckets[i] = head;
    }
  }

  public boolean isBinRel() {
    return true;
  }

  public boolean isNeBinRel() {
    return true;
  }

  public boolean isNeMap() {
    return true;
  }

  public boolean hasKey(Obj obj) {
    int hashcode = obj.hashCode();
    int index = hashcode % hashtable.length;
    int entryIdx = hashtable[index] - 1;
    while (entryIdx != -1) {
      Obj key = keys[entryIdx];
      if (key.isEq(obj))
        return true;
      entryIdx = buckets[entryIdx] - 1;
    }
    return false;
  }

  public boolean hasField(int symbId) {
    int len = keys.length;
    for (int i=0 ; i < len ; i++)
      if (keys[i].isSymb(symbId))
        return true;
    return false;
  }

  public boolean hasPair(Obj key, Obj value) {
    int hashcode = key.hashCode();
    int index = hashcode % hashtable.length;
    int entryIdx = hashtable[index] - 1;
    while (entryIdx != -1) {
      Obj entryKey = keys[entryIdx];
      if (key.isEq(entryKey))
        return value.isEq(values[entryIdx]);
      entryIdx = buckets[entryIdx] - 1;
    }
    return false;
  }

  public int getSize() {
    return keys.length;
  }

  public BinRelIter getBinRelIter() {
    return new BinRelIter(keys, values);
  }

  public BinRelIter getBinRelIterByCol1(Obj key) {
    int hashcode = key.hashCode();
    int index = hashcode % hashtable.length;
    int entryIdx = hashtable[index] - 1;
    while (entryIdx != -1) {
      Obj entryKey = keys[entryIdx];
      if (key.isEq(entryKey))
        return new BinRelIter(keys, values, entryIdx, entryIdx);
      entryIdx = buckets[entryIdx] - 1;
    }
    return nullIter;
  }

  public BinRelIter getBinRelIterByCol2(Obj obj) {
    if (revIdxs == null)
      revIdxs = Algs.sortedIndexes(values, keys);
    int[] firstAndCount = Algs.binSearchRange(revIdxs, values, obj);
    int first = firstAndCount[0];
    int count = firstAndCount[1];
    return new BinRelIter(keys, values, revIdxs, first, first+count-1);
  }

  public Obj lookup(Obj key) {
    int hashcode = key.hashCode();
    int index = hashcode % hashtable.length;
    int entryIdx = hashtable[index] - 1;
    while (entryIdx != -1) {
      Obj entryKey = keys[entryIdx];
      if (key.isEq(entryKey))
        return values[entryIdx];
      entryIdx = buckets[entryIdx] - 1;
    }
    throw Miscellanea.softFail("Key not found:", "collection", this, "key", key);
  }

  public Obj lookupField(int symbId) {
    int len = keys.length;
    for (int i=0 ; i < len ; i++)
      if (keys[i].isSymb(symbId))
        return values[i];
    // We should never get here. The typechecker should prevent it.
    throw Miscellanea.internalFail(this);
  }

  public int hashCode() {
    int hashcodesSum = 0;
    for (int i=0 ; i < keys.length ; i++)
      hashcodesSum += keys[i].hashCode() + values[i].hashCode();
    return hashcodesSum ^ (int) keys.length;
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      int len = keys.length;
      boolean isRec = isNeRecord();
      boolean breakLine = minPrintedSize() > maxLineLen;
      String argSep = isRec ? ":" : " ->";
      String entrySep = ",";

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
        Obj arg1 = keys[i];
        Obj arg2 = values[i];

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
        if (!lastLine)
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
      int len = keys.length;
      boolean isRec = isNeRecord();
      minPrintedSize = (2 + (isRec ? 2 : 4)) * len;
      for (int i=0 ; i < len ; i++)
        minPrintedSize += keys[i].minPrintedSize() + values[i].minPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase getValue() {
    int size = keys.length;
    ValueBase[] values1 = new ValueBase[size];
    ValueBase[] values2 = new ValueBase[size];
    for (int i=0 ; i < size ; i++) {
      values1[i] = keys[i].getValue();
      values2[i] = values[i].getValue();
    }
    return new NeBinRelValue(values1, values2, true);
  }

  protected int typeId() {
    return 6;
  }

  protected int internalCmp(Obj other) {
    return other.cmpNeBinRel(keys, values);
  }

  public int cmpNeBinRel(Obj[] otherCol1, Obj[] otherCol2) {
    int len = keys.length;
    int otherLen = otherCol1.length;
    if (otherLen != len)
      return otherLen < len ? 1 : -1;
    for (int i=0 ; i < len ; i++) {
      int res = otherCol1[i].cmp(keys[i]);
      if (res != 0)
        return res;
    }
    for (int i=0 ; i < len ; i++) {
      int res = otherCol2[i].cmp(values[i]);
      if (res != 0)
        return res;
    }
    return 0;
  }

  public int cmpRecord(int[] otherLabels, Obj[] otherValues) {
    int len = keys.length;
    int otherLen = otherLabels.length;
    if (otherLen != len)
      return otherLen < len ? 1 : -1;
    for (int i=0 ; i < len ; i++) {
      int res = SymbTable.get(otherLabels[i]).cmp(keys[i]);
      if (res != 0)
        return res;
    }
    for (int i=0 ; i < len ; i++) {
      int res = otherValues[i].cmp(values[i]);
      if (res != 0)
        return res;
    }
    return 0;
  }

  public boolean isNeRecord() {
    int len = keys.length;
    for (int i=0 ; i < len ; i++)
      if (!keys[i].isSymb())
        return false;
    return true;
  }
}
