import data.CSVDataFormat;
import data.IO;
import data.Samples;
import fft.FFT;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;

public class CSVDataTest {

    Samples samples = null;
    double[] data = null;

    int sampleRate = 44100;
    int secs = 5;

    double[] generateData(int num_samples, int sampleRate){
        if(this.data != null){
            return this.data;
        }
        double[] data = new double[num_samples];
        double period = 1/(double)sampleRate;
        double t = 0;
        for(int i=0; i < num_samples; i++){
            data[i] = 0;
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

    private void checkSamples(Samples samples){
        if (this.samples == null){
            this.samples = samples;
        }
        else {
            assert samples.getMagnitudes() == this.samples.getMagnitudes();
        }
    }



    private OutputStream NullOutputStream(){
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        };
    }

    @Test
    public void csvTest() throws Exception {
        double[] data = generateData(secs * sampleRate, sampleRate);
        IO io = new IO(System.in, NullOutputStream(), new CSVDataFormat());
        Samples samples = new Samples();
        samples.addSamples(data);
        FFT fft = new FFT(sampleRate, samples);
        long start = System.currentTimeMillis();
        fft.fft();
        System.out.println("Cores " + fft.getThreadsNumber() + " : "+(System.currentTimeMillis() - start) / 1000 + " s");
        System.out.println("Cores " + fft.getThreadsNumber() + " : "+(System.currentTimeMillis() - start) / 1000 / secs + " s per sec of record");
        io.write(samples);
        checkSamples(samples);
        assert samples.getMagnitudes().size() == secs;
    }
}
