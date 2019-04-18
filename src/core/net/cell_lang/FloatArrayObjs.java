package net.cell_lang;

import java.util.Arrays;


class FloatArrayObjs {
  static NeSeqObj create(double[] values) {
    Miscellanea._assert(values.length > 0);
    return new FloatArrayObj(values);
  }

  static NeSeqObj create(double[] values, int length) {
    Miscellanea._assert(length > 0);
    return new FloatArrayObj(Arrays.copyOf(values, length));
  }

  static NeSeqObj createRightPadded(double value) {
    return PaddedFloatArray.create(value);
  }

  static FloatArraySliceObj append(NeFloatSeqObj seq, double value) {
    return PaddedFloatArray.create(seq, value);
  }

  static FloatArraySliceObj concat(NeFloatSeqObj left, NeFloatSeqObj right) {
    return PaddedFloatArray.create(left, right);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

abstract class FloatArrayObjBase extends NeFloatSeqObj {
  int offset;
  double[] elts;


  public SeqObj reverse() {
    int length = getSize();
    int last = offset + length - 1;
    double[] revData = new double[length];
    for (int i=0 ; i < length ; i++)
      revData[i] = elts[last-i];
    return new FloatArrayObj(revData);
  }

  public double[] getArray(double[] buffer) {
    int len = getSize();
    if (len == elts.length)
      return elts;
    if (buffer == null)
      buffer = new double[len];
    copy(0, len, buffer, 0);
    return buffer;
  }

  public Obj[] getArray(Obj[] buffer) {
    int len = getSize();
    if (buffer == null)
      buffer = new Obj[len];
    copy(0, len, buffer, 0);
    return buffer;
  }

  public SeqObj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > getSize())
      throw new IndexOutOfBoundsException();
    if (len == 0)
      return EmptySeqObj.singleton;
    return new FloatArraySliceObj(null, elts, offset + (int) first, (int) len);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    if (other instanceof FloatArrayObjBase) {
      Miscellanea._assert(getSize() == other.getSize());

      FloatArrayObjBase otherArray = (FloatArrayObjBase) other;

      int len = getSize();
      int otherOffset = otherArray.offset;
      double[] otherDoubles = otherArray.elts;
      for (int i=0 ; i < len ; i++) {
        long elt = floatObjData(elts[offset + i]);
        long otherElt = floatObjData(otherDoubles[otherOffset + i]);
        if (elt != otherElt)
          return elt < otherElt ? -1 : 1;
      }
      return 0;
    }
    else
      return super.internalOrder(other);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void copy(int first, int count, double[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = elts[srcOffset+i];
  }

  public void copy(int first, int count, Obj[] array, int destOffset) {
    int srcOffset = offset + first;
    for (int i=0 ; i < count ; i++)
      array[destOffset+i] = new FloatObj(elts[srcOffset+i]);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class FloatArrayObj extends FloatArrayObjBase {
  public FloatArrayObj(double[] elts) {
    data = seqObjData(elts.length);
    extraData = neSeqObjExtraData();
    this.elts = elts;
  }

  public double getDoubleAt(long idx) {
    return elts[(int) idx];
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class FloatArraySliceObj extends FloatArrayObjBase {
  PaddedFloatArray source;


  public FloatArraySliceObj(PaddedFloatArray source, double[] elts, int offset, int len) {
    data = seqObjData(len);
    extraData = neSeqObjExtraData();
    this.elts = elts;
    this.offset = offset;
    this.source = source;
  }

  public double getDoubleAt(long idx) {
    if (idx >= 0 & idx < getSize())
      return elts[offset + (int) idx];
    else
      throw new ArrayIndexOutOfBoundsException();
  }

  public NeFloatSeqObj append(double value) {
    return source != null ? source.append(offset+getSize(), value) : super.append(value);
  }

  public NeFloatSeqObj concat(NeFloatSeqObj seq) {
    return source != null ? source.concat(offset+getSize(), seq) : super.concat(seq);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class PaddedFloatArray {
  double[] buffer;
  int used;

  PaddedFloatArray(double[] buffer, int used) {
    this.buffer = buffer;
    this.used = used;
  }

  public FloatArraySliceObj slice(int offset, int length) {
    return new FloatArraySliceObj(this, buffer, offset, length);
  }

  public synchronized FloatArraySliceObj append(int idx, double value) {
    if (idx == buffer.length) {
      // We run out of space, expanding the array buffer
      int size = buffer.length;
      int newSize = 2 * size;
      double[] newBuffer = new double[newSize];
      for (int i=0 ; i < size ; i++)
        newBuffer[i] = buffer[i];
      newBuffer[idx] = value;
      PaddedFloatArray newArray = new PaddedFloatArray(newBuffer, idx+1);
      return newArray.slice(0, idx+1);
      //## THINK ABOUT THIS. WOULD IT WORK?
      // buffer = newBuffer;
      // used++;
      // return new FloatArraySliceObj(this, buffer, 0, used);
    }
    else if (idx == used) {
      // There's space for the new element
      buffer[idx] = value;
      used++;
      return new FloatArraySliceObj(this, buffer, 0, used);
    }
    else {
      // The next slot was already taken. This is supposed to happen only rarely
      Miscellanea._assert(idx < used & idx < buffer.length);

      double[] newBuffer = new double[buffer.length];
      for (int i=0 ; i < idx ; i++)
        newBuffer[i] = buffer[i];
      newBuffer[idx] = value;
      PaddedFloatArray newArray = new PaddedFloatArray(newBuffer, idx+1);
      return newArray.slice(0, idx+1);
    }
  }

  public synchronized FloatArraySliceObj concat(int idx, NeFloatSeqObj seq) {
    int seqLen = seq.getSize();
    int newLen = idx + seqLen;

    if (newLen > buffer.length) {
      // We run out of space, expanding the array buffer
      int size = minBufferSize(newLen);
      double[] newBuffer = new double[size];
      for (int i=0 ; i < idx ; i++)
        newBuffer[i] = buffer[i];
      seq.copy(0, seqLen, newBuffer, idx);
      PaddedFloatArray newArray = new PaddedFloatArray(newBuffer, newLen);
      return newArray.slice(0, newLen);
      //## THINK ABOUT THIS. WOULD IT WORK?
      // buffer = newBuffer;
      // used = newLen;
      // return new FloatArraySliceObj(this, buffer, 0, used);
    }
    else if (idx == used) {
      // There's space for the new elements
      seq.copy(0, seqLen, buffer, idx);
      used = newLen;
      return new FloatArraySliceObj(this, buffer, 0, used);
    }
    else {
      // The next slot was already taken. This is supposed to happen only rarely
      Miscellanea._assert(idx < used & idx < buffer.length);

      double[] newBuffer = new double[buffer.length];
      for (int i=0 ; i < idx ; i++)
        newBuffer[i] = buffer[i];
      seq.copy(0, seqLen, newBuffer, idx);
      PaddedFloatArray newArray = new PaddedFloatArray(newBuffer, newLen);
      return newArray.slice(0, newLen);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public static FloatArraySliceObj create(double value) {
    double[] buffer = new double[32];
    buffer[0] = value;
    PaddedFloatArray paddedArray = new PaddedFloatArray(buffer, 1);
    return paddedArray.slice(0, 1);
  }

  public static FloatArraySliceObj create(NeFloatSeqObj seq, double value) {
    int len = seq.getSize();
    int size = minBufferSize(len+1);
    double[] buffer = new double[size];
    seq.copy(0, len, buffer, 0);
    buffer[len] = value;
    PaddedFloatArray paddedArray = new PaddedFloatArray(buffer, len+1);
    return paddedArray.slice(0, len+1);
  }

  public static FloatArraySliceObj create(NeFloatSeqObj left, NeFloatSeqObj right) {
    int leftLen = left.getSize();
    int rightLen = right.getSize();
    int len = leftLen + rightLen;
    int size = minBufferSize(len);
    double[] buffer = new double[size];
    left.copy(0, leftLen, buffer, 0);
    right.copy(0, rightLen, buffer, leftLen);
    PaddedFloatArray paddedArray = new PaddedFloatArray(buffer, len);
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
