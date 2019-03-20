package net.cell_lang;


final class ObjStore extends ValueStore {
  static final int INIT_SIZE = 256;
                                                         // VALUE     NO VALUE
  private Obj[] values             = new Obj[INIT_SIZE]; //           null
  private int[] hashcodeOrNextFree = new int[INIT_SIZE]; // hashcode  index of the next free slot (can be out of bound)

  private int[] hashtable = new int[INIT_SIZE/2]; // -1 when there's no value in that bucket
  private int[] buckets   = new int[INIT_SIZE]; // junk when there's no value

  private int count = 0;
  private int firstFree = 0;

  //////////////////////////////////////////////////////////////////////////////

  public ObjStore() {
    super(INIT_SIZE);
    Miscellanea.arrayFill(hashtable, -1);
    for (int i=0 ; i < INIT_SIZE ; i++)
      hashcodeOrNextFree[i] = i + 1;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void printInfo(String name) {
    // int usedBuckets = 0;
    // for (int i=0 ; i < values.length ; i++)
    //   if (values[i] != null)
    //     if (buckets[i] != -1)
    //       usedBuckets++;
    // System.out.printf(
    //   "%-14s: %6d / %7d (%f) - %f\n",
    //   name + ":",
    //   usedBuckets,
    //   count,
    //   (double) usedBuckets / (double) count,
    //   (double) count / (double) values.length
    // );
  }

  public void insert(Obj value, int hashcode, int index) {
    Miscellanea._assert(firstFree == index);
    // Miscellanea._assert(nextFreeIdx[index] != -1);
    Miscellanea._assert(index < values.length);
    Miscellanea._assert(values[index] == null);
    Miscellanea._assert(hashcode == value.hashcode());

    count++;
    firstFree = hashcodeOrNextFree[index];
    values[index] = value;
    hashcodeOrNextFree[index] = hashcode;

    insertIntoHashtable(index, hashcode);
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
      int idx = firstFree;
      insert(value, value.hashcode(), firstFree);
      return idx;
    }
  }

  public void resize(int minCapacity) {
    int currCapacity = values.length;
    int newCapacity = 2 * currCapacity;
    while (newCapacity < minCapacity)
      newCapacity = 2 * newCapacity;

    super.resizeRefsArray(newCapacity);

    Obj[]  currValues             = values;
    int[]  currHashcodeOrNextFree = hashcodeOrNextFree;

    values             = new Obj[newCapacity];
    hashcodeOrNextFree = new int[newCapacity];
    hashtable          = new int[newCapacity/2];
    buckets            = new int[newCapacity];

    Miscellanea.arrayCopy(currValues, values, currCapacity);
    Miscellanea.arrayCopy(currHashcodeOrNextFree, hashcodeOrNextFree, currCapacity);
    Miscellanea.arrayFill(hashtable, -1);

    for (int i=0 ; i < currCapacity ; i++)
      if (values[i] != null)
        insertIntoHashtable(i, hashcodeOrNextFree[i]);

    for (int i=currCapacity ; i < newCapacity ; i++)
      hashcodeOrNextFree[i] = i + 1;
  }

  //////////////////////////////////////////////////////////////////////////////

  public int count() {
    return count;
  }

  public int capacity() {
    return values.length;
  }

  public int nextFreeIdx(int index) {
    Miscellanea._assert(index == -1 || index >= values.length || values[index] == null);
    if (index == -1)
      return firstFree;
    if (index >= values.length)
      return index + 1;
    return hashcodeOrNextFree[index];
  }

  public int valueToSurr(Obj value) {
    if (count == 0)
      return -1;
    int hashcode = value.hashcode();
    int hashIdx = Integer.remainderUnsigned(hashcode, hashtable.length);
    int idx = hashtable[hashIdx];
    while (idx != -1) {
      Miscellanea._assert(values[idx] != null);
      if (hashcodeOrNextFree[idx] == hashcode && value.isEq(values[idx]))
        return idx;
      idx = buckets[idx];
    }
    return -1;
  }

  public Obj surrToValue(int index) {
    return values[index];
  }

  //////////////////////////////////////////////////////////////////////////////

  @Override
  protected void free(int index) {
    Miscellanea._assert(values[index] != null);

    removeFromHashtable(index);
    values[index] = null;
    hashcodeOrNextFree[index] = firstFree;
    count--;
    firstFree = index;
  }

  //## THIS IS REDUNDANT
  public Obj surrToObjValue(int index) {
    return values[index];
  }

  //////////////////////////////////////////////////////////////////////////////

  private void insertIntoHashtable(int index, int hashcode) {
    int hashIdx = Integer.remainderUnsigned(hashcode, hashtable.length);
    buckets[index] = hashtable[hashIdx];
    hashtable[hashIdx] = index;
  }

  private void removeFromHashtable(int index) {
    int hashcode = hashcodeOrNextFree[index];
    int hashIdx = Integer.remainderUnsigned(hashcode, hashtable.length);
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
}
