package net.cell_lang;

import java.util.Arrays;


class Algs {
  static void checkIsOrdered(Obj[] objs) {
    for (int i=1 ; i < objs.length ; i++) {
      int ord = objs[i-1].quickOrder(objs[i]);
      if (ord != -1) {
        System.out.println("*****************************************");
        System.out.println(objs[i-1].toString());
        System.out.println(objs[i].toString());
        System.out.printf("%d", ord);
        throw new RuntimeException();
      }
    }
  }

  public static int binSearch(Obj[] objs, Obj obj) {
    return binSearch(objs, 0, objs.length, obj);
  }

  public static int binSearch(Obj[] objs, int first, int count, Obj obj) {
    int low = first;
    int high = first + count - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int res = obj.quickOrder(objs[mid]);
      if (res == -1)
        high = mid - 1; // objs[mid] > obj
      else if (res == 1)
        low = mid + 1;  // objs[mid] < obj
      else
        return mid;
    }

    return -1;
  }

  // If the element exists, return its index
  // Otherwise, return the -(I + 1) where I is the index of
  // the first element that is greater than the searched one
  public static int binSearchEx(Obj[] objs, int first, int count, Obj obj) {
    int idx = _binSearchEx(objs, first, count, obj);
    if (idx >= 0) {
      Miscellanea._assert(objs[idx].isEq(obj));
    }
    else {
      int insIdx = -idx - 1;
      Miscellanea._assert(insIdx >= first & insIdx <= first + count);
      Miscellanea._assert(insIdx == first || objs[insIdx-1].quickOrder(obj) < 0);
      Miscellanea._assert(insIdx == first + count || obj.quickOrder(objs[insIdx]) <= 0);
    }
    return idx;
  }

  public static int _binSearchEx(Obj[] objs, int first, int count, Obj obj) {
    int low = first;
    int high = first + count - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int res = obj.quickOrder(objs[mid]);
      if (res == -1)
        high = mid - 1; // objs[mid] > obj
      else if (res == 1)
        low = mid + 1;  // objs[mid] < obj
      else
        return mid;
    }

    return -low - 1;
  }

  public static int[] binSearchRange(Obj[] objs, int offset, int length, Obj obj) {
    int first;

    int low = offset;
    int high = offset + length - 1;
    int lowerBound = low;
    int upperBound = high;


    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int ord = obj.quickOrder(objs[mid]);
      if (ord == -1) {
        upperBound = high = mid - 1; // objs[mid] > obj
      }
      else if (ord == 1) {
        lowerBound = low = mid + 1; // objs[mid] < obj
      }
      else {
        if (mid == offset || !objs[mid-1].isEq(obj)) {
          first = mid;
          low = lowerBound;
          high = upperBound;

          while (low <= high) {
            mid = (int) (((long) low + (long) high) / 2);
            ord = obj.quickOrder(objs[mid]);
            if (ord == -1) {
              high = mid - 1; // objs[mid] > obj
            }
            else if (ord == 1) {
              low = mid + 1; // objs[mid] < obj
            }
            else {
              if (mid == upperBound || !objs[mid+1].isEq(obj))
                return new int[] {first, mid - first + 1};
              else
                low = mid + 1;
            }
          }

          // We're not supposed to ever get here.
          throw new UnsupportedOperationException();
        }
        else
          high = mid - 1;
      }
    }

    return new int[] {0, 0};
  }


  public static int[] binSearchRange(int[] idxs, Obj[] objs, Obj obj) {
    Miscellanea._assert(idxs.length == objs.length);

    int offset = 0;
    int length = idxs.length;

    int low = offset;
    int high = offset + length - 1;
    int lowerBound = low;
    int upperBound = high;


    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int ord = obj.quickOrder(objs[idxs[mid]]);
      if (ord == -1) {
        upperBound = high = mid - 1; // objs[idxs[mid]] > obj
      }
      else if (ord == 1) {
        lowerBound = low = mid + 1; // objs[idxs[mid]] < obj
      }
      else {
        if (mid == offset || !objs[idxs[mid-1]].isEq(obj)) {
          int first = mid;
          low = lowerBound;
          high = upperBound;

          while (low <= high) {
            mid = (int) (((long) low + (long) high) / 2);
            ord = obj.quickOrder(objs[idxs[mid]]);
            if (ord == -1) {
              high = mid - 1; // objs[idxs[mid]] > obj
            }
            else if (ord == 1) {
              low = mid + 1; // objs[idxs[mid]] < obj
            }
            else {
              if (mid == upperBound || !objs[idxs[mid+1]].isEq(obj))
                return new int[] {first, mid - first + 1};
              else
                low = mid + 1;
            }
          }

          // We're not supposed to ever get here.
          throw new UnsupportedOperationException();
        }
        else
          high = mid - 1;
      }
    }

    return new int[] {0, 0};
  }


  public static int[] binSearchRange(Obj[] major, Obj[] minor, Obj majorVal, Obj minorVal) {
    int offset = 0;
    int length = major.length;

    int low = offset;
    int high = offset + length - 1;
    int lowerBound = low;
    int upperBound = high;


    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int ord = majorVal.quickOrder(major[mid]);
      if (ord == 0)
        ord = minorVal.quickOrder(minor[mid]);
      if (ord == -1) {
        // major[mid] > majorVal | (major[mid] == majorVal & minor[mid] > minorVal)
        upperBound = high = mid - 1;
      }
      else if (ord == 1) {
        // major[mid] < majorVal | (major[mid] == majorVal) & minor[mid] < minorVal)
        lowerBound = low = mid + 1;
      }
      else {
        if (mid == offset || (!major[mid-1].isEq(majorVal) || !minor[mid-1].isEq(minorVal))) {
          int first = mid;
          low = lowerBound;
          high = upperBound;

          while (low <= high) {
            mid = (int) (((long) low + (long) high) / 2);

            ord = majorVal.quickOrder(major[mid]);
            if (ord == 0)
              ord = minorVal.quickOrder(minor[mid]);

            if (ord == -1) {
              // major[mid] > majorVal | (major[mid] == majorVal & minor[mid] > minorVal)
              high = mid - 1;
            }
            else if (ord == 1) {
              // major[mid] < majorVal | (major[mid] == majorVal) & minor[mid] < minorVal)
              low = mid + 1;
            }
            else {
              if (mid == upperBound || (!major[mid+1].isEq(majorVal) || !minor[mid+1].isEq(minorVal)))
                return new int[] {first, mid - first + 1};
              else
                low = mid + 1;
            }
          }

          // We're not supposed to ever get here.
          throw new UnsupportedOperationException();
        }
        else
          high = mid - 1;
      }
    }

    return new int[] {0, 0};
  }


  public static int[] binSearchRange(int[] idxs, Obj[] major, Obj[] minor, Obj majorVal, Obj minorVal) {
    int offset = 0;
    int length = major.length;

    int low = offset;
    int high = offset + length - 1;
    int lowerBound = low;
    int upperBound = high;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int midIdx = idxs[mid];

      int ord = majorVal.quickOrder(major[midIdx]);
      if (ord == 0)
        ord = minorVal.quickOrder(minor[midIdx]);

      if (ord == -1) {
        // major[mid] > majorVal | (major[mid] == majorVal & minor[mid] > minorVal)
        upperBound = high = mid - 1;
      }
      else if (ord == 1) {
        // major[mid] < majorVal | (major[mid] == majorVal) & minor[mid] < minorVal)
        lowerBound = low = mid + 1;
      }
      else {
        boolean isFirst = mid == offset;
        if (!isFirst) {
          int prevIdx = idxs[mid-1];
          isFirst = !major[prevIdx].isEq(majorVal) || !minor[prevIdx].isEq(minorVal);
        }

        if (isFirst) {
          int first = mid;
          low = lowerBound;
          high = upperBound;

          while (low <= high) {
            mid = (int) (((long) low + (long) high) / 2);
            midIdx = idxs[mid];

            ord = majorVal.quickOrder(major[midIdx]);
            if (ord == 0)
              ord = minorVal.quickOrder(minor[midIdx]);

            if (ord == -1) {
              // major[mid] > majorVal | (major[mid] == majorVal & minor[mid] > minorVal)
              high = mid - 1;
            }
            else if (ord == 1) {
              // major[mid] < majorVal | (major[mid] == majorVal) & minor[mid] < minorVal)
              low = mid + 1;
            }
            else {
              boolean isLast = mid == upperBound;
              if (!isLast) {
                int nextIdx = idxs[mid+1];
                isLast = !major[nextIdx].isEq(majorVal) || !minor[nextIdx].isEq(minorVal);
              }
              if (isLast) {
                return new int[] {first, mid - first + 1};
              }
              else
                low = mid + 1;
            }
          }

          // We're not supposed to ever get here.
          throw new UnsupportedOperationException();
        }
        else
          high = mid - 1;
      }
    }

    return new int[] {0, 0};
  }

  // public static Obj[] sortUnique(Obj[] objs, int count) {
  //   Miscellanea._assert(count > 0);

  //   int extraData0 = objs[0].extraData;
  //   for (int i=1 ; i < count ; i++)
  //     if (objs[i].extraData != extraData0)
  //       return _sortUnique(objs, count);


  //   int[] keysIdxs = new int[3*count];
  //   for (int i=0 ; i < count ; i++) {
  //     long data = objs[i].data;
  //     int idx = 3 * i;
  //     keysIdxs[idx]   = (int) (data >>> 32);
  //     keysIdxs[idx+1] = (int) data;
  //     keysIdxs[idx+2] = i;
  //   }
  //   Ints123.sort(keysIdxs, count);

  //   Obj[] objs2 = new Obj[count];
  //   int groupKey1 = keysIdxs[0];
  //   int groupKey2 = keysIdxs[1];
  //   int groupStartIdx = 0;
  //   int nextIdx = 0;
  //   for (int i=0 ; i < count ; i++) {
  //     int i3 = 3 * i;
  //     int key1 = keysIdxs[i3];
  //     int key2 = keysIdxs[i3+1];
  //     int idx  = keysIdxs[i3+2];

  //     if (key1 != groupKey1 | key2 != groupKey2) {
  //       if (nextIdx - groupStartIdx > 1)
  //         nextIdx = sortUnique(objs2, groupStartIdx, nextIdx);
  //       groupKey1 = key1;
  //       groupKey2 = key2;
  //       groupStartIdx = nextIdx;
  //     }

  //     objs2[nextIdx++] = objs[idx];
  //   }
  //   if (nextIdx - groupStartIdx > 1)
  //     nextIdx = sortUnique(objs2, groupStartIdx, nextIdx);

  //   if (nextIdx == count)
  //     return objs2;
  //   else
  //     return Arrays.copyOf(objs2, nextIdx);
  // }

  public static Object[] _sortUnique(Obj[] objs, int count) {
    Miscellanea._assert(count > 0);

    long[] keysIdxs = indexesSortedByHashcode(objs, count);

    Obj[] sortedObjs = new Obj[count];
    int[] hashcodes = new int[count];
    int groupKey = (int) (keysIdxs[0] >>> 32);
    int groupStartIdx = 0;
    int nextIdx = 0;
    for (int i=0 ; i < count ; i++) {
      long keyIdx = keysIdxs[i];
      int key = (int) (keyIdx >> 32);
      int idx = (int) keyIdx;

      if (key != groupKey) {
        if (nextIdx - groupStartIdx > 1)
          nextIdx = sortUnique(sortedObjs, groupStartIdx, nextIdx);
        for (int j=groupStartIdx ; j < nextIdx ; j++)
          hashcodes[j] = groupKey;
        groupKey = key;
        groupStartIdx = nextIdx;
      }

      sortedObjs[nextIdx++] = objs[idx];
    }
    if (nextIdx - groupStartIdx > 1)
      nextIdx = sortUnique(sortedObjs, groupStartIdx, nextIdx);
    for (int j=groupStartIdx ; j < nextIdx ; j++)
      hashcodes[j] = groupKey;

    if (nextIdx == count)
      return new Object[] {sortedObjs, hashcodes};
    else
      return new Object[] {Arrays.copyOf(sortedObjs, nextIdx), Arrays.copyOf(hashcodes, nextIdx)};
  }

  private static long[] indexesSortedByHashcode(Obj[] objs, int count) {
    long[] keysIdxs = new long[count];
    for (int i=0 ; i < count ; i++) {
      keysIdxs[i] = (((long) objs[i].hashcode()) << 32) | i;
    }
    Arrays.sort(keysIdxs);
    return keysIdxs;
  }

  private static int sortUnique(Obj[] objs, int first, int end) {
    Arrays.sort(objs, first, end);
    int prev = first;
    for (int i=first+1 ; i < end ; i++)
      if (!objs[prev].isEq(objs[i]))
        if (i != ++prev)
          objs[prev] = objs[i];
    return prev + 1;
  }

  // public static Obj[] sortUnique(Obj[] objs, int count) {
  //   Miscellanea._assert(count > 0);
  //   Arrays.sort(objs, 0, count);
  //   int prev = 0;
  //   for (int i=1 ; i < count ; i++)
  //     if (!objs[prev].isEq(objs[i]))
  //       if (i != ++prev)
  //         objs[prev] = objs[i];
  //   int len = prev + 1;
  //   return Arrays.copyOf(objs, len);
  // }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // public static Obj[][] sortUnique(Obj[] col1, Obj[] col2, int count) {
  //   Miscellanea._assert(count > 0);

  //   int[] idxs = new int[count];
  //   for (int i=0 ; i < count ; i++)
  //     idxs[i] = i;

  //   sortIdxs(idxs, 0, count-1, col1, col2);

  //   int prev = 0;
  //   for (int i=1 ; i < count ; i++) {
  //     int j = idxs[i];
  //     int k = idxs[i-1];
  //     if (!col1[j].isEq(col1[k]) || !col2[j].isEq(col2[k]))
  //       if (i != ++prev)
  //         idxs[prev] = idxs[i];
  //   }

  //   int size = prev + 1;
  //   Obj[] normCol1 = new Obj[size];
  //   Obj[] normCol2 = new Obj[size];

  //   for (int i=0 ; i < size ; i++) {
  //     int j = idxs[i];
  //     normCol1[i] = col1[j];
  //     normCol2[i] = col2[j];
  //   }

  //   return new Obj[][] {normCol1, normCol2};
  // }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static Obj[][] sortUnique(Obj[] col1, Obj[] col2, Obj[] col3, int count) {
    Miscellanea._assert(count > 0);

    int[] idxs = new int[count];
    for (int i=0 ; i < count ; i++)
      idxs[i] = i;

    sortIdxs(idxs, 0, count-1, col1, col2, col3);

    int prev = 0;
    for (int i=1 ; i < count ; i++) {
      int j = idxs[i];
      int k = idxs[i-1];
      if (!col1[j].isEq(col1[k]) || !col2[j].isEq(col2[k]) || !col3[j].isEq(col3[k]))
        if (i != ++prev)
          idxs[prev] = idxs[i];
    }

    int size = prev + 1;
    Obj[] normCol1 = new Obj[size];
    Obj[] normCol2 = new Obj[size];
    Obj[] normCol3 = new Obj[size];

    for (int i=0 ; i < size ; i++) {
      int j = idxs[i];
      normCol1[i] = col1[j];
      normCol2[i] = col2[j];
      normCol3[i] = col3[j];
    }

    return new Obj[][] {normCol1, normCol2, normCol3};
  }

  public static boolean sortedArrayHasDuplicates(Obj[] objs) {
    for (int i=1 ; i < objs.length ; i++)
      if (objs[i].isEq(objs[i-1]))
        return true;
    return false;
  }

  public static int[] sortedIndexes(Obj[] major, Obj[] minor) {
    Miscellanea._assert(major.length == minor.length);

    int count = major.length;

    int[] idxs = new int[count];
    for (int i=0 ; i < count ; i++)
      idxs[i] = i;

    sortIdxs(idxs, 0, count-1, major, minor);

    return idxs;
  }

  public static int[] sortedIndexes(Obj[] col1, Obj[] col2, Obj[] col3) {
    Miscellanea._assert(col1.length == col2.length && col1.length == col3.length);

    int count = col1.length;

    int[] idxs = new int[count];
    for (int i=0 ; i < count ; i++)
      idxs[i] = i;

    sortIdxs(idxs, 0, count-1, col1, col2, col3);

    return idxs;
  }

  //////////////////////////////////////////////////////////////////////////////

  static void sortIdxs(int[] indexes, int first, int last, Obj[] major, Obj[] minor) {
    if (first >= last)
      return;

    int pivotIdx = first + (last - first) / 2;
    int pivot = indexes[pivotIdx];
    Obj pivotMajor = major[pivot];
    Obj pivotMinor = minor[pivot];

    if (pivotIdx > first)
      indexes[pivotIdx] = indexes[first];
      // indexes[first] = pivot; // Not necessary

    int low = first + 1;
    int high = last;

    while (low <= high) {
      while (low <= last) {
        int idx = indexes[low];
        int ord = major[idx].compareTo(pivotMajor);
        if (ord == 0)
          ord = minor[idx].compareTo(pivotMinor);

        if (ord > 0) // Including all elements that are lower or equal than the pivot
          break;
        else
          low++;
      }

      // <low> is now the lowest index that does not contain a value that is
      // lower or equal than the pivot. It may be outside the bounds of the array

      while (high >= first) {
        int idx = indexes[high];
        int ord = major[idx].compareTo(pivotMajor);
        if (ord == 0)
          ord = minor[idx].compareTo(pivotMinor);

        if (ord <= 0) // Including only elements that are greater than the pivot
          break;
        else
          high--;
      }

      // <high> is not the highest index that does not contain an element that
      // is greater than the pivot. It may be outside the bounds of the array

      Miscellanea._assert(low != high);

      if (low < high) {
        int tmp = indexes[low];
        indexes[low] = indexes[high];
        indexes[high] = tmp;
        low++;
        high--;
      }
    }

    if (low - 1 > first)
      indexes[first] = indexes[low - 1];
    indexes[low - 1] = pivot;

    if (low - 2 > first) {
      sortIdxs(indexes, first, low-2, major, minor);
    }

    if (high < last)
      sortIdxs(indexes, high+1, last, major, minor);
  }

  static void sortIdxs(int[] indexes, int first, int last, Obj[] ord1, Obj[] ord2, Obj[] ord3) {
    if (first >= last)
      return;

    int pivotIdx = first + (last - first) / 2;
    int pivot = indexes[pivotIdx];
    Obj pivotOrd1 = ord1[pivot];
    Obj pivotOrd2 = ord2[pivot];
    Obj pivotOrd3 = ord3[pivot];

    if (pivotIdx > first)
      indexes[pivotIdx] = indexes[first];
      // indexes[first] = pivot; // Not necessary

    int low = first + 1;
    int high = last;

    while (low <= high) {
      while (low <= last) {
        int idx = indexes[low];
        int ord = ord1[idx].compareTo(pivotOrd1);
        if (ord == 0)
          ord = ord2[idx].compareTo(pivotOrd2);
        if (ord == 0)
          ord = ord3[idx].compareTo(pivotOrd3);

        if (ord > 0) // Including all elements that are lower or equal than the pivot
          break;
        else
          low++;
      }

      // <low> is now the lowest index that does not contain a value that is
      // lower or equal than the pivot. It may be outside the bounds of the array

      while (high >= first) {
        int idx = indexes[high];
        int ord = ord1[idx].compareTo(pivotOrd1);
        if (ord == 0)
          ord = ord2[idx].compareTo(pivotOrd2);
        if (ord == 0)
          ord = ord3[idx].compareTo(pivotOrd3);

        if (ord <= 0) // Including only elements that are greater than the pivot
          break;
        else
          high--;
      }

      // <high> is not the highest index that does not contain an element that
      // is greater than the pivot. It may be outside the bounds of the array

      Miscellanea._assert(low != high);

      if (low < high) {
        int tmp = indexes[low];
        indexes[low] = indexes[high];
        indexes[high] = tmp;
        low++;
        high--;
      }
    }

    if (low - 1 > first)
      indexes[first] = indexes[low - 1];
    indexes[low - 1] = pivot;

    if (low - 2 > first) {
      sortIdxs(indexes, first, low-2, ord1, ord2, ord3);
    }

    if (high < last)
      sortIdxs(indexes, high+1, last, ord1, ord2, ord3);
  }
}
