package net.cell_lang;

import java.util.Arrays;


class IntArrayObjs {
  static ByteArrayObj create(byte[] data) {
    return new ByteArrayObj(data);
  }

  static ShortArrayObj create(short[] data) {
    return new ShortArrayObj(data);
  }

  static Int32ArrayObj create(int[] data) {
    return new Int32ArrayObj(data);
  }

  static IntArrayObj create(long[] data) {
    return new IntArrayObj(data);
  }

  static ByteArrayObj create(byte[] data, int length) {
    return new ByteArrayObj(Arrays.copyOf(data, length));
  }

  static ShortArrayObj create(short[] data, int length) {
    return new ShortArrayObj(Arrays.copyOf(data, length));
  }

  static Int32ArrayObj create(int[] data, int length) {
    return new Int32ArrayObj(Arrays.copyOf(data, length));
  }

  static IntArrayObj create(long[] data, int length) {
    return new IntArrayObj(Arrays.copyOf(data, length));
  }

  static ByteArrayObj createUnsigned(byte[] data) {
    //## BUG BUG BUG: IMPLEMENT
    return new ByteArrayObj(data);
  }

  static IntArrayObj createRightPadded(long value) {
    return PaddedIntArray.make(value);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

abstract class IntArrayObjBase extends NeIntSeqObj {
  int    offset;
  long[] longs;
  Obj[]  objs;


  public Obj reverse() {
    int last = offset + length - 1;
    long[] revData = new long[length];
    for (int i=0 ; i < length ; i++)
      revData[i] = longs[last-i];
    return new IntArrayObj(revData);
  }

  public long[] getArray(long[] buffer) {
    if (longs == null) {
      longs = new long[length];
      copy(0, longs);
    }
    return longs;
  }

  public Obj[] getArray(Obj[] buffer) {
    if (objs == null) {
      objs = new Obj[length];
      copy(0, objs);
    }
    return objs;
  }

  public Obj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > length)
      throw new IndexOutOfBoundsException();
    return new IntArraySliceObj(null, longs, offset + (int) first, (int) len);
  }

  public IntSeqObj append(long value) {
    return IntRopeObj.make(this, PaddedIntArray.make(value));
  }

  public IntSeqObj concat(IntSeqObj seq) {
    return IntRopeObj.make(this, seq);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    if (other instanceof IntArrayObjBase) {
      Miscellanea._assert(getSize() == other.getSize());

      IntArrayObjBase otherArray = (IntArrayObjBase) other;

      int len = getSize();
      for (int i=0 ; i < len ; i++) {
        long elt = getLongAt(i);
        long otherElt = other.getLongAt(i);
        if (elt != otherElt)
          return elt - otherElt;
      }
      return 0;
    }
    else
      super.internalOrder(other);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void copy(long[] array, int destOffset) {
    for (int i=0 ; i < length ; i++)
      array[destOffset+i] = longs[offset+i];
  }

  public void copy(Obj[] array, int destOffset) {
    for (int i=0 ; i < length ; i++)
      array[destOffset+i] = IntObj.get(longs[offset+i]);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class IntArrayObj extends IntArrayObjBase {
  public IntArrayObj(long[] data) {
    long eltsData
    data = seqObjData(len, eltsData);
  protected static long seqObjData(int length, long eltsData) {

    super(data.length);
    longs = data;
  }

  public long getLongAt(long idx) {
    return longs[(int) idx];
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class IntArraySliceObj extends IntArrayObjBase {
  PaddedIntArray source;


  public IntArraySliceObj(PaddedIntArray source, long[] data, int offset, int length) {
    super(data, offset, length);
    this.source = source;
  }

  public long getLongAt(long idx) {
    if (idx >= 0 & idx < length)
      return longs[offset + (int) idx];
    else
      throw new ArrayIndexOutOfBoundsException();
  }

  public IntSeqObj append(long value) {
    if (source != null)
      return source.append(offset+length, value);
    else
      return super.append(value);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class PaddedIntArray {
  long[] buffer;
  int used;

  PaddedIntArray(long[] buffer, int used) {
    this.buffer = buffer;
    this.used = used;
  }

  public static IntArraySliceObj make(long value) {
    long[] buffer = new long[32];
    buffer[0] = value;
    PaddedIntArray paddedArray = new PaddedIntArray(buffer, 1);
    return paddedArray.slice(0, 1);
  }

  public IntArraySliceObj slice(int offset, int length) {
    return new IntArraySliceObj(this, buffer, offset, length);
  }

  public synchronized IntArraySliceObj append(int idx, long value) {
    if (idx == buffer.length) {
      // We run out of space, expanding the array buffer
      int len = buffer.length;
      int newLen = 2 * len;
      long[] newBuffer = new long[newLen];
      for (int i=0 ; i < len ; i++)
        newBuffer[i] = buffer[i];
      newBuffer[idx] = value;
      buffer = newBuffer;
      used++;
      return new IntArraySliceObj(this, buffer, 0, used);
    }
    else if (idx == used) {
      // There's space for the new element
      buffer[idx] = value;
      used++;
      return new IntArraySliceObj(this, buffer, 0, used);
    }
    else {
      // The next slot was already taken. This is supposed to happen only rarely
      Miscellanea._assert(idx < used & idx < buffer.length);

      long[] newBuffer = new long[buffer.length];
      for (int i=0 ; i < idx ; i++)
        newBuffer[i] = buffer[i];
      newBuffer[idx] = value;
      PaddedIntArray newArray = new PaddedIntArray(newBuffer, idx+1);
      return newArray.slice(0, idx+1);
    }
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

abstract class ByteArrayObjBase extends IntArrayObjBase {
  byte[] bytes;


  protected ByteArrayObjBase(byte[] data) {
    super(data.length);
    bytes = data;
  }

  protected ByteArrayObjBase(byte[] data, int offset, int length) {
    super(offset, length);
    bytes = data;
  }

  public Obj reverse() {
    int last = offset + length - 1;
    byte[] revData = new byte[length];
    for (int i=0 ; i < length ; i++)
      revData[i] = bytes[last-i];
    return new ByteArrayObj(revData);
  }

  public Obj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > length)
      throw new IndexOutOfBoundsException();
    return new ByteArraySliceObj(bytes, offset + (int) first, (int) len);
  }

  public int hashcodesSum() {
    int sum = 0;
    int end = offset + length;
    for (int i=offset ; i < end ; i++)
      sum += IntObj.hashCode(bytes[i]);
    return sum;
  }

  public void copy(long[] array, int destOffset) {
    for (int i=0 ; i < length ; i++)
      array[destOffset+i] = bytes[offset+i];
  }

  public void copy(Obj[] array, int destOffset) {
    for (int i=0 ; i < length ; i++)
      array[destOffset+i] = IntObj.get(bytes[offset+i]);
  }
}


final class ByteArrayObj extends ByteArrayObjBase {
  protected ByteArrayObj(byte[] data) {
    super(data);
  }

  public long getLongAt(long idx) {
    return bytes[(int) idx];
  }
}


final class ByteArraySliceObj extends ByteArrayObjBase {
  public ByteArraySliceObj(byte[] data, int offset, int length) {
    super(data, offset, length);
  }

  public long getLongAt(long idx) {
    if (idx >= 0 & idx < length)
      return bytes[offset + (int) idx];
    else
      throw new ArrayIndexOutOfBoundsException();
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

abstract class ShortArrayObjBase extends IntArrayObjBase {
  short[] shorts;


  protected ShortArrayObjBase(short[] data) {
    super(data.length);
    shorts = data;
  }

  protected ShortArrayObjBase(short[] data, int offset, int length) {
    super(offset, length);
    shorts = data;
  }

  public long getLongAt(long idx) {
    return shorts[(int) idx];
  }

  public Obj reverse() {
    int last = offset + length - 1;
    short[] revData = new short[length];
    for (int i=0 ; i < length ; i++)
      revData[i] = shorts[last-i];
    return new ShortArrayObj(revData);
  }

  public Obj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > length)
      throw new IndexOutOfBoundsException();
    return new ShortArraySliceObj(shorts, offset + (int) first, (int) len);
  }

  public int hashcodesSum() {
    int sum = 0;
    int end = offset + length;
    for (int i=offset ; i < end ; i++)
      sum += IntObj.hashCode(shorts[i]);
    return sum;
  }

  public void copy(long[] array, int destOffset) {
    for (int i=0 ; i < length ; i++)
      array[destOffset+i] = shorts[offset+i];
  }

  public void copy(Obj[] array, int destOffset) {
    for (int i=0 ; i < length ; i++)
      array[destOffset+i] = IntObj.get(shorts[offset+i]);
  }
}


final class ShortArrayObj extends ShortArrayObjBase {
  protected ShortArrayObj(short[] data) {
    super(data);
  }

  public long getLongAt(long idx) {
    return shorts[(int) idx];
  }
}


final class ShortArraySliceObj extends ShortArrayObjBase {
  public ShortArraySliceObj(short[] data, int offset, int length) {
    super(data, offset, length);
  }

  public long getLongAt(long idx) {
    if (idx >= 0 & idx < length)
      return shorts[offset + (int) idx];
    else
      throw new ArrayIndexOutOfBoundsException();
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

abstract class Int32ArrayObjBase extends IntArrayObjBase {
  int[] ints;


  protected Int32ArrayObjBase(int[] data) {
    super(data.length);
    ints = data;
  }

  protected Int32ArrayObjBase(int[] data, int offset, int length) {
    super(offset, length);
    ints = data;
  }

  public long getLongAt(long idx) {
    return ints[(int) idx];
  }

  public Obj reverse() {
    int last = offset + length - 1;
    int[] revData = new int[length];
    for (int i=0 ; i < length ; i++)
      revData[i] = ints[last-i];
    return new Int32ArrayObj(revData);
  }

  public Obj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > length)
      throw new IndexOutOfBoundsException();
    return new Int32ArraySliceObj(ints, offset + (int) first, (int) len);
  }

  public int hashcodesSum() {
    int sum = 0;
    int end = offset + length;
    for (int i=offset ; i < end ; i++)
      sum += IntObj.hashCode(ints[i]);
    return sum;
  }

  public void copy(long[] array, int destOffset) {
    for (int i=0 ; i < length ; i++)
      array[destOffset+i] = ints[offset+i];
  }

  public void copy(Obj[] array, int destOffset) {
    for (int i=0 ; i < length ; i++)
      array[destOffset+i] = IntObj.get(ints[offset+i]);
  }
}


final class Int32ArrayObj extends Int32ArrayObjBase {
  protected Int32ArrayObj(int[] data) {
    super(data);
  }

  public long getLongAt(long idx) {
    return ints[(int) idx];
  }
}


final class Int32ArraySliceObj extends Int32ArrayObjBase {
  public Int32ArraySliceObj(int[] data, int offset, int length) {
    super(data, offset, length);
  }

  public long getLongAt(long idx) {
    if (idx >= 0 & idx < length)
      return ints[offset + (int) idx];
    else
      throw new ArrayIndexOutOfBoundsException();
  }
}
