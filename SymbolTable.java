import java.util.HashMap;

public class SymbolTable {

    private HashMap<String, String> typeTable =
            new HashMap<>();

    private HashMap<String, Object> valueTable =
            new HashMap<>();

    public void add(String name, String type) {

        typeTable.put(name, type);

        if (type.equals("সংখ্যা")) {

            valueTable.put(name, 0);

        } else {

            valueTable.put(name, "");
        }
    }

    public boolean exists(String name) {

        return typeTable.containsKey(name);
    }

    public String getType(String name) {

        return typeTable.get(name);
    }

    public void setValue(String name, Object value) {

        valueTable.put(name, value);
    }

    public Object getValue(String name) {

        return valueTable.get(name);
    }

    public int getIntValue(String name) {

        return (Integer) valueTable.get(name);
    }

    public void printTable() {

        System.out.println("\n--- Symbol Table ---");

        for (String key : typeTable.keySet()) {

            System.out.println(

                key
                + " -> type: "
                + typeTable.get(key)

                + " | value: "
                + valueTable.get(key)
            );
        }
    }
}



