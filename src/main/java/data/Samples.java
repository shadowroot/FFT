package data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Samples class
 */
public class Samples<T> {

    private List<T> samples = null;
    private List<Double[]> magnitudes = new ArrayList<>();
    private int processedSamples = 0;
    private Logger logger;

    public Samples(){
        logger = Logger.getLogger("FFT-Samples");
        samples = new ArrayList<>();
    }

    public void addSamples(Collection<T> array){
        samples.addAll(array);
    }

    public void addSamples(T[] array){
        for(int i=0; i < array.length; i++) {
            samples.add(array[i]);
        }
    }

    public void setSamples(List<T> samples) {
        this.samples = samples;
    }

    public void setMagnitudes(List<Double[]> magnitudes) {
        this.magnitudes = magnitudes;
    }

    public void addSamples(Samples samples){
        this.samples.addAll(samples.getSamples());
    }

    public int getNSamples(int n){
        if(processedSamples >= samples.size()){
            return -1;
        }
        int start = processedSamples;
        processedSamples += n;
        return start;
    }

    public T getSample(int pos){
        return samples.get(pos);
    }

    public void addMagnitudes(Double[] magnitudes){
        if(this.magnitudes == null){
            this.magnitudes = new ArrayList<>();
        }
        this.magnitudes.add(magnitudes);
    }
    public void addMagnitudes(List<Double> magnitudes){
        if(this.magnitudes == null){
            this.magnitudes = new ArrayList<>();
        }
        this.magnitudes.add((Double[]) magnitudes.toArray());
    }

    public List<Double[]> getMagnitudes() {
        return magnitudes;
    }

    public List<T> getSamples() {
        return samples;
    }

}
