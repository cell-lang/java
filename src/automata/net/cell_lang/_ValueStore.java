package net.cell_lang;


class _ValueStore {
  static final int INIT_SIZE = 256;

  private Obj[] values      = new Obj[INIT_SIZE]; // null when there's no value
  private int[] hashcodes   = new int[INIT_SIZE]; // junk when there's no value
  private int[] refCounts   = new int[INIT_SIZE]; //    0 when there's no value
  private int[] nextFreeIdx = new int[INIT_SIZE]; // junk when there's a value

  private int[] hashtable   = new int[INIT_SIZE]; //   -1 when there's no value in that bucket
  private int[] buckets     = new int[INIT_SIZE]; // junk when there's no value

  private int count = 0;
  private int firstFreeIdx  = 0;

  //////////////////////////////////////////////////////////////////////////////

  public _ValueStore() {
    Miscellanea.arrayFill(hashtable, -1);
    for (int i=0 ; i < INIT_SIZE ; i++)
      nextFreeIdx[i] = i + 1;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void insert(Obj value, int hashcode, int index) {
    Miscellanea._assert(firstFreeIdx == index);
    // Miscellanea._assert(nextFreeIdx[index] != -1);
    Miscellanea._assert(index < values.length);
    Miscellanea._assert(values[index] == null);
    Miscellanea._assert(hashcode == value.hashcode());

    count++;
    firstFreeIdx = nextFreeIdx[index];

    values[index] = value;
    hashcodes[index] = hashcode;
    insertIntoHashtable(index, hashcode);

    // nextFreeIdx[index] = -1; //## UNNECESSARY, BUT USEFUL FOR DEBUGGING
  }

  public void addRef(int index) {
    refCounts[index] = refCounts[index] + 1;
  }

  public void release(int index) {
    Miscellanea._assert(refCounts[index] > 0);
    Miscellanea._assert(values[index] != null);

    int refCount = refCounts[index];
    refCounts[index] = refCount - 1;
    if (refCount == 1) {
      removeFromHashtable(index);

      values[index] = null;
      // hashcodes[index] = 0; //## NOT STRICTLY NECESSARY...
      nextFreeIdx[index] = firstFreeIdx;

      count--;
      firstFreeIdx = index;
    }
  }

  public int insertOrAddRef(Obj value) {
    int surr = valueToSurr(value);
    if (surr != -1) {
      addRef(surr);
      return surr;
    }
    else {
      int capacity = capacity();
      Miscellanea._assert(count <= capacity);
      if (count == capacity)
        resize(count+1);
      int idx = firstFreeIdx;
      insert(value, value.hashcode(), firstFreeIdx);
      return idx;
    }
  }

  public void resize(int minCapacity) {
    int currCapacity = values.length;
    int newCapacity = 2 * currCapacity;
    while (newCapacity < minCapacity)
      newCapacity = 2 * newCapacity;

    Obj[] currValues      = values;
    int[] currHashcodes   = hashcodes;
    int[] currRefCounts   = refCounts;
    int[] currNextFreeIdx = nextFreeIdx;

    values      = new Obj[newCapacity];
    hashcodes   = new int[newCapacity];
    hashtable   = new int[newCapacity];
    buckets     = new int[newCapacity];
    refCounts   = new int[newCapacity];
    nextFreeIdx = new int[newCapacity];

    Miscellanea.arrayCopy(currValues, values, currCapacity);
    Miscellanea.arrayCopy(currHashcodes, hashcodes, currCapacity);
    Miscellanea.arrayCopy(currRefCounts, refCounts, currCapacity);
    Miscellanea.arrayCopy(currNextFreeIdx, nextFreeIdx, currCapacity);
    Miscellanea.arrayFill(hashtable, -1);

    for (int i=0 ; i < currCapacity ; i++)
      if (values[i] != null)
        insertIntoHashtable(i, hashcodes[i]);

    for (int i=currCapacity ; i < newCapacity ; i++)
      nextFreeIdx[i] = i + 1;
  }

  //////////////////////////////////////////////////////////////////////////////

  public int count() {
    return count;
  }

  public int capacity() {
    return values.length;
  }

  public int nextFreeIdx(int index) {
    Miscellanea._assert(index == -1 || index >= values.length || (values[index] == null & nextFreeIdx[index] != -1));
    if (index == -1)
      return firstFreeIdx;
    if (index >= nextFreeIdx.length)
      return index + 1;
    return nextFreeIdx[index];
  }

  public int valueToSurr(Obj value) {
    if (count == 0)
      return -1;
    int hashcode = value.hashcode();
    int hashIdx = Integer.remainderUnsigned(hashcode, hashtable.length);
    int idx = hashtable[hashIdx];
    while (idx != -1) {
      Miscellanea._assert(values[idx] != null);
      if (hashcodes[idx] == hashcode && value.isEq(values[idx]))
        return idx;
      idx = buckets[idx];
    }
    return -1;
  }

  public Obj surrToValue(int index) {
    return values[index];
  }

  //////////////////////////////////////////////////////////////////////////////

  private void insertIntoHashtable(int index, int hashcode) {
    int hashIdx = Integer.remainderUnsigned(hashcode, values.length);
    buckets[index] = hashtable[hashIdx];
    hashtable[hashIdx] = index;
  }

  private void removeFromHashtable(int index) {
    int hashcode = hashcodes[index];
    int hashIdx = Integer.remainderUnsigned(hashcode, values.length);
    int idx = hashtable[hashIdx];
    Miscellanea._assert(idx != -1);

    if (idx == index) {
      hashtable[hashIdx] = buckets[index];
      // buckets[index] = -1; // NOT STRICTLY NECESSARY...
      return;
    }

    int prevIdx = idx;
    idx = buckets[idx];
    while (idx != index) {
      prevIdx = idx;
      idx = buckets[idx];
      Miscellanea._assert(idx != -1);
    }

    buckets[prevIdx] = buckets[index];
    // buckets[index] = -1; // NOT STRICTLY NECESSARY
  }

  //////////////////////////////////////////////////////////////////////////////

  // public void dump() {
  //   super.dump();
  //   writeInts("refCounts", refCounts);
  //   writeInts("nextFreeIdx", nextFreeIdx);
  //   System.out.printf("firstFreeIdx = %d\n", firstFreeIdx);
  // }
}
