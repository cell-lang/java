package net.cell_lang;


final class RopeObj extends NeSeqObj {
  NeSeqObj left;
  NeSeqObj right;
  int depth;
  boolean balanced;


  RopeObj(NeSeqObj left, NeSeqObj right) {
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
    Obj[] array = new Obj[len];

    right.copy(0, rightLen, array, 0);
    reverseRange(array, 0, rightLen);

    left.copy(0, leftLen, array, rightLen);
    reverseRange(array, rightLen, leftLen);

    return ArrayObjs.create(array);
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
          (NeSeqObj) left.getSlice(first, leftLen-first),
          (NeSeqObj) right.getSlice(0, count-leftLen)
        );
    else
      return right.getSlice(first - leftLen, count);
  }

  public NeSeqObj append(long value) {
    return create(left, (NeSeqObj) right.append(value)); //## TRY TO REMOVE THE CAST
  }

  public NeSeqObj concat(NeSeqObj seq) {
    return create(left, (NeSeqObj) right.concat(seq));
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

  public static NeSeqObj create(NeSeqObj left, NeSeqObj right) {
    RopeObj rope = new RopeObj(left, right);
    if (rope.depth() <= 64)
      return rope;

    int ropeLen = rope.getSize();
    int minLeafLen = Math.max(ropeLen / 16, 32);
    int[] offsets = new int[64];
    NeSeqObj[] ranges = new NeSeqObj[64];
    int count = rope.packedRanges(minLeafLen, 0, offsets, ranges, 0);

    NeSeqObj[] leaves = new NeSeqObj[32];
    int idx = 0;
    int done = 0;
    for (int i=0 ; i < count ; i++) {
      int offset = offsets[i];
      NeSeqObj range = (NeSeqObj) ranges[i];
      if (offset != done)
        leaves[idx++] = rope.packedRange(done, offset-done);
      leaves[idx++] = (NeSeqObj) range;
      done = offset + range.getSize();
    }
    if (done != ropeLen)
      leaves[idx++] = rope.packedRange(done, ropeLen-done);

    RopeObj res = (RopeObj) create(leaves, 0, idx);
    res.balanced = true;
    return res;
  }

  private NeSeqObj packedRange(int start, int len) {
    Obj[] array = new Obj[len];
    copy(start, len, array, 0);
    return ArrayObjs.create(array);
  }

  //////////////////////////////////////////////////////////////////////////////

  private static NeSeqObj create(NeSeqObj[] leaves, int offset, int count) {
    if (count == 1)
      return leaves[offset];

    int half = count / 2;
    NeSeqObj left = create(leaves, offset, half);
    NeSeqObj right = create(leaves, offset+half, count-half);
    return new RopeObj(left, right);
  }

  private static void reverseRange(Obj[] array, int first, int count) {
    int last = first + count - 1;
    for (int i=0 ; i < count ; i++) {
      int ri = last - i;
      Obj tmp = array[i];
      array[i] = array[ri];
      array[ri] = tmp;
    }
  }
}