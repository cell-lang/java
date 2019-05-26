package net.cell_lang;

import java.util.Arrays;


class Array {
  public static <T> void copy(T[] src, T[] dest, int count) {
    for (int i=0 ; i < count ; i++)
      dest[i] = src[i];
  }

  public static void copy(byte[] src, byte[] dest, int count) {
    for (int i=0 ; i < count ; i++)
      dest[i] = src[i];
  }

  public static void copy(short[] src, short[] dest, int count) {
    for (int i=0 ; i < count ; i++)
      dest[i] = src[i];
  }

  public static void copy(int[] src, int[] dest, int count) {
    for (int i=0 ; i < count ; i++)
      dest[i] = src[i];
  }

  public static void copy(long[] src, long[] dest, int count) {
    for (int i=0 ; i < count ; i++)
      dest[i] = src[i];
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static byte[] append(byte[] array, int count, long value) {
    // Miscellanea._assert(count <= array.length);
    // Miscellanea._assert(value >= -128 & value <= 127);

    if (count == array.length) {
      int newLen = Math.max(32, (3 * count) / 2);
      byte[] newArray = new byte[newLen];
      copy(array, newArray, count);
      array = newArray;
    }

    array[count] = (byte) value;
    return array;
  }

  public static short[] append(short[] array, int count, long value) {
    // Miscellanea._assert(count <= array.length);
    // Miscellanea._assert(value >= -32768 & value <= 32767);

    if (count == array.length) {
      int newLen = Math.max(32, (3 * count) / 2);
      short[] newArray = new short[newLen];
      copy(array, newArray, count);
      array = newArray;
    }

    array[count] = (short) value;
    return array;
  }

  public static int[] append(int[] array, int count, int value) {
    Miscellanea._assert(count <= array.length);

    if (count == array.length) {
      int newLen = Math.max(32, (3 * count) / 2);
      int[] newArray = new int[newLen];
      copy(array, newArray, count);
      array = newArray;
    }

    array[count] = value;
    return array;
  }

  public static long[] append(long[] array, int count, long value) {
    Miscellanea._assert(count <= array.length);

    if (count == array.length) {
      int newLen = Math.max(32, (3 * count) / 2);
      long[] newArray = new long[newLen];
      copy(array, newArray, count);
      array = newArray;
    }

    array[count] = value;
    return array;
  }

  public static Obj[] append(Obj[] array, int count, Obj value) {
    Miscellanea._assert(count <= array.length);

    if (count == array.length) {
      int newLen = Math.max(32, (3 * count) / 2);
      array = Arrays.copyOf(array, newLen);
    }

    array[count] = value;
    return array;
  }

  public static int[] append2(int[] array, int count, int val1, int val2) {
    Miscellanea._assert(2 * count <= array.length);

    if (array.length < 2 * (count + 1)) {
      int newLen = Math.max(64, 2 * ((3 * count) / 2));
      int[] newArray = new int[newLen];
      copy(array, newArray, 2 * count);
      array = newArray;
    }

    array[2 * count] = val1;
    array[2 * count + 1] = val2;

    return array;
  }

  public static int[] append3(int[] array, int count, int val1, int val2, int val3) {
    Miscellanea._assert(3 * count <= array.length);

    if (array.length < 3 * (count + 1)) {
      int newLen = Math.max(96, 3 * ((3 * count) / 2));
      int[] newArray = new int[newLen];
      copy(array, newArray, 3 * count);
      array = newArray;
    }

    int offset = 3 * count;
    array[offset] = val1;
    array[offset + 1] = val2;
    array[offset + 2] = val3;

    return array;
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static Obj at(boolean[] array, int size, long idx) {
    if (idx < size)
      return SymbObj.get(array[(int) idx]);
    else
      throw Miscellanea.softFail();
  }

  public static Obj at(long[] array, int size, long idx) {
    if (idx < size)
      return IntObj.get(array[(int) idx]);
    else
      throw Miscellanea.softFail();
  }

  public static Obj at(double[] array, int size, long idx) {
    if (idx < size)
      return new FloatObj(array[(int) idx]);
    else
      throw Miscellanea.softFail();
  }

  public static Obj at(Obj[] array, int size, long idx) {
    if (idx < size)
      return array[(int) idx];
    else
      throw Miscellanea.softFail();
  }

  public static boolean boolAt(boolean[] array, int size, long idx) {
    if (idx < size)
      return array[(int) idx];
    else
      throw Miscellanea.softFail();
  }

  public static long longAt(byte[] array, int size, long idx) {
    if (idx < size)
      return array[(int) idx];
    else
      throw Miscellanea.softFail();
  }

  public static long unsignedAt(byte[] array, int size, long idx) {
    if (idx < size)
      return Byte.toUnsignedLong(array[(int) idx]);
    else
      throw Miscellanea.softFail();
  }

  public static long longAt(short[] array, int size, long idx) {
    if (idx < size)
      return array[(int) idx];
    else
      throw Miscellanea.softFail();
  }

  public static long longAt(char[] array, int size, long idx) {
    if (idx < size)
      return array[(int) idx];
    else
      throw Miscellanea.softFail();
  }

  public static long longAt(int[] array, int size, long idx) {
    if (idx < size)
      return array[(int) idx];
    else
      throw Miscellanea.softFail();
  }

  public static long unsignedAt(int[] array, int size, long idx) {
    if (idx < size)
      return Integer.toUnsignedLong(array[(int) idx]);
    else
      throw Miscellanea.softFail();
  }

  public static long longAt(long[] array, int size, long idx) {
    if (idx < size)
      return array[(int) idx];
    else
      throw Miscellanea.softFail();
  }

  public static double floatAt(double[] array, int size, long idx) {
    if (idx < size)
      return array[(int) idx];
    else
      throw Miscellanea.softFail();
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static void reset(long[] array) {
    int len = array.length;
    for (int i=0 ; i < len ; i++)
      array[i] = 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static void fill(int[] array, int value) {
    fill(array, array.length, value);
  }

  public static void fill(int[] array, int count, int value) {
    for (int i=0 ; i < count ; i++)
      array[i] = value;
  }

  public static void fill(int[] array, int offset, int count, int value) {
    for (int i=0 ; i < count ; i++)
      array[offset + i] = value;
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static int[] extend(int[] array, int newSize) {
    Miscellanea._assert(newSize > array.length);
    int[] newArray = new int[newSize];
    copy(array, newArray, array.length);
    return newArray;
  }

  public static long[] extend(long[] array, int newSize) {
    Miscellanea._assert(newSize > array.length);
    long[] newArray = new long[newSize];
    copy(array, newArray, array.length);
    return newArray;
  }

  public static Obj[] extend(Obj[] array, int newSize) {
    Miscellanea._assert(newSize > array.length);
    Obj[] newArray = new Obj[newSize];
    copy(array, newArray, array.length);
    return newArray;
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static void sort(long[] array) {
    Arrays.sort(array);
  }

  public static void sort(long[] array, int start, int end) {
    Arrays.sort(array, start, end);
  }

  public static int anyIndexOrEncodeInsertionPointIntoSortedArray(int[] array, int value) {
    return Arrays.binarySearch(array, value);
  }

  public static int anyIndexOrEncodeInsertionPointIntoSortedArray(int[] array, int start, int end, int value) {
    return Arrays.binarySearch(array, start, end, value);
  }
}