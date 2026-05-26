import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String fileName = "test_code.txt";
        SymbolTable symbolTable   = new SymbolTable();
        CodeGenerator codeGen     = new CodeGenerator();

        System.out.println("--- Bangla Compiler: Review 3 ---\n");

        // ── 1. Read entire source file at once ────────────────────────────────
        String source;
        try {
            source = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error: File not found -> " + fileName);
            return;
        }

        System.out.println("[Source Code]");
        System.out.println("─".repeat(50));
        System.out.println(source.trim());
        System.out.println("─".repeat(50));

        // ── 2. Lexer ──────────────────────────────────────────────────────────
        System.out.println("\n[Phase 1] Lexical Analysis");
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        for (Token t : tokens) System.out.println("  -> " + t);

        // ── 3. Parser ─────────────────────────────────────────────────────────
        System.out.println("\n[Phase 2] Parsing & Semantic Analysis");
        Parser parser = new Parser(tokens, symbolTable, codeGen);
        parser.parse();
        System.out.println(parser.hadError()
            ? "  ⚠ Parsing complete with some errors (recovered)."
            : "  ✓ Parsing complete. No errors.");

        // ── 4. Symbol Table ───────────────────────────────────────────────────
        symbolTable.printTable();

        // ── 5. Code Generation ────────────────────────────────────────────────
        System.out.println("\n[Phase 3] Code Generation");
        codeGen.finalizeCode();
        codeGen.printCode();

        // ── 6. Compile generated Java ─────────────────────────────────────────
        System.out.println("\n[Phase 4] Compiling Generated Code");
        try {
            Process compileProc = Runtime.getRuntime().exec("javac GeneratedCode.java");
            int exitCode = compileProc.waitFor();
            if (exitCode != 0) {
                String err = new String(compileProc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("  Compilation failed:\n" + err);
            } else {
                System.out.println("  Compilation successful! ✓");

                // ── 7. Run generated program ──────────────────────────────────
                System.out.println("\n[Phase 5] Running Generated Program");
                System.out.println("─".repeat(50));
                Process runProc = Runtime.getRuntime().exec(
                    new String[]{"java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "GeneratedCode"});
                String output = new String(runProc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String errOut = new String(runProc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                runProc.waitFor();
                if (!output.isEmpty()) System.out.print(output);
                if (!errOut.isEmpty()) System.out.print("[stderr] " + errOut);
                System.out.println("─".repeat(50));
            }
        } catch (Exception e) {
            System.out.println("  Could not compile/run: " + e.getMessage());
        }

        System.out.println("\n--- Compilation Finished ---");
    }
}
