package net.cell_lang;


class ValueStoreBase {
  protected Obj[] slots;
  protected int[] hashcodes;
  protected int[] hashtable;
  protected int[] buckets;
  protected int   count = 0;

  protected ValueStoreBase() {

  }

  protected ValueStoreBase(int initSize) {
    slots     = new Obj[initSize];
    hashcodes = new int[initSize];
    hashtable = new int[initSize];
    buckets   = new int[initSize];

    for (int i=0 ; i < initSize ; i++) {
      hashtable[i] = -1;
      buckets[i] = -1;
    }
  }

  public void reset() {
    if (slots != null) {
      int size = slots.length;
      for (int i=0 ; i < size ; i++) {
        slots[i] = null;
        hashcodes[i] = 0; //## IS THIS NECESSARY?
        hashtable[i] = -1;
        buckets[i] = -1;
      }
      count = 0;
    }
  }

  public int count() {
    return count;
  }

  public int capacity() {
    return slots != null ? slots.length : 0;
  }

  public int lookupValue(Obj value) {
    if (count == 0)
      return -1;
    int hashcode = value.hashcode();
    int hashIdx = Integer.remainderUnsigned(hashcode, hashtable.length);
    int idx = hashtable[hashIdx];
    while (idx != -1) {
      Miscellanea._assert(slots[idx] != null);
      if (hashcodes[idx] == hashcode && value.isEq(slots[idx]))
        return idx;
      idx = buckets[idx];
    }
    return -1;
  }

  //## IS THIS A DUPLICATE OF GetValue()?
  public Obj lookupSurrogate(int index) {
    return slots[index];
  }

  public Obj getValue(int index) {
    return slots[index];
  }

  public void insert(Obj value, int slotIdx) {
    insert(value, value.hashcode(), slotIdx);
  }

  public void insert(Obj value, int hashcode, int slotIdx) {
    Miscellanea._assert(slots != null && slotIdx < slots.length);
    Miscellanea._assert(slots[slotIdx] == null);
    Miscellanea._assert(hashcode == value.hashcode());

    slots[slotIdx] = value;
    hashcodes[slotIdx] = hashcode;

    int hashtableIdx = Integer.remainderUnsigned(hashcode, slots.length);
    int head = hashtable[hashtableIdx];
    hashtable[hashtableIdx] = slotIdx;
    buckets[slotIdx] = head;

    count++;
  }

  protected void delete(int index) {
    Miscellanea._assert(slots != null && index < slots.length);
    Miscellanea._assert(slots[index] != null);

    int hashcode = hashcodes[index];

    slots[index] = null;
    hashcodes[index] = 0; //## NOT STRICTLY NECESSARY...
    count--;

    int hashtableIdx = Integer.remainderUnsigned(hashcode, slots.length);
    int idx = hashtable[hashtableIdx];
    Miscellanea._assert(idx != -1);

    if (idx == index) {
      hashtable[hashtableIdx] = buckets[idx];
      buckets[idx] = -1;
      return;
    }

    int prevIdx = idx;
    idx = buckets[idx];
    while (idx != index) {
      prevIdx = idx;
      idx = buckets[idx];
      Miscellanea._assert(idx != -1);
    }

    buckets[prevIdx] = buckets[idx];
    buckets[idx] = -1;
  }

  public void resize(int minCapacity) {
    if (slots != null) {
      int   currCapacity  = slots.length;
      Obj[] currSlots     = slots;
      int[] currHashcodes = hashcodes;

      int newCapacity = 2 * currCapacity;
      while (newCapacity < minCapacity)
        newCapacity = 2 * newCapacity;

      slots     = new Obj[newCapacity];
      hashcodes = new int[newCapacity];
      hashtable = new int[newCapacity];
      buckets   = new int[newCapacity];

      Miscellanea.arrayCopy(currSlots, slots, currCapacity);
      Miscellanea.arrayCopy(currHashcodes, hashcodes, currCapacity);

      for (int i=0 ; i < newCapacity ; i++) {
        hashtable[i] = -1;
        buckets[i] = -1;
      }

      for (int i=0 ; i < currCapacity ; i++)
        if (slots[i] != null) {
          int slotIdx = Integer.remainderUnsigned(hashcodes[i], newCapacity);
          int head = hashtable[slotIdx];
          hashtable[slotIdx] = i;
          buckets[i] = head;
        }
    }
    else {
      final int MinCapacity = 32;

      slots     = new Obj[MinCapacity];
      hashcodes = new int[MinCapacity];
      hashtable = new int[MinCapacity];
      buckets   = new int[MinCapacity];

      for (int i=0 ; i < MinCapacity ; i++) {
        hashtable[i] = -1;
        buckets[i] = -1;
      }
    }
  }

  public void dump() {
    System.out.println("");
    System.out.printf("count = %d\n", count);
    writeObjs("slots", slots);
    writeInts("hashcodes", hashcodes);
    writeInts("hashtable", hashtable);
    writeInts("buckets", buckets);
  }

  protected void writeObjs(String name, Obj[] objs) {
    System.out.print(name + " = ");
    if (objs != null) {
      System.out.print("[");
      for (int i=0 ; i < objs.length ; i++) {
        if (i > 0)
          System.out.print(", ");
        Obj obj = objs[i];
        System.out.print(obj != null ? obj.toString() : "null");
      }
      System.out.println("]");
    }
    else
      System.out.println("null");
  }

  protected void writeInts(String name, int[] ints) {
    System.out.print(name);
    System.out.print(" = ");
    if (ints != null) {
      System.out.print("[");
      for (int i=0 ; i < ints.length ; i++) {
        if (i > 0)
          System.out.print(", ");
        System.out.printf("%d", ints[i]);
      }
      System.out.println("]");
    }
    else
      System.out.println("null");
  }
}
