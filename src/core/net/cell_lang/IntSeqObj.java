package net.cell_lang;

import java.io.Writer;


abstract class IntSeqObj extends Obj {
  protected int length;

  protected int minPrintedSize = -1;


  IntSeqObj(int length) {
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
    return IntObj.get(getLongAt(idx));
  }

  public Obj updatedAt(long idx, Obj obj) {
    if (idx < 0 | idx >= length)
      Miscellanea.softFail("Invalid sequence index");

    Obj[] newItems = new Obj[length];
    for (int i=0 ; i < length ; i++)
      newItems[i] = i == idx ? obj : getItem(i);

    return new MasterSeqObj(newItems);
  }

  public byte[] getByteArray() {
    byte[] bytes = new byte[length];
    for (int i=0 ; i < length ; i++) {
      long val = getLongAt(i);
      if (val < 0 | val > 255)
        throw new UnsupportedOperationException();
      bytes[i] = (byte) val;
    }
    return bytes;
  }

  public long[] getLongArray() {
    return getArray((long[]) null);
  }

  public Obj append(Obj obj) {
    if (obj.isInt()) {
      return append(obj.getLong());
    }
    else {
      Obj[] newItems = new Obj[length < 16 ? 32 : (3 * length) / 2];
      copy(newItems);
      newItems[length] = obj;
      return new MasterSeqObj(newItems, length+1);
    }
  }

  public Obj concat(Obj seq) {
    if (seq instanceof IntSeqObj) {
      return concat((IntSeqObj) seq);
    }
    else {
      int seqLen = seq.getSize();
      int minLen = length + seqLen;
      Obj[] newItems = new Obj[Math.max(4 * minLen, 32)];
      copy(newItems);
      SeqObj seqObj = (SeqObj) seq;
      System.arraycopy(seqObj.items, seqObj.offset, newItems, length, seqObj.length);
      return new MasterSeqObj(newItems, minLen);
    }
  }

  public int hashCode() {
    return hashcodesSum() ^ length;
  }

  public int cmpSeq(Obj[] otherItems, int otherOffset, int otherLength) {
    if (otherLength != length)
      return otherLength < length ? 1 : -1;
    for (int i=0 ; i < length ; i++) {
      int res = IntObj.compare(otherItems[otherOffset+i], getLongAt(i));
      if (res != 0)
        return res;
    }
    return 0;
  }

  protected int internalCmp(Obj other) {
    int otherLength = other.getSize();
    if (otherLength != length)
      return length < otherLength ? 1 : -1;

    if (other instanceof IntSeqObj) {
      IntSeqObj otherArray = (IntSeqObj) other;
      for (int i=0 ; i < length ; i++) {
        int res = IntObj.compare(getLongAt(i), other.getLongAt(i));
        if (res != 0)
          return res;
      }
      return 0;
    }
    else {
      for (int i=0 ; i < length ; i++) {
        int res = IntObj.compare(getLongAt(i), other.getItem(i));
        if (res != 0)
          return res;
      }
      return 0;
    }
  }

  public String toString() {
    String[] reprs = new String[length];
    for (int i=0 ; i < length ; i++)
      reprs[i] = Long.toString(getLongAt(i));
    return "(" + String.join(", ", reprs) + ")";
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
        getItem(i).print(writer, maxLineLen, breakLine & !newLine, indentLevel + 1);
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
        minPrintedSize += getItem(i).minPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase getValue() {
    ValueBase[] values = new ValueBase[length];
    for (int i=0 ; i < length ; i++)
      values[i] = new IntValue(getLongAt(i));
    return new SeqValue(values);
  }

  protected int typeId() {
    return staticTypeId;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static int staticTypeId = 3;

  static IntArrayObj emptyIntArray = new IntArrayObj(new long[] {});

  public static IntArrayObj empty() {
    return emptyIntArray;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public void copy(long[] array) {
    copy(array, 0);
  }

  public void copy(Obj[] array) {
    copy(array, 0);
  }

  public abstract void copy(long[] array, int offset);
  public abstract void copy(Obj[] array, int offset);

  public abstract IntSeqObj append(long value);
  public abstract IntSeqObj concat(IntSeqObj seq);

  public abstract int hashcodesSum();
}