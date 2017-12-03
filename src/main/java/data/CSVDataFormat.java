package data;

import config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CSVDataFormat implements FormatInterface{
    private final static String LINE_DELIMITER = "\n";
    private final static String ELEMENTS_DELIMITER = ",";
    private final static String KW_DELIMITER = "=";
    Config config;

    public void encode(Config config, Samples samples, OutputStream os) throws Exception {
        this.config = config;
        StringBuilder sb = new StringBuilder();
        Map<String, Object> opts = config.getKwOptions();
        for(String key : opts.keySet()){
            sb.append(makePair(key, opts.get(key)));
            sb.append(ELEMENTS_DELIMITER);
        }
        os.write(sb.toString().getBytes());
        os.write(LINE_DELIMITER.getBytes());

        if(config.ShouldWriteRAWData()) {
            List values = samples.getSamples();
            for (int i = 0; i < values.size(); i++) {
                Double val = (double)values.get(i);
                os.write(val.toString().getBytes());
                if (i < (values.size() - 1)) {
                    os.write(ELEMENTS_DELIMITER.getBytes());
                }
            }
            os.write(LINE_DELIMITER.getBytes());
        }

        //FFT results
        List<Double[]> magnitudes = samples.getMagnitudes();
        for(int i=0; i < magnitudes.size(); i++){
            Double[] mag = magnitudes.get(i);
            if(mag != null) {
                for (int j = 0; j < mag.length; j++) {
                    try {
                        os.write(mag[j].toString().getBytes());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (j < (mag.length - 1)) {
                        os.write(ELEMENTS_DELIMITER.getBytes());
                    }
                }
                os.write(LINE_DELIMITER.getBytes());
            }
        }
    }

    public Samples decode(Config config, InputStream is) throws IOException {
        Samples samples = new Samples<Double>();
        this.config = config;
        StringBuilder sb = new StringBuilder();
        boolean header_read = false;
        boolean fft = false;
        int sampleRate = 0;
        String key = "";
        List<Double> temp_buffer = new ArrayList<Double>();
        List<Double> mags = null;
        for(int b; (b=is.read()) >= 0;){
            if(b == KW_DELIMITER.charAt(0)){
                key = sb.toString().trim();
                sb = new StringBuilder();
            }
            else if(b == LINE_DELIMITER.charAt(0)){
                if(header_read && !fft){
                    fft = true;
                    sampleRate = config.getSampleRate();
                    mags = new ArrayList<>();
                }
                else if(fft){
                    samples.addMagnitudes((Double[]) mags.toArray());
                }
                header_read = true;
            }
            else if(b == ELEMENTS_DELIMITER.charAt(0)){
                if(!header_read && !fft){
                    addConfig(key, sb.toString().trim());
                    sb = new StringBuilder();
                }
                else if(!fft){
                    Double val = Double.parseDouble(sb.toString().trim());
                    temp_buffer.add(val);
                }
                else{
                    Double val = Double.parseDouble(sb.toString().trim());
                    mags.add(val);
                }
            }
            else{
                sb.append(b);
            }
        }
        samples.addSamples(temp_buffer);
        config.setSampleRate(sampleRate);
        return samples;
    }

    String makePair(String key, Object value){
        return key + KW_DELIMITER + value.toString();
    }

    void addConfig(String key, String value){
        try{
            Integer val = Integer.parseInt(value);
            config.addOption(key, val);
        }catch (NumberFormatException e){
            config.addOption(key, value);
        }
    }
}
