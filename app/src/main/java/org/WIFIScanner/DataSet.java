package org.WIFIScanner;

public class DataSet {

    public String timestamp;
    public double xpos;
    public double ypos;
    public double rssi;
    public String iot_id;

    public DataSet(){
    }
    public DataSet(String iot_id, String time, double x, double y, double rssi){
        this.iot_id = iot_id;
        this.timestamp = time;
        this.xpos = x;
        this.ypos =y;
        this.rssi = rssi;
    }


}
