package fun.feellmoose.computer;

public class Usage {
    private final long[] algoTimes;
    private final long[] totalTimes;

    private Usage(int types){
        algoTimes = new long[types];
        totalTimes = new long[types];
    }

    public static Usage init(int types){
        return new Usage(types);
    }

    public void add(int num, Runnable action){
        if (num >= 0 && algoTimes.length > num) {
            algoTimes[num] ++;
            long start = System.currentTimeMillis();
            action.run();
            long end = System.currentTimeMillis();
            totalTimes[num] += end - start;
        }
    }

    public void printSummary(){
        for (int i = 0; i < algoTimes.length; i++) {
            System.out.printf("Type num[%2d] use times = %10d, Total time = %10d ms\n",i , algoTimes[i], totalTimes[i]);
        }
        System.out.println("----------");
    }

}
