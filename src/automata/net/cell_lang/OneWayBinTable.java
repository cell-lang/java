package net.cell_lang;


// Valid slot states:
//   - Value + payload: 32 bit payload - 3 zeros   - 29 bit value
//   - Index + count:   32 bit count   - 3 bit tag - 29 bit index
//     This type of slot can only be stored in a hashed block or passed in and out
//   - Empty:           32 zeros - ArraySliceAllocator.EMPTY_MARKER == 0xFFFFFFFF
//     This type of slot can only be stored in a block, but cannot be passed in or out

class OneWayBinTable {
  private final static int MIN_CAPACITY = 16;

  private final static int INLINE_SLOT = OverflowTable.INLINE_SLOT;
  private final static int EMPTY_MARKER = OverflowTable.EMPTY_MARKER;
  private final static long EMPTY_SLOT = OverflowTable.EMPTY_SLOT;

  public long[] column = Array.emptyLongArray;
  public OverflowTable overflowTable = new OverflowTable();
  public int count = 0;

  //////////////////////////////////////////////////////////////////////////////

  private static int low(long slot) {
    return OverflowTable.low(slot);
  }

  private static int high(long slot) {
    return OverflowTable.high(slot);
  }

  private static int tag(int word) {
    return OverflowTable.tag(word);
  }

  private static boolean isEmpty(long slot) {
    return slot == EMPTY_SLOT;
  }

  private static boolean isIndex(long slot) {
    return slot != EMPTY_SLOT && tag(low(slot)) != OverflowTable.INLINE_SLOT;
  }

  private static int count(long slot) {
    return OverflowTable.count(slot);
  }

  // private static int value(long slot) {
  //   return OverflowTable.value(slot);
  // }

  private static long slot(int low, int high) {
    return OverflowTable.combine(low, high);
  }

  //////////////////////////////////////////////////////////////////////////////

  private void set(int index, long value) {
    column[index] = value;
  }

  private void set (int index, int low, int high) {
    set(index, slot(low, high));
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean contains(int surr1, int surr2) {
    if (surr1 >= column.length)
      return false;

    long slot = column[surr1];

    if (isEmpty(slot))
      return false;

    if (isIndex(slot))
      return overflowTable.contains(slot, surr2);

    if (low(slot) == surr2)
      return true;

    return high(slot) == surr2;
  }

  public boolean containsKey(int surr1) {
    return surr1 < column.length && !isEmpty(column[surr1]);
  }

  public int[] restrict(int surr) {
    if (surr >= column.length)
      return Array.emptyIntArray;

    long slot = column[surr];

    if (isEmpty(slot))
      return Array.emptyIntArray;

    if (isIndex(slot)) {
      int count = count(slot);
      int[] surrs = new int[count];
      overflowTable.copy(slot, surrs);
      return surrs;
    }

    int low = low(slot);
    int high = high(slot);
    return high == EMPTY_MARKER ? new int[] {low} : new int[] {low, high};
  }

  public int restrict(int surr, int[] output) {
    if (surr >= column.length)
      return 0;

    long slot = column[surr];

    if (isEmpty(slot))
      return 0;

    if (isIndex(slot)) {
      overflowTable.copy(slot, output);
      return count(slot);
    }

    output[0] = low(slot);
    int high = high(slot);
    if (high == EMPTY_MARKER)
      return 1;
    output[1] = high;
    return 2;
  }

  public int lookup(int surr) {
    if (surr >= column.length)
      return -1;
    long slot = column[surr];
    if (isEmpty(slot))
      return -1;
    if (isIndex(slot) | high(slot) != EMPTY_MARKER)
      throw Miscellanea.internalFail();
    Miscellanea._assert(tag(low(slot)) == INLINE_SLOT);
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
    return high(slot) == EMPTY_MARKER ? 1 : 2;
  }

  public boolean insert(int surr1, int surr2) {
    int size = column.length;
    if (surr1 >= size)
      resize(surr1);

    long slot = column[surr1];

    if (isEmpty(slot)) {
      set(surr1, surr2, EMPTY_MARKER);
      count++;
      return true;
    }

    int low = low(slot);
    int high = high(slot);

    if (tag(low) == INLINE_SLOT & high == EMPTY_MARKER) {
      if (surr2 == low)
        return false;
      set(surr1, low, surr2);
      count++;
      return true;
    }

    long updatedSlot = overflowTable.insert(slot, surr2);
    if (updatedSlot == slot)
      return false;

    set(surr1, updatedSlot);
    count++;
    return true;
  }

  public void insertUnique(int surr1, int surr2) {
    int size = column.length;
    if (surr1 >= size)
      resize(surr1);

    long slot = column[surr1];

    if (isEmpty(slot)) {
      set(surr1, surr2, EMPTY_MARKER);
      count++;
      return;
    }

    int low = low(slot);
    int high = high(slot);

    if (tag(low) == INLINE_SLOT & high == EMPTY_MARKER) {
      Miscellanea._assert(surr2 != low);
      set(surr1, low, surr2);
      count++;
      return;
    }

    long updatedSlot = overflowTable.insertUnique(slot, surr2);
    Miscellanea._assert(updatedSlot != slot);

    set(surr1, updatedSlot);
    count++;
  }

  // Assuming there's at most one entry whose first argument is surr1
  public int update(int surr1, int surr2) {
    if (surr1 >= column.length)
      resize(surr1);

    long slot = column[surr1];

    if (isEmpty(slot)) {
      set(surr1, surr2, EMPTY_MARKER);
      count++;
      return -1;
    }

    int low = low(slot);
    int high = high(slot);

    if (tag(low) == INLINE_SLOT & high == EMPTY_MARKER) {
      set(surr1, surr2, EMPTY_MARKER);
      return low;
    }

    throw Miscellanea.internalFail();
  }

  public boolean delete(int surr1, int surr2) {
    if (surr1 >= column.length)
      return false;

    long slot = column[surr1];

    if (isEmpty(slot))
      return false;

    if (isIndex(slot)) {
      long updatedSlot = overflowTable.delete(slot, surr2);
      if (updatedSlot == slot)
        return false;

      set(surr1, updatedSlot);
      count--;
      return true;
    }

    Miscellanea._assert(tag(low(slot)) == INLINE_SLOT);

    int low = low(slot);
    int high = high(slot);

    if (surr2 == low) {
      if (high == EMPTY_MARKER)
        set(surr1, EMPTY_SLOT);
      else
        set(surr1, high, EMPTY_MARKER);
      count--;
      return true;
    }

    if (surr2 == high) {
      set(surr1, low, EMPTY_MARKER);
      count--;
      return true;
    }

    return false;
  }

  public boolean isMap() {
    for (int i=0 ; i < column.length ; i++) {
      long slot = column[i];
      if (!isEmpty(slot) & (tag(low(slot)) != INLINE_SLOT | high(slot) != EMPTY_MARKER))
        return false;
    }
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
          overflowTable.copy(slot, data, next + 1, 2);
          next += 2 * slotCount;
        }
        else {
          data[next++] = i;
          data[next++] = low(slot);
          int high = high(slot);
          if (high != EMPTY_MARKER) {
            data[next++] = i;
            data[next++] = high;
          }
        }
      }
    }
    Miscellanea._assert(next == 2 * count);
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
          Miscellanea._assert(_count == slotCount);
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
          int high = high(slot);
          if (surr1 <= low) {
            data[next++] = surr1;
            data[next++] = low(slot);
          }
          if (high != EMPTY_MARKER & surr1 <= high) {
            data[next++] = surr1;
            data[next++] = high;
          }
        }
      }
    }
    Miscellanea._assert(next == count + eqCount);
    return data;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void initReverse(OneWayBinTable source) {
    Miscellanea._assert(count == 0);

    int len = source.column.length;
    for (int i=0 ; i < len ; i++) {
      int[] surrs = source.restrict(i);
      for (int j=0 ; j < surrs.length ; j++)
        insert(surrs[j], i);
    }
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

  //////////////////////////////////////////////////////////////////////////////


  // public void check() {
  //   overflowTable.check(column, count);
  // }

  // public void dump() {
  //   System.out.println("count = " + Integer.toString(count));
  //   System.out.print("column = [");
  //   for (int i=0 ; i < column.length ; i++)
  //     System.out.printf("%s%X", i > 0 ? " " : "", column[i]);
  //   System.out.println("]");
  //   overflowTable.dump();
  // }
}
