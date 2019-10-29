package net.cell_lang;

import java.util.Arrays;


class Array {
  public static Obj[] emptyObjArray = new Obj[0];
  public static int[] emptyIntArray = new int[0];
  public static long[] emptyLongArray = new long[0];
  public static double[] emptyDoubleArray = new double[0];
  public static boolean[] emptyBooleanArray = new boolean[0];

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static char[] repeat(char ch, int len) {
    char[] array = new char[len];
    fill(array, ch);
    return array;
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static int capacity(int curr, int min) {
    int capacity = 256;
    while (capacity < min)
      capacity *= 2;
    return capacity;
  }

  public static int nextCapacity(int currCapacity) {
    return Math.max(32, currCapacity + currCapacity / 2);
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

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

  public static int[] take(int[] array, int count) {
    Miscellanea._assert(count <= array.length);
    return Arrays.copyOf(array, count);
  }

  public static long[] take(long[] array, int count) {
    Miscellanea._assert(count <= array.length);
    return Arrays.copyOf(array, count);
  }

  public static Obj[] take(Obj[] array, int count) {
    Miscellanea._assert(count <= array.length);
    return Arrays.copyOf(array, count);
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static byte[] append(byte[] array, int count, long value) {
    // Miscellanea._assert(count <= array.length);
    // Miscellanea._assert(value >= -128 & value <= 127);

    if (count == array.length)
      array = extend(array, Math.max(32, 3 * count / 2));
    array[count] = (byte) value;
    return array;
  }

  public static short[] append(short[] array, int count, long value) {
    // Miscellanea._assert(count <= array.length);
    // Miscellanea._assert(value >= -32768 & value <= 32767);

    if (count == array.length)
      array = extend(array, Math.max(32, 3 * count / 2));
    array[count] = (short) value;
    return array;
  }

  public static int[] append(int[] array, int count, int value) {
    Miscellanea._assert(count <= array.length);

    if (count == array.length)
      array = extend(array, Math.max(32, 3 * count / 2));
    array[count] = value;
    return array;
  }

  public static long[] append(long[] array, int count, long value) {
    Miscellanea._assert(count <= array.length);

    if (count == array.length)
      array = extend(array, Math.max(32, 3 * count / 2));
    array[count] = value;
    return array;
  }

  public static double[] append(double[] array, int count, double value) {
    Miscellanea._assert(count <= array.length);

    if (count == array.length)
      array = extend(array, Math.max(32, 3 * count / 2));
    array[count] = value;
    return array;
  }

  public static Obj[] append(Obj[] array, int count, Obj value) {
    Miscellanea._assert(count <= array.length);

    if (count == array.length)
      array = extend(array, Math.max(32, 3 * count / 2));

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

  public static byte asByte(long value) {
    return (byte) value;
  }

  public static short asShort(long value) {
    return (short) value;
  }

  public static int asInt(long value) {
    return (int) value;
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

  public static void fill(char[] array, char value) {
    Arrays.fill(array, value);
  }

  public static void fill(int[] array, int value) {
    Arrays.fill(array, value);
  }

  public static void fill(long[] array, long value) {
    Arrays.fill(array, value);
  }

  public static void fill(double[] array, double value) {
    Arrays.fill(array, value);
  }

  public static void fill(int[] array, int count, int value) {
    Arrays.fill(array, 0, count, value);
  }

  public static void fill(int[] array, int offset, int count, int value) {
    Arrays.fill(array, offset, offset+count, value);
  }

  public static void fill(long[] array, int offset, int count, long value) {
    Arrays.fill(array, offset, offset+count, value);
  }

  public static void fill(double[] array, int offset, int count, double value) {
    Arrays.fill(array, offset, offset+count, value);
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static byte[] extend(byte[] array, int newSize) {
    Miscellanea._assert(newSize > array.length);
    return Arrays.copyOf(array, newSize);
  }

  public static short[] extend(short[] array, int newSize) {
    Miscellanea._assert(newSize > array.length);
    return Arrays.copyOf(array, newSize);
  }

  public static char[] extend(char[] array, int newSize) {
    Miscellanea._assert(newSize > array.length);
    return Arrays.copyOf(array, newSize);
  }

  public static int[] extend(int[] array, int newSize) {
    Miscellanea._assert(newSize > array.length);
    return Arrays.copyOf(array, newSize);
  }

  public static long[] extend(long[] array, int newSize) {
    Miscellanea._assert(newSize > array.length);
    return Arrays.copyOf(array, newSize);
  }

  public static double[] extend(double[] array, int newSize) {
    Miscellanea._assert(newSize > array.length);
    return Arrays.copyOf(array, newSize);
  }

  public static Obj[] extend(Obj[] array, int newSize) {
    Miscellanea._assert(newSize > array.length);
    return Arrays.copyOf(array, newSize);
  }

  public static long[] extend(long[] array, int newSize, long defaultValue) {
    Miscellanea._assert(newSize > array.length);
    long[] newArray = Arrays.copyOf(array, newSize);
    Arrays.fill(newArray, array.length, newSize, defaultValue);
    return newArray;
  }

  public static double[] extend(double[] array, int newSize, double defaultValue) {
    Miscellanea._assert(newSize > array.length);
    double[] newArray = Arrays.copyOf(array, newSize);
    Arrays.fill(newArray, array.length, newSize, defaultValue);
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

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static boolean isPrefix(long[] prefixArray, long[] array) {
    for (int i=0 ; i < prefixArray.length ; i++)
      if (prefixArray[i] != array[i])
        return false;
    return true;
  }
}