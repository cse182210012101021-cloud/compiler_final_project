import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String fileName = "test_code.txt";

        SymbolTable symbolTable = new SymbolTable();

        CodeGenerator codeGenerator =
                new CodeGenerator();

        Parser parser = null;

        System.out.println(
            "--- Bangla Compiler: Review 3 ---"
        );

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileName),
                        StandardCharsets.UTF_8))) {

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty())
                    continue;

                System.out.println(
                    "\n>>> Processing Line: " + line
                );

                Lexer lexer = new Lexer(line);

                List<Token> tokens =
                        lexer.tokenize();

                System.out.println(
                    "Tokens generated:"
                );

                for (Token token : tokens) {

                    System.out.println(
                        "  -> " + token
                    );
                }

                try {

                    parser = new Parser(
                        tokens,
                        symbolTable,
                        codeGenerator
                    );

                    parser.parse();

                } catch (RuntimeException e) {

                    System.out.println(
                        "Error: " + e.getMessage()
                    );
                }
            }

            codeGenerator.finalizeCode();

            codeGenerator.printCode();

            symbolTable.printTable();

            System.out.println(
                "\n--- Compilation Finished ---"
            );

        } catch (IOException e) {

            System.out.println(
                "Error: File not found -> "
                + fileName
            );
        }
    }
}