package net.cell_lang;


class ValueStore : ValueStoreBase {
  final int InitSize = 16;

  int[] refCounts     = new int[InitSize];
  int[] nextFreeIdx   = new int[InitSize];
  int   firstFreeIdx  = 0;

  public ValueStore() : base(InitSize) {
    for (int i=0 ; i < InitSize ; i++)
      nextFreeIdx[i] = i + 1;
  }

  public void addRef(int index) {
    refCounts[index] = refCounts[index] + 1;
  }

  public void release(int index) {
    int refCount = refCounts[index];
    Miscellanea._assert(refCount > 0);
    refCounts[index] = refCount - 1;
    if (refCount == 1) {
      Delete((int) index);
      nextFreeIdx[index] = firstFreeIdx;
      firstFreeIdx = (int) index;
    }
  }

  override public void Insert(Obj value, int hashcode, int index) {
    Miscellanea._assert(firstFreeIdx == index);
    Miscellanea._assert(nextFreeIdx[index] != -1);
    base.insert(value, hashcode, index);
    firstFreeIdx = nextFreeIdx[index];
    nextFreeIdx[index] = -1; //## UNNECESSARY, BUT USEFUL FOR DEBUGGING
  }

  override public void Resize(int minCapacity) {
    base.resize(minCapacity);
    int capacity = slots.length;

    int[] currRefCounts   = refCounts;
    int[] currNextFreeIdx = nextFreeIdx;
    int currCapacity = currRefCounts.length;

    refCounts   = new int[capacity];
    nextFreeIdx = new int[capacity];

    Miscellanea.arrayCopy(currRefCounts, refCounts, currCapacity);
    Miscellanea.arrayCopy(currNextFreeIdx, nextFreeIdx, currCapacity);

    for (int i=currCapacity ; i < capacity ; i++)
      nextFreeIdx[i] = i + 1;
  }

  public int nextFreeIdx(int index) {
    Miscellanea._assert(index == -1 || index >= slots.length || (slots[index] == null & nextFreeIdx[index] != -1));
    if (index == -1)
      return firstFreeIdx;
    if (index >= nextFreeIdx.length)
      return index + 1;
    return nextFreeIdx[index];
  }

  override public void Dump() {
    base.dump();
    WriteInts("refCounts", refCounts);
    WriteInts("nextFreeIdx", nextFreeIdx);
    System.out.println("firstFreeIdx = " + firstFreeIdx.toString());
  }
}
