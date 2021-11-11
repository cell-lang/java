package net.cell_lang;


// A slot can be in any of the following states:
//   - Value + data:        32 bit data              - 3 zeros   - 29 bit value
//   - Index + count:       32 bit count             - 3 bit tag - 29 bit index
//     This type of slot can only be stored in a hashed block or passed in and out
//   - Empty:               32 zeros                 - 32 ones
//     This type of slot can only be stored in a block, but cannot be passed in or out

class LoadedOverflowTable extends ArraySliceAllocator {
  public  final static int INLINE_SLOT    = 0;
  private final static int SIZE_2_BLOCK   = 1;
  private final static int SIZE_4_BLOCK   = 2;
  private final static int SIZE_8_BLOCK   = 3;
  private final static int SIZE_16_BLOCK  = 4;
  private final static int HASHED_BLOCK   = 5;

  private final static int SIZE_2_BLOCK_MIN_COUNT   = 2;
  private final static int SIZE_4_BLOCK_MIN_COUNT   = 2;
  private final static int SIZE_8_BLOCK_MIN_COUNT   = 3;
  private final static int SIZE_16_BLOCK_MIN_COUNT  = 7;
  private final static int HASHED_BLOCK_MIN_COUNT   = 7;

  public final static long EMPTY_SLOT = 0xFFFFFFFFL;

  //////////////////////////////////////////////////////////////////////////////

  public long insertUnique(long handle, int value, int data) {
    int low = low(handle);
    int tag = tag(low);

    if (tag == 0)
      return insertUnique2Block(handle, value, data);

    if (tag == HASHED_BLOCK)
      return insertUniqueIntoHashedBlock(payload(low), count(handle), value, data);

    return insertUniqueWithLinearBlock(handle, value, data);
  }

  public long delete(long handle, int value, int[] data) {
    int low = low(handle);
    int tag = tag(low);

    // Miscellanea._assert(tag != INLINE_SLOT);

    if (tag == HASHED_BLOCK)
      return deleteFromHashedBlock(payload(low), count(handle), value, data);
    else
      return deleteFromLinearBlock(handle, value, data);
  }

  public void delete(long handle) {
    int low = low(handle);
    int tag = tag(low);
    int blockIdx = payload(low);

    // Miscellanea._assert(tag != INLINE_SLOT);

    if (tag == SIZE_2_BLOCK)
      release2Block(blockIdx);
    else if (tag == SIZE_4_BLOCK)
      release4Block(blockIdx);
    else if (tag == SIZE_8_BLOCK)
      release8Block(blockIdx);
    else if (tag == SIZE_16_BLOCK)
      release16Block(blockIdx);
    else {
      // Miscellanea._assert(tag == HASHED_BLOCK);
      for (int i=0 ; i < 16 ; i++) {
        long slot = slot(blockIdx + i);
        if (slot != EMPTY_SLOT && tag(low(slot)) != INLINE_SLOT)
          delete(slot);
      }
    }
  }

  public int lookup(long handle, int value) {
    int low = low(handle);
    int tag = tag(low);
    int blockIdx = payload(low);

    // Miscellanea._assert(tag != INLINE_SLOT);
    // Miscellanea._assert(tag(tag, blockIdx) == low(handle));

    if (tag != HASHED_BLOCK)
      return linearBlockLookup(blockIdx, count(handle), value);
    else
      return hashedBlockLookup(blockIdx, value);
  }

  public void copy(long handle, int[] surrs2, int[] data) {
    copy(handle, surrs2, data, 0, 1);
  }

  public void copy(long handle, int[] surrs2, int[] data, int offset, int step) {
    int low = low(handle);
    int tag = tag(low);
    int blockIdx = payload(low);

    // Miscellanea._assert(tag != INLINE_SLOT);
    // Miscellanea._assert(tag(tag, blockIdx) == low(handle));

    if (tag != HASHED_BLOCK) {
      int count = count(handle);
      int targetIdx = offset;

      for (int i=0 ; i < count ; i++) {
        long slot = slot(blockIdx + i);

        // Miscellanea._assert(slot != EMPTY_SLOT & tag(low(slot)) == INLINE_SLOT);

        surrs2[targetIdx] = low(slot);
        if (data != null)
          data[targetIdx] = high(slot);

        targetIdx += step;
      }
    }
    else
      copyHashedBlock(blockIdx, surrs2, data, offset, step, 0, 0);
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static int count(long slot) {
    // Miscellanea._assert(tag(low(slot)) >= SIZE_2_BLOCK & tag(low(slot)) <= HASHED_BLOCK);
    // // Miscellanea._assert(high(slot) > 2); // Not true when initializing a hashed block
    return high(slot);
  }

  ////////////////////////////////////////////////////////////////////////////

  private static int capacity(int tag) {
    // Miscellanea._assert(tag >= SIZE_2_BLOCK & tag <= SIZE_16_BLOCK);
    // Miscellanea._assert(SIZE_2_BLOCK == 1 | SIZE_16_BLOCK == 4);
    return 1 << tag;
  }

  private static long linearBlockHandle(int tag, int index, int count) {
    return combine(tag(tag, index), count);
  }

  private static long size2BlockHandle(int index) {
    // Miscellanea._assert(tag(index) == 0);
    return combine(tag(SIZE_2_BLOCK, index), 2);
  }

  private static long size4BlockHandle(int index, int count) {
    // Miscellanea._assert(tag(index) == 0);
    // Miscellanea._assert(count >= SIZE_4_BLOCK_MIN_COUNT & count <= 4);
    return combine(tag(SIZE_4_BLOCK, index), count);
  }

  private static long size8BlockHandle(int index, int count) {
    // Miscellanea._assert(tag(index) == 0);
    // Miscellanea._assert(count >= SIZE_8_BLOCK_MIN_COUNT & count <= 8);
    return combine(tag(SIZE_8_BLOCK, index), count);
  }

  private static long size16BlockHandle(int index, int count) {
    // Miscellanea._assert(tag(index) == 0);
    // Miscellanea._assert(count >= SIZE_16_BLOCK_MIN_COUNT & count <= 16);
    return combine(tag(SIZE_16_BLOCK, index), count);
  }

  private static long hashedBlockHandle(int index, int count) {
    // Miscellanea._assert(tag(index) == 0);
    // // Miscellanea._assert(count >= 7); // Not true when initializing a hashed block
    long handle = combine(tag(HASHED_BLOCK, index), count);
    // Miscellanea._assert(tag(low(handle)) == HASHED_BLOCK);
    // Miscellanea._assert(payload(low(handle)) == index);
    // Miscellanea._assert(count(handle) == count);
    return handle;
  }

  private static int index(int value) {
    // Miscellanea._assert(tag(value) == INLINE_SLOT);
    return value & 0xF;
  }

  private static int clipped(int value) {
    return value >>> 4;
  }

  private static int unclipped(int value, int index) {
    // Miscellanea._assert(tag(value) == 0);
    // Miscellanea._assert(tag(value << 4) == 0);
    // Miscellanea._assert(index >= 0 & index < 16);
    return (value << 4) | index;
  }

  private int minCount(int tag) {
    if (tag == SIZE_2_BLOCK)
      return SIZE_2_BLOCK_MIN_COUNT;

    if (tag == SIZE_4_BLOCK)
      return SIZE_4_BLOCK_MIN_COUNT;

    if (tag == SIZE_8_BLOCK)
      return SIZE_8_BLOCK_MIN_COUNT;

    // Miscellanea._assert(tag == SIZE_16_BLOCK | tag == HASHED_BLOCK);
    // Miscellanea._assert(SIZE_16_BLOCK_MIN_COUNT == HASHED_BLOCK_MIN_COUNT);

    return SIZE_16_BLOCK_MIN_COUNT; // Same as HASHED_BLOCK_MIN_COUNT
  }

  private static boolean isEven(int value) {
    return (value % 2) == 0;
  }

  ////////////////////////////////////////////////////////////////////////////

  private void markSlotAsEmpty(int index) {
    setFullSlot(index, EMPTY_SLOT);
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  private int linearBlockLookup(int blockIdx, int count, int value) {
    for (int i=0 ; i < count ; i++) {
      long slot = slot(blockIdx + i);
      // Miscellanea._assert(tag(low(slot)) == INLINE_SLOT);
      if (value == low(slot))
        return high(slot);
    }
    return 0xFFFFFFFF;
  }

  private int hashedBlockLookup(int blockIdx, int value) {
    int slotIdx = blockIdx + index(value);
    long slot = slot(slotIdx);

    if (slot == EMPTY_SLOT)
      return 0xFFFFFFFF;

    int low = low(slot);
    int tag = tag(low);

    if (tag == INLINE_SLOT)
      return value == low ? high(slot) : 0xFFFFFFFF;

    int subblockIdx = payload(low);
    int clippedValue = clipped(value);

    if (tag == HASHED_BLOCK)
      return hashedBlockLookup(subblockIdx, clippedValue);
    else
      return linearBlockLookup(subblockIdx, count(slot), clippedValue);
  }

  ////////////////////////////////////////////////////////////////////////////

  private int copyHashedBlock(int blockIdx, int[] surrs2, int[] data, int offset, int step, int shift, int leastBits) {
    int subshift = shift + 4;
    int targetIdx = offset;

    for (int i=0 ; i < 16 ; i++) {
      int slotLeastBits = (i << shift) + leastBits;
      long slot = slot(blockIdx + i);

      if (slot != EMPTY_SLOT) {
        int low = low(slot);
        int tag = tag(low);

        if (tag == INLINE_SLOT) {
          surrs2[targetIdx] = (payload(low) << shift) + leastBits;
          if (data != null)
            data[targetIdx] = high(slot);
          targetIdx += step;
        }
        else if (tag == HASHED_BLOCK) {
          targetIdx = copyHashedBlock(payload(low), surrs2, data, targetIdx, step, subshift, slotLeastBits);
        }
        else {
          int subblockIdx = payload(low);
          int count = count(slot);

          for (int j=0 ; j < count ; j++) {
            long subslot = slot(subblockIdx + j);

            // Miscellanea._assert(subslot != EMPTY_SLOT & tag(low(subslot)) == INLINE_SLOT);

            surrs2[targetIdx] = (low(subslot) << subshift) + slotLeastBits;
            if (data != null)
              data[targetIdx] = high(subslot);
            targetIdx += step;
          }
        }
      }
    }

    return targetIdx;
  }

  ////////////////////////////////////////////////////////////////////////////

  private long insertUnique2Block(long handle, int value, int data) {
    // Miscellanea._assert(low(handle) != value);
    // Miscellanea._assert(tag(low(handle)) == INLINE_SLOT);

    int blockIdx = alloc2Block();
    setFullSlot(blockIdx, handle);
    setSlot(blockIdx + 1, value, data);
    return size2BlockHandle(blockIdx);
  }

  ////////////////////////////////////////////////////////////////////////////

  private long insertUniqueWithLinearBlock(long handle, int value, int data) {
    // Miscellanea._assert(tag(low(handle)) >= SIZE_2_BLOCK & tag(low(handle)) <= SIZE_16_BLOCK);

    int low = low(handle);
    int tag = tag(low);
    int blockIdx = payload(low);
    int count = count(handle);
    int capacity = capacity(tag);

    // Inserting the new value if there's still room here
    if (count < capacity) {
      int slotIdx = blockIdx + count;
      setSlot(slotIdx, value, data);
      return linearBlockHandle(tag, blockIdx, count + 1);
    }

    if (tag != SIZE_16_BLOCK) {
      // Allocating the new block
      int newBlockIdx;
      if (tag == SIZE_2_BLOCK)
        newBlockIdx = alloc4Block();
      else if (tag == SIZE_4_BLOCK)
        newBlockIdx = alloc8Block();
      else
        newBlockIdx = alloc16Block();

      // Initializing the new block
      for (int i=0 ; i < count ; i++)
        setFullSlot(newBlockIdx + i, slot(blockIdx + i));
      setSlot(newBlockIdx + count, value, data);
      for (int i=count+1 ; i < 2 * count ; i++)
        markSlotAsEmpty(newBlockIdx + i);

      // Releasing the old block
      if (tag == SIZE_2_BLOCK)
        release2Block(blockIdx);
      else if (tag == SIZE_4_BLOCK)
        release4Block(blockIdx);
      else
        release8Block(blockIdx);

      return linearBlockHandle(tag + 1, newBlockIdx, count + 1);
    }

    // Allocating and initializing the hashed block
    int hashedBlockIdx = alloc16Block();
    for (int i=0 ; i < 16 ; i++)
      markSlotAsEmpty(hashedBlockIdx + i);

    // Transferring the existing values
    for (int i=0 ; i < 16 ; i++) {
      long slot = slot(blockIdx + i);
      long tempHandle = insertUniqueIntoHashedBlock(hashedBlockIdx, i, low(slot), high(slot));
      // Miscellanea._assert(count(tempHandle) == i + 1);
      // Miscellanea._assert(payload(low(tempHandle)) == hashedBlockIdx);
    }

    // Releasing the old block
    release16Block(blockIdx);

    // Adding the new value
    return insertUniqueIntoHashedBlock(hashedBlockIdx, 16, value, data);
  }

  private long insertUniqueIntoHashedBlock(int blockIdx, int count, int value, int data) {
    int slotIdx = blockIdx + index(value);
    long slot = slot(slotIdx);

    // Checking for empty slots
    if (slot == EMPTY_SLOT) {
      setSlot(slotIdx, value, data);
      return hashedBlockHandle(blockIdx, count + 1);
    }

    int low = low(slot);
    int tag = tag(low);

    // Checking for inline slots
    if (tag == INLINE_SLOT) {
      // Miscellanea._assert(value != low);
      long handle = insertUnique2Block(combine(clipped(low), high(slot)), clipped(value), data);
      // Miscellanea._assert(count(handle) == 2);
      setFullSlot(slotIdx, handle);
      return hashedBlockHandle(blockIdx, count + 1);
    }

    // The slot is not an inline one. Inserting the clipped value into the subblock

    long handle;
    if (tag == HASHED_BLOCK)
      handle = insertUniqueIntoHashedBlock(payload(low), count(slot), clipped(value), data);
    else
      handle = insertUniqueWithLinearBlock(slot, clipped(value), data);

    // Miscellanea._assert(count(handle) == count(slot) + 1);
    setFullSlot(slotIdx, handle);
    return hashedBlockHandle(blockIdx, count + 1);
  }

  ////////////////////////////////////////////////////////////////////////////

  private long deleteFromLinearBlock(long handle, int value, int[] data) {
    int tag = tag(low(handle));
    int blockIdx = payload(low(handle));
    int count = count(handle);

    int lastSlotIdx = blockIdx + count - 1;
    long lastSlot = slot(lastSlotIdx);

    // Miscellanea._assert(lastSlot != EMPTY_SLOT);
    // Miscellanea._assert(count == capacity(tag) || slot(blockIdx + count) == EMPTY_SLOT);

    int lastLow = low(lastSlot);

    // Checking the last slot first
    if (value == lastLow) {
      // Removing the value
      markSlotAsEmpty(lastSlotIdx);

      if (data != null)
        data[0] = high(lastSlot);

      // Shrinking the block if need be
      if (count == minCount(tag))
        return shrinkLinearBlock(tag, blockIdx, count - 1);
      else
        return linearBlockHandle(tag, blockIdx, count - 1);
    }

    // The last slot didn't contain the searched value, looking in the rest of the array
    for (int i = lastSlotIdx - 1 ; i >= blockIdx ; i--) {
      long slot = slot(i);
      int low = low(slot);

      // Miscellanea._assert(slot != EMPTY_SLOT && tag(low) == INLINE_SLOT);

      if (value == low) {
        // Replacing the value to be deleted with the last one
        setFullSlot(i, lastSlot);

        // Clearing the last slot whose value has been stored in the delete slot
        markSlotAsEmpty(lastSlotIdx);

        if (data != null)
          data[0] = high(slot);

        // Shrinking the block if need be
        if (count == minCount(tag))
          return shrinkLinearBlock(tag, blockIdx, count - 1);
        else
          return linearBlockHandle(tag, blockIdx, count - 1);
      }
    }

    // Value not found
    return handle;
  }

  private long shrinkLinearBlock(int tag, int blockIdx, int count) {
    if (tag == SIZE_2_BLOCK | tag == SIZE_4_BLOCK) {
      // Miscellanea._assert(count == 1);
      return slot(blockIdx);
    }

    if (tag == SIZE_8_BLOCK) {
      // Miscellanea._assert(count == 2);
      int block2Idx = alloc2Block();
      setFullSlot(block2Idx, slot(blockIdx));
      setFullSlot(block2Idx + 1, slot(blockIdx + 1));
      release8Block(blockIdx);
      return size2BlockHandle(block2Idx);

      // release8BlockUpperHalf(blockIdx);
      // return size4BlockHandle(blockIdx, count);
    }

    // Miscellanea._assert(tag == SIZE_16_BLOCK);
    // Miscellanea._assert(count == 6);
    release16BlockUpperHalf(blockIdx);
    return size8BlockHandle(blockIdx, count);
  }

  ////////////////////////////////////////////////////////////////////////////

  private long deleteFromHashedBlock(int blockIdx, int count, int value, int[] data) {
    int index = index(value);
    int slotIdx = blockIdx + index;
    long slot = slot(slotIdx);

    // If the slot is empty there's nothing to do
    if (slot == EMPTY_SLOT)
      return hashedBlockHandle(blockIdx, count);

    int low = low(slot);

    // If the slot is not inline, we recursively call delete(..) with a clipped value
    if (tag(low) != INLINE_SLOT) {
      long handle = delete(slot, clipped(value), data);
      if (handle == slot)
        return hashedBlockHandle(blockIdx, count);
      int handleLow = low(handle);
      if (tag(handleLow) == INLINE_SLOT)
        handle = combine(unclipped(handleLow, index), high(handle));
      setFullSlot(slotIdx, handle);
    }
    else if (low == value) {
      markSlotAsEmpty(slotIdx);
      if (data != null)
        data[0] = high(slot);
    }
    else {
      return hashedBlockHandle(blockIdx, count);
    }

    // Miscellanea._assert(count >= HASHED_BLOCK_MIN_COUNT);

    // The value has actually been deleted. Shrinking the block if need be
    if (count > HASHED_BLOCK_MIN_COUNT)
      return hashedBlockHandle(blockIdx, count - 1);
    else
      return shrinkHashedBlock(blockIdx);
  }

  private long shrinkHashedBlock(int blockIdx) {
    // Miscellanea._assert(HASHED_BLOCK_MIN_COUNT == 7);

    // Here we've exactly 6 elements left, therefore we need the save the first 6 slots
    long slot0  = slot(blockIdx);
    long slot1  = slot(blockIdx + 1);
    long slot2  = slot(blockIdx + 2);
    long slot3  = slot(blockIdx + 3);
    long slot4  = slot(blockIdx + 4);
    long slot5  = slot(blockIdx + 5);

    int nextIdx = blockIdx;
    nextIdx = copyAndReleaseBlock(slot0, nextIdx, 0);
    nextIdx = copyAndReleaseBlock(slot1, nextIdx, 1);
    nextIdx = copyAndReleaseBlock(slot2, nextIdx, 2);
    nextIdx = copyAndReleaseBlock(slot3, nextIdx, 3);
    nextIdx = copyAndReleaseBlock(slot4, nextIdx, 4);
    nextIdx = copyAndReleaseBlock(slot5, nextIdx, 5);

    int endIdx = blockIdx + 6;
    for (int i=6 ; nextIdx < endIdx ; i++)
      nextIdx = copyAndReleaseBlock(slot(blockIdx + i), nextIdx, i);

    markSlotAsEmpty(blockIdx + 6);
    markSlotAsEmpty(blockIdx + 7);

    release16BlockUpperHalf(blockIdx);
    return size8BlockHandle(blockIdx, 6);
  }

  private int copyAndReleaseBlock(long handle, int nextIdx, int leastBits) {
    if (handle == EMPTY_SLOT)
      return nextIdx;

    int low = low(handle);
    int tag = tag(low);

    if (tag == INLINE_SLOT) {
      setFullSlot(nextIdx++, handle);
      return nextIdx;
    }

    // The block the handle is pointing to cannot have more than 6 elements,
    // so the block is a linear one, and at most an 8-block

    int blockIdx = payload(low);
    int count = count(handle);

    for (int i=0 ; i < count ; i++) {
      long slot = slot(blockIdx + i);
      // Miscellanea._assert(slot != EMPTY_SLOT & tag(low(slot)) == INLINE_SLOT);
      setSlot(nextIdx++, unclipped(low(slot), leastBits), high(slot));
    }

    if (tag == SIZE_2_BLOCK) {
      release2Block(blockIdx);
    }
    else if (tag == SIZE_4_BLOCK) {
      release4Block(blockIdx);
    }
    else {
      // Both 16-slot and hashed blocks contain at least 7 elements, so they cannot appear
      // here, as the parent hashed block being shrunk has only six elements left
      // Miscellanea._assert(tag == SIZE_8_BLOCK);
      release8Block(blockIdx);
    }

    return nextIdx;
  }
}
