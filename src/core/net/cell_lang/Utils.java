package net.cell_lang;


class Utils {
  public static int hashcode(long data64, int data32) {
    return (int) ((data64 >>> 32) ^ (data64 & 0xFFFFFFFF) ^ ~data32);
  }
}
