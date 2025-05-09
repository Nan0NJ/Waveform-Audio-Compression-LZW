# Waveform-Audio-Compression-LZW

## Description
This project analyzes the performance of Lempel-Ziv-Welch (LZW) compression on WAV audio files compared to MP3 compression. Conducted by Nenad Jakovchevski, Ekaterina Bochvaroska, and Kristina Piiarska, it tests four audio types: pure sine wave, cricket chirps, repeated speech ("Laurel" or "Yanny"), and a complex song ("AREA - Site moi sliki").

## Hypothesis
LZW compression will produce smaller files than MP3 for all audio types by exploiting exact repetitions losslessly.

## Results
- **Sine Wave**: LZW increased size (6,142,172 bytes vs. 5,300,302 WAV); MP3 excelled (722,997 bytes). Entropy: 1 (high).
- **Cricket Noise**: LZW slightly reduced size (1,545,636 bytes vs. 1,769,550 WAV); MP3 better (242,135 bytes). Entropy: 0.9957.
- **Repeating Word**: LZW reduced size (4,938,666 bytes vs. 5,299,466 WAV); MP3 smaller (481,626 bytes). Entropy: 0.9993.
- **Full Song**: LZW increased size (51,194,742 bytes vs. 40,665,166 WAV); MP3 much smaller (5,534,125 bytes). Entropy: 1.
- **Conclusion**: Hypothesis disproved. MP3’s perceptual coding outperformed LZW, which struggled with high-entropy and complex data.

## Files
- `LZWCompression.java`: Main Java code for LZW compression, including data preparation, dictionary initialization, and compression ratio calculation.
- `compression_results.java.csv`: Output CSV with WAV, LZW, MP3 sizes, entropy, and compression ratios.
- `Group_O.pdf`: Detailed project report.

## Setup
1. Clone the repo:
   ```bash
   git clone <repo-url>
2. Ensure Java is installed (JDK 8+ recommended).
3. Place WAV and MP3 audio samples files that are in the folder in the correct directory path.

## Usage
1. Compile and Run Java Code:
   ```bash
   javac LZWCompression.java
   java LZWCompression
2. The program processes WAV files, applies LZW compression, calculates entropy, and logs results to compression_results.java.csv.

## Notes
- The code converts WAV files to bit strings for LZW compression.
- Entropy is calculated to assess data randomness.
- MP3 consistently outperforms LZW due to its lossy, perception-based compression.
