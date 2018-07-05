package net.cell_lang;

import java.util.Arrays;


class ArrayObjs {
  static NeSeqObj create(Obj[] objs) {
    return new ArrayObj(objs);
  }

  static NeSeqObj create(Obj[] objs, int length) {
    return new ArrayObj(Arrays.copyOf(objs, length));
  }

  static NeSeqObj createRightPadded(Obj obj) {
    PaddedArray.create(obj);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

abstract class ArrayObjBase extends NeSeqObj {
  Obj[] objs;
  int   offset;


  public Obj reverse() {
    int len = getSize();
    int last = offset + length - 1;
    Obj[] revData = new Obj[length];
    for (int i=0 ; i < len ; i++)
      revObjs[i] = objs[last-i];
    return new ArrayObj(revObjs);
  }

  public Obj[] getArray(Obj[] buffer) {
    int len = getSize();
    if (objs.length != len) {
      objs Arrays.copyOfRange(objs, offset, offset+len);
      offset = 0;
    }
    return objs;
  }

  public Obj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > getSize())
      throw new IndexOutOfBoundsException();
    if (len == 0)
      return EmptySeqObj.singleton;
    return new ArraySliceObj(null, longs, offset + (int) first, (int) len);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void copy(int from, int count, Obj[] array, int destOffset) {
    int srcOffset = offset + from;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = objs[srcOffset+i];
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class ArrayObj extends ArrayObjBase {
  public ArrayObj(Obj[] data) {
    int len = data.length;
    long eltsData = data[0];
    if (len > 1)
      eltsData += data[len-1];
    if (len > 2)
      eltsData += data[len/2];
    data = seqObjData(len, eltsData);
    objs = data;
  }

  public Obj getObjAt(long idx) {
    return objs[(int) idx];
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class ArraySliceObj extends ArrayObjBase {
  PaddedArray source;

  public ArraySliceObj(PaddedArray source, Obj[] data, int offset, int length) {
    long eltsData = data[offset];
    if (length > 1)
      eltsData += data[offset+length-1];
    if (length > 2)
      eltsData += data[offset+length/2];
    data = seqObjData(len, eltsData);
    objs = data;
    this.offset = offset;
  }

  public long getObjAt(long idx) {
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

final class PaddedArray {
  Obj[] buffer;
  int used;

  PaddedArray(Obj[] buffer, int used) {
    this.buffer = buffer;
    this.used = used;
  }

  public static ArraySliceObj create(Obj obj) {
    Obj[] buffer = new Obj[32];
    buffer[0] = obj;
    PaddedArray paddedArray = new PaddedArray(buffer, 1);
    return paddedArray.slice(0, 1);
  }

  public ArraySliceObj slice(int offset, int length) {
    return new ArraySliceObj(this, buffer, offset, length);
  }

  public synchronized ArraySliceObj append(int idx, Obj obj) {
    if (idx == buffer.length) {
      // We run out of space, expanding the array buffer
      int len = buffer.length;
      int newLen = 2 * len;
      Obj[] newBuffer = new Obj[newLen];
      for (int i=0 ; i < len ; i++)
        newBuffer[i] = buffer[i];
      newBuffer[idx] = obj;
      buffer = newBuffer;
      used++;
      return new ArraySliceObj(this, buffer, 0, used);
    }
    else if (idx == used) {
      // There's space for the new element
      buffer[idx] = obj;
      used++;
      return new ArraySliceObj(this, buffer, 0, used);
    }
    else {
      // The next slot was already taken. This is supposed to happen only rarely
      Miscellanea._assert(idx < used & idx < buffer.length);

      long[] newBuffer = new long[buffer.length];
      for (int i=0 ; i < idx ; i++)
        newBuffer[i] = buffer[i];
      newBuffer[idx] = obj;
      PaddedArray newArray = new PaddedArray(newBuffer, idx+1);
      return newArray.slice(0, idx+1);
    }
  }
}
