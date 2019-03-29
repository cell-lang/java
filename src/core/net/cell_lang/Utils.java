package net.cell_lang;


class Utils {
  public static int jenkinsHash(int a, int b, int c) {
    a= a - b;  a = a - c;  a = a^ (c >>> 13);
    b= b - c;  b = b - a;  b = b^ (a << 8);
    c= c - a;  c = c - b;  c = c^ (b >>> 13);
    a= a - b;  a = a - c;  a = a^ (c >>> 12);
    b= b - c;  b = b - a;  b = b^ (a << 16);
    c= c - a;  c = c - b;  c = c^ (b >>> 5);
    a= a - b;  a = a - c;  a = a^ (c >>> 3);
    b= b - c;  b = b - a;  b = b^ (a << 10);
    c= c - a;  c = c - b;  c = c^ (b >>> 15);
    return c;
  }

  public static int hash6432shift(int a, int b) {
    long key = ((long) a) | (((long) b) << 32);
    key = (~key) + (key << 18); // key = (key << 18) - key - 1;
    key = key ^ (key >>> 31);
    key = key * 21; // key = (key + (key << 2)) + (key << 4);
    key = key ^ (key >>> 11);
    key = key + (key << 6);
    key = key ^ (key >>> 22);
    return (int) key;
  }

  private static long murmur64(long h) {
    h ^= h >>> 33;
    h *= 0xff51afd7ed558ccdL;
    h ^= h >>> 33;
    h *= 0xc4ceb9fe1a85ec53L;
    h ^= h >>> 33;
    return h;
  }

  public static int murmur64to32(int a, int b) {
    return (int) murmur64(((long) a) | (((long) b) << 32));
  }
}
