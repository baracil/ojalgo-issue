package org.example;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class MatrixReader {

    public static MatrixStore<Double> readMatrix(String resourceName) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(ExpressionBasedModelTest.class.getResourceAsStream(resourceName)))) {
            final double[][] rows = r.lines().map(MatrixReader::parseLine).toArray(double[][]::new);
            return Primitive64Store.FACTORY.rows(rows).get();
        }
    }

    private static double[] parseLine(String line) {
        return Arrays.stream(line.split(","))
                     .mapToDouble(Double::parseDouble)
                     .toArray();
    }

}
