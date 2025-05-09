import java.io.*;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LZWCompression {
    private static final int BITS = 12;
    private static final int HASHING_SHIFT = 4;
    private static final int MAX_VALUE = (1 << BITS) - 1;
    private static final int MAX_CODE = MAX_VALUE - 1;
    private static final int TABLE_SIZE = 5021;
    private static final int EOF = -1;

    private final BufferedInputStream input;
    private final BufferedOutputStream output;

    private int outputBitCount = 0;
    private int outputBitBuffer = 0;

    private final short[] codeValue = new short[TABLE_SIZE];
    private final short[] prefixCode = new short[TABLE_SIZE];
    private final short[] appendCharacter = new short[TABLE_SIZE];

    String[] samples = {"SINEWAVE", "CRICKETSOUND", "REPEATINGWORD", "AREA"};

    public LZWCompression(InputStream input, OutputStream output) {
        this.input = new BufferedInputStream(input);
        this.output = new BufferedOutputStream(output);
    }

    public void compress() throws IOException {
        int nextCode = 128;

        for (int i = 0; i < 128; i++) {
            codeValue[i] = (short) i;
            prefixCode[i] = -1;
            appendCharacter[i] = (short) i;
        }

        for (int i = 128; i < TABLE_SIZE; i++) {
            codeValue[i] = -1;
        }

        int stringCode = readByte();
        int character;

        while ((character = readByte()) != EOF) {
            int index = findMatch(stringCode, character);
            if (codeValue[index] != -1) {
                stringCode = codeValue[index];
            } else {
                outputCode(stringCode);
                if (nextCode <= MAX_CODE) {
                    codeValue[index] = (short) nextCode++;
                    prefixCode[index] = (short) stringCode;
                    appendCharacter[index] = (short) character;
                }
                stringCode = character;
            }
        }

        outputCode(stringCode);
        outputCode(MAX_VALUE);
        if (outputBitCount > 0) {
            output.write(outputBitBuffer << (8 - outputBitCount));
        }
        output.flush();
    }

    private int readByte() throws IOException {
        return input.read();
    }

    private int findMatch(int prefixCodeVal, int character) {
        int index = (character << HASHING_SHIFT) ^ prefixCodeVal;
        int offset = (index == 0) ? 1 : TABLE_SIZE - index;

        while (codeValue[index] != -1) {
            if (prefixCodeVal == prefixCode[index] && character == appendCharacter[index]) {
                return index;
            }
            index -= offset;
            if (index < 0) index += TABLE_SIZE;
        }
        return index;
    }

    private void outputCode(int code) throws IOException {
        outputBitBuffer = (outputBitBuffer << BITS) | code;
        outputBitCount += BITS;

        while (outputBitCount >= 8) {
            outputBitCount -= 8;
            output.write(outputBitBuffer >> outputBitCount);
        }
    }

    private static double calculateEntropy(String bits) {
        int[] freq = new int[2];
        for (char c : bits.toCharArray()) {
            freq[c == '1' ? 1 : 0]++;
        }
        double entropy = 0.0, total = bits.length();
        for (int count : freq) {
            if (count == 0) continue;
            double p = count / total;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    private static void logResults(String wavPath,
                                   long wavSize,
                                   long compSize,
                                   long mp3Size,
                                   double entropy) throws IOException {
        Path csv = Paths.get(System.getProperty("user.dir"), "compression_results_java.csv");
        boolean exists = Files.exists(csv);
        try (BufferedWriter writer = Files.newBufferedWriter(
                csv, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (!exists) {
                writer.write("WAV_File,WAV_Size,Compressed_Size,MP3_Size,Entropy,Compression_Ratio\n");
            }
            double ratio = (double) wavSize / compSize;
            writer.write(String.format("%s,%d,%d,%d,%.4f,%.4f\n",
                    wavPath, wavSize, compSize, mp3Size, entropy, ratio));
        }
    }

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(LZWCompression.class.getName());
        ///             STATE THE PATH LOCATION of THE FOLDER samples
        Path samplesDir = Paths.get("C:\\Users\\devne\\Documents\\lebissol\\Waveform-Audio-Compression-LZW\\samples");

        try {
            if (!Files.exists(samplesDir) || !Files.isDirectory(samplesDir)) {
                logger.log(Level.SEVERE, "Samples directory not found: " + samplesDir);
                return;
            }

            for (String sample : new LZWCompression(null, null).samples) {
                Path wavPath = samplesDir.resolve("WAV_" + sample + ".wav");
                Path mp3Path = samplesDir.resolve("MP3_" + sample + ".mp3");
                Path bitPath = Paths.get(System.getProperty("user.dir"), sample + "_bitInput.txt");
                Path compressedPath = Paths.get(System.getProperty("user.dir"), sample + "_output.lzw");

                try {
                    byte[] inputBytes = Files.readAllBytes(wavPath);
                    StringBuilder bitStringBuilder = new StringBuilder(inputBytes.length * 8);

                    for (byte b : inputBytes) {
                        bitStringBuilder.append(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(b))).replace(' ', '0'));
                    }

                    String bitString = bitStringBuilder.toString();

                    double entropy = calculateEntropy(bitString);
                    System.out.printf("Entropy (bits/bit) for WAV_%s.wav: %.4f%n", sample, entropy);

                    Files.writeString(bitPath, bitString);

                    try (InputStream bitInput = Files.newInputStream(bitPath);
                         OutputStream compressedOutput = Files.newOutputStream(compressedPath)) {
                        new LZWCompression(bitInput, compressedOutput).compress();
                    }

                    long wavSize = Files.size(wavPath);
                    long compressedSize = Files.size(compressedPath);
                    long mp3Size = Files.exists(mp3Path) ? Files.size(mp3Path) : -1;

                    System.out.printf("WAV file size for WAV_%s.wav: %,d bytes%n", sample, wavSize);
                    System.out.printf("Compressed file size: %,d bytes%n", compressedSize);
                    System.out.printf("MP3 file size for MP3_%s.mp3: %s%n", sample, mp3Size == -1 ? "MP3 file not found" : String.format("%,d bytes", mp3Size));
                    logResults(wavPath.toString(), wavSize, compressedSize, mp3Size, entropy);

                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Compression failed for WAV_" + sample + ".wav", e);
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error accessing samples directory", e);
        }
    }
}