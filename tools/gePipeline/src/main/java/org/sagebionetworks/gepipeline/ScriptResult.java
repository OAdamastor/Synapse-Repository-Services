package org.sagebionetworks.gepipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagebionetworks.utils.ExternalProcessHelper.ExternalProcessResult;

public class ScriptResult {
	
//	private static final String OUTPUT_LAYER_JSON_KEY = "layerId";
	private static final Pattern OUTPUT_DELIMITER_PATTERN = Pattern.compile(
			".*SynapseWorkflowResult_START(.*)SynapseWorkflowResult_END.*",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	JSONObject structuredOutput;
	
	ExternalProcessResult result;
	
	public ScriptResult(ExternalProcessResult result) throws JSONException {
		this.result = result;
		
		Matcher resultMatcher = OUTPUT_DELIMITER_PATTERN.matcher(result.getStdout());
		if (resultMatcher.matches()) {
			structuredOutput = new JSONObject(resultMatcher.group(1));
		}
	}
	
	public String getStringResult(String key) {
		String ans = null;
		if (structuredOutput.has(key)) {
			try {
				ans = structuredOutput.getString(key);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		return ans;
	}

	public List<String> getStringListResult(String key) {
		List<String> ans = new ArrayList<String>();
		if (structuredOutput.has(key)) {
			try {
				JSONArray a = structuredOutput.getJSONArray(key);
				for (int i=0; i<a.length(); i++) ans.add(a.getString(i));
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		return ans;
	}

	/**
	 * @return all output sent to stdout by this script
	 */
	public String getStdout() {
		return result.getStdout();
	}

	/**
	 * @return all output sent to stderr by this script
	 */
	public String getStderr() {
		return result.getStderr();
	}
}
