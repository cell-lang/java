package net.cell_lang;


class ArraySliceAllocator {
  private static final int MIN_SIZE = 32;

  public static final int EMPTY_MARKER  = 0xFFFFFFFF;

  private static final int END_LOWER_MARKER     = 0xFFFFFFFF;
  private static final int END_UPPER_MARKER_2   = 0x3FFFFFFF;
  private static final int END_UPPER_MARKER_4   = 0x5FFFFFFF;
  private static final int END_UPPER_MARKER_8   = 0x7FFFFFFF;
  private static final int END_UPPER_MARKER_16  = 0x9FFFFFFF;

  private static final int PAYLOAD_MASK  = 0x1FFFFFFF;

  // private static final int BLOCK_1    = 0;
  private static final int BLOCK_2    = 1;
  private static final int BLOCK_4    = 2;
  private static final int BLOCK_8    = 3;
  private static final int BLOCK_16   = 4;
  // private static final int BLOCK_32   = 5;
  // private static final int BLOCK_64   = 6;
  private static final int AVAILABLE  = 7;

  //////////////////////////////////////////////////////////////////////////////

  private long[] slots;
  private int head2, head4, head8, head16;

  //////////////////////////////////////////////////////////////////////////////

  protected static int low(long slot) {
    return (int) (slot & 0xFFFFFFFFL);
  }

  protected static int high(long slot) {
    return (int) (slot >>> 32);
  }

  protected static long combine(int low, int high) {
    long slot = (((long) low) & 0xFFFFFFFFL) | (((long) high) << 32);
    // Miscellanea._assert(low(slot) == low & high(slot) == high);
    return slot;
  }

  //////////////////////////////////////////////////////////////////////////////

  protected static int tag(int word) {
    return word >>> 29;
  }

  protected static int payload(int word) {
    return word & PAYLOAD_MASK;
  }

  protected static int tag(int tag, int payload) {
    // Miscellanea._assert(tag(payload) == 0);
    return (tag << 29) | payload;
  }

  //////////////////////////////////////////////////////////////////////////////

  protected final long[] slots() {
    return slots;
  }

  protected final long slot(int index) {
    return slots[index];
  }

  protected final void setFullSlot(int index, long value) {
    slots[index] = value;
  }

  protected final void setSlot(int index, int low, int high) {
    slots[index] = combine(low, high);
  }

  protected final void setSlotLow(int index, int value) {
    setSlot(index, value, high(slot(index)));
  }

  protected final void setSlotHigh(int index, int value) {
    setSlot(index, low(slot(index)), value);
  }

  //////////////////////////////////////////////////////////////////////////////

  protected ArraySliceAllocator() {
    slots = new long[MIN_SIZE];

    setSlot(0, END_LOWER_MARKER, tag(BLOCK_16, 16));
    for (int i=16 ; i < MIN_SIZE - 16 ; i += 16)
      setSlot(i, tag(AVAILABLE, i - 16), tag(BLOCK_16, i + 16));
    setSlot(MIN_SIZE-16, tag(AVAILABLE, MIN_SIZE-32), END_UPPER_MARKER_16);

    head2 = head4 = head8 = EMPTY_MARKER;
    head16 = 0;
  }

  //////////////////////////////////////////////////////////////////////////////

  protected final int alloc2Block() {
    if (head2 != EMPTY_MARKER) {
      // Miscellanea._assert(low(slot(head2)) == END_LOWER_MARKER);
      // Miscellanea._assert(high(slot(head2)) == END_UPPER_MARKER_2 || tag(high(slot(head2))) == BLOCK_2);

      int blockIdx = head2;
      head2 = removeBlockFromChain(blockIdx, slot(blockIdx), END_UPPER_MARKER_2, head2);
      return blockIdx;
    }
    else {
      int block4Idx = alloc4Block();
      head2 = addBlockToChain(block4Idx, BLOCK_2, END_UPPER_MARKER_2, head2);
      return block4Idx + 2;
    }
  }

  protected final void release2Block(int blockIdx) {
    // Miscellanea._assert((blockIdx & 1) == 0);

    boolean isFirst = (blockIdx & 3) == 0;
    int otherBlockIdx = blockIdx + (isFirst ? 2 : -2);
    long otherBlockSlot0 = slot(otherBlockIdx);

    if (tag(low(otherBlockSlot0)) == AVAILABLE) {
      // Miscellanea._assert(tag(high(otherBlockSlot0)) == BLOCK_2);

      // The matching block is available, so we release both at once as a 4-slot block
      // But first we have to remove the matching block from the 2-slot block chain
      head2 = removeBlockFromChain(otherBlockIdx, otherBlockSlot0, END_UPPER_MARKER_2, head2);
      release4Block(isFirst ? blockIdx : otherBlockIdx);
    }
    else {
      // The matching block is not available, so we
      // just add the new one to the 2-slot block chain
      head2 = addBlockToChain(blockIdx, BLOCK_2, END_UPPER_MARKER_2, head2);
    }
  }

  protected final int alloc4Block() {
    if (head4 != EMPTY_MARKER) {
      // Miscellanea._assert(low(slot(head4)) == END_LOWER_MARKER);
      // Miscellanea._assert(high(slot(head4)) == END_UPPER_MARKER_4 | tag(high(slot(head4))) == BLOCK_4);

      int blockIdx = head4;
      head4 = removeBlockFromChain(blockIdx, slot(blockIdx), END_UPPER_MARKER_4, head4);
      return blockIdx;
    }
    else {
      int block8Idx = alloc8Block();
      head4 = addBlockToChain(block8Idx, BLOCK_4, END_UPPER_MARKER_4, head4);
      return block8Idx + 4;
    }
  }

  protected final void release4Block(int blockIdx) {
    // Miscellanea._assert((blockIdx & 3) == 0);

    boolean isFirst = (blockIdx & 7) == 0;
    int otherBlockIdx = blockIdx + (isFirst ? 4 : -4);
    long otherBlockSlot0 = slot(otherBlockIdx);

    if (tag(low(otherBlockSlot0)) == AVAILABLE & tag(high(otherBlockSlot0)) == BLOCK_4) {
      head4 = removeBlockFromChain(otherBlockIdx, otherBlockSlot0, END_UPPER_MARKER_4, head4);
      release8Block(isFirst ? blockIdx : otherBlockIdx);
    }
    else
      head4 = addBlockToChain(blockIdx, BLOCK_4, END_UPPER_MARKER_4, head4);
  }

  protected final int alloc8Block() {
    if (head8 != EMPTY_MARKER) {
      // Miscellanea._assert(low(slot(head8)) == END_LOWER_MARKER);
      // Miscellanea._assert(high(slot(head8)) == END_UPPER_MARKER_8 | tag(high(slot(head8))) == BLOCK_8);

      int blockIdx = head8;
      head8 = removeBlockFromChain(blockIdx, slot(blockIdx), END_UPPER_MARKER_8, head8);
      return blockIdx;
    }
    else {
      int block16Idx = alloc16Block();
      // Miscellanea._assert(low(slot(block16Idx)) == END_LOWER_MARKER);
      // Miscellanea._assert(high(slot(block16Idx)) == END_UPPER_MARKER_16 | tag(high(slot(block16Idx))) == BLOCK_16);
      head8 = addBlockToChain(block16Idx, BLOCK_8, END_UPPER_MARKER_8, head8);
      return block16Idx + 8;
    }
  }

  protected final void release8Block(int blockIdx) {
    // Miscellanea._assert((blockIdx & 7) == 0);

    boolean isFirst = (blockIdx & 15) == 0;
    int otherBlockIdx = blockIdx + (isFirst ? 8 : -8);
    long otherBlockSlot0 = slot(otherBlockIdx);

    if (tag(low(otherBlockSlot0)) == AVAILABLE & tag(high(otherBlockSlot0)) == BLOCK_8) {
      head8 = removeBlockFromChain(otherBlockIdx, otherBlockSlot0, END_UPPER_MARKER_8, head8);
      release16Block(isFirst ? blockIdx : otherBlockIdx);
    }
    else
      head8 = addBlockToChain(blockIdx, BLOCK_8, END_UPPER_MARKER_8, head8);
  }

  protected final void release8BlockUpperHalf(int blockIdx) {
    head4 = addBlockToChain(blockIdx+4, BLOCK_4, END_UPPER_MARKER_4, head4);
  }

  protected final int alloc16Block() {
    if (head16 == EMPTY_MARKER) {
      int len = slots.length;
      long[] newSlots = new long[2*len];
      Array.copy(slots, newSlots, len);
      slots = newSlots;
      for (int i=len ; i < 2 * len ; i += 16)
        setSlot(i, tag(AVAILABLE, i - 16), tag(BLOCK_16, i + 16));

      // Miscellanea._assert(high(slot(len)) == tag(BLOCK_16, len + 16));
      // Miscellanea._assert(low(slot(2 * len - 16)) == tag(AVAILABLE, 2 * len - 32));

      setSlot(len, END_LOWER_MARKER, tag(BLOCK_16, len + 16));
      setSlot(2 * len - 16, tag(AVAILABLE, 2 * len - 32), END_UPPER_MARKER_16);

      head16 = len;
    }

    // Miscellanea._assert(low(slot(head16)) == END_LOWER_MARKER);
    // Miscellanea._assert(high(slot(head16)) == END_UPPER_MARKER_16 | tag(high(slot(head16))) == BLOCK_16);

    int blockIdx = head16;
    head16 = removeBlockFromChain(blockIdx, slot(blockIdx), END_UPPER_MARKER_16, head16);
    return blockIdx;
  }

  protected final void release16Block(int blockIdx) {
    head16 = addBlockToChain(blockIdx, BLOCK_16, END_UPPER_MARKER_16, head16);
  }

  protected final void release16BlockUpperHalf(int blockIdx) {
    head8 = addBlockToChain(blockIdx+8, BLOCK_8, END_UPPER_MARKER_8, head8);
  }

  //////////////////////////////////////////////////////////////////////////////

  private int removeBlockFromChain(int blockIdx, long firstSlot, int endUpperMarker, int head) {
    int firstLow = low(firstSlot);
    int firstHigh = high(firstSlot);

    if (firstLow != END_LOWER_MARKER) {
      // Not the first block in the chain
      // Miscellanea._assert(head != blockIdx);
      int prevBlockIdx = payload(firstLow);

      if (firstHigh != endUpperMarker) {
        // The block is in the middle of the chain
        // The previous and next blocks must be repointed to each other
        int nextBlockIdx = payload(firstHigh);
        setSlotHigh(prevBlockIdx, firstHigh);
        setSlotLow(nextBlockIdx, firstLow);
      }
      else {
        // Last block in a chain with multiple blocks
        // The 'next' field of the previous block must be cleared
        setSlotHigh(prevBlockIdx, endUpperMarker);
      }
    }
    else {
      // First slot in the chain, must be the one pointed to by head
      // Miscellanea._assert(head == blockIdx);

      if (firstHigh != endUpperMarker) {
        // The head must be repointed at the next block,
        // whose 'previous' field must now be cleared
        int nextBlockIdx = payload(firstHigh);
        head = nextBlockIdx;
        setSlotLow(nextBlockIdx, END_LOWER_MARKER);
      }
      else {
        // No 'previous' nor 'next' slots, it must be the only one
        // Just resetting the head of the 2-slot block chain
        head = EMPTY_MARKER;
      }
    }

    return head;
  }

  private int addBlockToChain(int blockIdx, int sizeTag, int endUpperMarker, int head) {
    if (head != EMPTY_MARKER) {
      // If the list of blocks is not empty, we link the first two blocks
      // The 'previous' field of the newly released block must be cleared
      setSlot(blockIdx, END_LOWER_MARKER, tag(sizeTag, head));
      setSlotLow(head, tag(AVAILABLE, blockIdx));
    }
    else {
      // Otherwise we just clear then 'next' field of the newly released block
      // The 'previous' field of the newly released block must be cleared
      setSlot(blockIdx, END_LOWER_MARKER, endUpperMarker);
    }
    // The new block becomes the head one
    return blockIdx;
  }
}