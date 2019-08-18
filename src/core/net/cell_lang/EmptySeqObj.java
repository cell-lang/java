package net.cell_lang;

import java.io.Writer;


final class EmptySeqObj extends SeqObj {
  public static final EmptySeqObj singleton = new EmptySeqObj();

  private EmptySeqObj() {
    extraData = emptySeqObjExtraData();
  }

  //////////////////////////////////////////////////////////////////////////////

  public Obj getObjAt(long idx) {
    throw Miscellanea.softFail();
  }

  public SeqObj getSlice(long first, long len) {
    if (first == 0 & len == 0)
      return this;
    else
      throw Miscellanea.softFail();
  }

  public boolean[] getArray(boolean[] buffer) {
    return emptyBooleanArray;
  }

  public long[] getArray(long[] buffer) {
    return emptyLongArray;
  }

  public double[] getArray(double[] buffer) {
    return emptyDoubleArray;
  }

  public Obj[] getArray(Obj[] buffer) {
    return emptyObjArray;
  }

  public byte[] getUnsignedByteArray() {
    return emptyByteArray;
  }

  public NeSeqObj append(Obj obj) {
    if (obj.isInt())
      return append(obj.getLong());
    else if (obj.isFloat())
      return append(obj.getDouble());
    else
      return ArrayObjs.createRightPadded(obj);
  }

  public NeSeqObj append(long value) {
    return IntArrayObjs.createRightPadded(value);
  }

  public NeSeqObj append(double value) {
    return FloatArrayObjs.createRightPadded(value);
  }

  public SeqObj concat(Obj seq) {
    return (SeqObj) seq;
  }

  public SeqObj reverse() {
    return this;
  }

  public NeSeqObj updatedAt(long idx, Obj obj) {
    throw Miscellanea.softFail("Invalid sequence index: " + Long.toString(idx));
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }

  @Override
  public int hashcode() {
    return 0;
  }

  public TypeCode getTypeCode() {
    return TypeCode.EMPTY_SEQ;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write("()");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return 2;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static final boolean[] emptyBooleanArray = new boolean[0];
  private static final byte[]    emptyByteArray    = new byte[0];
  private static final long[]    emptyLongArray    = new long[0];
  private static final double[]  emptyDoubleArray  = new double[0];
  private static final Obj[]     emptyObjArray     = new Obj[0];
}
