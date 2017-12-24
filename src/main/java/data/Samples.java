package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Samples class
 */
public class Samples<T extends Number, SAMPLES_T extends Number> implements Serializable{

    private List<T> samples;
    private List<List<SAMPLES_T> > magnitudes = new ArrayList<>();
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

    public void setMagnitudes(List<List<SAMPLES_T> > magnitudes) {
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

    public void addMagnitudes(List<SAMPLES_T> magnitudes){
        if(this.magnitudes == null){
            this.magnitudes = new ArrayList<>();
        }
        this.magnitudes.add(magnitudes);
    }

    public List<List<SAMPLES_T> > getMagnitudes() {
        return magnitudes;
    }

    public List<T> getSamples() {
        return samples;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Samples<T, SAMPLES_T> samples1 = (Samples<T, SAMPLES_T>) o;
        return  Objects.equals(samples, samples1.samples) &&
                Objects.equals(magnitudes, samples1.magnitudes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(samples, magnitudes, processedSamples, logger);
    }
}
