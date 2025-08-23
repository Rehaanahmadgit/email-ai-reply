package com.example.email.service;




import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;


@Service
public class JavaRunnerService {


    public static class ProcResult {
        public final int exitCode; public final String out; public final String err; public final long timeMs;
        public ProcResult(int exitCode, String out, String err, long timeMs) {
            this.exitCode = exitCode; this.out = out; this.err = err; this.timeMs = timeMs;
        }
    }


    private ProcResult runProcess(List<String> cmd, File workDir, int timeoutSec) throws Exception {
        long start = System.currentTimeMillis();
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(workDir);
        pb.redirectErrorStream(false);
        Process p = pb.start();


        Callable<String> readStdout = () -> new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Callable<String> readStderr = () -> new String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);


        ExecutorService es = Executors.newFixedThreadPool(2);
        Future<String> outF = es.submit(readStdout);
        Future<String> errF = es.submit(readStderr);


        boolean finished = p.waitFor(timeoutSec, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            es.shutdownNow();
            return new ProcResult(-1, "", "⏱️ Process timed out ("+timeoutSec+"s)", System.currentTimeMillis()-start);
        }
        String out = outF.get(200, TimeUnit.MILLISECONDS);
        String err = errF.get(200, TimeUnit.MILLISECONDS);
        es.shutdown();
        return new ProcResult(p.exitValue(), out, err, System.currentTimeMillis()-start);
    }

    /**
     * Compiles and runs provided Java code expecting a public class Main with main method.
     * Creates an isolated temp dir per request and cleans it up.
     */
    public RunResult compileAndRun(String code) throws Exception {
        String session = "session-" + UUID.randomUUID();
        Path work = Files.createTempDirectory(session);
        File workDir = work.toFile();
        File src = new File(workDir, "Main.java");
        try {
            FileUtils.writeStringToFile(src, ensureMainWrapper(code), StandardCharsets.UTF_8);


// Compile
            ProcResult comp = runProcess(List.of("javac", "Main.java"), workDir, 10);
            if (comp.exitCode != 0) {
                return new RunResult(false, "compile", comp.out, comp.err, comp.timeMs);
            }


// Run with memory limit flag and headless
            ProcResult run = runProcess(List.of("java", "-Xmx64m", "-Djava.awt.headless=true", "Main"), workDir, 3);
            boolean ok = (run.exitCode == 0);
            return new RunResult(ok, "run", run.out, run.err, run.timeMs);
        } finally {
            FileUtils.deleteQuietly(workDir);
        }
    }


    // If user didn't provide a public class Main, wrap it.
    private String ensureMainWrapper(String code) {
        String trimmed = code.trim();
        if (trimmed.contains("class Main") && trimmed.contains("static void main")) {
            return code; // user already has Main
        }
// Wrap inside Main and print returned value if method returns something
        return "public class Main {\n" +
                " public static void main(String[] args) throws Exception {\n" +
                " // --- User code below as a function run() ---\n" +
                " System.out.print(run());\n" +
                " }\n" +
                " static Object run() throws Exception {\n" +
                code +
                " }\n" +
                "}";
    }


    public record RunResult(boolean success, String stage, String out, String err, long timeMs) {}
}