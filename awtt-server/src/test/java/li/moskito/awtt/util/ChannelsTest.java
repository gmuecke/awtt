package li.moskito.awtt.util;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class ChannelsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testStream_dataFitsInBuffer() throws Exception {
        final Path srcFile = Files.createTempFile("src", "bin");
        final Path dstFile = Files.createTempFile("dst", "bin");
        // buffersize of channels is 16KB, creating data with 8KB will always fit into buffer
        this.generateRandomDataChunks(srcFile, 8);

        final FileChannel src = FileChannel.open(srcFile, StandardOpenOption.READ);
        final FileChannel dst = FileChannel.open(dstFile, StandardOpenOption.WRITE);

        Channels.stream(src, dst);
        src.close();
        dst.close();

        final byte[] srcData = Files.readAllBytes(srcFile);
        final byte[] dstData = Files.readAllBytes(dstFile);
        assertArrayEquals(srcData, dstData);
    }

    @Test
    public void testStream_dataLargerThanBuffer() throws Exception {
        final Path srcFile = Files.createTempFile("src", "bin");
        final Path dstFile = Files.createTempFile("dst", "bin");
        // buffersize of channels is 16KB, creating data with 24KB will require multiple cycles
        this.generateRandomDataChunks(srcFile, 24);

        final FileChannel src = FileChannel.open(srcFile, StandardOpenOption.READ);
        final FileChannel dst = FileChannel.open(dstFile, StandardOpenOption.WRITE);

        Channels.stream(src, dst);
        src.close();
        dst.close();

        final byte[] srcData = Files.readAllBytes(srcFile);
        final byte[] dstData = Files.readAllBytes(dstFile);
        assertArrayEquals(srcData, dstData);
    }

    private void generateRandomDataChunks(final Path file, final int numChunks) throws IOException {
        final Random rand = new Random();
        final byte[] chunk = new byte[1024];

        for (int i = 0; i < numChunks; i++) {
            rand.nextBytes(chunk);
            Files.write(file, chunk, StandardOpenOption.APPEND);
        }
    }

}
