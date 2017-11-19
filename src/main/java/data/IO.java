package data;

import config.Config;

import java.io.*;

public class IO {
    InputStream is;
    OutputStream os;
    FormatInterface fmt;
    Config config = Config.getInstance();

    public IO(String path, FormatInterface fmt){
        this.fmt = fmt;
        try {
            is = new FileInputStream(path);
            os = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public IO(InputStream is, OutputStream os, FormatInterface fmt){
        this.is = is;
        this.os = os;
        this.fmt = fmt;
    }

    public void close() throws IOException {
        is.close();
        os.close();
    }

    public void write(Samples samples) throws Exception {
        fmt.encode(config, samples, os);
    }

    public Samples read() throws IOException {
        return fmt.decode(config, is);
    }
}