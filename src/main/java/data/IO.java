package data;

import config.Config;

import java.io.*;

public class IO {
    InputStream is;
    OutputStream os;
    FormatInterface fmt;
    Config config;

    public IO(Config config, String path, FormatInterface fmt){
        this.config = config;
        this.fmt = fmt;
        try {
            is = new FileInputStream(path);
            os = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public IO(Config config, InputStream is, OutputStream os, FormatInterface fmt){
        this.config = config;
        this.is = is;
        this.os = os;
        this.fmt = fmt;
    }


    public void close() throws IOException {
        if(is != null) {
            is.close();
        }
        if(os != null) {
            os.close();
        }
    }

    public void write(Samples samples) throws Exception {
        if(os == null){
            throw new IOException("No IS");
        }
        fmt.encode(config, samples, os);
        close();
    }

    public Samples read() throws IOException {
        if(is == null){
            throw new IOException("No IS");
        }
        Samples samples = fmt.decode(config, is);
        close();
        return samples;
    }
}
