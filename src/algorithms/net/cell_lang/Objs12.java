package net.cell_lang;


final class Objs12 {
  void sort(long[] array, int first, int last) {
    if (first >= last)
      return;

    int pivot = first + (last - first) / 2;

    if (pivot != first)
      swap(first, pivot, array);

    int low = first + 1;
    int high = last;

    for ( ; ; ) {
      // Incrementing low until it points to the first slot that does
      // not contain a value that is lower or equal to the pivot
      // Such slot may be the first element after the end of the array
      while (low <= last && !isGreater(low, first, array))
        low++;

      // Decrementing high until it points to the first slot whose
      // value is lower or equal to the pivot. Such slot may be the
      // first one, which contains the pivot
      while (high > first && isGreater(high, first, array))
        high--;

      Miscellanea._assert(low != high);
      Miscellanea._assert(low < high | low == high + 1);

      // Once low and high have moved past each other all elements have been partitioned
      if (low > high)
        break;

      // Swapping the first pair of out-of-order elements before resuming the scan
      swap(low++, high--, array);
    }

    // Putting the pivot between the two partitions
    int lastLeq = low - 1;
    if (lastLeq != first)
      swap(lastLeq, first, array);

    // Now the lower-or-equal partition starts at 'first' and ends at
    // 'lastLeq - 1' (inclusive), since lastLeq contains the pivot
    sort(array, first, lastLeq-1);

    // The greater-then partition starts at high + 1 and
    // continues until the end of the array
    sort(array, high+1, last);
  }

  //////////////////////////////////////////////////////////////////////////////

  public static boolean contains(Obj leftObj, Obj rightObj, int[] leftHashes, Obj[] leftObjs, Obj[] rightObjs) {
    int hashcode = leftObj.hashcode();

    int low = 0;
    int high = leftObjs.length - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int midHash = leftHashes[mid];

      if (hashcode < midHash) {
        high = mid - 1; // leftObj < leftObjs[mid]
        continue;
      }

      if (hashcode > midHash) {
        low = mid + 1; // leftObj > leftObjs[mid]
        continue;
      }

      int ord = leftObj.quickOrder(leftObjs[mid]);

      if (ord == -1) {
        high = mid - 1; // leftObj < leftObjs[mid]
        continue;
      }

      if (ord == 1) {
        low = mid + 1; // leftObj > leftObjs[mid]
        continue;
      }

      // leftObj == leftObjs[mid]

      ord = rightObj.quickOrder(rightObjs[mid]);

      if (ord == -1) {
        high = mid - 1; // rightObj < rightObjs[mid]
        continue;
      }

      if (ord == 1) {
        low = mid + 1; // rightObj > rightObjs[mid]
        continue;
      }

      // rightObj == rightObj[mid]
      return true;
    }

    return false;
  }

  //////////////////////////////////////////////////////////////////////////////

  public static int lookup(Obj obj, int[] hashes, Obj[] objs) {
    int hashcode = obj.hashcode();

    int low = 0;
    int high = objs.length - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int midHash = hashes[mid];

      if (hashcode < midHash) {
        high = mid - 1; // obj < objs[mid]
        continue;
      }

      if (hashcode > midHash) {
        low = mid + 1; // obj > objs[mid]
        continue;
      }

      int ord = obj.quickOrder(objs[mid]);
      if (ord == -1)
        high = mid - 1; // obj < objs[mid]
      else if (ord == 1)
        low = mid + 1; // obj > objs[mid]
      else
        return mid; // obj == objs[mid]
    }

    return -1;
  }

  public static int lookupFirst(Obj obj, int[] hashes, Obj[] objs) {
    int hashcode = obj.hashcode();

    int low = 0;
    int high = objs.length - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int midHash = hashes[mid];

      if (hashcode < midHash) {
        high = mid - 1; // obj < objs[mid]
        continue;
      }

      if (hashcode > midHash) {
        low = mid + 1; // obj > objs[mid]
        continue;
      }

      int ord = obj.quickOrder(objs[mid]);
      if (ord == -1) {
        high = mid - 1; // obj < objs[mid]
        continue;
      }

      if (ord == 1) {
        low = mid + 1; // obj > objs[mid]
        continue;
      }

      // obj == objs[mid]

      if (mid == 0)
        return mid;

      int prevHash = hashes[mid-1];
      if (prevHash != hashcode)
        return mid;

      Obj prevObj = objs[mid-1];
      ord = prevObj.quickOrder(obj);
      Miscellanea._assert(ord == -1 | ord == 0);

      if (ord == -1)
        return mid;

      high = mid - 1;
    }

    return -1;
  }

  //## IMPLEMENT FOR REAL
  public static int countEqUpward(int first, Obj obj, int[] hashes, Obj[] objs) {
    int len = objs.length;
    int hashcode = obj.hashcode();
    for (int i = first ; i < len ; i++)
      if (hashcode != hashes[i] || !obj.isEq(objs[i]))
        return i - first;
    return len - first;
  }

  //////////////////////////////////////////////////////////////////////////////

  public static int lookupFirst(Obj obj, long[] hashIdxs, Obj[] objs) {
    int hashcode = obj.hashcode();

    int low = 0;
    int high = objs.length - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      long midHashIdx = hashIdxs[mid];
      int midHash = (int) (midHashIdx >>> 32);

      if (hashcode < midHash) {
        high = mid - 1; // obj < objs[idxs[mid]]
        continue;
      }

      if (hashcode > midHash) {
        low = mid + 1; // obj > objs[idxs[mid]]
        continue;
      }

      int midIdx = (int) midHashIdx;
      int ord = obj.quickOrder(objs[midIdx]);
      if (ord == -1) {
        high = mid - 1; // obj < objs[idxs[mid]]
        continue;
      }

      if (ord == 1) {
        low = mid + 1; // obj > objs[idxs[mid]]
        continue;
      }

      // obj == objs[idxs[mid]]

      if (mid == 0)
        return mid;

      long prevHashIdx = hashIdxs[mid-1];

      int prevHash = (int) (prevHashIdx >>> 32);
      if (prevHash != hashcode)
        return mid;

      int prevIdx = (int) prevHashIdx;
      ord = objs[prevIdx].quickOrder(obj);
      Miscellanea._assert(ord == -1 | ord == 0);

      if (ord == -1)
        return mid;

      high = mid - 1;
    }

    return -1;
  }

  //## IMPLEMENT FOR REAL
  public static int countEqUpward(int first, Obj obj, long[] hashIdxs, Obj[] objs) {
    int len = objs.length;
    int hashcode = obj.hashcode();
    for (int i = first ; i < len ; i++) {
      long currHashIdx = hashIdxs[i];

      int currHash = (int) (currHashIdx >>> 32);
      if (hashcode != currHash)
        return i - first;

      int currIdx = (int) currHashIdx;
      if (!obj.isEq(objs[currIdx]))
        return i - first;
    }
    return len - first;
  }

  //////////////////////////////////////////////////////////////////////////////

  void swap(int idx1, int idx2, long[] array) {
    long tmp = array[idx1];
    array[idx1] = array[idx2];
    array[idx2] = tmp;
  }

  //////////////////////////////////////////////////////////////////////////////

  boolean isGreater(int idx1, int idx2, long[] array) {
    long data1 = array[idx1];
    long data2 = array[idx2];

    int hashcode1 = (int) (data1 >>> 32);
    int hashcode2 = (int) (data2 >>> 32);
    if (hashcode1 != hashcode2)
      return hashcode1 > hashcode2;

    return deepIsGreater((int) data1, (int) data2);
  }

  boolean deepIsGreater(int idx1, int idx2) {
    Obj obj1 = col1[idx1];
    Obj obj2 = col1[idx2];
    int ord = obj1.quickOrder(obj2);
    if (ord != 0)
      return ord == 1;

    obj1 = col2[idx1];
    obj2 = col2[idx2];
    return obj1.quickOrder(obj2) == 1;
  }

  //////////////////////////////////////////////////////////////////////////////

  public long[] sortedLeftHashIdxPairs(int first, int len) {
    long[] data = new long[len];
    for (int i=0 ; i < len ; i++) {
      int idx = first + i;
      data[i] = (((long) col1[idx].hashcode()) << 32) | idx;
    }
    sort(data, 0, len-1);
    return data;
  }

  // Returns the number of unique entries if it's a map, or its negated value if not
  public int clearDuplicatesAndCheckMap(long[] data) {
    int len = data.length;
    int size = len;
    boolean isMap = true;

    long prev = data[0];
    for (int i=1 ; i < len ; i++) {
      long curr = data[i];
      int prevHash = (int) (prev >>> 32);
      int currHash = (int) (curr >>> 32);
      if (prevHash == currHash) {
        int prevIdx = (int) prev;
        int currIdx = (int) curr;
        Obj prevKey = col1[prevIdx];
        Obj currKey = col1[currIdx];
        if (prevKey.isEq(currKey)) {
          Obj prevValue = col2[prevIdx];
          Obj currValue = col2[currIdx];
          if (prevValue.isEq(currValue)) {
            data[i-1] = -1;
            size--;
          }
          else
            isMap = false;
        }
      }
      prev = curr;
    }

    return isMap ? size : -size;
  }

  //////////////////////////////////////////////////////////////////////////////

  Obj[] col1, col2;

  public Objs12(Obj[] col1, Obj[] col2) {
    this.col1 = col1;
    this.col2 = col2;
  }
}
