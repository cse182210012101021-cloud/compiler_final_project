
import java.util.*;
import java.io.*;

public class CodeGenerator {

    private List<String> code = new ArrayList<>();

    public CodeGenerator() {

        code.add("public class GeneratedCode {");
        code.add("    public static void main(String[] args) {");
    }

    public void generateNumberDeclaration(String var, String value) {

        code.add("        int " + var + " = " + value + ";");
    }

    public void generateStringDeclaration(String var, String value) {

        code.add("        String " + var + " = \"" + value + "\";");
    }

    public void generateAssignment(String var, String value) {

        code.add("        " + var + " = " + value + ";");
    }

    public void generateIfElse(
            String condition,
            String trueStmt,
            String falseStmt
    ) {

        code.add("        if (" + condition + ") {");
        code.add("            " + trueStmt);
        code.add("        }");

        if (falseStmt != null) {

            code.add("        else {");
            code.add("            " + falseStmt);
            code.add("        }");
        }
    }

    public void finalizeCode() {

        code.add("    }");
        code.add("}");

        try {

            FileWriter writer =
                    new FileWriter("GeneratedCode.java");

            for (String line : code) {

                writer.write(line + "\n");
            }

            writer.close();

            System.out.println(
                "\nGenerated Java code saved to GeneratedCode.java"
            );

        } catch (IOException e) {

            System.out.println(
                "Error writing generated code file."
            );
        }
    }

    public void printCode() {

        System.out.println("\n--- Generated Java Code ---");

        for (String line : code) {

            System.out.println(line);
        }
    }
}


