package net.cell_lang;

import java.io.Writer;


abstract class NeSeqObj extends SeqObj {
  private int minPrintedSize = -1;

  //////////////////////////////////////////////////////////////////////////////

  public boolean[] getArray(boolean[] buffer) {
    int len = getSize();
    if (buffer == null)
      buffer = new boolean[len];
    for (int i=0 ; i < len ; i++)
      buffer[i] = getBoolAt(i);
    return buffer;
  }

  public long[] getArray(long[] buffer) {
    int len = getSize();
    if (buffer == null)
      buffer = new long[len];
    for (int i=0 ; i < len ; i++)
      buffer[i] = getLongAt(i);
    return buffer;
  }

  public double[] getArray(double[] buffer) {
    int len = getSize();
    if (buffer == null)
      buffer = new double[len];
    for (int i=0 ; i < len ; i++)
      buffer[i] = getDoubleAt(i);
    return buffer;
  }

  public Obj[] getArray(Obj[] buffer) {
    int len = getSize();
    if (buffer == null)
      buffer = new Obj[len];
    for (int i=0 ; i < len ; i++)
      buffer[i] = getObjAt(i);
    return buffer;
  }

  //////////////////////////////////////////////////////////////////////////////

  public SeqIter getSeqIter() {
    Obj[] elts = getArray((Obj[]) null);
    return new SeqIter(elts, 0, elts.length-1);
  }

  //////////////////////////////////////////////////////////////////////////////

  public NeSeqObj append(Obj obj) {
    return ArrayObjs.append(this, obj);
  }

  public SeqObj concat(Obj seq) {
    return seq.getSize() != 0 ? ArrayObjs.concat(this, (NeSeqObj) seq) : this;
  }

  //////////////////////////////////////////////////////////////////////////////

  public byte[] getUnsignedByteArray() {
    int len = getSize();
    byte[] bytes = new byte[len];
    for (int i=0 ; i < len ; i++) {
      long value = getLongAt(i);
      if (value < 0 | value > 255)
        throw new UnsupportedOperationException();
      bytes[i] = (byte) value;
    }
    return bytes;
  }

  //////////////////////////////////////////////////////////////////////////////

  public NeSeqObj updatedAt(long idx, Obj obj) {
    int len = getSize();

    if (idx < 0 | idx >= len)
      Miscellanea.softFail("Invalid sequence index");

    Obj[] newItems = new Obj[len];
    for (int i=0 ; i < len ; i++)
      newItems[i] = i == idx ? obj : getObjAt(i);

    return ArrayObjs.create(newItems);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    Miscellanea._assert(other instanceof NeSeqObj && getSize() == other.getSize());

    int len = getSize();
    for (int i=0 ; i < len ; i++) {
      int ord = getObjAt(i).quickOrder(other.getObjAt(i));
      if (ord != 0)
        return ord;
    }
    return 0;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    int len = getSize();

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

      for (int i=0 ; i < len ; i++) {
        if (i > 0) {
          writer.write(',');
          if (breakLine)
            Miscellanea.writeIndentedNewLine(writer, indentLevel + 1);
          else
            writer.write(' ');
        }
        getObjAt(i).print(writer, maxLineLen, breakLine & !newLine, indentLevel + 1);
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
      int len = getSize();
      minPrintedSize = 2 * len;
      for (int i=0 ; i < len ; i++)
        minPrintedSize += getObjAt(i).minPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase getValue() {
    int len = getSize();
    ValueBase[] values = new ValueBase[len];
    for (int i=0 ; i < len ; i++)
      values[i] = getObjAt(i).getValue();
    return new SeqValue(values);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int packedRanges(int minSize, int offset, int[] offsets, NeSeqObj[] ranges, int writeOffset) {
    if (getSize() >= minSize) {
      offsets[writeOffset] = offset;
      ranges[writeOffset++] = this;
    }
    return writeOffset;
  }

  public int depth() {
    return 0;
  }

  //////////////////////////////////////////////////////////////////////////////

  public abstract void copy(int first, int count, Obj[] buffer, int destOffset);
}