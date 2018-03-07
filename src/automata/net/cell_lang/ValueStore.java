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

  public void AddRef(uint index) {
    refCounts[index] = refCounts[index] + 1;
  }

  public void Release(uint index) {
    int refCount = refCounts[index];
    Miscellanea.Assert(refCount > 0);
    refCounts[index] = refCount - 1;
    if (refCount == 1) {
      Delete((int) index);
      nextFreeIdx[index] = firstFreeIdx;
      firstFreeIdx = (int) index;
    }
  }

  override public void Insert(Obj value, uint hashcode, int index) {
    Miscellanea.Assert(firstFreeIdx == index);
    Miscellanea.Assert(nextFreeIdx[index] != -1);
    base.Insert(value, hashcode, index);
    firstFreeIdx = nextFreeIdx[index];
    nextFreeIdx[index] = -1; //## UNNECESSARY, BUT USEFUL FOR DEBUGGING
  }

  override public void Resize(int minCapacity) {
    base.Resize(minCapacity);
    int capacity = slots.Length;

    int[] currRefCounts   = refCounts;
    int[] currNextFreeIdx = nextFreeIdx;
    int currCapacity = currRefCounts.Length;

    refCounts   = new int[capacity];
    nextFreeIdx = new int[capacity];

    Array.Copy(currRefCounts, refCounts, currCapacity);
    Array.Copy(currNextFreeIdx, nextFreeIdx, currCapacity);

    for (int i=currCapacity ; i < capacity ; i++)
      nextFreeIdx[i] = i + 1;
  }

  public int NextFreeIdx(int index) {
    Miscellanea.Assert(index == -1 || index >= slots.Length || (slots[index] == null & nextFreeIdx[index] != -1));
    if (index == -1)
      return firstFreeIdx;
    if (index >= nextFreeIdx.Length)
      return index + 1;
    return nextFreeIdx[index];
  }

  override public void Dump() {
    base.Dump();
    WriteInts("refCounts", refCounts);
    WriteInts("nextFreeIdx", nextFreeIdx);
    Console.WriteLine("firstFreeIdx = " + firstFreeIdx.ToString());
  }
}
