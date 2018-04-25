package net.cell_lang;


class ValueStore extends ValueStoreBase {
  final int InitSize = 16; //## REMEMBER TO CHANGE super{16} IN THE CONSTRUCTOR WHEN CHANGING THIS

  int[] refCounts     = new int[InitSize];
  int[] nextFreeIdx   = new int[InitSize];
  int   firstFreeIdx  = 0;

  public ValueStore() {
    super(16); //## SHOULD BE super(InitSize)
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
      delete(index);
      nextFreeIdx[index] = firstFreeIdx;
      firstFreeIdx = (int) index;
    }
  }

  public void insert(Obj value, int hashcode, int index) {
    Miscellanea._assert(firstFreeIdx == index);
    Miscellanea._assert(nextFreeIdx[index] != -1);
    super.insert(value, hashcode, index);
    firstFreeIdx = nextFreeIdx[index];
    nextFreeIdx[index] = -1; //## UNNECESSARY, BUT USEFUL FOR DEBUGGING
  }

  public void resize(int minCapacity) {
    super.resize(minCapacity);
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

  @Override
  public void dump() {
    super.dump();
    writeInts("refCounts", refCounts);
    writeInts("nextFreeIdx", nextFreeIdx);
    System.out.printf("firstFreeIdx = %d\n", firstFreeIdx);
  }
}
