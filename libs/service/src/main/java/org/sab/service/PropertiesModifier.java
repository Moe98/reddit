package org.sab.service;

import java.io.*;
import java.util.ArrayList;

public class PropertiesModifier {

    public static ArrayList<String> readLines(String FileName) throws IOException{
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(FileName));
        String line;
        while((line = br.readLine()) != null){
            lines.add(line);
        }
        return lines;
    }
    public static void setProperty(String FileName,String PropertyName, String PropertyValue) throws IOException {
        File f = new File(FileName);
        ArrayList<String> lines = readLines(FileName);

        FileOutputStream fos = new FileOutputStream(f);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for(int i = 0;i<lines.size();i++){
            if(lines.get(i).startsWith(PropertyName)){
                bw.write(PropertyName+" = "+PropertyValue);
            }
            else{
                bw.write(lines.get(i));
            }
            bw.newLine();
        }
        bw.close();
    }

    public static void deleteProperty(String FileName,String PropertyName) throws IOException {
        File f = new File(FileName);
        ArrayList<String> lines = readLines(FileName);

        FileOutputStream fos = new FileOutputStream(f);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for(int i = 0;i<lines.size();i++){
            if(!lines.get(i).startsWith(PropertyName)) {
                bw.write(lines.get(i));
                bw.newLine();
            }
        }
        bw.close();
    }

    public static void addProperty(String FileName,String PropertyName, String PropertyValue) throws IOException {
        File f = new File(FileName);
        ArrayList<String> lines = readLines(FileName);

        FileOutputStream fos = new FileOutputStream(f);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for(int i = 0;i<lines.size();i++){
            bw.write(lines.get(i));
            bw.newLine();
        }
        bw.write(PropertyName+" = "+PropertyValue);
        bw.close();
    }
}


