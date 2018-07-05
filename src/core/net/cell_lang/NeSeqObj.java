package net.cell_lang;


abstract class NeSeqObj extends SeqObj {
  int minPrintedSize = -1;

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

  public SeqObj append(Obj obj) {
    if (obj.isInt())
      append(obj.getLong());
    else if (obj.isFloat())
      append(obj.getFloat());
    else
      return RopeObj.create(this, ArrayObjs.createRightPadded(obj));
  }

  public SeqObj append(long value) {
    return RopeObj.create(this, IntArrayObjs.createRightPadded(value));
  }

  public SeqObj append(double value) {
    return RopeObj.create(this, FloatArrayObjs.createRightPadded(value));
  }

  public SeqObj concat(Obj seq) {
    if (seq.getSize() != 0)
      return RopeObj.create(this, seq);
    else
      return this;
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

  public Obj updatedAt(long idx, Obj obj) {
    int len = getSize();

    if (idx < 0 | idx >= len)
      Miscellanea.softFail("Invalid sequence index");

    Obj[] newItems = new Obj[len];
    for (int i=0 ; i < len ; i++)
      newItems[i] = i == idx ? obj : getObjAt(i);

    // return new MasterSeqObj(newItems);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int extraData() {
    return neSeqObjExtraData();
  }

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
      minPrintedSize = 2 * length;
      for (int i=0 ; i < length ; i++)
        minPrintedSize += getObjAt(i).minPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase getValue() {
    ValueBase[] values = new ValueBase[length];
    for (int i=0 ; i < length ; i++)
      values[i] = getObjAt(i).getValue();
    return new SeqValue(values);
  }

  //////////////////////////////////////////////////////////////////////////////

  public abstract void copy(int from, int count, Obj[] buffer, int destOffset);
}