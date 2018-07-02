package net.cell_lang;


class IntRopeObj extends IntSeqObj {
  IntSeqObj left;
  IntSeqObj right;

  IntRopeObj(IntSeqObj left, IntSeqObj right) {
    super(left.length + right.length);
    this.left = left;
    this.right = right;
  }

  public static IntSeqObj make(IntSeqObj left, IntSeqObj right) {
    return new IntRopeObj(left, right);
  }

  public long getLongAt(long idx) {
    if (idx < left.length)
      return left.getLongAt(idx);
    else
      return right.getLongAt(idx - left.length);
  }

  public Obj reverse() {
    long[] array = new long[length];
    left.copy(array);
    right.copy(array, left.length);
    int len = array.length;
    int last = len - 1;
    for (int i=0 ; i < len / 2 ; i++) {
      int ri = last - i;
      long tmp = array[i];
      array[i] = array[ri];
      array[ri] = tmp;
    }
    return IntArrayObjs.create(array);
  }

  public long[] getArray(long[] buffer) {
    long[] array = new long[length];
    left.copy(array);
    right.copy(array, left.length);
    return array;
  }

  public Obj[] getArray(Obj[] buffer) {
    Obj[] array = new Obj[length];
    left.copy(array);
    right.copy(array, left.length);
    return array;
  }

  public Obj getSlice(long first, long count) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND count ARE NONNEGATIVE?
    if (first + count > length)
      throw new IndexOutOfBoundsException();

    int left_len = left.length;

    if (first < left_len)
      if (first + count <= left_len)
        return left.getSlice(first, count);
      else
        return make(
          (IntSeqObj) left.getSlice(first, left_len-first),
          (IntSeqObj) right.getSlice(0, count-left_len)
        );
    else
      return right.getSlice(first - left_len, count);
  }

  public IntSeqObj append(long value) {
    return make(left, right.append(value));
  }

  public IntSeqObj concat(IntSeqObj seq) {
    return make(left, seq.concat(seq));
  }

  public int hashcodesSum() {
    return left.hashcodesSum() + right.hashcodesSum();
  }

  public void copy(long[] array, int offset) {
    left.copy(array, offset);
    right.copy(array, offset+left.length);
  }

  public void copy(Obj[] array, int offset) {
    left.copy(array, offset);
    right.copy(array, offset+left.length);
  }
}