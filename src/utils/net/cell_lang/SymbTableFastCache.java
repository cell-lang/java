package net.cell_lang;


final class SymbTableFastCache {
  private static final int SIZE = 4096;

  private static long[] encSymbs1     = new long[SIZE];
  private static int[]  encSymbsIdxs1 = new  int[SIZE];

  private static long[] encSymbs2     = new long[2 * SIZE];
  private static int[]  encSymbsIdxs2 = new  int[SIZE];

  private static long[] encSymbs3;
  private static int[]  encSymbsIdxs3;

  private static long[][] encSymbs;
  private static int[]    encSymbsIdxs;


  public static int encToIdx(long encWord) {
    int hashcode = Hashing.hashcode64(encWord);
    int idx = hashcode % SIZE;
    if (idx < 0)
      idx = -idx;

    long storedEnc = encSymbs1[idx];
    if (storedEnc == encWord)
      return encSymbsIdxs1[idx];

    byte[] bytes = decode(encWord);
    int symbIdx = SymbObj.bytesToIdx(bytes);

    if (storedEnc == 0) {
      encSymbs1[idx] = encWord;
      encSymbsIdxs1[idx] = symbIdx;
    }

    return symbIdx;
  }

  public static int encToIdx(long encWord1, long encWord2) {
    int hashcode1 = Hashing.hashcode64(encWord1);
    int hashcode2 = Hashing.hashcode64(encWord2);
    int idx = (31 * hashcode1 + hashcode2) % SIZE;
    if (idx < 0)
      idx = -idx;

    long storedEnc1 = encSymbs2[2 * idx];
    long storedEnc2 = encSymbs2[2 * idx + 1];

    if (storedEnc1 == encWord1 & storedEnc2 == encWord2)
      return encSymbsIdxs2[idx];

    byte[] bytes = decode(encWord1, encWord2);
    int symbIdx = SymbObj.bytesToIdx(bytes);

    if (storedEnc1 == 0) {
      encSymbs2[2 * idx] = encWord1;
      encSymbs2[2 * idx + 1] = encWord2;
      encSymbsIdxs2[idx] = symbIdx;
    }

    return symbIdx;
  }

  public static int encToIdx(long encWord1, long encWord2, long encWord3) {
    int hashcode1 = Hashing.hashcode64(encWord1);
    int hashcode2 = Hashing.hashcode64(encWord2);
    int hashcode3 = Hashing.hashcode64(encWord3);
    int idx = (31 * 31 * hashcode1 + 31 * hashcode2 + hashcode3) % SIZE;
    if (idx < 0)
      idx = -idx;

    if (encSymbs3 == null) {
      encSymbs3     = new long[3 * SIZE];
      encSymbsIdxs3 = new  int[SIZE];
    }

    long storedEnc1 = encSymbs3[3 * idx];
    long storedEnc2 = encSymbs3[3 * idx + 1];
    long storedEnc3 = encSymbs3[3 * idx + 2];

    if (storedEnc1 == encWord1 & storedEnc2 == encWord2 & storedEnc3 == encWord3)
      return encSymbsIdxs3[idx];

    byte[] bytes = decode(encWord1, encWord2, encWord3);
    int symbIdx = SymbObj.bytesToIdx(bytes);

    if (storedEnc1 == 0) {
      encSymbs3[3 * idx] = encWord1;
      encSymbs3[3 * idx + 1] = encWord2;
      encSymbs3[3 * idx + 2] = encWord3;
      encSymbsIdxs3[idx] = symbIdx;
    }

    return symbIdx;
  }

  public static int encToIdx(long[] encWords, int count) {
    int hashcode = Hashing.hashcode64(encWords[0]);
    for (int i=1 ; i < count ; i++)
      hashcode = 31 * hashcode + Hashing.hashcode64(encWords[i]);
    int idx = hashcode % SIZE;
    if (idx < 0)
      idx = -idx;

    if (encSymbs == null) {
      encSymbs = new long[SIZE][];
      encSymbsIdxs = new int[SIZE];
    }

    long[] storedEncs = encSymbs[idx];
    if (storedEncs != null && storedEncs.length == count && Array.isPrefix(storedEncs, encWords))
      return encSymbsIdxs[idx];

    byte[] bytes = decode(encWords, count);
    int symbIdx = SymbObj.bytesToIdx(bytes);

    if (storedEncs == null) {
      encSymbs[idx] = Array.take(encWords, count);
      encSymbsIdxs[idx] = symbIdx;
    }

    return symbIdx;
  }

  //////////////////////////////////////////////////////////////////////////////

  //  0         Empty
  //  1 - 26    Letter
  // 27 - 36    Digit
  // 37         Underscore (followed by a digit)
  // 38 - 63    Underscore + letter

  public static int ENCODED_UNDERSCORE = 37;

  public static int encodedLetter(int ch) {
    return ch - 'a' + 1;
  }

  public static int encodedDigit(int ch) {
    return ch - '0' + 27;
  }

  public static int encodedUnderscoredLetter(int ch) {
    return ch - 'a' + 38;
  }

  public static byte[] decode(long encWord) {
    int size = size(encWord);
    byte[] bytes = new byte[size];
    int idx = decode(encWord, bytes, size-1);
    Miscellanea._assert(idx == -1);
    return bytes;
  }

  public static byte[] decode(long encWord1, long encWord2) {
    int size = size(encWord1, encWord2);
    byte[] bytes = new byte[size];
    int idx = decode(encWord2, bytes, size-1);
    idx = decode(encWord1, bytes, idx);
    Miscellanea._assert(idx == -1);
    return bytes;
  }

  public static byte[] decode(long encWord1, long encWord2, long encWord3) {
    int size = size(encWord1, encWord2, encWord3);
    byte[] bytes = new byte[size];
    int idx = decode(encWord3, bytes, size-1);
    idx = decode(encWord2, bytes, idx);
    idx = decode(encWord1, bytes, idx);
    Miscellanea._assert(idx == -1);
    return bytes;
  }

  public static byte[] decode(long[] encWords, int count) {
    int size = size(encWords, count);
    byte[] bytes = new byte[size];
    int idx = size - 1;
    for (int i = count - 1 ; i >= 0 ; i--)
      idx = decode(encWords[i], bytes, idx);
    Miscellanea._assert(idx == -1);
    return bytes;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static int size(long word) {
    int size = 0;
    while (word != 0) {
      int code = (int) (word & 0x3F);
      size += code >= 38 ? 2 : 1;
      word = word >>> 6;
    }
    Miscellanea._assert(size > 0);
    return size;
  }

  private static int size(long word1, long word2) {
    return size(word1) + size(word2);
  }

  private static int size(long word1, long word2, long word3) {
    return size(word1) + size(word2) + size(word3);
  }

  private static int size(long[] words, int count) {
    int size = 0;
    for (int i=0 ; i < count ; i++)
      size += size(words[i]);
    return size;
  }

  private static int decode(long word, byte[] bytes, int idx) {
    while (word != 0) {
      int code = (int) (word & 0x3F);
      Miscellanea._assert(code != 0);
      if (code <= 26) {
        bytes[idx--] = (byte) (code - 1 + 'a');
      }
      else if (code <= 36) {
        bytes[idx--] = (byte) (code - 27 + '0');
      }
      else if (code == 37) {
        bytes[idx--] = '_';
      }
      else {
        bytes[idx--] = (byte) (code - 38 + 'a');
        bytes[idx--] = '_';
      }
      word = word >>> 6;
    }
    return idx;
  }
}
