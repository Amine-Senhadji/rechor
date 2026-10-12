package ch.epfl.rechor.journey;

/*
  ParetoFront represents an immutable Pareto frontier of optimization criteria.
  Each criterion is packed into a long and stored in lexicographical order.
 */
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;
/**  @author Amine Lotfi Senhadji
 * @author Nadine Jegham
 */

public final class ParetoFront {
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);
    private final long[] criteria;
    private ParetoFront(long[] optimizationCriteria) {
        this.criteria = optimizationCriteria;
    }

    /**
     * Returns the number of packed optimization criteria in this Pareto frontier.
     * @return the number of packed optimization criteria
     */
    public int size() {
        return criteria.length;
    }

    /**
     * Returns the packed optimization criteria with the given arrival minutes and changes.
     * @param arrMins the arrival minutes
     * @param changes the number of changes
     * @return the packed optimization criteria that match the given parameters
     * @throws NoSuchElementException if no matching criterion is found
     */
    public long get(int arrMins, int changes) {
        for (long optimizationCriteria : criteria) {
            int packedArrMins = PackedCriteria.arrMins(optimizationCriteria);
            int packedChanges = PackedCriteria.changes(optimizationCriteria);
            if (packedArrMins == arrMins && packedChanges == changes) {
                return optimizationCriteria;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Applies the given action to each packed optimization criterion in this Pareto frontier.
     * @param action a LongConsumer to process each criterion
     */
    public void forEach(LongConsumer action) {
        for (long optimizationCriteria : criteria) {
            action.accept(optimizationCriteria);
        }
    }

    /**
     * Returns a string representation of this Pareto frontier.
     * @return a string representation of the Pareto frontier
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ParetoFront [size=").append(criteria.length).append("]:\n");
        for (long tuple : criteria) {
            sb.append("{");
            if (PackedCriteria.hasDepMins(tuple)) {
                sb.append("dep: ").append(PackedCriteria.depMins(tuple)).append(", ");
            }
            sb.append("arr: ").append(PackedCriteria.arrMins(tuple))
                    .append(", chg: ").append(PackedCriteria.changes(tuple));
            sb.append("}\n");
        }
        return sb.toString();
    }

    /**
     * Builder for constructing a ParetoFront.
     */
    public static class Builder {
        private long[] tuples;
        private int size;

        /**
         * Creates a new, empty Builder with an initial capacity of 2.
         */
        public Builder() {
            int initialCapacity = 2;
            this.tuples = new long[initialCapacity];
            this.size = 0;
        }

        /**
         * Creates a new Builder by copying the contents of the given Builder.
         * @param that the Builder to copy
         */
        public Builder(Builder that) {
            this.tuples = Arrays.copyOf(that.tuples, that.size);
            this.size = that.size;
        }

        /**
         * Checks if the builder's frontier is empty.
         * @return true if empty, false otherwise
         */
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * Clears the builder's frontier.
         * @return this Builder
         */
        public Builder clear() {
            size = 0;
            return this;
        }

        /**
         * Adds the given packed optimization criterion to the frontier
         * if it is not dominated by an existing one.
         * @param packedTuple the packed optimization criterion
         * @return this Builder
         */
        public Builder add(long packedTuple) {
            for (int i = 0; i < size; i++) {
                if (PackedCriteria.dominatesOrIsEqual(
                        tuples[i], PackedCriteria.withPayload(packedTuple, 0xFFFFFFFF))) {
                    return this;
                }
            }

            int insertPosition = 0;
            while (insertPosition < size && tuples[insertPosition]
                    <= PackedCriteria.withPayload(packedTuple, 0xFFFFFFFF)) {
                insertPosition++;
            }

            int newEnd = insertPosition;
            for (int i = insertPosition; i < size; i++) {
                if (!PackedCriteria.dominatesOrIsEqual(packedTuple, tuples[i])) {
                    tuples[newEnd++] = tuples[i];
                }
            }
            size = newEnd;

            if (size == tuples.length) {
                tuples = Arrays.copyOf(tuples, (int)(tuples.length * 1.5) + 1);
            }

            if (insertPosition < size) {
                System.arraycopy(tuples, insertPosition, tuples,
                        insertPosition + 1, size - insertPosition);
            }

            tuples[insertPosition] = packedTuple;
            size++;
            return this;
        }

        /**
         * Packs the given parameters into a criterion and adds it to the frontier.
         * @param arrMins the arrival minutes
         * @param changes the number of changes
         * @param payload the payload
         * @return this Builder
         */
        public Builder add(int arrMins, int changes, int payload) {
            long packedTuples = PackedCriteria.pack(arrMins, changes, payload);
            return add(packedTuples);
        }

        /**
         * Adds all criteria from the given Builder to this Builder.
         * @param that the Builder whose criteria are to be added
         * @return this Builder
         */
        public Builder addAll(Builder that) {
            for (int i = 0; i < that.size; i++) {
                this.add(that.tuples[i]);
            }
            return this;
        }

        /**
         * Checks if this Builder fully dominates the criteria in the given Builder
         * when fixing the departure minutes.
         * @param that the Builder to compare
         * @param depMins the fixed departure minutes
         * @return true if this Builder fully dominates, false otherwise
         */
        public boolean fullyDominates(Builder that, int depMins) {
            for (int i = 0; i < that.size; i++) {
                long fixedTuple = PackedCriteria.withDepMins(that.tuples[i], depMins);
                boolean dominated = false;

                for (int j = 0; j < this.size; j++) {
                    if (PackedCriteria.dominatesOrIsEqual(this.tuples[j], fixedTuple)) {
                        dominated = true;
                        break;
                    }
                }

                if (!dominated) {
                    return false;
                }
            }

            return true;
        }

        /**
         * Applies the given action to each packed criterion in this Builder.
         * @param action a LongConsumer to process each criterion
         */
        public void forEach(LongConsumer action) {
            for (int i = 0; i < size; i++) {
                action.accept(tuples[i]);
            }
        }

        /**
         * Builds an immutable ParetoFront from the current criteria in the Builder.
         * @return the resulting ParetoFront
         */
        public ParetoFront build() {
            long[] result = new long[size];
            System.arraycopy(tuples, 0, result, 0, size);
            return new ParetoFront(result);
        }
    }
}
