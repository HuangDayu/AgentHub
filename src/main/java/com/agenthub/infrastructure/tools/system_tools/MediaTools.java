package com.agenthub.infrastructure.tools.system_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;

@AgentTools(name = "MediaTools", description = "多媒体处理工具，提供图像处理、音频处理、视频处理、文本转语音(TTS)等功能")
public class MediaTools {

    // ==================== 图像处理 ====================

    @Tool(name = "image_info", description = "Get image information")
    public String imageInfo(String path) throws Exception {
        BufferedImage img = ImageIO.read(new File(path));
        return String.format("Width: %d, Height: %d, Type: %d", 
            img.getWidth(), img.getHeight(), img.getType());
    }

    @Tool(name = "image_resize", description = "Resize image")
    public String imageResize(String input, String output, int w, int h) throws Exception {
        BufferedImage src = ImageIO.read(new File(input));
        BufferedImage dest = new BufferedImage(w, h, src.getType());
        Graphics2D g = dest.createGraphics();
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        ImageIO.write(dest, getFormat(output), new File(output));
        return "Resized to " + w + "x" + h;
    }

    @Tool(name = "image_crop", description = "Crop image")
    public String imageCrop(String input, String output, int x, int y, int w, int h) throws Exception {
        BufferedImage src = ImageIO.read(new File(input));
        BufferedImage dest = src.getSubimage(x, y, w, h);
        ImageIO.write(dest, getFormat(output), new File(output));
        return "Cropped: " + w + "x" + h;
    }

    @Tool(name = "image_convert", description = "Convert image format")
    public String imageConvert(String input, String output, String format) throws Exception {
        BufferedImage img = ImageIO.read(new File(input));
        ImageIO.write(img, format, new File(output));
        return "Converted to: " + format;
    }

    @Tool(name = "image_rotate", description = "Rotate image")
    public String imageRotate(String input, String output, double degrees) throws Exception {
        BufferedImage src = ImageIO.read(new File(input));
        double rads = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = (int)(src.getWidth() * cos + src.getHeight() * sin);
        int h = (int)(src.getWidth() * sin + src.getHeight() * cos);
        BufferedImage dest = new BufferedImage(w, h, src.getType());
        Graphics2D g = dest.createGraphics();
        g.translate((w - src.getWidth()) / 2, (h - src.getHeight()) / 2);
        g.rotate(rads, src.getWidth() / 2.0, src.getHeight() / 2.0);
        g.drawRenderedImage(src, null);
        g.dispose();
        ImageIO.write(dest, getFormat(output), new File(output));
        return "Rotated " + degrees + " degrees";
    }

    @Tool(name = "image_flip_horizontal", description = "Flip image horizontally")
    public String imageFlipHorizontal(String input, String output) throws Exception {
        BufferedImage src = ImageIO.read(new File(input));
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Graphics2D g = dest.createGraphics();
        g.drawImage(src, src.getWidth(), 0, -src.getWidth(), src.getHeight(), null);
        g.dispose();
        ImageIO.write(dest, getFormat(output), new File(output));
        return "Flipped horizontally";
    }

    @Tool(name = "image_flip_vertical", description = "Flip image vertically")
    public String imageFlipVertical(String input, String output) throws Exception {
        BufferedImage src = ImageIO.read(new File(input));
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Graphics2D g = dest.createGraphics();
        g.drawImage(src, 0, src.getHeight(), src.getWidth(), -src.getHeight(), null);
        g.dispose();
        ImageIO.write(dest, getFormat(output), new File(output));
        return "Flipped vertically";
    }

    @Tool(name = "image_grayscale", description = "Convert to grayscale")
    public String imageGrayscale(String input, String output) throws Exception {
        BufferedImage src = ImageIO.read(new File(input));
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = dest.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        ImageIO.write(dest, getFormat(output), new File(output));
        return "Converted to grayscale";
    }

    @Tool(name = "image_thumbnail", description = "Create thumbnail")
    public String imageThumbnail(String input, String output, int size) throws Exception {
        BufferedImage src = ImageIO.read(new File(input));
        int w = src.getWidth(), h = src.getHeight();
        int max = Math.max(w, h);
        int nw = w * size / max, nh = h * size / max;
        return imageResize(input, output, nw, nh);
    }

    @Tool(name = "image_combine", description = "Combine images horizontally")
    public String imageCombine(String input1, String input2, String output) throws Exception {
        BufferedImage img1 = ImageIO.read(new File(input1));
        BufferedImage img2 = ImageIO.read(new File(input2));
        int h = Math.max(img1.getHeight(), img2.getHeight());
        BufferedImage dest = new BufferedImage(img1.getWidth() + img2.getWidth(), h, img1.getType());
        Graphics g = dest.getGraphics();
        g.drawImage(img1, 0, 0, null);
        g.drawImage(img2, img1.getWidth(), 0, null);
        g.dispose();
        ImageIO.write(dest, getFormat(output), new File(output));
        return "Images combined";
    }

    // ==================== 音频处理 ====================

    @Tool(name = "audio_info", description = "Get audio file info")
    public String audioInfo(String path) throws Exception {
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path));
        AudioFormat format = stream.getFormat();
        return String.format("Channels: %d, SampleRate: %.1f, Bits: %d, Encoding: %s",
            format.getChannels(), format.getSampleRate(), 
            format.getSampleSizeInBits(), format.getEncoding());
    }

    @Tool(name = "audio_duration", description = "Get audio duration in seconds")
    public double audioDuration(String path) throws Exception {
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path));
        AudioFormat format = stream.getFormat();
        long frames = stream.getFrameLength();
        return frames / format.getFrameRate();
    }

    @Tool(name = "audio_convert", description = "Convert audio format")
    public String audioConvert(String input, String output, String format) throws Exception {
        AudioInputStream source = AudioSystem.getAudioInputStream(new File(input));
        AudioFormat targetFormat = getAudioFormat(format, source.getFormat());
        AudioInputStream converted = AudioSystem.getAudioInputStream(targetFormat, source);
        AudioSystem.write(converted, AudioFileFormat.Type.WAVE, new File(output));
        return "Converted to: " + format;
    }

    @Tool(name = "audio_volume", description = "Adjust audio volume")
    public String audioVolume(String input, String output, double factor) throws Exception {
        AudioInputStream source = AudioSystem.getAudioInputStream(new File(input));
        byte[] bytes = source.readAllBytes();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte)(bytes[i] * factor);
        }
        Files.write(Paths.get(output), bytes);
        return "Volume adjusted by factor: " + factor;
    }

    @Tool(name = "audio_trim", description = "Trim audio")
    public String audioTrim(String input, String output, double startSec, double endSec) throws Exception {
        AudioInputStream source = AudioSystem.getAudioInputStream(new File(input));
        AudioFormat format = source.getFormat();
        long startFrame = (long)(startSec * format.getFrameRate());
        long frames = (long)((endSec - startSec) * format.getFrameRate());
        source.skip(startFrame * format.getFrameSize());
        byte[] buffer = new byte[(int)(frames * format.getFrameSize())];
        source.read(buffer);
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        AudioInputStream trimmed = new AudioInputStream(bais, format, frames);
        AudioSystem.write(trimmed, AudioFileFormat.Type.WAVE, new File(output));
        return "Trimmed: " + startSec + "s to " + endSec + "s";
    }

    @Tool(name = "audio_play", description = "Play audio file")
    public String audioPlay(String path) throws Exception {
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path));
        AudioFormat format = stream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(stream);
        clip.start();
        return "Playing: " + path;
    }

    // ==================== 视频处理（使用FFmpeg）====================

    @Tool(name = "video_info", description = "Get video info using ffprobe")
    public String videoInfo(String path) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ffprobe", "-v", "quiet", 
            "-print_format", "json", "-show_format", "-show_streams", path);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        return new String(process.getInputStream().readAllBytes());
    }

    @Tool(name = "video_thumbnail", description = "Extract video thumbnail")
    public String videoThumbnail(String video, String output, int sec) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", video, 
            "-ss", String.valueOf(sec), "-vframes", "1", "-q:v", "2", output);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
        return "Thumbnail saved: " + output;
    }

    @Tool(name = "video_convert", description = "Convert video format")
    public String videoConvert(String input, String output, String format) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", input, "-y", output);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
        return "Converted to: " + output;
    }

    @Tool(name = "video_trim", description = "Trim video")
    public String videoTrim(String input, String output, double start, double end) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", input, 
            "-ss", String.valueOf(start), "-to", String.valueOf(end), "-c", "copy", output);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
        return "Trimmed: " + start + "s to " + end + "s";
    }

    @Tool(name = "video_extract_audio", description = "Extract audio from video")
    public String videoExtractAudio(String video, String audio) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", video, "-vn", "-acodec", "copy", audio);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
        return "Audio extracted: " + audio;
    }

    @Tool(name = "video_add_audio", description = "Add audio to video")
    public String videoAddAudio(String video, String audio, String output) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", video, "-i", audio, 
            "-c:v", "copy", "-c:a", "aac", "-map", "0:v:0", "-map", "1:a:0", output);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
        return "Audio added: " + output;
    }

    @Tool(name = "video_resize", description = "Resize video")
    public String videoResize(String input, String output, int w, int h) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", input, 
            "-vf", "scale=" + w + ":" + h, output);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
        return "Resized to " + w + "x" + h;
    }

    // ==================== TTS（使用系统命令）====================

    @Tool(name = "tts_say", description = "Text to speech using system say command")
    public String ttsSay(String text) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            ProcessBuilder pb = new ProcessBuilder("say", text);
            pb.start().waitFor();
            return "Spoken: " + text;
        } else if (os.contains("win")) {
            ProcessBuilder pb = new ProcessBuilder("powershell", 
                "-Command", "Add-Type -AssemblyName System.Speech; " +
                "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                "$speak.Speak('" + text.replace("'", "''") + "')");
            pb.start().waitFor();
            return "Spoken: " + text;
        }
        return "TTS not supported on this OS";
    }

    @Tool(name = "tts_to_file", description = "TTS and save to audio file")
    public String ttsToFile(String text, String output) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            ProcessBuilder pb = new ProcessBuilder("say", "-o", output, text);
            pb.start().waitFor();
            return "Saved to: " + output;
        }
        return "TTS to file not supported on this OS";
    }

    // ==================== 辅助方法 ====================

    private String getFormat(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot + 1).toLowerCase() : "png";
    }

    private AudioFormat getAudioFormat(String format, AudioFormat source) {
        return switch (format.toLowerCase()) {
            case "mp3" -> new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
                44100, 16, 2, 4, 44100, false);
            case "wav" -> new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                source.getSampleRate(), 16, source.getChannels(), 
                source.getChannels() * 2, source.getSampleRate(), false);
            default -> source;
        };
    }
}
