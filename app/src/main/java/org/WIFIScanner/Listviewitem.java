package org.WIFIScanner;

public class Listviewitem {
//    private int icon;
    private String name;
    private int level;
    private String iotInformation;
//    public int getIcon(){return icon;}
    public String getName(){return name;}
    public int getLevel(){return level;}
    public String getIotInformation(){return iotInformation;}

    public Listviewitem(String name){
//        this.icon=icon;
        this.name=name;
    }

    public Listviewitem(String name, int level){
//        this.icon=icon;
        this.name=name;
        this.level = level;
    }

    public Listviewitem(String name, int level, String iotInformation){
//        this.icon=icon;
        this.name=name;
        this.level = level;
        this.iotInformation = iotInformation;
    }
}
