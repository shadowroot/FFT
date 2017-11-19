package fft;

import config.Config;
import data.Complex;
import data.Samples;

import java.nio.DoubleBuffer;
import java.util.Date;

public class FFT {
    Config config = Config.getInstance();
    Samples samples;
    double[] sins;
    double[] coss;

    public FFT(Samples samples){
        this.samples = samples;
    }

    public void setSamples(Samples samples) {
        this.samples = samples;
    }

    void precalcs(int n){
        int valid_freqs = n/2;
        if(sins.length == valid_freqs * n){
            return;
        }
        sins = new double[valid_freqs * n];
        coss = new double[valid_freqs * n];
        for(int k=0; k < valid_freqs; k++) {
            for (int m = 0; m < valid_freqs; m++) {
                sins[m*k] = Math.sin(-2 * Math.PI * m * k);
                coss[m*k] = Math.cos(-2 * Math.PI * m * k);
            }
        }
    }

    public Samples getSamples() {
        return samples;
    }

    public void fft(){
        int n = config.getSampleRate();
        if(n < 0){
            System.out.println(new Date().toString() + ": Using FFT without sample rate.");
            n = samples.getSamples().capacity();
        }

        precalcs(n);

        for(int pos; (pos = samples.getNSamples(n)) >= 0;) {
            DoubleBuffer currentSamples = samples.getSamples();
            double[] magnitudes = new double[n / 2];
            for (int k = 0; k < n / 2; k++) {
                Complex sum1 = new Complex(0, 0);
                for (int m = 0; m < n / 2; m++) {
                    sum1.add(new Complex(coss[m * k], sins[m * k]).mul(currentSamples.get(pos + 2 * m)));
                }
                Complex sum2 = new Complex(0, 0);
                for (int m = 0; m < n / 2; m++) {
                    sum2.add(new Complex(coss[m * k], sins[m * k]).mul(currentSamples.get(pos + 2 * m + 1)));
                }
                sum2.mul(new Complex(coss[k], sins[k]));
                sum1.add(sum2);
                magnitudes[k] = sum1.mag_in_db();
            }
            samples.addMagnitudes(pos, magnitudes);
        }
    }

}
