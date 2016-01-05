package evaluation;

import java.io.IOException;

public class FMeasure extends Evaluator {

	public FMeasure() throws IOException {
	}

	public double BETA = 1.0;

	@Override
	public double evalQuery(Integer[] retResult, int q) {
		double REL = getRelevanceAssesment().get(q).size();
		double relRet = 0;
		double recall = 0;
		double precision = 0;

		for (int i = 0; i < retResult.length; i++) {
			if (getRelevanceAssesment().get(q).contains(retResult[i])) {
				relRet++;
			}
		}

		recall = relRet / REL;
		precision = relRet / retResult.length;

		return (recall == 0 && precision == 0) ? 0.0 : computeF(precision,
				recall, BETA);
	}

	private double computeF(double precision, double recall, double BETA) {
		return ((BETA * BETA + 1.0) * precision * recall)
				/ ((BETA * BETA * precision) + recall);
	}
}
