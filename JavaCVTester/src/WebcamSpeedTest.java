/**
 * @author Ben Davenport
 * 
 * This class is a simple example for broadcasting a video capture device (ie, webcam) and an audio capture device (ie, microphone)
 * using an FFmpegFrameRecorder. 
 * 
 * FFmpegFrameRecorder allows the output destination to be either a FILE or an RTMP endpoint (Wowza, FMS, et al)
 * 
 * IMPORTANT: There are potential timing issues with audio/video synchronicity across threads, I am working on finding a solution, but
 * chime in if you can fig it out :o)
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class WebcamSpeedTest
{
    final private static int WEBCAM_DEVICE_INDEX = 1;
    final private static int AUDIO_DEVICE_INDEX = 4;

    final private static int FRAME_RATE = 60;
    final private static int GOP_LENGTH_IN_FRAMES = 60;

    private static long startTime = 0;
    private static long videoTS = 0;

    public static void main(String[] args) throws Exception, org.bytedeco.javacv.FrameGrabber.Exception
    {
    	
    	
        int captureWidth = 1280;
        int captureHeight = 720;

        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.setImageWidth(captureWidth);
        grabber.setImageHeight(captureHeight);
        grabber.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                "C:\\Recordings\\NewVideo.flv",
                captureWidth, captureHeight, 2);
        recorder.setInterleaved(true);

        recorder.setVideoOption("tune", "zerolatency");

        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("crf", "28");
        recorder.setVideoBitrate(2000000);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(FRAME_RATE);

        recorder.setGopSize(GOP_LENGTH_IN_FRAMES);

        recorder.setAudioOption("crf", "0");

        recorder.setAudioQuality(0);

        recorder.setAudioBitrate(192000);
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(2);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);


        recorder.start();


        new Thread(new Runnable() {
            @Override
            public void run()
            {

                AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);

                Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
                Mixer mixer = AudioSystem.getMixer(minfoSet[AUDIO_DEVICE_INDEX]);
                DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

                try
                {
                    TargetDataLine line = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
                    line.open(audioFormat);
                    line.start();

                    int sampleRate = (int) audioFormat.getSampleRate();
                    int numChannels = audioFormat.getChannels();


                    int audioBufferSize = sampleRate * numChannels;
                    byte[] audioBytes = new byte[audioBufferSize];

                    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                    exec.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run()
                        {
                            try
                            {
                                int nBytesRead = 0;
                                while (nBytesRead == 0) {
                                    nBytesRead = line.read(audioBytes, 0, line.available());
                                }

                                int nSamplesRead = nBytesRead / 2;
                                short[] samples = new short[nSamplesRead];

                                ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                                ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);

                                recorder.recordSamples(sampleRate, numChannels, sBuff);
                            } 
                            catch (org.bytedeco.javacv.FrameRecorder.Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }, 0, (long) 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);
                } 
                catch (LineUnavailableException e1)
                {
                    e1.printStackTrace();
                }
            }
        }).start();


        CanvasFrame cFrame = new CanvasFrame("Capture Preview", CanvasFrame.getDefaultGamma() / grabber.getGamma());

        Frame capturedFrame = null;

        while ((capturedFrame = grabber.grab()) != null)
        {
            if (cFrame.isVisible())
            {
                cFrame.showImage(capturedFrame);
            }


            if (startTime == 0)
                startTime = System.currentTimeMillis();


            videoTS = 1000 * (System.currentTimeMillis() - startTime);


            if (videoTS > recorder.getTimestamp())
            {
                

                recorder.setTimestamp(videoTS);
            }

            recorder.record(capturedFrame);
        }

        cFrame.dispose();
        recorder.stop();
        grabber.stop();
    }
}
