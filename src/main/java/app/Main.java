package main.java.app;

import main.java.io.CrateWriter;
import main.java.model.Crate;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        Crate crate = new Crate("House");

        crate.addTrack("/Músicas/Música Eletrônica/House/House 00's/A Touch Of Class - Around the World (La La La La La) (Radio Version).mp3");

        File out = new File("D:/_Serato_/Subcrates/House.crate");

        try {
            new CrateWriter().write(crate, out);
            System.out.println("Crate criado!");
        } catch (IOException e) {
            System.err.println("Erro ao escrever crate");
            e.printStackTrace();
        }

        System.out.println("Crate criado!");
    }
}