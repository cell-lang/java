package net.cell_lang;


// Valid slot states:
//   - Value + payload: 32 bit payload - 3 zeros   - 29 bit value
//   - Index + count:   32 bit count   - 3 bit tag - 29 bit index
//     This type of slot can only be stored in a hashed block or passed in and out
//   - Empty:           32 zeros - ArraySliceAllocator.EMPTY_MARKER == 0xFFFFFFFF
//     This type of slot can only be stored in a block, but cannot be passed in or out

class LoadedOneWayBinTable {
  private final static int MIN_CAPACITY = 16;

  private final static int INLINE_SLOT = LoadedOverflowTable.INLINE_SLOT;
  private final static int EMPTY_MARKER = LoadedOverflowTable.EMPTY_MARKER;
  private final static long EMPTY_SLOT = LoadedOverflowTable.EMPTY_SLOT;

  public long[] column = Array.emptyLongArray;
  public LoadedOverflowTable overflowTable = new LoadedOverflowTable();
  public int count = 0;

  //////////////////////////////////////////////////////////////////////////////

  private int[] _data = new int[1]; // Used as an output argument inside delete(..)

  //////////////////////////////////////////////////////////////////////////////

  private static int low(long slot) {
    return LoadedOverflowTable.low(slot);
  }

  private static int high(long slot) {
    return LoadedOverflowTable.high(slot);
  }

  private static int tag(int word) {
    return LoadedOverflowTable.tag(word);
  }

  private static boolean isEmpty(long slot) {
    return slot == EMPTY_SLOT;
  }

  private static boolean isIndex(long slot) {
    return slot != EMPTY_SLOT && tag(low(slot)) != LoadedOverflowTable.INLINE_SLOT;
  }

  private static int count(long slot) {
    return LoadedOverflowTable.count(slot);
  }

  // private static int value(long slot) {
  //   return LoadedOverflowTable.value(slot);
  // }

  private static long slot(int low, int high) {
    return LoadedOverflowTable.combine(low, high);
  }

  //////////////////////////////////////////////////////////////////////////////

  private void set(int index, long value) {
    column[index] = value;
  }

  private void set(int index, int low, int high) {
    set(index, slot(low, high));
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean contains(int surr1, int surr2) {
    return payload(surr1, surr2) != 0xFFFFFFFF;
  }

  public boolean containsKey(int surr1) {
    return surr1 < column.length && !isEmpty(column[surr1]);
  }

  public int payload(int surr1, int surr2) {
    if (surr1 < column.length) {
      long slot = column[surr1];
      if (!isEmpty(slot)) {
        if (isIndex(slot))
          return overflowTable.lookup(slot, surr2);
        else if (low(slot) == surr2)
          return high(slot);
      }
    }
    return 0xFFFFFFFF;
  }

  public int[] restrict(int surr) {
    if (surr >= column.length)
      return Array.emptyIntArray;

    long slot = column[surr];

    if (isEmpty(slot))
      return Array.emptyIntArray;

    if (isIndex(slot)) {
      int count = count(slot);
      int[] surrs_idxs = new int[count];
      overflowTable.copy(slot, surrs_idxs, null);
      return surrs_idxs;
    }

    return new int[] {low(slot)};
  }

  public int restrict(int surr, int[] output) {
    if (surr >= column.length)
      return 0;

    long slot = column[surr];

    if (isEmpty(slot))
      return 0;

    if (isIndex(slot)) {
      overflowTable.copy(slot, output, null);
      return count(slot);
    }

    output[0] = low(slot);
    return 1;
  }

  public int lookup(int surr) {
    if (surr >= column.length)
      return -1;
    long slot = column[surr];
    if (isEmpty(slot))
      return -1;
    if (isIndex(slot))
      throw Miscellanea.internalFail();
    // Miscellanea._assert(tag(low(slot)) == INLINE_SLOT);
    return low(slot);
  }

  public int count(int surr) {
    if (surr >= column.length)
      return 0;
    long slot = column[surr];
    if (isEmpty(slot))
      return 0;
    if (isIndex(slot))
      return count(slot);
    return 1;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void insertUnique(int surr1, int surr2, int data) {
    int size = column.length;
    if (surr1 >= size)
      resize(surr1);

    long slot = column[surr1];

    if (isEmpty(slot)) {
      set(surr1, surr2, data);
      count++;
      return;
    }

    // Miscellanea._assert(low(slot) != surr2);

    long updatedSlot = overflowTable.insertUnique(slot, surr2, data);
    // Miscellanea._assert(updatedSlot != slot);

    set(surr1, updatedSlot);
    count++;
  }

  public int delete(int surr1, int surr2) {
    if (surr1 >= column.length)
      return 0xFFFFFFFF;

    long slot = column[surr1];

    if (isEmpty(slot))
      return 0xFFFFFFFF;

    if (isIndex(slot)) {
      long updatedSlot = overflowTable.delete(slot, surr2, _data);
      if (updatedSlot != slot) {
        set(surr1, updatedSlot);
        count--;
        return _data[0];
      }
      else
        return 0xFFFFFFFF;
    }

    // Miscellanea._assert(tag(low(slot)) == INLINE_SLOT);

    if (low(slot) == surr2) {
      int data = high(slot);
      set(surr1, EMPTY_SLOT);
      count--;
      return data;
    }
    else
      return 0xFFFFFFFF;
  }

  public void deleteByKey(int surr1, int[] surrs2, int[] data) {
    if (surr1 >= column.length)
      return;

    long slot = column[surr1];

    if (isEmpty(slot))
      return;

    set(surr1, EMPTY_SLOT);

    if (isIndex(slot)) {
      int slotCount = count(slot);
      overflowTable.copy(slot, surrs2, data);
      overflowTable.delete(slot);
      count -= slotCount;
    }
    else {
      // Miscellanea._assert(tag(low(slot)) == INLINE_SLOT);
      surrs2[0] = low(slot);
      data[0] = high(slot);
      count--;
    }
  }

  public boolean isMap() {
    for (int i=0 ; i < column.length ; i++)
      if (isIndex(column[i]))
        return false;
    return true;
  }

  public int[] copy() {
    int[] data = new int[2 * count];
    int next = 0;
    for (int i=0 ; i < column.length ; i++) {
      long slot = column[i];
      if (!isEmpty(slot)) {
        if (isIndex(slot)) {
          int slotCount = count(slot);
          for (int j=0 ; j < slotCount ; j++)
            data[next+2*j] = i;
          overflowTable.copy(slot, data, null, next + 1, 2);
          next += 2 * slotCount;
        }
        else {
          data[next++] = i;
          data[next++] = low(slot);
        }
      }
    }
    // Miscellanea._assert(next == 2 * count);
    return data;
  }

  public int[] copySym(int eqCount) {
    int[] data = new int[count+eqCount];

    int[] buffer = new int[32];

    int next = 0;
    for (int surr1 = 0 ; surr1 < column.length ; surr1++) {
      long slot = column[surr1];
      if (!isEmpty(slot)) {
        if (isIndex(slot)) {
          int slotCount = count(slot);
          if (slotCount > buffer.length)
            buffer = new int[Array.capacity(buffer.length, slotCount)];
          int _count = restrict(surr1, buffer);
          // Miscellanea._assert(_count == slotCount);
          for (int i=0 ; i < slotCount ; i++) {
            int surr2 = buffer[i];
            if (surr1 <= surr2) {
              data[next++] = surr1;
              data[next++] = surr2;
            }
          }
        }
        else {
          int low = low(slot);
          if (surr1 <= low) {
            data[next++] = surr1;
            data[next++] = low(slot);
          }
        }
      }
    }
    // Miscellanea._assert(next == count + eqCount);
    return data;
  }

  //////////////////////////////////////////////////////////////////////////////

  private void resize(int index) {
    int size = column.length;
    int newSize = size == 0 ? MIN_CAPACITY : 2 * size;
    while (index >= newSize)
      newSize *= 2;
    long[] newColumn = new long[newSize];
    Array.copy(column, newColumn, size);
    Array.fill(newColumn, size, newSize - size, EMPTY_SLOT);
    column = newColumn;
  }
}
