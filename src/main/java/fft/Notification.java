package fft;

public class Notification {

    FFT fft;
    NotificationInterface ni;


    public Notification(FFT fft, NotificationInterface ni){
        this.fft = fft;
        this.ni = ni;
    }

    public void update(){
        ni.updateProgress(fft.progress());
    }
}
