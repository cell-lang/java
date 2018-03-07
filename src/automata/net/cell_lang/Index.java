package net.cell_lang;


class Index {
  final int Empty = 0xFFFFFFFF;

  public int[] hashtable;
  public int[] buckets;

  public void init(int size) {
    Miscellanea._assert(hashtable == null & buckets == null);
    hashtable = new int[size];
    buckets   = new int[size];
    for (int i=0 ; i < size ; i++) {
      hashtable[i] = Empty;
      buckets[i] = Empty;
    }
  }

  public void reset() {
    hashtable = null;
    buckets = null;
  }

  public void clear() {
    if (hashtable != null) {
      int size = hashtable.length;
      for (int i=0 ; i < size ; i++) {
        hashtable[i] = Empty;
        buckets[i] = Empty;
      }
    }
  }

  public void insert(int index, int hashcode) {
    Miscellanea._assert(buckets[index] == Empty);
    Miscellanea._assert(index < hashtable.length);

    int hashIdx = hashcode % hashtable.length;
    int head = hashtable[hashIdx];
    hashtable[hashIdx] = index;
    buckets[index] = head;
  }

  public void delete(int index, int hashcode) {
    int hashIdx = hashcode % hashtable.length;
    int head = hashtable[hashIdx];
    Miscellanea._assert(head != Empty);

    if (head == index) {
      hashtable[hashIdx] = buckets[index];
      buckets[index] = Empty;
      return;
    }

    int curr = head;
    for ( ; ; ) {
      int next = buckets[curr];
      Miscellanea._assert(next != Empty);
      if (next == index) {
        buckets[curr] = buckets[next];
        buckets[next] = Empty;
        return;
      }
      curr = next;
    }
  }

  public bool IsBlank() {
    return hashtable == null;
  }

  public int head(int hashcode) {
    return hashtable[hashcode % hashtable.length];
  }

  public int next(int index) {
    return buckets[index];
  }

  public void dump() {
    System.out.print("hashtable =");
    if (hashtable != null)
      for (int i=0 ; i < hashtable.length ; i++)
        System.out.print(" " + (hashtable[i] == Empty ? "-" : hashtable[i].toString()));
    else
      System.out.print(" null");
    System.out.println("");

    System.out.print("buckets   =");
    if (hashtable != null)
      for (int i=0 ; i < buckets.length ; i++)
        System.out.print(" " + (buckets[i] == Empty ? "-" : buckets[i].toString()));
    else
      System.out.print(" null");
    System.out.println("");
    System.out.println("");
  }
}
