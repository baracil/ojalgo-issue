package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.structure.Access1D;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class ExpressionBasedModelTest {


    private MatrixStore<Double> qModel;
    private MatrixStore<Double> lModel;
    private MatrixStore<Double> qRegul;
    private Access1D<BigDecimal> someProfile;

    public static Stream<Double> alphaValues() {
        return Stream.of(0.269e-6,0.271e-6, 0.272e-6,0.273e-6);
    }

    @BeforeEach
    void setUp() throws IOException {
        qModel = MatrixReader.readMatrix("model_quadratic.csv");
        lModel = MatrixReader.readMatrix("model_linear.csv");
        qRegul = MatrixReader.readMatrix("regul_quadratic.csv").get();
        someProfile = Access1D.wrap(SOME_PROFILE);
    }


    @ParameterizedTest
    @MethodSource("alphaValues")
    public void solutionShouldBeBetterThanSomeProfile(double alpha) {
        final Optimisation.State state;
        final double objectiveOfMinimum;
        final double objectiveOfSomeProfile;

        {
            final long nbVariables = Math.toIntExact(lModel.count());
            final ExpressionsBasedModel model = new ExpressionsBasedModel();

            //all variables must be positive
            for (int i = 0; i < nbVariables; i++) {
                model.addVariable().lower(0);
            }

            final Expression residual = model.addExpression();
            final Expression regularisation = model.addExpression();

            residual.setQuadraticFactors(model.getVariables(), qModel);
            residual.setLinearFactors(model.getVariables(), lModel);
            residual.weight(1);

            regularisation.setQuadraticFactors(model.getVariables(), qRegul);
            regularisation.weight(alpha * alpha);

            final Optimisation.Result result = model.minimise();
            state = result.getState();
            objectiveOfMinimum = result.getValue();
            objectiveOfSomeProfile = model.objective().evaluate(someProfile).doubleValue();
        }

        Assertions.assertEquals(Optimisation.State.OPTIMAL,state);
        Assertions.assertTrue(objectiveOfMinimum <= objectiveOfSomeProfile, objectiveOfMinimum+"<="+objectiveOfSomeProfile);

    }

    public static final BigDecimal[] SOME_PROFILE = DoubleStream.of(0.09725837861004, 0.37672400255866, 4.1113E-10, 0, 4.87388E-9, 0, 7.8263E-9, 1.294368E-8, 0, 2.1125E-10, 0, 5.136447E-8, 0, 1.55817013356898, 0, 0, 2.76219E-9, 0, 0, 1.509177E-8, 3.189669E-8, 1.02735E-8, 0, 0, 4.867634E-8, 3.41886143390198, 1.31198668591809, 0, 0, 1.784353E-8, 1.87058E-8, 3.035083E-8, 0, 0, 7.95869E-9, 2.615025E-8, 1.941942E-8, 2.69228E-9, 7.15326E-9, 0, 0, 0, 2.62665E-8, 0, 3.836841E-8, 0, 1.272672E-8, 3.208536E-8, 0, 0)
                                                                .mapToObj(BigDecimal::valueOf)
                                                                .toArray(BigDecimal[]::new);

}
