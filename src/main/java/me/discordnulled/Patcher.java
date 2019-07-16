package me.discordnulled;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import jd.commonide.IdeDecompiler;
import jd.commonide.preferences.IdePreferences;
import me.discordnulled.helpers.JarHandler;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Patcher {

    private class Target {

        String classPath;
        String methodName;
        String replacementBody;

        Target(String classPath, String methodName, String replacementBody) {

            this.classPath = classPath;
            this.methodName = methodName;
            this.replacementBody = replacementBody;

        }

        Target() {
            this(null, null, null);
        }

        public String getClassPath() {
            return classPath;
        }

        public String getMethodName() {
            return methodName;
        }

    }

    private String inputPath, outputPath;
    private boolean debugMessages;

    private ArrayList<String> classes;

    public Patcher(String inputPath, String outputPath, boolean debugMessages) throws FileNotFoundException {

        this.inputPath = inputPath;
        this.outputPath = outputPath.replace(" ", "_");
        this.debugMessages = debugMessages;
        this.classes = new ArrayList<>();

        if(!new File(inputPath).exists()) throw new FileNotFoundException(String.format("Couldn't find input file : %s", inputPath));

    }

    public Patcher(String inputPath, String outputPath) throws FileNotFoundException {

        this(inputPath, outputPath, false);

    }

    public boolean run() {

        Instant starts = Instant.now();

        String version = detectVersion();

        info(String.format("Charles Version: %s", version));

        Target target = new Target("com/xk72/charles/efXe.class", "run", "public final void run() { }");

        if(!version.equals("4.2.8")) {

            info("This version of Charles is unknown! Looking for potentially new class name..");
            target = findTarget();

        }

        debug("Loading ClassPool..");

        try {

            ClassPool classPool = ClassPool.getDefault();

            classPool.insertClassPath(inputPath);

            CtClass targetClass = classPool.getCtClass(target.classPath.replace("/", ".").replace(".class", ""));
            CtMethod targetMethod = targetClass.getDeclaredMethod(target.methodName);

            debug("Removing method..");

            targetClass.removeMethod(targetMethod);

            debug("Adding replacement method..");

            targetClass.addMethod(CtNewMethod.make(target.replacementBody, targetClass));

            info("Patch applied, moving to output directory..");

            File outputFile = new File(outputPath);

            if(outputFile.exists()) {

                debug("Output file already exists, deleting it..");

                if(!outputFile.delete())
                    debug("Couldn't delete output file, continuing thinking it's content matches the input's.");
            }

            Files.copy(Paths.get(inputPath), Paths.get(outputPath));
            debug("Original file copied to output.");

            byte[] classAsByteCode = targetClass.toBytecode();

            JarHandler jarHandler = new JarHandler();

            jarHandler.replaceJarFile(outputPath, classAsByteCode, target.classPath);

            Instant ends = Instant.now();
            info(String.format("Job done, file patched in %d second(s).", Duration.between(starts, ends).getSeconds()));
            info(String.format("You can find the patched file at %s", outputPath));
            try {
                Desktop.getDesktop().open(new File(outputPath).getParentFile());
            } catch(Exception exp) {
                // ignored
            }

            return true;

        } catch(Exception exp) {

            error(String.format("An error occurred while patching Charles v%s : ", version));
            error(String.format("Exception Type: %s", exp.getClass().getSimpleName()));
            error(String.format("Exception Message: %s", exp.getMessage()));
            error("Exception Stacktrace: ");
            exp.printStackTrace();

        }

        return false;

    }

    private String detectVersion() {

        String version = null;

        try {
            FileInputStream inputStream = new FileInputStream(inputPath);
            ZipInputStream zInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry = zInputStream.getNextEntry();
            while(zipEntry != null) {
                String fileName = zipEntry.getName();

                if(fileName.endsWith(".class"))
                    classes.add(fileName);

                if(fileName.contains("init.properties")) {

                    Scanner scanner = new Scanner(zInputStream);

                    while(scanner.hasNextLine()) {

                        String line;
                        if((line = scanner.nextLine()).contains("charlesVersion"))
                            version = line.split("charlesVersion=")[1];

                    }

                    zInputStream.closeEntry();

                }

                zipEntry = zInputStream.getNextEntry();
            }
            zInputStream.closeEntry();
            zInputStream.close();
            inputStream.close();
        } catch (IOException exp) {
            // ignored
        }

        if(isNullOrEmpty(version))
            debug("Couldn't retrieve Charles version, maybe the way Charles display it's version has changed?");

        return version;

    }

    private Target findTarget() {

        Target target = new Target();

        // Disable JD-Core error logging
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));

        String FILTER = "This unlicensed copy of Charles will only run for 30 minutes";

        for(String classPath : classes) {
            debug(String.format("Decompiling %s..", classPath));
            try {

                String decompiledClass = IdeDecompiler.decompile(new IdePreferences(false, false, false, false, false, false, false), inputPath, classPath);

                if(decompiledClass.contains(FILTER)) {

                    Pattern pattern = Pattern.compile(".* void (.*)\\(\\)([^}]+)}", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(decompiledClass);

                    while(matcher.find()) {

                        String methodName = matcher.group(1);
                        String methodBody = matcher.group(2);

                        if(methodBody.contains(FILTER)) {

                            String replacementBody = String.format("%s { }", matcher.group().split("\\{")[0].replace("\n", "").replace("\r", ""));

                            target = new Target(classPath, methodName, replacementBody);
                            info(String.format("Target Found -> %s:%s", classPath, methodName));
                        }
                    }

                }

            } catch (Exception exp) {
                debug(String.format("Couldn't decompile %s", classPath));
            }

        }

        return target;

    }

    private void info(Object content) {
        System.out.println(String.format("[INFO] %s", content));
    }

    private void error(Object content) {
        System.out.println(String.format("[ERROR] %s", content));
    }

    private void debug(Object content) {

        if(debugMessages)
            System.out.println(String.format("[DEBUG] %s", content));

    }

    private boolean isNullOrEmpty(String param) {
        return param == null || param.trim().length() == 0;
    }

}
