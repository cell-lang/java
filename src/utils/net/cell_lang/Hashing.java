package net.cell_lang;


class Hashing {
  public static int hashcode(int n) {
    return n;
  }

  public static int hashcode64(long n) {
    return hashcode((int) n, (int) (n >> 32));
  }

  public static int hashcode(int n1, int n2) {
    return hash6432shift(n1, n2);
  }

  public static int hashcode(int n1, int n2, int n3) {
    return jenkinsHash(n1, n2, n3);
  }

  //////////////////////////////////////////////////////////////////////////////

  private static int jenkinsHash(int a, int b, int c) {
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

  private static int hash6432shift(int a, int b) {
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

  private static int murmur64to32(int a, int b) {
    return (int) murmur64(((long) a) | (((long) b) << 32));
  }

  private static int wang32hash(int key) {
    key = ~key + (key << 15); // key = (key << 15) - key - 1;
    key = key ^ (key >>> 12);
    key = key + (key << 2);
    key = key ^ (key >>> 4);
    key = key * 2057; // key = (key + (key << 3)) + (key << 11);
    key = key ^ (key >>> 16);
    return key;
  }
}
