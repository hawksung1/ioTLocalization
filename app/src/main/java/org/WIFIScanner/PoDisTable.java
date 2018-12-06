package org.WIFIScanner;

public class PoDisTable {

    public double power;
    public double distance;
    public double avgDis;
    public double stdDis;

    public PoDisTable(double p, double d, double aD, double sD){
        this.power = p;
        this.distance = d;
        this.avgDis = aD;
        this.stdDis = sD;
    }

}
