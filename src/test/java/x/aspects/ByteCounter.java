package x.aspects;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import x.DualModeXmlChannel;

import java.util.HashMap;
import java.util.Map;

@Aspect
public class ByteCounter {

    public static final Map<String, DescriptiveStatistics> READ_STATISTICS = new HashMap<>();

    @AfterReturning(pointcut = "execution(byte[] x.DualModeXmlChannel.streamReceive()) && this(c)", returning = "b", argNames = "b,c")
    public void channelRead(byte[] b, DualModeXmlChannel c) {
        DescriptiveStatistics statistics = statisticsFor(c.getMode());
        statistics.addValue(b.length);
    }

    @AfterReturning(pointcut = "execution(int java.net.SocketInputStream.read())", returning = "len")
    public void socketRead(int len) {
        System.out.println("read " + len + " bytes");
    }

    public static void dump() {
        for (Map.Entry<String, DescriptiveStatistics> entry : READ_STATISTICS.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    private DescriptiveStatistics statisticsFor(String mode) {
        DescriptiveStatistics statistics = READ_STATISTICS.get(mode);
        if (statistics == null) {
            statistics = new DescriptiveStatistics();
            READ_STATISTICS.put(mode, statistics);
        }
        return statistics;
    }
}
