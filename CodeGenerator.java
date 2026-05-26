import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class CodeGenerator {

    private List<String> code = new ArrayList<>();
    private Map<String, String> idMap = new LinkedHashMap<>();
    private int idCounter = 1;
    private int indentLevel = 2; // starts inside main()

    public CodeGenerator() {
        code.add("public class GeneratedCode {");
        code.add("    public static void main(String[] args) throws Exception {");
        code.add("        System.setOut(new java.io.PrintStream(System.out, true, \"UTF-8\"));");
    }

    private String indent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) sb.append("    ");
        return sb.toString();
    }

    public String safeId(String name) {
        boolean allAscii = name.chars().allMatch(c -> c < 128);
        if (allAscii) return name;
        return idMap.computeIfAbsent(name, k -> "var" + (idCounter++));
    }

    public void generateNumberDeclaration(String var, String value) {
        code.add(indent() + "int " + safeId(var) + " = " + value + ";");
    }

    public void generateStringDeclaration(String var, String value) {
        code.add(indent() + "String " + safeId(var) + " = \"" + value + "\";");
    }

    public void generateAssignment(String var, String value) {
        code.add(indent() + safeId(var) + " = " + value + ";");
    }

    public void openIf(String condition) {
        code.add(indent() + "if (" + condition + ") {");
        indentLevel++;
    }

    // Call this INSTEAD of closeBlock when there's an else following
    public void transitionToElse() {
        indentLevel--;
        code.add(indent() + "} else {");
        indentLevel++;
    }

    public void closeBlock() {
        indentLevel--;
        code.add(indent() + "}");
    }

    public void finalizeCode() {
        code.add("    }");
        code.add("}");
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream("GeneratedCode.java"), StandardCharsets.UTF_8);
            for (String line : code) writer.write(line + "\n");
            writer.close();
            System.out.println("\nGenerated Java code saved to GeneratedCode.java");
        } catch (IOException e) {
            System.out.println("Error writing generated code file.");
        }
    }

    public void printCode() {
        System.out.println("\n--- Generated Java Code ---");
        for (String line : code) System.out.println(line);
    }
}
