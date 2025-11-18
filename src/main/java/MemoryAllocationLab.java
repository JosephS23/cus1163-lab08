import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class MemoryAllocationLab {

    static class MemoryBlock {
        int start;
        int size;
        String processName;  // null if free

        public MemoryBlock(int start, int size, String processName) {
            this.start = start;
            this.size = size;
            this.processName = processName;
        }

        public boolean isFree() {
            return processName == null;
        }

        public int getEnd() {
            return start + size - 1;
        }
    }

    static int totalMemory;
    static ArrayList<MemoryBlock> memory;
    static int successfulAllocations = 0;
    static int failedAllocations = 0;

    /**
     * TODO 1, 2: Process memory requests from file
     * <p>
     * This method reads the input file and processes each REQUEST and RELEASE.
     * <p>
     * TODO 1: Read and parse the file
     *   - Open the file using BufferedReader
     *   - Read the first line to get total memory size
     *   - Initialize the memory list with one large free block
     *   - Read each subsequent line and parse it
     *   - Call appropriate method based on REQUEST or RELEASE
     * <p>
     * TODO 2: Implement allocation and deallocation
     *   - For REQUEST: implement First-Fit algorithm
     *     * Search memory list for first free block >= requested size
     *     * If found: split the block if necessary and mark as allocated
     *     * If not found: increment failedAllocations
     *   - For RELEASE: find the process's block and mark it as free
     *   - Optionally: merge adjacent free blocks (bonus)
     */
    public static void processRequests(String filename) {
        memory = new ArrayList<>();

        // TODO 1: Read file and initialize memory
        // Try-catch block to handle file reading
        // Read first line for total memory size
        // Create initial free block: new MemoryBlock(0, totalMemory, null)
        // Read remaining lines in a loop
        // Parse each line and call allocate() or deallocate()
        File file = new File(filename);
        try{
        Scanner scan = new Scanner(file);
        totalMemory = Integer.parseInt(scan.nextLine());
        memory.add(new MemoryBlock(0, totalMemory, null));

        while (scan.hasNextLine()) {
              String line = scan.nextLine().trim();
             if (line.isEmpty()) continue;

              String[] parts = line.split(" ");

           if (parts[0].equals("REQUEST")) {
               allocate(parts[1], Integer.parseInt(parts[2]));
               }

           if (parts[0].equals("RELEASE")) {
               deallocate(parts[1]);
                }      
}


scan.close();

        }
        catch(FileNotFoundException e){
            return;
        }

        // TODO 2: Implement these helper methods
        
    }

    /**
     * TODO 2A: Allocate memory using First-Fit
     */
private static void allocate(String processName, int size) {
    for (int i = 0; i < memory.size(); i++) {
        MemoryBlock block = memory.get(i);

        if (block.isFree() && block.size >= size) {

            if (block.size == size) {
                block.processName = processName;
            } else {
                MemoryBlock allocated = new MemoryBlock(block.start, size, processName);
                MemoryBlock remaining = new MemoryBlock(
                        block.start + size,
                        block.size - size,
                        null
                );

                memory.set(i, allocated);
                memory.add(i + 1, remaining);
            }

            successfulAllocations++;
            System.out.println("ALLOCATED " + processName);
            return;
        }
    }

    failedAllocations++;
    System.out.println("FAILED " + processName);
}


private static void deallocate(String processName) {
    for (int i = 0; i < memory.size(); i++) {
        MemoryBlock block = memory.get(i);

        if (processName.equals(block.processName)) {
            block.processName = null;

            if (i + 1 < memory.size() && memory.get(i + 1).isFree()) {
                MemoryBlock next = memory.remove(i + 1);
                block.size += next.size;
            }

            if (i > 0 && memory.get(i - 1).isFree()) {
                MemoryBlock prev = memory.get(i - 1);
                prev.size += block.size;
                memory.remove(i);
            }

            System.out.println("RELEASED " + processName);
            return;
        }
    }
}



    public static void displayStatistics() {
        System.out.println("\n========================================");
        System.out.println("Final Memory State");
        System.out.println("========================================");

        int blockNum = 1;
        for (MemoryBlock block : memory) {
            String status = block.isFree() ? "FREE" : block.processName;
            String allocated = block.isFree() ? "" : " - ALLOCATED";
            System.out.printf("Block %d: [%d-%d]%s%s (%d KB)%s\n",
                    blockNum++,
                    block.start,
                    block.getEnd(),
                    " ".repeat(Math.max(1, 10 - String.valueOf(block.getEnd()).length())),
                    status,
                    block.size,
                    allocated);
        }

        System.out.println("\n========================================");
        System.out.println("Memory Statistics");
        System.out.println("========================================");

        int allocatedMem = 0;
        int freeMem = 0;
        int numProcesses = 0;
        int numFreeBlocks = 0;
        int largestFree = 0;

        for (MemoryBlock block : memory) {
            if (block.isFree()) {
                freeMem += block.size;
                numFreeBlocks++;
                largestFree = Math.max(largestFree, block.size);
            } else {
                allocatedMem += block.size;
                numProcesses++;
            }
        }

        double allocatedPercent = (allocatedMem * 100.0) / totalMemory;
        double freePercent = (freeMem * 100.0) / totalMemory;
        double fragmentation = freeMem > 0 ?
                ((freeMem - largestFree) * 100.0) / freeMem : 0;

        System.out.printf("Total Memory:           %d KB\n", totalMemory);
        System.out.printf("Allocated Memory:       %d KB (%.2f%%)\n", allocatedMem, allocatedPercent);
        System.out.printf("Free Memory:            %d KB (%.2f%%)\n", freeMem, freePercent);
        System.out.printf("Number of Processes:    %d\n", numProcesses);
        System.out.printf("Number of Free Blocks:  %d\n", numFreeBlocks);
        System.out.printf("Largest Free Block:     %d KB\n", largestFree);
        System.out.printf("External Fragmentation: %.2f%%\n", fragmentation);

        System.out.println("\nSuccessful Allocations: " + successfulAllocations);
        System.out.println("Failed Allocations:     " + failedAllocations);
        System.out.println("========================================");
    }

    /**
     * Main method (FULLY PROVIDED)
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MemoryAllocationLab <input_file>");
            System.out.println("Example: java MemoryAllocationLab memory_requests.txt");
            return;
        }

        System.out.println("========================================");
        System.out.println("Memory Allocation Simulator (First-Fit)");
        System.out.println("========================================\n");
        System.out.println("Reading from: " + args[0]);

        processRequests(args[0]);
        displayStatistics();
    }
}