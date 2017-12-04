package data;

import config.Config;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CSVDataFormat implements FormatInterface{

    private static final String CONFIG = "CONFIG";
    private static final String DATA = "DATA";
    private static final String MAGNITUDES = "MAGNITUDES";
    private static final String encoding = "UTF-8";
    private Config config;
    private CSVFormat csvFormat = CSVFormat.DEFAULT;

    public void encode(Config config, Samples samples, OutputStream os) throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
        this.config = config;
        CSVPrinter csvPrinter = new CSVPrinter(osw, csvFormat);
        if(config.getKwOptions() != null) {
            csvPrinter.printRecord(CONFIG);
            csvPrinter.printRecords(config.getKwOptions());
        }
        if(config.ShouldWriteRAWData()) {
            List values = samples.getSamples();
            csvPrinter.printRecord(DATA);
            csvPrinter.printRecord(values);
        }
        //FFT results
        List<List<Double> > magnitudes = samples.getMagnitudes();
        if(magnitudes != null) {
            csvPrinter.printRecord(MAGNITUDES, magnitudes.size());
            for(int i=0 ; i < magnitudes.size(); i++) {
                csvPrinter.printRecord(magnitudes.get(i));
            }
        }
    }

    public Samples decode(Config config, InputStream is) throws IOException {
        Samples samples = new Samples<Double>();
        InputStreamReader isr = new InputStreamReader(is, encoding);
        this.config = config;
        CSVParser csvParser = new CSVParser(isr, csvFormat);
        String key = null;
        int magnitudesLength = 0;
        List<CSVRecord> records = csvParser.getRecords();
        for(int idx=0; idx < records.size(); idx++){
            if(key == null) {
                key = records.get(idx).get(0);
                if(key.equals(MAGNITUDES)){
                    magnitudesLength = Integer.parseInt(records.get(idx).get(1));
                }
            }
            else{
                switch (key){
                    case CONFIG:
                        CSVRecord record = records.get(idx);
                        for(String val : record){
                            StringBuilder map = new StringBuilder();
                            for(int i=0 ; i < val.length(); i++){
                                switch (val.charAt(i)){
                                    case '{':
                                        i++;
                                        char c;
                                        while((c = val.charAt(i)) != '}'){
                                            map.append(c);
                                            i++;
                                        }
                                        break;
                                }
                            }
                            String[] values = map.toString().split(",");
                            for(String value : values) {
                                String[] kw = value.split("=");
                                config.addOption(kw[0].trim(), kw[1].trim());
                            }
                        }
                        break;
                    case DATA:
                        List<Double> values = new ArrayList<>();
                        for(int i=0; i < records.get(idx).size(); i++){
                            String num = records.get(idx).get(i);
                            try {
                                Double val = Double.parseDouble(num);
                                values.add(val);
                            }catch (NumberFormatException e){
                                e.printStackTrace();
                                values.add(0.0);
                            }
                        }
                        samples.setSamples(values);
                        break;
                    case MAGNITUDES:
                        for(int i=0 ; i < magnitudesLength; i++) {
                            List<Double> magnitudes = new ArrayList<>();
                            for (String num : records.get(idx)) {
                                try {
                                    Double val = Double.parseDouble(num);
                                    magnitudes.add(val);
                                }catch (NumberFormatException e){
                                    e.printStackTrace();
                                    magnitudes.add(0.0);
                                }
                            }
                            samples.addMagnitudes(magnitudes);
                            idx++;
                        }

                        break;
                }
                key = null;
            }
        }
        return samples;
    }

    /*
    private Double correctNumber(String num){
        String patternGoodExp = "[+\\-]*\\d+\\.\\d*[Ee][+\\-]*\\d+$";
        String patternGoodDecMatch = "([+\\-]*\\d+\\.\\d*)$";
        String patternGoodDec = "([+\\-]*\\d+\\.\\d*)";
        Double val;
        if(num.matches(patternGoodExp) || num.matches(patternGoodDecMatch)){
            val = Double.parseDouble(num);
        }
        else{
            String res = num.replaceAll(patternGoodDec, "$1");
            if(res.isEmpty()){
                return 0.0;
            }
            val = Double.parseDouble(res);
        }
        return val;
    }
    */
}
