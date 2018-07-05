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

  static IntArraySliceObj createRightPadded(long value) {
    return PaddedIntArray.create(value);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

abstract class IntArrayObjBase extends NeIntSeqObj {
  int    offset;
  long[] longs;
  Obj[]  objs;


  public SeqObj reverse() {
    int length = getSize();
    int last = offset + length - 1;
    long[] revData = new long[length];
    for (int i=0 ; i < length ; i++)
      revData[i] = longs[last-i];
    return new IntArrayObj(revData);
  }

  public long[] getArray(long[] buffer) {
    if (longs == null) {
      int length = getSize();
      longs = new long[length];
      copy(0, length, longs, 0);
    }
    return longs;
  }

  public Obj[] getArray(Obj[] buffer) {
    if (objs == null) {
      int length = getSize();
      objs = new Obj[length];
      copy(0, length, objs, 0);
    }
    return objs;
  }

  public SeqObj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > getSize())
      throw new IndexOutOfBoundsException();
    if (len == 0)
      return EmptySeqObj.singleton;
    return new IntArraySliceObj(null, longs, offset + (int) first, (int) len);
  }

  public NeIntSeqObj append(long value) {
    return IntRopeObj.create(this, PaddedIntArray.create(value));
  }

  public NeIntSeqObj concat(NeIntSeqObj seq) {
    return IntRopeObj.create(this, seq);
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
          return (int) (elt - otherElt);
      }
      return 0;
    }
    else
      super.internalOrder(other);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void copy(int first, int count, long[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = longs[srcOffset+i];
  }

  public void copy(int first, int count, Obj[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = IntObj.get(longs[srcOffset+i]);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class IntArrayObj extends IntArrayObjBase {
  public IntArrayObj(long[] elts) {
    int len = elts.length;
    long hashcode = elts[0] + (len > 2 ? elts[len/2] : 0) + (len > 1 ? elts[len-1] : 0);
    data = seqObjData(len, hashcode);
    longs = elts;
  }

  public long getLongAt(long idx) {
    return longs[(int) idx];
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class IntArraySliceObj extends IntArrayObjBase {
  PaddedIntArray source;


  public IntArraySliceObj(PaddedIntArray source, long[] elts, int offset, int len) {
    long hashcode = elts[offset] + (len > 2 ? elts[offset+len/2] : 0) + (len > 1 ? elts[offset+len-1] : 0);
    data = seqObjData(len, hashcode);
    longs = elts;
    this.offset = offset;
    this.source = source;
  }

  public long getLongAt(long idx) {
    if (idx >= 0 & idx < getSize())
      return longs[offset + (int) idx];
    else
      throw new ArrayIndexOutOfBoundsException();
  }

  public NeIntSeqObj append(long value) {
    if (source != null)
      return source.append(offset+getSize(), value);
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

  public static IntArraySliceObj create(long value) {
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


  public SeqObj reverse() {
    int length = getSize();
    int last = offset + length - 1;
    byte[] revData = new byte[length];
    for (int i=0 ; i < length ; i++)
      revData[i] = bytes[last-i];
    return new ByteArrayObj(revData);
  }

  public SeqObj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > getSize())
      throw new IndexOutOfBoundsException();
    if (len == 0)
      return EmptySeqObj.singleton;
    return new ByteArraySliceObj(bytes, offset + (int) first, (int) len);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void copy(int first, int count, long[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = bytes[srcOffset+i];
  }

  public void copy(int first, int count, Obj[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = IntObj.get(bytes[srcOffset+i]);
  }
}


final class ByteArrayObj extends ByteArrayObjBase {
  protected ByteArrayObj(byte[] elts) {
    int len = elts.length;
    long hashcode = elts[0] + (len > 2 ? elts[len/2] : 0) + (len > 1 ? elts[len-1] : 0);
    data = seqObjData(len, hashcode);
    bytes = elts;
  }

  public long getLongAt(long idx) {
    return bytes[(int) idx];
  }
}


final class ByteArraySliceObj extends ByteArrayObjBase {
  public ByteArraySliceObj(byte[] elts, int offset, int len) {
    long hashcode = elts[offset] + (len > 2 ? elts[offset+len/2] : 0) + (len > 1 ? elts[offset+len-1] : 0);
    data = seqObjData(len, hashcode);
    bytes = elts;
    this.offset = offset;
  }

  public long getLongAt(long idx) {
    if (idx >= 0 & idx < getSize())
      return bytes[offset + (int) idx];
    else
      throw new ArrayIndexOutOfBoundsException();
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

abstract class ShortArrayObjBase extends IntArrayObjBase {
  short[] shorts;


  public SeqObj reverse() {
    int length = getSize();
    int last = offset + length - 1;
    short[] revData = new short[length];
    for (int i=0 ; i < length ; i++)
      revData[i] = shorts[last-i];
    return new ShortArrayObj(revData);
  }

  public SeqObj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > getSize())
      throw new IndexOutOfBoundsException();
    if (len == 0)
      return EmptySeqObj.singleton;
    return new ShortArraySliceObj(shorts, offset + (int) first, (int) len);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void copy(int first, int count, long[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = shorts[srcOffset+i];
  }

  public void copy(int first, int count, Obj[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = IntObj.get(shorts[srcOffset+i]);
  }
}


final class ShortArrayObj extends ShortArrayObjBase {
  protected ShortArrayObj(short[] elts) {
    int len = elts.length;
    long hashcode = elts[0] + (len > 2 ? elts[len/2] : 0) + (len > 1 ? elts[len-1] : 0);
    data = seqObjData(len, hashcode);
    shorts = elts;
  }

  public long getLongAt(long idx) {
    return shorts[(int) idx];
  }
}


final class ShortArraySliceObj extends ShortArrayObjBase {
  public ShortArraySliceObj(short[] elts, int offset, int len) {
    long hashcode = elts[offset] + (len > 2 ? elts[offset+len/2] : 0) + (len > 1 ? elts[offset+len-1] : 0);
    data = seqObjData(len, hashcode);
    shorts = elts;
    this.offset = offset;
  }

  public long getLongAt(long idx) {
    if (idx >= 0 & idx < getSize())
      return shorts[offset + (int) idx];
    else
      throw new ArrayIndexOutOfBoundsException();
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

abstract class Int32ArrayObjBase extends IntArrayObjBase {
  int[] ints;


  public SeqObj reverse() {
    int length = getSize();
    int last = offset + length - 1;
    int[] revData = new int[length];
    for (int i=0 ; i < length ; i++)
      revData[i] = ints[last-i];
    return new Int32ArrayObj(revData);
  }

  public SeqObj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > getSize())
      throw new IndexOutOfBoundsException();
    if (len == 0)
      return EmptySeqObj.singleton;
    return new Int32ArraySliceObj(ints, offset + (int) first, (int) len);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void copy(int first, int count, long[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = ints[srcOffset+i];
  }

  public void copy(int first, int count, Obj[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = IntObj.get(ints[srcOffset+i]);
  }
}


final class Int32ArrayObj extends Int32ArrayObjBase {
  protected Int32ArrayObj(int[] elts) {
    int len = elts.length;
    long hashcode = elts[0] + (len > 2 ? elts[len/2] : 0) + (len > 1 ? elts[len-1] : 0);
    data = seqObjData(len, hashcode);
    ints = elts;
  }

  public long getLongAt(long idx) {
    return ints[(int) idx];
  }
}


final class Int32ArraySliceObj extends Int32ArrayObjBase {
  public Int32ArraySliceObj(int[] elts, int offset, int len) {
    long hashcode = elts[offset] + (len > 2 ? elts[offset+len/2] : 0) + (len > 1 ? elts[offset+len-1] : 0);
    data = seqObjData(len, hashcode);
    ints = elts;
    this.offset = offset;
  }

  public long getLongAt(long idx) {
    if (idx >= 0 & idx < getSize())
      return ints[offset + (int) idx];
    else
      throw new ArrayIndexOutOfBoundsException();
  }
}
