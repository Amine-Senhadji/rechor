package ch.epfl.rechor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class MyJsonTest {

    @Test
    public void testJArrayToString() {
        Json.JArray array = new Json.JArray(List.of(
                new Json.JString("Earth"),
                new Json.JNumber(6371.0)
        ));
        assertEquals("[\"Earth\",6371.0]", array.toString());
    }

    @Test
    public void testJObjectToString() {
        Json.JObject object = new Json.JObject(Map.of(
                "name", new Json.JString("Earth"),
                "radius", new Json.JNumber(6371.0)
        ));
        // Les clés et valeurs peuvent apparaître dans n'importe quel ordre
        String result = object.toString();
        assertTrue(
                result.equals("{\"name\":\"Earth\",\"radius\":6371.0}") ||
                        result.equals("{\"radius\":6371.0,\"name\":\"Earth\"}")
        );
    }

    @Test
    public void testJStringToString() {
        Json.JString string = new Json.JString("Earth");
        assertEquals("\"Earth\"", string.toString());
    }

    @Test
    public void testJNumberToString() {
        Json.JNumber number = new Json.JNumber(6371.0);
        assertEquals("6371.0", number.toString());
    }

    @Test
    public void testComplexJsonStructure() {
        // Création d'un tableau JSON similaire à l'exemple de l'énoncé
        Json.JArray planetArray = new Json.JArray(List.of(
                new Json.JObject(Map.of(
                        "name", new Json.JString("Earth"),
                        "radius", new Json.JNumber(6371.0)
                )),
                new Json.JObject(Map.of(
                        "name", new Json.JString("Mars"),
                        "radius", new Json.JNumber(3389.5)
                )),
                new Json.JObject(Map.of(
                        "name", new Json.JString("Venus"),
                        "radius", new Json.JNumber(6051.8)
                )),
                new Json.JObject(Map.of(
                        "name", new Json.JString("Jupiter"),
                        "radius", new Json.JNumber(69911.0)
                ))
        ));

        String result = planetArray.toString();
        // Vérifie que le résultat est une chaîne JSON valide contenant les bonnes données
        assertTrue(result.contains("\"name\":\"Earth\""));
        assertTrue(result.contains("\"radius\":6371.0"));
        assertTrue(result.contains("\"name\":\"Mars\""));
        assertTrue(result.contains("\"radius\":3389.5"));
        assertTrue(result.contains("\"name\":\"Venus\""));
        assertTrue(result.contains("\"radius\":6051.8"));
        assertTrue(result.contains("\"name\":\"Jupiter\""));
        assertTrue(result.contains("\"radius\":69911.0"));
}
}