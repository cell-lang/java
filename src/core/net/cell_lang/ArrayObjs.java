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
    return PaddedArray.create(obj);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

abstract class ArrayObjBase extends NeSeqObj {
  Obj[] objs;
  int   offset;


  public SeqObj reverse() {
    int len = getSize();
    int last = offset + len - 1;
    Obj[] revObjs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      revObjs[i] = objs[last-i];
    return new ArrayObj(revObjs);
  }

  public Obj[] getArray(Obj[] buffer) {
    int len = getSize();
    if (objs.length != len) {
      objs = Arrays.copyOfRange(objs, offset, offset+len);
      offset = 0;
    }
    return objs;
  }

  public SeqObj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > getSize())
      throw new IndexOutOfBoundsException();
    if (len == 0)
      return EmptySeqObj.singleton;
    return new ArraySliceObj(null, objs, offset + (int) first, (int) len);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void copy(int first, int count, Obj[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = objs[srcOffset+i];
  }

  //////////////////////////////////////////////////////////////////////////////

  protected static long hashcode(Obj[] objs, int offset, int len) {
    return objs[offset].data + (len > 2 ? objs[offset+len/2].data : 0) + (len > 1 ? objs[offset+len-1].data : 0);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class ArrayObj extends ArrayObjBase {
  public ArrayObj(Obj[] objs) {
    int len = objs.length;
    long hashcode = hashcode(objs, 0, len);
    data = seqObjData(len, hashcode);
    this.objs = objs;
  }

  public Obj getObjAt(long idx) {
    return objs[(int) idx];
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class ArraySliceObj extends ArrayObjBase {
  PaddedArray source;

  public ArraySliceObj(PaddedArray source, Obj[] objs, int offset, int len) {
    long hashcode = hashcode(objs, offset, len);
    data = seqObjData(len, hashcode);
    this.objs = objs;
    this.offset = offset;
    this.source = source;
  }

  public Obj getObjAt(long idx) {
    int len = getSize();
    if (idx >= 0 & idx < len)
      return objs[offset + (int) idx];
    else
      throw new ArrayIndexOutOfBoundsException();
  }

  public NeSeqObj append(Obj obj) {
    if (source != null)
      return source.append(offset+getSize(), obj);
    else
      return super.append(obj);
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

      Obj[] newBuffer = new Obj[buffer.length];
      for (int i=0 ; i < idx ; i++)
        newBuffer[i] = buffer[i];
      newBuffer[idx] = obj;
      PaddedArray newArray = new PaddedArray(newBuffer, idx+1);
      return newArray.slice(0, idx+1);
    }
  }
}
