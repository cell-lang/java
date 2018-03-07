package net.cell_lang;


class ValueStoreBase {
  protected Obj[] slots;
  protected int[] hashcodes; //## WAS uint
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

  public void Reset() {
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

  public int Count() {
    return count;
  }

  public int Capacity() {
    return slots != null ? slots.length : 0;
  }

  public int LookupValue(Obj value) {
    if (count == 0)
      return -1;
    int hashcode = value.Hashcode(); //## WAS uint
    int idx = hashtable[hashcode % hashtable.length];
    while (idx != -1) {
      Miscellanea.Assert(slots[idx] != null);
      if (hashcodes[idx] == hashcode && value.IsEq(slots[idx]))
        return idx;
      idx = buckets[idx];
    }
    return -1;
  }

  //## IS THIS A DUPLICATE OF GetValue()?
  public Obj LookupSurrogate(int index) {
    return slots[index];
  }

  public Obj GetValue(int index) {
    return slots[index];
  }

  public void Insert(Obj value, int slotIdx) {
    Insert(value, value.Hashcode(), slotIdx);
  }

  public void Insert(Obj value, int hashcode, int slotIdx) {
    Miscellanea.Assert(slots != null && slotIdx < slots.length);
    Miscellanea.Assert(slots[slotIdx] == null);
    Miscellanea.Assert(hashcode == value.Hashcode());

    slots[slotIdx] = value;
    hashcodes[slotIdx] = hashcode;

    //## DOES IT MAKE ANY DIFFERENCE HERE TO USE int INSTEAD OF uint?
    int hashtableIdx = hashcode % slots.length; //## WAS uint
    int head = hashtable[hashtableIdx];
    hashtable[hashtableIdx] = slotIdx;
    buckets[slotIdx] = head;

    count++;
  }

  protected void Delete(int index) {
    Miscellanea.Assert(slots != null && index < slots.length);
    Miscellanea.Assert(slots[index] != null);

    int hashcode = hashcodes[index];

    slots[index] = null;
    hashcodes[index] = 0; //## NOT STRICTLY NECESSARY...
    count--;

    int hashtableIdx = hashcode % slots.length;
    int idx = hashtable[hashtableIdx];
    Miscellanea.Assert(idx != -1);

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
      Miscellanea.Assert(idx != -1);
    }

    buckets[prevIdx] = buckets[idx];
    buckets[idx] = -1;
  }

  public void Resize(int minCapacity) {
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

      Array.Copy(currSlots, slots, currCapacity);
      Array.Copy(currHashcodes, hashcodes, currCapacity);

      for (int i=0 ; i < newCapacity ; i++) {
        hashtable[i] = -1;
        buckets[i] = -1;
      }

      for (int i=0 ; i < currCapacity ; i++)
        if (slots[i] != null) {
          int slotIdx = hashcodes[i] % newCapacity;
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

  public void Dump() {
    System.out.println("");
    System.out.printf("count = %d\n", count);
    WriteObjs("slots", slots);
    WriteInts("hashcodes", hashcodes);
    WriteInts("hashtable", hashtable);
    WriteInts("buckets", buckets);
  }

  protected void WriteObjs(string name, Obj[] objs) {
    System.out.print(name + " = ");
    if (objs != null) {
      System.out.print("[");
      for (int i=0 ; i < objs.length ; i++) {
        if (i > 0)
          System.out.print(", ");
        Obj obj = objs[i];
        System.out.print(obj != null ? obj.ToString() : "null");
      }
      System.out.println("]");
    }
    else
      System.out.println("null");
  }

  protected void WriteInts(string name, int[] ints) {
    System.out.print(name + " = ");
    if (ints != null) {
      System.out.print("[");
      for (int i=0 ; i < ints.length ; i++) {
        if (i > 0)
          System.out.print(", ");
        System.out.print(ints[i].ToString());
      }
      System.out.println("]");
    }
    else
      System.out.println("null");
  }

  protected void WriteInts(string name, int[] ints) {
    System.out.print(name + " = ");
    if (ints != null) {
      System.out.print("[");
      for (int i=0 ; i < ints.length ; i++) {
        if (i > 0)
          System.out.print(", ");
        System.out.print(ints[i].ToString());
      }
      System.out.println("]");
    }
    else
      System.out.println("null");
  }
}
