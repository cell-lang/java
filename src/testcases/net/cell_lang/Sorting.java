package net.cell_lang;

import java.util.Random;


public class Sorting {
  static Random rand = new Random(0);

  public static void main(String[] args) {
    for (int i=0 ; i < 1000 ; i++) {
      runInts123RandomTest();
      if ((i + 1) % 100 == 0)
        System.out.print('.');
    }
  }

  static void runInts123RandomTest() {
    int len = rand.nextInt(200);
    int[] array = new int[3 * len];

    int range = 1 + rand.nextInt(1000);
    for (int i=0 ; i < 3 * len ; i++)
      array[i] = rand.nextInt(range);

    Ints123.sort(array, len);

    for (int i=1 ; i < len ; i++)
      if (!isOrd(array, i)) {
        System.out.println("ERROR!");
        System.exit(1);
      }
  }

  static boolean isOrd(int[] array, int i) {
    int offset = 3 * i;

    int prev1 = array[offset-3];
    int prev2 = array[offset-2];
    int prev3 = array[offset-1];
    int curr1 = array[offset];
    int curr2 = array[offset+1];
    int curr3 = array[offset+2];

    if (prev1 != curr1)
      return prev1 < curr1;

    if (prev2 != curr2)
      return prev2 < curr2;

    return prev3 <= curr3;
  }
}
