package net.cell_lang;


class ValueStoreUpdater extends ValueStoreBase {
  int[] surrogates;
  int   lastSurrogate = -1;

  ValueStore store;

  public ValueStoreUpdater(ValueStore store) {
    this.store = store;
  }

  public int insert(Obj value) {
    int capacity = slots != null ? slots.length : 0;
    Miscellanea._assert(count <= capacity);

    if (count == capacity)
      resize(count+1);

    lastSurrogate = store.nextFreeIdx(lastSurrogate);
    surrogates[count] = lastSurrogate;
    insert(value, count);
    return lastSurrogate;
  }

  @Override
  public void resize(int minCapacity) {
    super.resize(count+1);
    int[] currSurrogates = surrogates;
    surrogates = new int[slots.length];
    if (count > 0)
      Miscellanea.arrayCopy(currSurrogates, surrogates, count);
  }

  public void apply() {
    if (count == 0)
      return;

    int storeCapacity = store.capacity();
    int reqCapacity = store.count() + count;

    if (storeCapacity < reqCapacity)
      store.resize(reqCapacity);

    for (int i=0 ; i < count ; i++)
      store.insert(slots[i], hashcodes[i], surrogates[i]);

    reset();
  }

  @Override
  public void reset() {
    super.reset();
    lastSurrogate = -1;
    //## IS THIS NECESSARY?
    if (surrogates != null) {
      int len = surrogates.length;
      for (int i=0 ; i < len ; i++)
        surrogates[i] = 0;
    }
  }

  public int lookupValueEx(Obj value) {
    int surrogate = store.lookupValue(value);
    if (surrogate != -1)
      return surrogate;
    int index = lookupValue(value);
    if (index == -1)
      return -1;
    return surrogates[index];
  }

  public Obj lookupSurrogateEx(int surr) {
    Obj obj1 = lookupSurrogate(surr);
    if (obj1 != null)
      return obj1;
    return store.lookupSurrogate(surr);
  }

  @Override
  public void dump() {
    super.dump();
    writeInts("surrogates", surrogates);
    System.out.printf("lastSurrogate = {0}\n\n", lastSurrogate);
  }
}
