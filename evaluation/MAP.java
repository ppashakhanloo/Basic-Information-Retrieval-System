package evaluation;

import java.io.IOException;

public class MAP extends Evaluator {

	public MAP() throws IOException {
	}

	@Override
	public double evalQuery(Integer[] retResult, int q) {
		double finalResult = 0;
		int relRet = 0;
		double REL = getRelevanceAssesment().get(q).size();

		for (int i = 0; i < retResult.length; i++) {
			// check if it is relevant!
			if (getRelevanceAssesment().get(q).contains(retResult[i])) {
				// compute precision
				relRet++;
				finalResult += (relRet * 1.0) / (i + 1.0);
			}
		}

		finalResult /= REL;

		return (REL == 0) ? 0 : finalResult;
	}
}
