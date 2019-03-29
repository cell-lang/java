package net.cell_lang;


class Index {
  final static int Empty = 0xFFFFFFFF;

  public int[] hashtable;
  public int[] buckets;

  public Index(int size) {
    hashtable = new int[size/2];
    buckets   = new int[size];
    Miscellanea.arrayFill(hashtable, Empty);
    Miscellanea.arrayFill(buckets, Empty); //## IS THIS NECESSARY?
  }

  public void clear() {
    Miscellanea.arrayFill(hashtable, Empty);
    Miscellanea.arrayFill(buckets, Empty); //## IS THIS NECESSARY?
  }

  public void insert(int index, int hashcode) {
    Miscellanea._assert(buckets[index] == Empty);
    Miscellanea._assert(index < buckets.length);

    int hashIdx = Integer.remainderUnsigned(hashcode, hashtable.length);
    int head = hashtable[hashIdx];
    hashtable[hashIdx] = index;
    buckets[index] = head;
  }

  public void delete(int index, int hashcode) {
    int hashIdx = Integer.remainderUnsigned(hashcode, hashtable.length);
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

  public int head(int hashcode) {
    return hashtable[Integer.remainderUnsigned(hashcode, hashtable.length)];
  }

  public int next(int index) {
    return buckets[index];
  }

  public void dump() {
    System.out.print("hashtable =");
    if (hashtable != null)
      for (int i=0 ; i < hashtable.length ; i++)
        System.out.print(" " + (hashtable[i] == Empty ? "-" : Integer.toString(hashtable[i])));
    else
      System.out.print(" null");
    System.out.println("");

    System.out.print("buckets   =");
    if (hashtable != null)
      for (int i=0 ; i < buckets.length ; i++)
        System.out.print(" " + (buckets[i] == Empty ? "-" : Integer.toString(buckets[i])));
    else
      System.out.print(" null");
    System.out.println("");
    System.out.println("");
  }
}
