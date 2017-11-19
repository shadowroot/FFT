import data.CSVDataFormat;
import data.IO;
import data.Samples;
import fft.FFT;
import org.junit.jupiter.api.Test;

public class CSVDataTest {

    double[] generateData(int num_samples, int sampleRate){
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
        return data;
    }

    @Test
    public void csvTest() throws Exception {
        int sampleRate = 8000;
        int secs = 20;
        double[] data = generateData(secs * sampleRate, sampleRate);
        long start = System.currentTimeMillis();
        IO io = new IO(System.in, System.out, new CSVDataFormat());
        Samples samples = new Samples();
        samples.addSamples(data);
        FFT fft = new FFT(sampleRate, samples, 4);
        fft.fft();
        io.write(samples);
        System.out.println((System.currentTimeMillis() - start) / 1000 + " s");
        assert samples.getMagnitudes().size() == secs;
    }
}
