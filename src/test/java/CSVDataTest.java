import config.Config;
import data.CSVDataFormat;
import data.IO;
import data.Samples;
import fft.FFT;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CSVDataTest {

    Samples samples;
    Double[] data = null;
    StringBuilder sb = new StringBuilder();

    int sampleRate = 44100;
    int secs = 1;

    Config config = new Config(sampleRate);



    Double[] generateData(int num_samples, int sampleRate){
        if(this.data != null){
            return this.data;
        }
        Double[] data = new Double[num_samples];
        double period = 1/(double)sampleRate;
        double t = 0;
        for(int i=0; i < num_samples; i++){
            data[i] = new Double(0);
            /*
            for(int f=0; f < sampleRate / 16; f++){
                data[i] += Math.sin(2*Math.PI*f*t);
            }
            */
            int f = sampleRate;
            data[i] += Math.sin(2*Math.PI*f*t);
            t+=period;
        }
        this.data = data;
        return data;
    }

    private OutputStream NullOutputStream(){
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        };
    }

    private OutputStream StringOutputStream(){
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                sb.append((char) b);
            }
        };
    }

    private InputStream StringInputStream(final String buff){

        return new InputStream() {
            String buffer = buff;
            int pos = 0;
            @Override
            public int read() throws IOException {
                if(pos >= buffer.length()){
                    return -1;
                }
                int cr = (int)buffer.charAt(pos);
                pos++;
                return cr;
            }
        };
    }

    @Test
    public void csvTest() throws Exception {
        Double[] data = generateData(secs * sampleRate, sampleRate);
        IO io = new IO(config, StringInputStream(null), StringOutputStream(), new CSVDataFormat());
        Samples<Double> samples = new Samples<>();
        samples.addSamples(data);
        FFT fft = new FFT(config, samples);
        long start = System.currentTimeMillis();
        fft.fft();
        System.out.println("Cores " + fft.getThreadsNumber() + " : "+(System.currentTimeMillis() - start) / 1000 + " s");
        System.out.println("Cores " + fft.getThreadsNumber() + " : "+(System.currentTimeMillis() - start) / 1000 / secs + " s per sec of record");
        config.writeRAWDATA();
        io.write(samples);
        io = new IO(config, StringInputStream(sb.toString()), StringOutputStream(), new CSVDataFormat());
        Samples new_samples = io.read();
        Assert.assertEquals(samples.getMagnitudes(), new_samples.getMagnitudes());
        Assert.assertEquals(samples, new_samples);
    }
}
