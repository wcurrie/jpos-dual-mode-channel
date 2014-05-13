package x;

import org.jpos.iso.*;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.ThreadPool;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.apache.commons.lang.StringUtils.repeat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static x.TestMessages.ping;

public class StressTest {

    private static final int PORT = 8976;
    public static final int MESSAGES_PER_CLIENT = 10;
    private static Logger logger;

    private ISOServer server;
    private static XMLPackager packager;
    private ExecutorService clientExecutor;

    @BeforeClass
    public static void createPackager() throws ISOException {
        packager = new XMLPackager();
        logger = new Logger();
        logger.setName("logger");
//        logger.addListener(new SimpleLogListener());
    }

    @Before
    public void setUp() throws Exception {
        DualModeXmlChannel clientSide = new DualModeXmlChannel(packager);
        clientSide.setLogger(logger, "server.channel");
        server = new ISOServer(PORT, clientSide, new ThreadPool(100, 100));
        server.setLogger(logger, "server");
        server.addISORequestListener(new PingHandler());
        Executors.newSingleThreadExecutor().submit(server);
        clientExecutor = Executors.newCachedThreadPool();
    }

    @Test
    public void chaos() throws Exception {
        Stream<BaseChannel> channels = new Random().ints(0, 2).limit(100).mapToObj(Mode::channelFor);
        Stream<Future<Integer>> futures = channels.map(c -> clientExecutor.submit(new Client(c)));
        futures.forEach(f -> {
            try {
                Integer integer = f.get(2, TimeUnit.SECONDS);
                assertThat(integer, is(MESSAGES_PER_CLIENT));
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        });
    }

    private class Client implements Callable<Integer> {

        private final BaseChannel channel;

        public Client(BaseChannel channel) {
            this.channel = channel;
        }

        @Override
        public Integer call() throws Exception {
            channel.connect();
            int successes = 0;
            for (int i = 0; i < MESSAGES_PER_CLIENT; i++) {
                String payload = repeat("a", 1024);
                channel.send(ping(payload));
                ISOMsg response = channel.receive();
                if (response.getString("48.1").equals(payload)) {
                    successes++;
                }
            }
            return successes;
        }
    }

    private class PingHandler implements ISORequestListener {

        @Override
        public boolean process(ISOSource source, ISOMsg m) {
            try {
                ISOMsg response = (ISOMsg) m.clone();
                response.setResponseMTI();
                response.set("48.2", String.valueOf(System.currentTimeMillis()));
                source.send(response);
            } catch (ISOException | IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
    }

    private enum Mode {
        Raw {
            @Override
            BaseChannel newChannel() throws IOException {
                return new XMLChannel(packager);
            }
        },
        Gzip {
            @Override
            BaseChannel newChannel() throws IOException {
                return new VersionedGzipChannel(packager);
            }
        };

        abstract BaseChannel newChannel() throws IOException;

        static BaseChannel channelFor(int i) {
            try {
                Mode mode = fromIndex(i);
                BaseChannel channel = mode.newChannel();
                channel.setPort(PORT);
                channel.setLogger(logger, mode + "-client");
                return channel;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static Mode fromIndex(int i) {
            return Mode.values()[i];
        }
    }
}
