package me.discordnulled;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class Launcher {

    public static void main(String[] args) {

        System.out.println("Charles Proxy Patcher - v1.0");
        System.out.println("Once applied, Charles will no longer have the 30 minutes session limitation.");
        System.out.println("You'll have to replace charles.jar (Can be found in C:/Program Files/Charles/lib or wherever you installed it) by the patched one manually.\n");

        while (true) {

            try {

                Scanner scanner = new Scanner(System.in);

                System.out.print("Non-Patched File: ");

                String inputPath = scanner.nextLine();

                System.out.print("Output File: ");

                String outputPath = scanner.nextLine();

                Patcher patcher = new Patcher(
                        inputPath,
                        outputPath, Arrays.asList(args).contains("--debug"));

                patcher.run();

                break;

            } catch (FileNotFoundException exp) {

                System.err.println(exp.getMessage());

            }
        }

    }

}
