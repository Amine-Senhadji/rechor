package ch.epfl.rechor.timetable;
/**
 * Represents indexed data within a timetable system.
 * Indexed data is conceptually stored in an array,
 * where each element is identified by an index ranging from 0 (inclusive)
 * to the size of the dataset (exclusive).
 * @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */
public interface Indexed {
    /**
     * Returns the number of elements in the indexed dataset.
     * @return The total number of indexed elements.
     */
    int size();

}
