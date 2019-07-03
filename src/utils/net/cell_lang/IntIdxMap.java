package net.cell_lang;


public class IntIdxMap {
  private static final int INIT_SIZE = 32;

  int count = 0;
  int hashRange = 0;

  long[] keychains = new long[INIT_SIZE];
  int[]  hashtable = new int[INIT_SIZE / 2];

  //////////////////////////////////////////////////////////////////////////////

  private long filledSlot(int value, int next) {
    long slot = (((long) next) << 32) | (long) value;
    Miscellanea._assert(slotValue(slot) == value);
    Miscellanea._assert(nextSlot(slot) == next);
    Miscellanea._assert(slotFlags(slot) == 0);
    return slot;
  }

  private long setFlags(long slot, int flags) {
    long newSlot = (slot & 0x1FFFFFFFFFFFFFFFL) | flags;
    Miscellanea._assert(slotValue(newSlot) == slotValue(slot));
    Miscellanea._assert(nextSlot(newSlot) == nextSlot(slot));
    Miscellanea._assert(slotFlags(newSlot) == flags);
    return newSlot;
  }

  private int slotValue(long slot) {
    return (int) slot;
  }

  private int nextSlot(long slot) {
    return (int) ((slot >>> 32) & 0x1FFFFFFFL);
  }

  private int slotFlags(long slot) {
    return (int) (slot >> 61);
  }

  //////////////////////////////////////////////////////////////////////////////

  protected IntIdxMap() {
    Array.fill(hashtable, -1);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void insertKey(int key) {
    Miscellanea._assert(!hasKey(key));

    if (count == keychains.length)
      resize();

    if (count < 16) {
      Miscellanea._assert(hashRange == 0);
      keychains[count++] = filledSlot(key, 0);
    }
    else {
      if (count == 16 | count == 2 * hashRange) {
        hashRange = count;
        for (int i=0 ; i < count ; i++)
          insert(slotValue(keychains[i]), i);
      }
      insert(key, count++);
    }
  }

  public void setFlags(int index, int flags) {
    Miscellanea._assert(index < count);
    keychains[index] = setFlags(keychains[index], flags);
  }

  public void clear() {
    //## TODO: RESET IF THE THING HAS BECOME TOO LARGE
    if (hashRange != 0)
      Array.fill(hashtable, -1, hashRange);
    count = 0;
    hashRange = 0;
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean hasKey(int key) {
    return index(key) != -1;
  }

  public int index(int key) {
    if (hashRange == 0) {
      for (int i=0 ; i < count ; i++)
        if (keychains[i] == key)
          return i;
    }
    else {
      int hashIdx = Integer.remainderUnsigned(key, hashRange);
      int idx = hashtable[hashIdx];
      while (idx != -1) {
        long slot = keychains[idx];
        if (slotValue(slot) == key)
          return idx;
        idx = nextSlot(slot);
      }
    }
    return -1;
  }

  //////////////////////////////////////////////////////////////////////////////

  private void insert(int key, int index) {
    Miscellanea._assert(hashRange != 0);
    int hashIdx = Integer.remainderUnsigned(key, hashRange);
    int head = hashtable[hashIdx];
    hashtable[hashIdx] = index;
    keychains[index] = filledSlot(key, head);
  }

  private void resize() {
    Miscellanea._assert(count == keychains.length);

    int newSize = 2 * count;
    long[] currKeychains = keychains;

    keychains = new long[newSize];
    hashtable = new int[newSize / 2];

    Array.fill(hashtable, -1);

    hashRange = count;
    for (int i=0 ; i < count ; i++)
      insert(slotValue(currKeychains[i]), i);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int count() {
    return count;
  }

  public int keyAt(int idx) {
    return slotValue(keychains[idx]);
  }

  public int flagsAt(int idx) {
    return slotFlags(keychains[idx]);
  }
}