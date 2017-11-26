package data;

import java.nio.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Samples class
 */
public class Samples {

    private DoubleBuffer samples = null;
    private List<double[]> magnitudes = new ArrayList<double[]>();
    private int processedSamples = 0;

    public Samples(){
    }

    private void allocate(Buffer b){
        if(samples == null) {
            samples = DoubleBuffer.allocate(b.capacity());
        }
        else{
            DoubleBuffer tmp_allocate = DoubleBuffer.allocate(b.capacity() + samples.capacity());
            tmp_allocate.put(samples);
            samples = tmp_allocate;
        }
    }

    public void addSamples(IntBuffer buffer){
        int[] array = buffer.array();
        allocate(buffer);
        for(int i=0; i < array.length; i++){
            samples.put(((double)array[i] / Integer.MAX_VALUE));
        }
    }

    public void addSamples(ShortBuffer buffer){
        short[] array = buffer.array();
        allocate(buffer);
        for(int i=0; i < array.length; i++){
            samples.put(((double)array[i] / Short.MAX_VALUE));
        }
    }

    public void addSamples(FloatBuffer buffer){
        float[] array = buffer.array();
        allocate(buffer);
        for(int i=0; i < array.length; i++){
            samples.put((double)array[i]);
        }
    }

    public void addSamples(double[] array){
        addSamples(DoubleBuffer.wrap(array));
    }

    public void addSamples(DoubleBuffer buffer){
        double[] array = buffer.array();
        allocate(buffer);
        for(int i=0; i < array.length; i++){
            samples.put(array[i]);
        }
    }

    public void setSamples(DoubleBuffer samples) {
        this.samples = samples;
    }

    public void setMagnitudes(List<double[]> magnitudes) {
        this.magnitudes = magnitudes;
    }

    public void addSamples(Samples samples){
        allocate(samples.getSamples());
        this.samples.put(samples.getSamples());
    }

    public int getNSamples(int n){
        if(processedSamples >= samples.capacity()){
            return -1;
        }
        int start = processedSamples;
        processedSamples += n;
        return start;
    }

    public void addMagnitudes(int offset, double [] magnitudes){
        this.magnitudes.add(magnitudes);
    }

    public List<double[]> getMagnitudes() {
        return magnitudes;
    }

    public DoubleBuffer getSamples() {
        return samples;
    }

}
