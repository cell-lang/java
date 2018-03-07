package net.cell_lang;


class ValueStoreUpdater : ValueStoreBase {
  int[] surrogates;
  int   lastSurrogate = -1;

  ValueStore store;

  public ValueStoreUpdater(ValueStore store) {
    this.store = store;
  }

  public int Insert(Obj value) {
    int capacity = slots != null ? slots.length : 0;
    Miscellanea._assert(count <= capacity);

    if (count == capacity)
      Resize(count+1);

    lastSurrogate = store.NextFreeIdx(lastSurrogate);
    surrogates[count] = lastSurrogate;
    Insert(value, count);
    return lastSurrogate;
  }

  override public void Resize(int minCapacity) {
    base.Resize(count+1);
    int[] currSurrogates = surrogates;
    surrogates = new int[slots.length];
    if (count > 0)
      Array.Copy(currSurrogates, surrogates, count);
  }

  public void Apply() {
    if (count == 0)
      return;

    int storeCapacity = store.Capacity();
    int reqCapacity = store.Count() + count;

    if (storeCapacity < reqCapacity)
      store.Resize(reqCapacity);

    for (int i=0 ; i < count ; i++)
      store.Insert(slots[i], hashcodes[i], surrogates[i]);

    Reset();
  }

  override public void Reset() {
    base.Reset();
    lastSurrogate = -1;
    //## IS THIS NECESSARY?
    if (surrogates != null) {
      int len = surrogates.length;
      for (int i=0 ; i < len ; i++)
        surrogates[i] = 0;
    }
  }

  public int LookupValueEx(Obj value) {
    int surrogate = store.LookupValue(value);
    if (surrogate != -1)
      return surrogate;
    int index = LookupValue(value);
    if (index == -1)
      return -1;
    return surrogates[index];
  }

  public Obj LookupSurrogateEx(int surr) {
    Obj obj1 = LookupSurrogate(surr);
    if (obj1 != null)
      return obj1;
    return store.LookupSurrogate(surr);
  }

  override public void Dump() {
    base.Dump();
    WriteInts("surrogates", surrogates);
    System.out.printf("lastSurrogate = {0}", lastSurrogate);
  }
}
