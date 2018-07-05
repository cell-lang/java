package net.cell_lang;

import java.io.Writer;


final class EmptySeqObj extends SeqObj {
  public static final singleton = new EmptySeqObj();

  private EmptySeqObj() {
    Miscellanea._assert(getSize() == 0);
  }

  //////////////////////////////////////////////////////////////////////////////

  public Obj getObjAt(long idx) {
    throw new IndexOutOfBoundsException();
  }

  public SeqObj getSlice(long first, long len) {
    throw new IndexOutOfBoundsException();
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

  public SeqObj append(Obj obj) {
    if (obj.isInt())
      return IntArrayObjs.createRightPadded(obj.getLong());
    else if (obj.isFloat())
      return FloatArrayObjs.createRightPadded(obj.getFloat());
    else
      return GenArrayObjs.createRightPadded(obj);
  }

  public SeqObj append(long value) {
    return IntArrayObjs.createRightPadded(obj.getLong());
  }

  public SeqObj append(double value) {
    return FloatArrayObjs.createRightPadded(obj.getFloat());
  }

  public SeqObj concat(Obj seq) {
    return seq;
  }

  public SeqObj reverse() {
    return this;
  }

  public SeqObj updatedAt(long idx, Obj obj) {
    throw Miscellanea.softFail("Invalid sequence index: " + Long.toString(idx));
  }

  //////////////////////////////////////////////////////////////////////////////

  public int extraData() {
    return emptySeqObjExtraData();
  }

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
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

  public ValueBase getValue() {
    return valueObj;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static final boolean[] emptyBooleanArray = new boolean[0];
  private static final byte[]    emptyByteArray    = new byte[0];
  private static final long[]    emptyLongArray    = new long[0];
  private static final double[]  emptyDoubleArray  = new double[0];
  private static final Obj[]     emptyObjArray     = new Obj[0];

  private static final SeqValue valueObj = new SeqValue(new ValueBase[0]);
  private static final SeqOrSetIter iter = new SeqOrSetIter(new Obj[0], 0, -1);
}
