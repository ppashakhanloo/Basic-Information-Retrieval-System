package evaluation;

import java.io.IOException;

public class RPrecision extends Evaluator {

	public RPrecision() throws IOException {
	}

	@Override
	public double evalQuery(Integer[] retResult, int q) {
		double REL = getRelevanceAssesment().get(q).size();
		int relRet = 0;

		for (int i = 0; i < REL; i++) {
			if (getRelevanceAssesment().get(q).contains(retResult[i])) {
				relRet++;
			}
		}

		return (REL == 0) ? 0 : (relRet * 1.0) / (REL);
	}
}
