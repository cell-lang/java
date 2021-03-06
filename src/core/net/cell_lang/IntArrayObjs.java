package net.cell_lang;

import java.util.Arrays;


class IntArrayObjs {
  static ByteArrayObj create(byte[] data) {
    Miscellanea._assert(data.length > 0);
    return new ByteArrayObj(data);
  }

  static ShortArrayObj create(short[] data) {
    Miscellanea._assert(data.length > 0);
    return new ShortArrayObj(data);
  }

  static Int32ArrayObj create(int[] data) {
    Miscellanea._assert(data.length > 0);
    return new Int32ArrayObj(data);
  }

  static IntArrayObj create(long[] data) {
    Miscellanea._assert(data.length > 0);
    return new IntArrayObj(data);
  }

  static ByteArrayObj create(byte[] data, int length) {
    Miscellanea._assert(length > 0);
    return new ByteArrayObj(Arrays.copyOf(data, length));
  }

  static ShortArrayObj create(short[] data, int length) {
    Miscellanea._assert(length > 0);
    return new ShortArrayObj(Arrays.copyOf(data, length));
  }

  static Int32ArrayObj create(int[] data, int length) {
    Miscellanea._assert(length > 0);
    return new Int32ArrayObj(Arrays.copyOf(data, length));
  }

  static IntArrayObj create(long[] data, int length) {
    Miscellanea._assert(length > 0);
    return new IntArrayObj(Arrays.copyOf(data, length));
  }

  static UnsignedByteArrayObj createUnsigned(byte[] data) {
    Miscellanea._assert(data.length > 0);
    return new UnsignedByteArrayObj(data);
  }

  static UnsignedByteArrayObj createUnsigned(byte[] data, int length) {
    Miscellanea._assert(length > 0);
    return new UnsignedByteArrayObj(Arrays.copyOf(data, length));
  }

  static IntArraySliceObj createRightPadded(long value) {
    return PaddedIntArray.create(value);
  }

  static IntArraySliceObj append(NeIntSeqObj seq, long value) {
    return PaddedIntArray.create(seq, value);
  }

  static IntArraySliceObj concat(NeIntSeqObj left, NeIntSeqObj right) {
    return PaddedIntArray.create(left, right);
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
          return elt < otherElt ? -1 : 1;
      }
      return 0;
    }
    else
      return super.internalOrder(other);
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
    data = seqObjData(elts.length);
    extraData = neSeqObjExtraData();
    longs = elts;
  }

  public long getLongAt(long idx) {
    return longs[(int) idx];
  }

  @Override
  public IntArrayObjBase packForString() {
    int len = longs.length;

    long min = 0, max = 0;
    for (int i=0 ; i < len ; i++) {
      long elt = longs[i];
      min = elt < min ? elt : min;
      max = elt > max ? elt : max;
      if (min < -2147483648 | max > 2147483647)
        return this;
    }

    if (min >= -128 & max <= 127) {
      byte[] packedElts = new byte[len];
      for (int i=0 ; i < len ; i++)
        packedElts[i] = (byte) longs[i];
      return new ByteArrayObj(packedElts);
    }

    if (min >= -32768 & max < 32768) {
      short[] packedElts = new short[len];
      for (int i=0 ; i < len ; i++)
        packedElts[i] = (short) longs[i];
      return new ShortArrayObj(packedElts);
    }

    int[] packedElts = new int[len];
    for (int i=0 ; i < len ; i++)
      packedElts[i] = (int) longs[i];
    return new Int32ArrayObj(packedElts);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class IntArraySliceObj extends IntArrayObjBase {
  PaddedIntArray source;


  public IntArraySliceObj(PaddedIntArray source, long[] elts, int offset, int len) {
    data = seqObjData(len);
    extraData = neSeqObjExtraData();
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

  public long[] getArray(long[] buffer) {
    int length = getSize();
    long[] copy = new long[length];
    copy(0, length, copy, 0);
    return copy;
  }

  public NeIntSeqObj append(long value) {
    return source != null ? source.append(offset+getSize(), value) : super.append(value);
  }

  public NeIntSeqObj concat(NeIntSeqObj seq) {
    return source != null ? source.concat(offset+getSize(), seq) : super.concat(seq);
  }

  @Override
  public IntArrayObjBase packForString() {
    int len = getSize();

    long min = 0, max = 0;
    for (int i=0 ; i < len ; i++) {
      long elt = longs[offset+i];
      min = elt < min ? elt : min;
      max = elt > max ? elt : max;
      if (min < -2147483648 | max > 2147483647)
        return this;
    }

    if (min >= -128 & max <= 127) {
      byte[] packedElts = new byte[len];
      for (int i=0 ; i < len ; i++)
        packedElts[i] = (byte) longs[offset+i];
      return new ByteArrayObj(packedElts);
    }

    if (min >= -32768 & max < 32768) {
      short[] packedElts = new short[len];
      for (int i=0 ; i < len ; i++)
        packedElts[i] = (short) longs[offset+i];
      return new ShortArrayObj(packedElts);
    }

    int[] packedElts = new int[len];
    for (int i=0 ; i < len ; i++)
      packedElts[i] = (int) longs[offset+i];
    return new Int32ArrayObj(packedElts);
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

  public IntArraySliceObj slice(int offset, int length) {
    return new IntArraySliceObj(this, buffer, offset, length);
  }

  public /*synchronized*/ IntArraySliceObj append(int idx, long value) {
    if (idx == buffer.length) {
      // We run out of space, expanding the array buffer
      int size = buffer.length;
      int newSize = 2 * size;
      long[] newBuffer = new long[newSize];
      for (int i=0 ; i < size ; i++)
        newBuffer[i] = buffer[i];
      newBuffer[idx] = value;
      PaddedIntArray newArray = new PaddedIntArray(newBuffer, idx+1);
      return newArray.slice(0, idx+1);
      //## THINK ABOUT THIS. WOULD IT WORK?
      // buffer = newBuffer;
      // used++;
      // return new IntArraySliceObj(this, buffer, 0, used);
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

  public /*synchronized*/ IntArraySliceObj concat(int idx, NeIntSeqObj seq) {
    int seqLen = seq.getSize();
    int newLen = idx + seqLen;

    if (newLen > buffer.length) {
      // We run out of space, expanding the array buffer
      int size = minBufferSize(newLen);
      long[] newBuffer = new long[size];
      for (int i=0 ; i < idx ; i++)
        newBuffer[i] = buffer[i];
      seq.copy(0, seqLen, newBuffer, idx);
      PaddedIntArray newArray = new PaddedIntArray(newBuffer, newLen);
      return newArray.slice(0, newLen);
      //## THINK ABOUT THIS. WOULD IT WORK?
      // buffer = newBuffer;
      // used = newLen;
      // return new IntArraySliceObj(this, buffer, 0, used);
    }
    else if (idx == used) {
      // There's space for the new elements
      seq.copy(0, seqLen, buffer, idx);
      used = newLen;
      return new IntArraySliceObj(this, buffer, 0, used);
    }
    else {
      // The next slot was already taken. This is supposed to happen only rarely
      Miscellanea._assert(idx < used & idx < buffer.length);

      long[] newBuffer = new long[buffer.length];
      for (int i=0 ; i < idx ; i++)
        newBuffer[i] = buffer[i];
      seq.copy(0, seqLen, newBuffer, idx);
      PaddedIntArray newArray = new PaddedIntArray(newBuffer, newLen);
      return newArray.slice(0, newLen);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public static IntArraySliceObj create(long value) {
    long[] buffer = new long[32];
    buffer[0] = value;
    PaddedIntArray paddedArray = new PaddedIntArray(buffer, 1);
    return paddedArray.slice(0, 1);
  }

  public static IntArraySliceObj create(NeIntSeqObj seq, long value) {
    int len = seq.getSize();
    int size = minBufferSize(len+1);
    long[] buffer = new long[size];
    seq.copy(0, len, buffer, 0);
    buffer[len] = value;
    PaddedIntArray paddedArray = new PaddedIntArray(buffer, len+1);
    return paddedArray.slice(0, len+1);
  }

  public static IntArraySliceObj create(NeIntSeqObj left, NeIntSeqObj right) {
    int leftLen = left.getSize();
    int rightLen = right.getSize();
    int len = leftLen + rightLen;
    int size = minBufferSize(len);
    long[] buffer = new long[size];
    left.copy(0, leftLen, buffer, 0);
    right.copy(0, rightLen, buffer, leftLen);
    PaddedIntArray paddedArray = new PaddedIntArray(buffer, len);
    return paddedArray.slice(0, len);
  }

  public static int minBufferSize(int len) {
    int minSize = (5 * len) / 4;
    int size = 32;
    while (size < minSize)
      size = 2 * size;
    return size;
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
    data = seqObjData(elts.length);
    extraData = neSeqObjExtraData();
    bytes = elts;
  }

  public long getLongAt(long idx) {
    return bytes[(int) idx];
  }
}


final class ByteArraySliceObj extends ByteArrayObjBase {
  public ByteArraySliceObj(byte[] elts, int offset, int len) {
    data = seqObjData(len);
    extraData = neSeqObjExtraData();
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

abstract class UnsignedByteArrayObjBase extends IntArrayObjBase {
  byte[] bytes;


  public SeqObj reverse() {
    int length = getSize();
    int last = offset + length - 1;
    byte[] revData = new byte[length];
    for (int i=0 ; i < length ; i++)
      revData[i] = bytes[last-i];
    return new UnsignedByteArrayObj(revData);
  }

  public SeqObj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > getSize())
      throw new IndexOutOfBoundsException();
    if (len == 0)
      return EmptySeqObj.singleton;
    return new UnsignedByteArraySliceObj(bytes, offset + (int) first, (int) len);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void copy(int first, int count, long[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = unsigned(bytes[srcOffset+i]);
  }

  public void copy(int first, int count, Obj[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = IntObj.get(unsigned(bytes[srcOffset+i]));
  }

  //////////////////////////////////////////////////////////////////////////////

  protected static int unsigned(byte b) {
    return b & 0xFF;
  }
}


final class UnsignedByteArrayObj extends UnsignedByteArrayObjBase {
  protected UnsignedByteArrayObj(byte[] elts) {
    data = seqObjData(elts.length);
    extraData = neSeqObjExtraData();
    bytes = elts;
  }

  public long getLongAt(long idx) {
    return unsigned(bytes[(int) idx]);
  }
}


final class UnsignedByteArraySliceObj extends UnsignedByteArrayObjBase {
  public UnsignedByteArraySliceObj(byte[] elts, int offset, int len) {
    data = seqObjData(len);
    extraData = neSeqObjExtraData();
    bytes = elts;
    this.offset = offset;
  }

  public long getLongAt(long idx) {
    if (idx >= 0 & idx < getSize())
      return unsigned(bytes[offset + (int) idx]);
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
    data = seqObjData(elts.length);
    extraData = neSeqObjExtraData();
    shorts = elts;
  }

  public long getLongAt(long idx) {
    return shorts[(int) idx];
  }
}


final class ShortArraySliceObj extends ShortArrayObjBase {
  public ShortArraySliceObj(short[] elts, int offset, int len) {
    data = seqObjData(len);
    extraData = neSeqObjExtraData();
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
    data = seqObjData(elts.length);
    extraData = neSeqObjExtraData();
    ints = elts;
  }

  public long getLongAt(long idx) {
    return ints[(int) idx];
  }
}


final class Int32ArraySliceObj extends Int32ArrayObjBase {
  public Int32ArraySliceObj(int[] elts, int offset, int len) {
    data = seqObjData(len);
    extraData = neSeqObjExtraData();
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
