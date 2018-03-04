package net.cell_lang;

import java.io.Writer;


abstract class SeqObj extends Obj {
  public Obj[] items;
  protected int offset;
  public int length;
  int minPrintedSize = -1;

  protected SeqObj(int length) {
    this.items = new Obj[length];
    this.length = length;
  }

  protected SeqObj(Obj[] items, int length) {
    Miscellanea._assert(items != null && length >= 0 && length <= items.length);
    this.items = items;
    this.offset = 0;
    this.length = length;
  }

  protected SeqObj(Obj[] items, int offset, int length) {
    Miscellanea._assert(items != null && offset >= 0 && length >= 0 && offset + length <= items.length);
    this.items = items;
    this.offset = offset;
    this.length = length;
  }

  public boolean isSeq() {
    return true;
  }

  public boolean isEmptySeq() {
    return length == 0;
  }

  public boolean isNeSeq() {
    return length != 0;
  }

  public int getSize() {
    return length;
  }

  public Obj getItem(long idx) {
    if (idx < length)
      return items[offset + (int) idx];
    else
      throw new IndexOutOfBoundsException();
  }

  public Obj updatedAt(long idx, Obj obj) {
    if (idx < 0 | idx >= length)
      Miscellanea.softFail("Invalid sequence index");

    Obj[] newItems = new Obj[length];
    for (int i=0 ; i < length ; i++)
      newItems[i] = i == idx ? obj : items[offset + i];

    return new MasterSeqObj(newItems);
  }

  public Obj reverse() {
    int last = offset + length - 1;
    Obj[] revItems = new Obj[length];
    for (int i=0 ; i < length ; i++)
      revItems[i] = items[last-i];
    return new MasterSeqObj(revItems);
  }

  public long[] getLongArray() {
    long[] longs = new long[length];
    for (int i=0 ; i < length ; i++)
      longs[i] = items[offset+i].getLong();
    return longs;
  }

  public byte[] getByteArray() {
    byte[] bytes = new byte[length];
    for (int i=0 ; i < length ; i++) {
      long val = items[offset+i].getLong();
      if (val < 0 | val > 255)
        throw new UnsupportedOperationException();
      bytes[i] = (byte) val;
    }
    return bytes;
  }

  public String toString() {
    String[] reprs = new String[length];
    for (int i=0 ; i < length ; i++)
      reprs[i] = items[offset+i].toString();
    return "(" + String.join(", ", reprs) + ")";
  }

  public Obj concat(Obj seq) {
    int seqLen = seq.getSize();
    int minLen = length + seqLen;
    Obj[] newItems = new Obj[Math.max(4 * minLen, 32)];
    System.arraycopy(items, offset, newItems, 0, length);
    SeqObj seqObj = (SeqObj) seq;
    System.arraycopy(seqObj.items, seqObj.offset, newItems, length, seqObj.length);
    return new MasterSeqObj(newItems, minLen);
  }

  public void copyItems(Obj[] array, int offset) {
    System.arraycopy(items, offset, array, offset, length);
  }

  public int hashCode() {
    int hashcodesSum = 0;
    for (int i=0 ; i < length ; i++)
      hashcodesSum += items[offset+i].hashCode();
    return hashcodesSum ^ length;
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      boolean breakLine = minPrintedSize() > maxLineLen;

      writer.write('(');

      if (breakLine) {
        // If we are on a fresh line, we start writing the first element
        // after the opening bracket, with just a space in between
        // Otherwise we start on the next line
        if (newLine)
          writer.write(' ');
        else
          Miscellanea.writeIndentedNewLine(writer, indentLevel + 1);
      }

      for (int i=0 ; i < length ; i++) {
        if (i > 0) {
          writer.write(',');
          if (breakLine)
            Miscellanea.writeIndentedNewLine(writer, indentLevel + 1);
          else
            writer.write(' ');
        }
        items[offset+i].print(writer, maxLineLen, breakLine & !newLine, indentLevel + 1);
      }

      if (breakLine)
        Miscellanea.writeIndentedNewLine(writer, indentLevel);

      writer.write(')');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    if (minPrintedSize == -1) {
      minPrintedSize = 2 * length;
      for (int i=0 ; i < length ; i++)
        minPrintedSize += items[offset+i].minPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase getValue() {
    ValueBase[] values = new ValueBase[length];
    for (int i=0 ; i < length ; i++)
      values[i] = items[offset+i].getValue();
    return new SeqValue(values);
  }

  protected int typeId() {
    return 3;
  }

  protected int internalCmp(Obj other) {
    return other.cmpSeq(items, offset, length);
  }

  public int cmpSeq(Obj[] other_items, int other_offset, int other_length) {
    if (other_length != length)
      return other_length < length ? 1 : -1;
    for (int i=0 ; i < length ; i++) {
      int res = other_items[other_offset+i].cmp(items[offset+i]);
      if (res != 0)
        return res;
    }
    return 0;
  }

  static Obj emptySeq = new MasterSeqObj(new Obj[] {});

  public static Obj empty() {
    return emptySeq;
  }
}
