package data;

import java.io.Serializable;

public class Complex implements Serializable {
    private double re;
    private double im;

    public Complex(double re, double im){
        this.re = re;
        this.im = im;
    }

    public Complex add(Complex c){
        re += c.re;
        im += c.im;
        return this;
    }

    public Complex mul(double a){
        re *= a;
        im *= a;
        return this;
    }

    public Complex mul(Complex c){
        re = re * c.re - im * c.im;
        im = im * c.re + re * c.im;
        return this;
    }

    public double magnitude(){
        return Math.sqrt(re*re + im*im);
    }
    public static double mag_in_db(double magnitude){
        return 10*Math.log10(magnitude);
    }

}
