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
            for(int f=0; f < sampleRate / 16; f++){
                data[i] += Math.sin(2*Math.PI*f*t);
            }
            t+=period;
        }
        return data;
    }

    @Test
    public void csvTest() throws Exception {
        int sampleRate = 44100;
        double[] data = generateData(sampleRate, sampleRate);
        IO io = new IO(System.in, System.out, new CSVDataFormat());
        Samples samples = new Samples();
        samples.addSamples(data);
        FFT fft = new FFT(sampleRate, samples);
        fft.fft();
        io.write(samples);
        assert samples.getMagnitudes().size() == 1;
    }
}
