package net.cell_lang;


final class IntStore extends ValueStore {
  private static final int INIT_SIZE = 256;
  private static final int INV_IDX = 0x3FFFFFFF;

  // Bits  0 - 31: 32-bit value, or index of 64-bit value
  // Bits 32 - 61: index of next value in the bucket if used or next free index otherwise
  // Bits 62 - 63: tag: 00 used (32 bit), 01 used (64 bit), 10 free
  private long[] slots = new long[INIT_SIZE];

  // INV_IDX when there's no value in that bucket
  private int[] hashtable = new int[INIT_SIZE/2];

  private int count = 0;
  private int firstFree = 0;

  //////////////////////////////////////////////////////////////////////////////

  private int hashIdx(long value) {
    int hashcode = (int) (value ^ (value >> 32));
    return Integer.remainderUnsigned(hashcode, hashtable.length);
  }

  private long emptySlot(int next) {
    Miscellanea._assert(next >= 0 & next <= 0x1FFFFFFF);
    return (((long) next) | (2L << 30)) << 32;
  }

  private long filledSlot(long value, int next) {
    if (value != ((int) value))
      throw new RuntimeException();
    Miscellanea._assert(!isEmpty(value | (((long) next) << 32)));
    return value | (((long) next) << 32);
  }

  private long value(long slot) {
    Miscellanea._assert(!isEmpty(slot));
    return (int) slot;
  }

  private int next(long slot) {
    Miscellanea._assert(!isEmpty(slot));
    return (int) ((slot >>> 32) & 0x3FFFFFFF);
  }

  private int nextFree(long slot) {
    Miscellanea._assert(isEmpty(slot));
    return (int) ((slot >> 32) & 0x3FFFFFFF);
  }

  private boolean isEmpty(long slot) {
    long tag = slot >>> 62;
    Miscellanea._assert(tag == 0 | tag == 2);
    return tag == 2;
    // return (slot >>> 62) == 2;
  }

  //////////////////////////////////////////////////////////////////////////////

  public IntStore() {
    super(INIT_SIZE);
    for (int i=0 ; i < INIT_SIZE ; i++)
      slots[i] = emptySlot(i+1);
    for (int i=0 ; i < INIT_SIZE ; i++)
      Miscellanea._assert(isEmpty(slots[i]));
    Miscellanea.arrayFill(hashtable, INV_IDX);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void insert(long value, int index) {
    Miscellanea._assert(firstFree == index);
    Miscellanea._assert(index < slots.length);
    // Miscellanea._assert(references[index] == 0);

    count++;
    firstFree = nextFree(slots[index]);

    int hashIdx = hashIdx(value);
    slots[index] = filledSlot(value, hashtable[hashIdx]);
    Miscellanea._assert(!isEmpty(slots[index]));
    hashtable[hashIdx] = index;
  }

  public int insertOrAddRef(long value) {
    int surr = valueToSurr(value);
    if (surr != -1) {
      addRef(surr);
      return surr;
    }
    else {
      Miscellanea._assert(count <= capacity());
      if (count == capacity())
        resize(count + 1);
      int idx = firstFree;
      insert(value, idx);
      return idx;
    }
  }

  public void resize(int minCapacity) {
    int currCapacity = capacity();
    int newCapacity = 2 * currCapacity;
    while (newCapacity < minCapacity)
      newCapacity = 2 * newCapacity;

    super.resizeRefsArray(newCapacity);

    long[] currSlots = slots;

    slots     = new long[newCapacity];
    hashtable = new int[newCapacity/2];

    Miscellanea.arrayFill(hashtable, INV_IDX);

    for (int i=0 ; i < currCapacity ; i++) {
      long slot = currSlots[i];
      long value = value(slot);
      int hashIdx = hashIdx(value);

      slots[i] = filledSlot(value, hashtable[hashIdx]);
      hashtable[hashIdx] = i;
    }

    for (int i=currCapacity ; i < newCapacity ; i++)
      slots[i] = emptySlot(i+1);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int count() {
    return count;
  }

  public int capacity() {
    return slots.length;
  }

  public int nextFreeIdx(int index) {
    Miscellanea._assert(index == -1 || index >= capacity() || isEmpty(slots[index]));
    if (index == -1)
      return firstFree;
    if (index >= capacity())
      return index + 1;
    return nextFree(slots[index]);
  }

  public int valueToSurr(long value) {
    int hashIdx = hashIdx(value);
    int idx = hashtable[hashIdx];
    while (idx != INV_IDX) {
      long slot = slots[idx];
      if (value(slot) == value)
        return idx;
      idx = next(slot);
    }
    return -1;
  }

  public long surrToValue(int surr) {
    return value(slots[surr]);
  }

  //////////////////////////////////////////////////////////////////////////////

  @Override
  public Obj surrToObjValue(int surr) {
    return IntObj.get(surrToValue(surr));
  }

  protected void free(int index) {
    long slot = slots[index];
    int hashIdx = hashIdx(value(slot));

    int idx = hashtable[hashIdx];
    Miscellanea._assert(idx != INV_IDX);

    if (idx == index) {
      hashtable[hashIdx] = next(slot);
    }
    else {
      for ( ; ; ) {
        slot = slots[idx];
        int next = next(slot);
        if (next == index) {
          slots[idx] = filledSlot(value(slot), next(slots[next]));
          break;
        }
        idx = next;
      }
    }

    slots[index] = emptySlot(firstFree);
    firstFree = index;
    count--;
  }
}
