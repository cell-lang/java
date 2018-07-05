package net.cell_lang;


final class IntRopeObj extends NeIntSeqObj {
  NeIntSeqObj left;
  NeIntSeqObj right;
  int depth;
  boolean balanced;


  IntRopeObj(NeIntSeqObj left, NeIntSeqObj right) {
    int leftLen = left.getSize();
    int rightLen = right.getSize();
    int len = leftLen + rightLen;
    long hashcode = left.getLongAt(0) + right.getLongAt(rightLen-1);
    if (len > 2) {
      int midIdx = len / 2;
      hashcode += midIdx < leftLen ? left.getLongAt(midIdx) : right.getLongAt(midIdx-leftLen);
    }
    this.left = left;
    this.right = right;
    depth = 1 + Math.max(left.depth(), right.depth());
  }

  public long getLongAt(long idx) {
    int leftLen = left.getSize();
    if (idx < leftLen)
      return left.getLongAt(idx);
    else
      return right.getLongAt(idx - leftLen);
  }

  public SeqObj reverse() {
    int len = getSize();
    int leftLen = left.getSize();
    int rightLen = len - rightLen;
    long[] array = new long[len];

    right.copy(0, rightLen, array, 0);
    reverseRange(array, 0, rightLen);

    left.copy(0, leftLen, array, rightLen);
    reverseRange(array, rightLen, leftLen);

    return IntArrayObjs.create(array);
  }

  public long[] getArray(long[] buffer) {
    int len = getSize();
    int leftLen = left.getSize();
    int rightLen = right.getSize();
    long[] array = new long[len];
    left.copy(0, leftLen, array, 0);
    right.copy(0, rightLen, array, leftLen);
    return array;
  }

  public Obj[] getArray(Obj[] buffer) {
    int len = getSize();
    int leftLen = left.getSize();
    int rightLen = right.getSize();
    Obj[] array = new Obj[len];
    left.copy(0, leftLen, array, 0);
    right.copy(0, rightLen, array, leftLen);
    return array;
  }

  public SeqObj getSlice(long first, long count) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND count ARE NONNEGATIVE?
    if (first + count > getSize())
      throw new IndexOutOfBoundsException();

    if (count == 0)
      return EmptySeqObj.singleton;

    int leftLen = left.getSize();

    if (first < leftLen)
      if (first + count <= leftLen)
        return left.getSlice(first, count);
      else
        return create(
          (NeIntSeqObj) left.getSlice(first, leftLen-first),
          (NeIntSeqObj) right.getSlice(0, count-leftLen)
        );
    else
      return right.getSlice(first - leftLen, count);
  }

  public NeIntSeqObj append(long value) {
    return create(left, (NeIntSeqObj) right.append(value)); //## TRY TO REMOVE THE CAST
  }

  public NeIntSeqObj concat(NeIntSeqObj seq) {
    return create(left, right.concat(seq));
  }

  //////////////////////////////////////////////////////////////////////////////

  public int packedRanges(int minSize, int offset, int[] offsets, NeSeqObj[] ranges, int writeOffset) {
    if (balanced) {
      offsets[writeOffset] = offset;
      ranges[writeOffset] = this;
      return writeOffset + 1;
    }
    else {
      writeOffset = left.packedRanges(minSize, offset, offsets, ranges, writeOffset);
      return right.packedRanges(minSize, offset+left.getSize(), offsets, ranges, writeOffset);
    }
  }

  public int depth() {
    return depth;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void copy(int first, int count, long[] array, int offset) {
    int leftLen = left.getSize();

    if (first < leftLen) {
      if (first + count <= leftLen) {
        left.copy(first, count, array, offset);
        return;
      }
      else {
        int leftCount = leftLen - first;
        left.copy(first, leftCount, array, offset);
        count -= leftCount;
        offset += leftCount;
      }
    }

    right.copy(first-leftLen, count, array, offset);
  }

  public void copy(int first, int count, Obj[] array, int offset) {
    int leftLen = left.getSize();

    if (first < leftLen) {
      if (first + count <= leftLen) {
        left.copy(first, count, array, offset);
        return;
      }
      else {
        int leftCount = leftLen - first;
        left.copy(first, leftCount, array, offset);
        count -= leftCount;
        offset += leftCount;
      }
    }

    right.copy(first-leftLen, count, array, offset);
  }

  //////////////////////////////////////////////////////////////////////////////

  public static NeIntSeqObj create(NeIntSeqObj left, NeIntSeqObj right) {
    IntRopeObj rope = new IntRopeObj(left, right);
    if (rope.depth() <= 64)
      return rope;

    int ropeLen = rope.getSize();
    int minLeafLen = Math.max(ropeLen / 16, 32);
    int[] offsets = new int[64];
    NeSeqObj[] ranges = new NeIntSeqObj[64];
    int count = rope.packedRanges(minLeafLen, 0, offsets, ranges, 0);

    NeIntSeqObj[] leaves = new NeIntSeqObj[32];
    int idx = 0;
    int done = 0;
    for (int i=0 ; i < count ; i++) {
      int offset = offsets[i];
      NeIntSeqObj range = (NeIntSeqObj) ranges[i];
      if (offset != done)
        leaves[idx++] = rope.packedRange(done, offset-done);
      leaves[idx++] = (NeIntSeqObj) range;
      done = offset + range.getSize();
    }
    if (done != ropeLen)
      leaves[idx++] = rope.packedRange(done, ropeLen-done);

    IntRopeObj res = (IntRopeObj) create(leaves, 0, idx);
    res.balanced = true;
    return res;
  }

  private NeIntSeqObj packedRange(int start, int len) {
    long[] array = new long[len];
    copy(start, len, array, 0);
    return IntArrayObjs.create(array);
  }

  //////////////////////////////////////////////////////////////////////////////

  private static NeIntSeqObj create(NeIntSeqObj[] leaves, int offset, int count) {
    if (count == 1)
      return leaves[offset];

    int half = count / 2;
    NeIntSeqObj left = create(leaves, offset, half);
    NeIntSeqObj right = create(leaves, offset+half, count-half);
    return new IntRopeObj(left, right);
  }

  private static void reverseRange(long[] array, int first, int count) {
    int last = first + count - 1;
    for (int i=0 ; i < count ; i++) {
      int ri = last - i;
      long tmp = array[i];
      array[i] = array[ri];
      array[ri] = tmp;
    }
  }
}